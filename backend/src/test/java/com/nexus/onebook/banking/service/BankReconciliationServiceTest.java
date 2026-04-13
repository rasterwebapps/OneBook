package com.nexus.onebook.banking.service;

import com.nexus.onebook.banking.dto.BankReconciliationResult;
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
import com.nexus.onebook.banking.repository.BankFeedTransactionRepository;
import com.nexus.onebook.accounts.repository.JournalEntryRepository;
import com.nexus.onebook.accounts.repository.LedgerAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankReconciliationServiceTest {

    @Mock
    private BankFeedTransactionRepository bankFeedRepository;
    @Mock
    private JournalEntryRepository journalEntryRepository;
    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    @InjectMocks
    private BankReconciliationService reconciliationService;

    private LedgerAccount bankAccount;

    @BeforeEach
    void setUp() {
        bankAccount = new LedgerAccount();
        bankAccount.setId(1L);
        bankAccount.setAccountCode("1010");
        bankAccount.setAccountName("Bank Account");
        bankAccount.setAccountType(AccountType.ASSET);
    }

    @Test
    void ingestBankFeed_validData_succeeds() {
        when(ledgerAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccount));
        when(bankFeedRepository.findByTenantIdAndExternalTransactionId("tenant-1", "EXT-001"))
                .thenReturn(Optional.empty());
        when(bankFeedRepository.save(any(BankFeedTransaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        BankFeedTransaction result = reconciliationService.ingestBankFeed(
                "tenant-1", 1L, "EXT-001",
                LocalDate.of(2024, 3, 1), new BigDecimal("5000.0000"),
                "Payment received", "MANUAL");

        assertNotNull(result);
        assertEquals("EXT-001", result.getExternalTransactionId());
        assertFalse(result.isMatched());
    }

    @Test
    void ingestBankFeed_duplicateExtId_throws() {
        when(ledgerAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccount));
        when(bankFeedRepository.findByTenantIdAndExternalTransactionId("tenant-1", "EXT-001"))
                .thenReturn(Optional.of(new BankFeedTransaction()));

        assertThrows(IllegalArgumentException.class, () ->
                reconciliationService.ingestBankFeed(
                        "tenant-1", 1L, "EXT-001",
                        LocalDate.of(2024, 3, 1), new BigDecimal("5000.0000"),
                        "Payment", "MANUAL"));
    }

    @Test
    void matchTransaction_validIds_setsMatched() {
        BankFeedTransaction feedTxn = new BankFeedTransaction(
                "tenant-1", bankAccount, "EXT-001",
                LocalDate.of(2024, 3, 1), new BigDecimal("5000.0000"));

        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setId(10L);

        when(bankFeedRepository.findById(1L)).thenReturn(Optional.of(feedTxn));
        when(journalEntryRepository.findById(10L)).thenReturn(Optional.of(journalEntry));
        when(bankFeedRepository.save(any(BankFeedTransaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        BankFeedTransaction result = reconciliationService.matchTransaction(1L, 10L);

        assertTrue(result.isMatched());
    }

    @Test
    void getReconciliationStatus_returnsCorrectCounts() {
        BankFeedTransaction matched1 = createFeedTxn("EXT-001", "100.0000", true);
        BankFeedTransaction unmatched1 = createFeedTxn("EXT-002", "200.0000", false);

        when(bankFeedRepository.findByTenantId("tenant-1"))
                .thenReturn(List.of(matched1, unmatched1));
        when(bankFeedRepository.findByTenantIdAndMatched("tenant-1", true))
                .thenReturn(List.of(matched1));
        when(bankFeedRepository.findByTenantIdAndMatched("tenant-1", false))
                .thenReturn(List.of(unmatched1));

        BankReconciliationResult result = reconciliationService.getReconciliationStatus("tenant-1");

        assertEquals(2, result.totalFeedTransactions());
        assertEquals(1, result.matchedTransactions());
        assertEquals(1, result.unmatchedTransactions());
        assertEquals(new BigDecimal("300.0000"), result.totalFeedAmount());
    }

    private BankFeedTransaction createFeedTxn(String extId, String amount, boolean matched) {
        BankFeedTransaction txn = new BankFeedTransaction(
                "tenant-1", bankAccount, extId,
                LocalDate.of(2024, 3, 1), new BigDecimal(amount));
        txn.setMatched(matched);
        return txn;
    }
}
