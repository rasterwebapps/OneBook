package com.nexus.onebook.ledger.intelligence.service;

import com.nexus.onebook.ledger.intelligence.dto.TransactionAnomaly;
import com.nexus.onebook.ledger.accounts.model.*;
import com.nexus.onebook.ledger.auditor.model.*;
import com.nexus.onebook.ledger.banking.model.*;
import com.nexus.onebook.ledger.clientaccount.model.*;
import com.nexus.onebook.ledger.compliance.model.*;
import com.nexus.onebook.ledger.credit.model.*;
import com.nexus.onebook.ledger.currency.model.*;
import com.nexus.onebook.ledger.entitlement.model.*;
import com.nexus.onebook.ledger.fixedasset.model.*;
import com.nexus.onebook.ledger.foundation.model.*;
import com.nexus.onebook.ledger.intelligence.model.*;
import com.nexus.onebook.ledger.inventory.model.*;
import com.nexus.onebook.ledger.operations.model.*;
import com.nexus.onebook.ledger.payroll.model.*;
import com.nexus.onebook.ledger.reporting.model.*;
import com.nexus.onebook.ledger.tenant.model.*;
import com.nexus.onebook.ledger.accounts.repository.JournalEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnomalyDetectionServiceTest {

    @Mock
    private JournalEntryRepository entryRepository;

    @InjectMocks
    private AnomalyDetectionService anomalyDetectionService;

    private LedgerAccount expenseAccount;
    private JournalTransaction postedTransaction;

    @BeforeEach
    void setUp() {
        expenseAccount = new LedgerAccount();
        expenseAccount.setId(1L);
        expenseAccount.setAccountCode("5000");
        expenseAccount.setAccountName("Operating Expense");
        expenseAccount.setAccountType(AccountType.EXPENSE);

        postedTransaction = new JournalTransaction();
        postedTransaction.setPosted(true);
    }

    @Test
    void detectAnomalies_noEntries_returnsEmpty() {
        when(entryRepository.findPostedEntriesByTenantId("tenant-1"))
                .thenReturn(Collections.emptyList());

        List<TransactionAnomaly> anomalies = anomalyDetectionService.detectAnomalies("tenant-1");

        assertTrue(anomalies.isEmpty());
    }

    @Test
    void detectAnomalies_duplicateEntries_detected() {
        JournalEntry entry1 = createEntry(1L, expenseAccount, EntryType.DEBIT, "500.0000", "Payment");
        JournalEntry entry2 = createEntry(2L, expenseAccount, EntryType.DEBIT, "500.0000", "Payment");

        when(entryRepository.findPostedEntriesByTenantId("tenant-1"))
                .thenReturn(List.of(entry1, entry2));

        List<TransactionAnomaly> anomalies = anomalyDetectionService.detectAnomalies("tenant-1");

        assertTrue(anomalies.stream().anyMatch(a -> a.anomalyType().equals("DUPLICATE")));
    }

    @Test
    void detectAnomalies_unusualAmount_detected() {
        List<JournalEntry> entries = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            entries.add(createEntry((long) (i + 1), expenseAccount, EntryType.DEBIT,
                    "100.0000", "Expense " + i));
        }
        entries.add(createEntry(11L, expenseAccount, EntryType.DEBIT, "10000.0000", "Large expense"));

        when(entryRepository.findPostedEntriesByTenantId("tenant-1"))
                .thenReturn(entries);

        List<TransactionAnomaly> anomalies = anomalyDetectionService.detectAnomalies("tenant-1");

        assertTrue(anomalies.stream().anyMatch(a -> a.anomalyType().equals("UNUSUAL_AMOUNT")));
    }

    @Test
    void detectAnomalies_roundNumber_detected() {
        JournalEntry entry = createEntry(1L, expenseAccount, EntryType.DEBIT, "50000.0000", "Big round payment");

        when(entryRepository.findPostedEntriesByTenantId("tenant-1"))
                .thenReturn(List.of(entry));

        List<TransactionAnomaly> anomalies = anomalyDetectionService.detectAnomalies("tenant-1");

        assertTrue(anomalies.stream().anyMatch(a -> a.anomalyType().equals("ROUND_NUMBER")));
    }

    private JournalEntry createEntry(Long id, LedgerAccount account, EntryType type,
                                     String amount, String description) {
        JournalEntry entry = new JournalEntry(
                "tenant-1", account, type, new BigDecimal(amount), description);
        entry.setId(id);
        entry.setTransaction(postedTransaction);
        return entry;
    }
}
