package com.nexus.onebook.ledger.service;

import com.nexus.onebook.ledger.cache.WarmCacheService;
import com.nexus.onebook.ledger.dto.TrialBalanceLine;
import com.nexus.onebook.ledger.dto.TrialBalanceReport;
import com.nexus.onebook.ledger.model.*;
import com.nexus.onebook.ledger.repository.JournalEntryRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrialBalanceServiceTest {

    @Mock
    private JournalEntryRepository entryRepository;

    @Mock
    private WarmCacheService warmCacheService;

    @InjectMocks
    private TrialBalanceService trialBalanceService;

    private LedgerAccount cashAccount;
    private LedgerAccount revenueAccount;
    private LedgerAccount expenseAccount;
    private JournalTransaction postedTransaction;

    @BeforeEach
    void setUp() {
        cashAccount = new LedgerAccount();
        cashAccount.setId(1L);
        cashAccount.setAccountCode("1000");
        cashAccount.setAccountName("Cash");
        cashAccount.setAccountType(AccountType.ASSET);

        revenueAccount = new LedgerAccount();
        revenueAccount.setId(2L);
        revenueAccount.setAccountCode("4000");
        revenueAccount.setAccountName("Sales Revenue");
        revenueAccount.setAccountType(AccountType.REVENUE);

        expenseAccount = new LedgerAccount();
        expenseAccount.setId(3L);
        expenseAccount.setAccountCode("5000");
        expenseAccount.setAccountName("Cost of Goods Sold");
        expenseAccount.setAccountType(AccountType.EXPENSE);

        postedTransaction = new JournalTransaction();
        postedTransaction.setPosted(true);

        // Cache stub — simulate cold cache (cache miss) for unit tests
        lenient().when(warmCacheService.getTrialBalance(anyString())).thenReturn(null);
    }

    @Test
    void generateTrialBalance_noEntries_returnsEmptyBalanced() {
        when(entryRepository.findPostedEntriesByTenantIdAndDateRange(eq("tenant-1"), isNull(), isNull()))
                .thenReturn(Collections.emptyList());

        TrialBalanceReport report = trialBalanceService.generateTrialBalance("tenant-1");

        assertEquals("tenant-1", report.tenantId());
        assertTrue(report.lines().isEmpty());
        assertEquals(BigDecimal.ZERO, report.totalDebits());
        assertEquals(BigDecimal.ZERO, report.totalCredits());
        assertTrue(report.balanced());
    }

    @Test
    void generateTrialBalance_singleBalancedTransaction_isBalanced() {
        JournalEntry debitEntry = createEntry(cashAccount, EntryType.DEBIT, "500.0000");
        JournalEntry creditEntry = createEntry(revenueAccount, EntryType.CREDIT, "500.0000");

        when(entryRepository.findPostedEntriesByTenantIdAndDateRange(eq("tenant-1"), isNull(), isNull()))
                .thenReturn(List.of(debitEntry, creditEntry));

        TrialBalanceReport report = trialBalanceService.generateTrialBalance("tenant-1");

        assertEquals(2, report.lines().size());
        assertTrue(report.balanced());
        assertEquals(new BigDecimal("500.0000"), report.totalDebits());
        assertEquals(new BigDecimal("500.0000"), report.totalCredits());
    }

    @Test
    void generateTrialBalance_multipleTransactions_aggregatesCorrectly() {
        // Transaction 1: Cash debit 500, Revenue credit 500
        JournalEntry entry1 = createEntry(cashAccount, EntryType.DEBIT, "500.0000");
        JournalEntry entry2 = createEntry(revenueAccount, EntryType.CREDIT, "500.0000");

        // Transaction 2: Expense debit 200, Cash credit 200
        JournalEntry entry3 = createEntry(expenseAccount, EntryType.DEBIT, "200.0000");
        JournalEntry entry4 = createEntry(cashAccount, EntryType.CREDIT, "200.0000");

        when(entryRepository.findPostedEntriesByTenantIdAndDateRange(eq("tenant-1"), isNull(), isNull()))
                .thenReturn(List.of(entry1, entry2, entry3, entry4));

        TrialBalanceReport report = trialBalanceService.generateTrialBalance("tenant-1");

        assertEquals(3, report.lines().size());
        assertTrue(report.balanced());
        assertEquals(new BigDecimal("700.0000"), report.totalDebits());
        assertEquals(new BigDecimal("700.0000"), report.totalCredits());

        // Cash: debit 500, credit 200
        TrialBalanceLine cashLine = report.lines().stream()
                .filter(l -> l.accountCode().equals("1000"))
                .findFirst().orElseThrow();
        assertEquals(new BigDecimal("500.0000"), cashLine.totalDebits());
        assertEquals(new BigDecimal("200.0000"), cashLine.totalCredits());

        // Revenue: credit 500
        TrialBalanceLine revenueLine = report.lines().stream()
                .filter(l -> l.accountCode().equals("4000"))
                .findFirst().orElseThrow();
        assertEquals(BigDecimal.ZERO, revenueLine.totalDebits());
        assertEquals(new BigDecimal("500.0000"), revenueLine.totalCredits());

        // Expense: debit 200
        TrialBalanceLine expenseLine = report.lines().stream()
                .filter(l -> l.accountCode().equals("5000"))
                .findFirst().orElseThrow();
        assertEquals(new BigDecimal("200.0000"), expenseLine.totalDebits());
        assertEquals(BigDecimal.ZERO, expenseLine.totalCredits());
    }

    @Test
    void generateTrialBalance_linesContainAccountDetails() {
        JournalEntry debitEntry = createEntry(cashAccount, EntryType.DEBIT, "100.0000");
        JournalEntry creditEntry = createEntry(revenueAccount, EntryType.CREDIT, "100.0000");

        when(entryRepository.findPostedEntriesByTenantIdAndDateRange(eq("tenant-1"), isNull(), isNull()))
                .thenReturn(List.of(debitEntry, creditEntry));

        TrialBalanceReport report = trialBalanceService.generateTrialBalance("tenant-1");

        TrialBalanceLine cashLine = report.lines().get(0);
        assertEquals(1L, cashLine.accountId());
        assertEquals("1000", cashLine.accountCode());
        assertEquals("Cash", cashLine.accountName());
        assertEquals("ASSET", cashLine.accountType());
    }

    @Test
    void generateTrialBalance_withDateRange_filtersEntries() {
        JournalEntry debitEntry = createEntry(cashAccount, EntryType.DEBIT, "300.0000");
        JournalEntry creditEntry = createEntry(revenueAccount, EntryType.CREDIT, "300.0000");
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 3, 31);

        when(entryRepository.findPostedEntriesByTenantIdAndDateRange("tenant-1", from, to))
                .thenReturn(List.of(debitEntry, creditEntry));

        TrialBalanceReport report = trialBalanceService.generateTrialBalance("tenant-1", from, to);

        assertEquals(2, report.lines().size());
        assertTrue(report.balanced());
        assertEquals(new BigDecimal("300.0000"), report.totalDebits());
        assertEquals(new BigDecimal("300.0000"), report.totalCredits());
    }

    private JournalEntry createEntry(LedgerAccount account, EntryType type, String amount) {
        JournalEntry entry = new JournalEntry(
                "tenant-1", account, type, new BigDecimal(amount), "Test entry");
        entry.setTransaction(postedTransaction);
        return entry;
    }
}
