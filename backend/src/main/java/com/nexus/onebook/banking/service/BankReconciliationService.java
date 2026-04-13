package com.nexus.onebook.banking.service;

import com.nexus.onebook.banking.dto.BankReconciliationResult;
import com.nexus.onebook.banking.model.BankFeedSource;
import com.nexus.onebook.banking.model.BankFeedTransaction;
import com.nexus.onebook.accounts.model.JournalEntry;
import com.nexus.onebook.accounts.model.LedgerAccount;
import com.nexus.onebook.banking.repository.BankFeedTransactionRepository;
import com.nexus.onebook.accounts.repository.JournalEntryRepository;
import com.nexus.onebook.accounts.repository.LedgerAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class BankReconciliationService {

    private final BankFeedTransactionRepository bankFeedRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final LedgerAccountRepository ledgerAccountRepository;

    public BankReconciliationService(BankFeedTransactionRepository bankFeedRepository,
                                      JournalEntryRepository journalEntryRepository,
                                      LedgerAccountRepository ledgerAccountRepository) {
        this.bankFeedRepository = bankFeedRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.ledgerAccountRepository = ledgerAccountRepository;
    }

    @Transactional
    public BankFeedTransaction ingestBankFeed(String tenantId, Long bankAccountId,
                                               String externalTransactionId,
                                               LocalDate transactionDate, BigDecimal amount,
                                               String description, String source) {
        // Validate bank account exists
        LedgerAccount bankAccount = ledgerAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Bank account not found: " + bankAccountId));

        // Check for duplicate
        bankFeedRepository.findByTenantIdAndExternalTransactionId(tenantId, externalTransactionId)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Bank feed transaction already exists: " + externalTransactionId);
                });

        BankFeedTransaction feedTxn = new BankFeedTransaction(
                tenantId, bankAccount, externalTransactionId, transactionDate, amount);
        feedTxn.setDescription(description);
        feedTxn.setSource(BankFeedSource.valueOf(source != null ? source : "MANUAL"));

        return bankFeedRepository.save(feedTxn);
    }

    @Transactional
    public BankFeedTransaction matchTransaction(Long feedTransactionId, Long journalEntryId) {
        BankFeedTransaction feedTxn = bankFeedRepository.findById(feedTransactionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Bank feed transaction not found: " + feedTransactionId));

        JournalEntry journalEntry = journalEntryRepository.findById(journalEntryId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Journal entry not found: " + journalEntryId));

        feedTxn.setMatched(true);
        feedTxn.setMatchedJournalEntry(journalEntry);

        return bankFeedRepository.save(feedTxn);
    }

    @Transactional(readOnly = true)
    public BankReconciliationResult getReconciliationStatus(String tenantId) {
        List<BankFeedTransaction> allFeeds = bankFeedRepository.findByTenantId(tenantId);
        List<BankFeedTransaction> matched = bankFeedRepository.findByTenantIdAndMatched(tenantId, true);
        List<BankFeedTransaction> unmatched = bankFeedRepository.findByTenantIdAndMatched(tenantId, false);

        BigDecimal totalAmount = allFeeds.stream()
                .map(BankFeedTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal matchedAmount = matched.stream()
                .map(BankFeedTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal unmatchedAmount = unmatched.stream()
                .map(BankFeedTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BankReconciliationResult(
                tenantId, allFeeds.size(), matched.size(), unmatched.size(),
                totalAmount, matchedAmount, unmatchedAmount);
    }

    @Transactional(readOnly = true)
    public List<BankFeedTransaction> getUnmatchedTransactions(String tenantId) {
        return bankFeedRepository.findByTenantIdAndMatched(tenantId, false);
    }
}
