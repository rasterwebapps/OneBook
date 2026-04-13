package com.nexus.onebook.intelligence.service;

import com.nexus.onebook.intelligence.dto.ScenarioRequest;
import com.nexus.onebook.intelligence.dto.ScenarioResult;
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
import com.nexus.onebook.accounts.repository.JournalEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScenarioModelingServiceTest {

    @Mock
    private JournalEntryRepository entryRepository;

    @InjectMocks
    private ScenarioModelingService scenarioModelingService;

    private LedgerAccount revenueAccount;
    private LedgerAccount expenseAccount;
    private JournalTransaction postedTransaction;

    @BeforeEach
    void setUp() {
        revenueAccount = new LedgerAccount();
        revenueAccount.setId(1L);
        revenueAccount.setAccountCode("4000");
        revenueAccount.setAccountName("Sales Revenue");
        revenueAccount.setAccountType(AccountType.REVENUE);

        expenseAccount = new LedgerAccount();
        expenseAccount.setId(2L);
        expenseAccount.setAccountCode("5000");
        expenseAccount.setAccountName("Rent Expense");
        expenseAccount.setAccountType(AccountType.EXPENSE);

        postedTransaction = new JournalTransaction();
        postedTransaction.setPosted(true);
    }

    @Test
    void runScenario_noEntries_returnsZeroImpact() {
        ScenarioRequest request = new ScenarioRequest(
                "tenant-1", "Downturn",
                new BigDecimal("-10"), new BigDecimal("5"), null, 12);

        when(entryRepository.findPostedEntriesByTenantId("tenant-1"))
                .thenReturn(Collections.emptyList());

        ScenarioResult result = scenarioModelingService.runScenario(request);

        assertEquals("tenant-1", result.tenantId());
        assertEquals("Downturn", result.scenarioName());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.baselineNetIncome()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.impactOnCash()));
    }

    @Test
    void runScenario_revenueDropAndExpenseIncrease_negativeImpact() {
        ScenarioRequest request = new ScenarioRequest(
                "tenant-1", "Recession",
                new BigDecimal("-20"), new BigDecimal("10"), null, 12);

        JournalEntry revCredit = createEntry(revenueAccount, EntryType.CREDIT, "10000.0000");
        JournalEntry expDebit = createEntry(expenseAccount, EntryType.DEBIT, "6000.0000");

        when(entryRepository.findPostedEntriesByTenantId("tenant-1"))
                .thenReturn(List.of(revCredit, expDebit));

        ScenarioResult result = scenarioModelingService.runScenario(request);

        assertEquals(0, new BigDecimal("4000.0000").compareTo(result.baselineNetIncome()));
        assertTrue(result.projectedNetIncome().compareTo(result.baselineNetIncome()) < 0);
        assertTrue(result.impactOnCash().compareTo(BigDecimal.ZERO) < 0);
    }

    @Test
    void runScenario_positiveScenario_positiveSummary() {
        ScenarioRequest request = new ScenarioRequest(
                "tenant-1", "Growth",
                new BigDecimal("15"), new BigDecimal("-5"), null, 12);

        JournalEntry revCredit = createEntry(revenueAccount, EntryType.CREDIT, "10000.0000");
        JournalEntry expDebit = createEntry(expenseAccount, EntryType.DEBIT, "6000.0000");

        when(entryRepository.findPostedEntriesByTenantId("tenant-1"))
                .thenReturn(List.of(revCredit, expDebit));

        ScenarioResult result = scenarioModelingService.runScenario(request);

        assertTrue(result.projectedNetIncome().compareTo(result.baselineNetIncome()) > 0);
        assertTrue(result.impactOnCash().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(result.summary());
        assertTrue(result.summary().contains("Growth"));
    }

    private JournalEntry createEntry(LedgerAccount account, EntryType type, String amount) {
        JournalEntry entry = new JournalEntry(
                "tenant-1", account, type, new BigDecimal(amount), "Test entry");
        entry.setTransaction(postedTransaction);
        return entry;
    }
}
