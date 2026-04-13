package com.nexus.onebook.intelligence.service;

import com.nexus.onebook.intelligence.dto.CashFlowForecast;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForecastingServiceTest {

    @Mock
    private JournalEntryRepository entryRepository;

    @InjectMocks
    private ForecastingService forecastingService;

    private LedgerAccount revenueAccount;
    private LedgerAccount expenseAccount;
    private LedgerAccount cashAccount;
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

        cashAccount = new LedgerAccount();
        cashAccount.setId(3L);
        cashAccount.setAccountCode("1000");
        cashAccount.setAccountName("Cash");
        cashAccount.setAccountType(AccountType.ASSET);

        postedTransaction = new JournalTransaction();
        postedTransaction.setPosted(true);
    }

    @Test
    void generateForecast_noEntries_returnsZeros() {
        when(entryRepository.findPostedEntriesByTenantId("tenant-1"))
                .thenReturn(Collections.emptyList());

        CashFlowForecast forecast = forecastingService.generateForecast("tenant-1");

        assertEquals("tenant-1", forecast.tenantId());
        assertEquals(0, BigDecimal.ZERO.compareTo(forecast.currentCashPosition()));
        assertEquals(0, BigDecimal.ZERO.compareTo(forecast.forecast30Day()));
        assertEquals(0, BigDecimal.ZERO.compareTo(forecast.forecast60Day()));
        assertEquals(0, BigDecimal.ZERO.compareTo(forecast.forecast90Day()));
        assertEquals("LOW", forecast.riskLevel());
    }

    @Test
    void generateForecast_withRevenueAndExpenses_calculatesCorrectly() throws Exception {
        Instant now = Instant.now();
        Instant thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS);

        JournalEntry revCredit = createEntry(revenueAccount, EntryType.CREDIT, "3000.0000");
        setCreatedAt(revCredit, thirtyDaysAgo);

        JournalEntry expDebit = createEntry(expenseAccount, EntryType.DEBIT, "600.0000");
        setCreatedAt(expDebit, thirtyDaysAgo);

        JournalEntry cashDebit = createEntry(cashAccount, EntryType.DEBIT, "5000.0000");
        setCreatedAt(cashDebit, now);

        when(entryRepository.findPostedEntriesByTenantId("tenant-1"))
                .thenReturn(List.of(revCredit, expDebit, cashDebit));

        CashFlowForecast forecast = forecastingService.generateForecast("tenant-1");

        assertEquals("tenant-1", forecast.tenantId());
        assertEquals(0, new BigDecimal("5000.0000").compareTo(forecast.currentCashPosition()));
        assertTrue(forecast.forecast30Day().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(forecast.forecast90Day().compareTo(BigDecimal.ZERO) > 0);
        assertEquals("LOW", forecast.riskLevel());
    }

    @Test
    void generateForecast_negativeOutlook_highRisk() throws Exception {
        Instant now = Instant.now();
        Instant thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS);

        JournalEntry revCredit = createEntry(revenueAccount, EntryType.CREDIT, "300.0000");
        setCreatedAt(revCredit, thirtyDaysAgo);

        JournalEntry expDebit = createEntry(expenseAccount, EntryType.DEBIT, "3000.0000");
        setCreatedAt(expDebit, thirtyDaysAgo);

        JournalEntry cashDebit = createEntry(cashAccount, EntryType.DEBIT, "500.0000");
        setCreatedAt(cashDebit, now);

        when(entryRepository.findPostedEntriesByTenantId("tenant-1"))
                .thenReturn(List.of(revCredit, expDebit, cashDebit));

        CashFlowForecast forecast = forecastingService.generateForecast("tenant-1");

        assertTrue(forecast.forecast30Day().compareTo(BigDecimal.ZERO) < 0);
        assertEquals("HIGH", forecast.riskLevel());
    }

    private JournalEntry createEntry(LedgerAccount account, EntryType type, String amount) {
        JournalEntry entry = new JournalEntry(
                "tenant-1", account, type, new BigDecimal(amount), "Test entry");
        entry.setTransaction(postedTransaction);
        return entry;
    }

    private void setCreatedAt(JournalEntry entry, Instant instant) throws Exception {
        java.lang.reflect.Field f = JournalEntry.class.getDeclaredField("createdAt");
        f.setAccessible(true);
        f.set(entry, instant);
    }
}
