package com.nexus.onebook.ingestion.mapper;

import com.nexus.onebook.accounts.dto.JournalEntryRequest;
import com.nexus.onebook.accounts.dto.JournalTransactionRequest;
import com.nexus.onebook.payment.model.PaymentRegisterEntry;
import com.nexus.onebook.accounts.model.LedgerAccount;
import com.nexus.onebook.accounts.repository.LedgerAccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Universal Mapper — transforms any adapter-produced {@link FinancialEvent}
 * into the core Double-Entry format ({@link JournalTransactionRequest}) with
 * JSONB industry tags preserved as metadata.
 * <p>
 * Account codes from the event are resolved to ledger account IDs
 * using the {@link LedgerAccountRepository}.
 */
@Service
public class UniversalMapper {

    private final LedgerAccountRepository accountRepository;

    public UniversalMapper(LedgerAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Maps a normalised financial event into a balanced journal transaction request.
     *
     * @param event the normalised event from any adapter
     * @return a JournalTransactionRequest ready for the double-entry engine
     * @throws IllegalArgumentException if required fields are missing or accounts cannot be resolved
     */
    public JournalTransactionRequest mapToJournalRequest(PaymentRegisterEntry event) {
        if (event.getAmount() == null) {
            throw new IllegalArgumentException("Financial event amount is required for mapping");
        }
        if (event.getDebitAccountCode() == null || event.getDebitAccountCode().isBlank()) {
            throw new IllegalArgumentException("Debit account code is required for mapping");
        }
        if (event.getCreditAccountCode() == null || event.getCreditAccountCode().isBlank()) {
            throw new IllegalArgumentException("Credit account code is required for mapping");
        }

        Long debitAccountId = resolveAccountId(event.getTenantId(), event.getDebitAccountCode());
        Long creditAccountId = resolveAccountId(event.getTenantId(), event.getCreditAccountCode());

        String description = buildDescription(event);

        JournalEntryRequest debitEntry = new JournalEntryRequest(
                debitAccountId,
                "DEBIT",
                event.getAmount(),
                description,
                event.getIndustryTags()
        );

        JournalEntryRequest creditEntry = new JournalEntryRequest(
                creditAccountId,
                "CREDIT",
                event.getAmount(),
                description,
                event.getIndustryTags()
        );

        return new JournalTransactionRequest(
                event.getTenantId(),
                event.getEventDate(),
                description,
                event.getIndustryTags(),
                List.of(debitEntry, creditEntry)
        );
    }

    private Long resolveAccountId(String tenantId, String accountCode) {
        return accountRepository
                .findByTenantIdAndAccountCode(tenantId, accountCode)
                .map(LedgerAccount::getId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ledger account not found for tenant=" + tenantId
                                + ", accountCode=" + accountCode));
    }

    private String buildDescription(PaymentRegisterEntry event) {
        String desc = event.getDescription();
        if (desc != null && !desc.isBlank()) {
            return desc;
        }
        return event.getAdapterType() + " " + event.getEventType()
                + " — ref: " + event.getSourceReference();
    }
}
