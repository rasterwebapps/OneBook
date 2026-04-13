package com.nexus.onebook.accounts.service;

import com.nexus.onebook.cache.WarmCacheService;
import com.nexus.onebook.accounts.dto.JournalEntryRequest;
import com.nexus.onebook.accounts.dto.JournalTransactionRequest;
import com.nexus.onebook.exception.UnbalancedTransactionException;
import com.nexus.onebook.accounts.model.*;
import com.nexus.onebook.auditor.model.*;
import com.nexus.onebook.banking.model.*;
import com.nexus.onebook.clientaccount.model.*;
import com.nexus.onebook.compliance.model.*;
import com.nexus.onebook.credit.model.*;
import com.nexus.onebook.currency.model.*;
import com.nexus.onebook.entitlement.model.*;
import com.nexus.onebook.fixedasset.model.*;
import com.nexus.onebook.foundation.model.*;
import com.nexus.onebook.intelligence.model.*;
import com.nexus.onebook.inventory.model.*;
import com.nexus.onebook.operations.model.*;
import com.nexus.onebook.payroll.model.*;
import com.nexus.onebook.reporting.model.*;
import com.nexus.onebook.tenant.model.*;
import com.nexus.onebook.accounts.repository.JournalTransactionRepository;
import com.nexus.onebook.accounts.repository.LedgerAccountRepository;
import com.nexus.onebook.security.AuditLogService;
import com.nexus.onebook.security.BlindIndexService;
import com.nexus.onebook.security.FieldEncryptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Core double-entry accounting service.
 * Validates that sum(debits) == sum(credits) before any transaction is committed.
 * Integrates field-level encryption, blind indexing, audit logging,
 * and Redis warm-cache invalidation on writes.
 */
@Service
public class JournalService {

    private final JournalTransactionRepository transactionRepository;
    private final LedgerAccountRepository accountRepository;
    private final FieldEncryptionService encryptionService;
    private final BlindIndexService blindIndexService;
    private final AuditLogService auditLogService;
    private final WarmCacheService warmCacheService;

    public JournalService(JournalTransactionRepository transactionRepository,
                          LedgerAccountRepository accountRepository,
                          FieldEncryptionService encryptionService,
                          BlindIndexService blindIndexService,
                          AuditLogService auditLogService,
                          WarmCacheService warmCacheService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.encryptionService = encryptionService;
        this.blindIndexService = blindIndexService;
        this.auditLogService = auditLogService;
        this.warmCacheService = warmCacheService;
    }

    /**
     * Fetches all transactions for a tenant with entries eagerly initialized.
     */
    @Transactional(readOnly = true)
    public List<JournalTransaction> getTransactionsByTenant(String tenantId) {
        List<JournalTransaction> transactions = transactionRepository.findByTenantId(tenantId);
        for (JournalTransaction tx : transactions) {
            tx.getEntries().forEach(e -> {
                e.getAccount().getAccountName(); // force initialize account
            });
        }
        return transactions;
    }

    /**
     * Fetches a single transaction by UUID with entries eagerly initialized.
     */
    @Transactional(readOnly = true)
    public Optional<JournalTransaction> getTransactionByUuid(UUID uuid) {
        Optional<JournalTransaction> opt = transactionRepository.findByTransactionUuid(uuid);
        opt.ifPresent(tx -> tx.getEntries().forEach(e -> {
            e.getAccount().getAccountName(); // force initialize account
        }));
        return opt;
    }

    /**
     * Creates, validates, and immediately posts a balanced journal transaction
     * (Tally/Nexus behavior: Save = Validate + Post).
     * The transaction description is encrypted (AES-256-GCM) and a blind
     * index (HMAC-SHA256) is generated for searchability.
     *
     * @param request the transaction request containing all debit/credit entries
     * @return the persisted JournalTransaction with {@code posted = true}
     * @throws UnbalancedTransactionException if debits and credits do not balance
     * @throws IllegalArgumentException       if entries are missing or invalid
     */
    @Transactional
    public JournalTransaction createTransaction(JournalTransactionRequest request) {
        List<JournalEntryRequest> entryRequests = request.entries();

        // 1. Validate the entries balance
        validateBalance(entryRequests);

        // 2. Build the transaction header
        JournalTransaction transaction = new JournalTransaction(
                request.tenantId(),
                request.transactionDate(),
                request.description()
        );
        if (request.metadata() != null) {
            transaction.setMetadata(request.metadata());
        }

        // Zero-Knowledge: encrypt description and generate blind index
        if (request.description() != null) {
            transaction.setDescriptionEncrypted(encryptionService.encrypt(request.description()));
            transaction.setDescriptionBlindIndex(blindIndexService.generateBlindIndex(request.description()));
        }

        // 3. Build and attach each entry line
        for (JournalEntryRequest entryReq : entryRequests) {
            LedgerAccount account = accountRepository.findById(entryReq.accountId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Ledger account not found: " + entryReq.accountId()));

            EntryType entryType = EntryType.valueOf(entryReq.entryType());

            JournalEntry entry = new JournalEntry(
                    request.tenantId(),
                    account,
                    entryType,
                    entryReq.amount(),
                    entryReq.description()
            );
            if (entryReq.metadata() != null) {
                entry.setMetadata(entryReq.metadata());
            }

            transaction.addEntry(entry);
        }

        // AUTO-POST: Save = Validate + Post (Tally/Nexus behavior).
        // Validation has already passed above, so mark as posted immediately.
        transaction.setPosted(true);

        JournalTransaction saved = transactionRepository.save(transaction);

        // Audit trail: log the transaction creation
        auditLogService.logInsert(request.tenantId(), "journal_transactions", saved.getId(),
                "{\"transactionUuid\":\"" + saved.getTransactionUuid() + "\"}");

        // Cache invalidation: trial balance is now stale
        warmCacheService.evictTrialBalance(request.tenantId());

        return saved;
    }

    /**
     * Updates an existing journal transaction — replaces all entries.
     */
    @Transactional
    public JournalTransaction updateTransaction(UUID uuid, JournalTransactionRequest request) {
        JournalTransaction existing = transactionRepository.findByTransactionUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + uuid));

        validateBalance(request.entries());

        // Update header fields
        existing.setTransactionDate(request.transactionDate());
        existing.setDescription(request.description());
        if (request.metadata() != null) {
            existing.setMetadata(request.metadata());
        }
        if (request.description() != null) {
            existing.setDescriptionEncrypted(encryptionService.encrypt(request.description()));
            existing.setDescriptionBlindIndex(blindIndexService.generateBlindIndex(request.description()));
        }

        // Replace entries (orphanRemoval will delete old ones)
        existing.getEntries().clear();
        for (JournalEntryRequest entryReq : request.entries()) {
            LedgerAccount account = accountRepository.findById(entryReq.accountId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Ledger account not found: " + entryReq.accountId()));
            EntryType entryType = EntryType.valueOf(entryReq.entryType());
            JournalEntry entry = new JournalEntry(
                    request.tenantId(), account, entryType,
                    entryReq.amount(), entryReq.description()
            );
            if (entryReq.metadata() != null) {
                entry.setMetadata(entryReq.metadata());
            }
            existing.addEntry(entry);
        }

        JournalTransaction saved = transactionRepository.save(existing);
        auditLogService.logInsert(request.tenantId(), "journal_transactions", saved.getId(),
                "{\"action\":\"UPDATE\",\"transactionUuid\":\"" + saved.getTransactionUuid() + "\"}");
        warmCacheService.evictTrialBalance(request.tenantId());
        return saved;
    }

    /**
     * Posts a journal transaction by UUID, marking it as posted.
     * The transaction must be balanced (debits == credits).
     * This operation is idempotent — posting an already-posted transaction returns it unchanged.
     *
     * @param uuid the unique identifier of the transaction to post
     * @return the updated JournalTransaction with posted=true
     * @throws IllegalArgumentException       if the transaction is not found
     * @throws UnbalancedTransactionException if the transaction entries do not balance
     */
    @Transactional
    public JournalTransaction postTransaction(UUID uuid) {
        JournalTransaction existing = transactionRepository.findByTransactionUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + uuid));

        if (existing.isPosted()) {
            return existing;
        }

        List<JournalEntry> entries = existing.getEntries();
        if (entries == null || entries.size() < 2) {
            throw new UnbalancedTransactionException(
                    "A transaction requires at least two entries (one debit and one credit)");
        }

        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;
        for (JournalEntry entry : entries) {
            if (entry.getEntryType() == EntryType.DEBIT) {
                totalDebits = totalDebits.add(entry.getAmount());
            } else {
                totalCredits = totalCredits.add(entry.getAmount());
            }
        }

        if (totalDebits.compareTo(totalCredits) != 0) {
            throw new UnbalancedTransactionException(
                    String.format("Transaction is unbalanced: total debits=%s, total credits=%s",
                            totalDebits.toPlainString(), totalCredits.toPlainString()));
        }

        existing.setPosted(true);
        JournalTransaction saved = transactionRepository.save(existing);

        auditLogService.logInsert(existing.getTenantId(), "journal_transactions", saved.getId(),
                "{\"action\":\"POST\",\"transactionUuid\":\"" + uuid + "\"}");
        warmCacheService.evictTrialBalance(existing.getTenantId());

        return saved;
    }

    /**
     * Deletes a journal transaction by UUID.
     */
    @Transactional
    public void deleteTransaction(UUID uuid) {
        JournalTransaction existing = transactionRepository.findByTransactionUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + uuid));
        String tenantId = existing.getTenantId();
        transactionRepository.delete(existing);
        auditLogService.logInsert(tenantId, "journal_transactions", existing.getId(),
                "{\"action\":\"DELETE\",\"transactionUuid\":\"" + uuid + "\"}");
        warmCacheService.evictTrialBalance(tenantId);
    }

    /**
     * Asserts that the journal entries satisfy double-entry rules:
     * <ul>
     *   <li>At least one DEBIT and one CREDIT entry must exist</li>
     *   <li>sum(debit amounts) must equal sum(credit amounts)</li>
     * </ul>
     *
     * @param entries the list of entry requests to validate
     * @throws UnbalancedTransactionException if validation fails
     */
    public void validateBalance(List<JournalEntryRequest> entries) {
        if (entries == null || entries.size() < 2) {
            throw new UnbalancedTransactionException(
                    "A transaction requires at least two entries (one debit and one credit)");
        }

        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;
        boolean hasDebit = false;
        boolean hasCredit = false;

        for (JournalEntryRequest entry : entries) {
            EntryType type = EntryType.valueOf(entry.entryType());
            BigDecimal amount = entry.amount();

            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new UnbalancedTransactionException(
                        "Entry amount must be greater than zero");
            }

            if (type == EntryType.DEBIT) {
                totalDebits = totalDebits.add(amount);
                hasDebit = true;
            } else {
                totalCredits = totalCredits.add(amount);
                hasCredit = true;
            }
        }

        if (!hasDebit || !hasCredit) {
            throw new UnbalancedTransactionException(
                    "Transaction must contain at least one debit and one credit entry");
        }

        if (totalDebits.compareTo(totalCredits) != 0) {
            throw new UnbalancedTransactionException(
                    String.format("Transaction is unbalanced: total debits=%s, total credits=%s",
                            totalDebits.toPlainString(), totalCredits.toPlainString()));
        }
    }
}
