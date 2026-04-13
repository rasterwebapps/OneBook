package com.nexus.onebook.accounts.service;

import com.nexus.onebook.accounts.dto.ProfitAndLossReport;
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
class ProfitAndLossServiceTest {

    @Mock
    private JournalEntryRepository entryRepository;

    @InjectMocks
    private ProfitAndLossService profitAndLossService;

    private LedgerAccount revenueAccount;
    private LedgerAccount expenseAccount;
    private LedgerAccount assetAccount;
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
        expenseAccount.setAccountName("Cost of Goods Sold");
        expenseAccount.setAccountType(AccountType.EXPENSE);

        assetAccount = new LedgerAccount();
        assetAccount.setId(3L);
        assetAccount.setAccountCode("1000");
        assetAccount.setAccountName("Cash");
        assetAccount.setAccountType(AccountType.ASSET);

        postedTransaction = new JournalTransaction();
        postedTransaction.setPosted(true);
    }

    @Test
    void generateProfitAndLoss_noEntries_returnsZeros() {
        when(entryRepository.findPostedEntriesByTenantId("tenant-1"))
                .thenReturn(Collections.emptyList());

        ProfitAndLossReport report = profitAndLossService.generateProfitAndLoss("tenant-1");

        assertEquals("tenant-1", report.tenantId());
        assertTrue(report.revenueLines().isEmpty());
        assertTrue(report.expenseLines().isEmpty());
        assertEquals(BigDecimal.ZERO, report.totalRevenue());
        assertEquals(BigDecimal.ZERO, report.totalExpenses());
        assertEquals(BigDecimal.ZERO, report.netIncome());
    }

    @Test
    void generateProfitAndLoss_revenueAndExpenses_calculatesNetIncome() {
        JournalEntry revCredit = createEntry(revenueAccount, EntryType.CREDIT, "1000.0000");
        JournalEntry expDebit = createEntry(expenseAccount, EntryType.DEBIT, "600.0000");
        JournalEntry cashDebit = createEntry(assetAccount, EntryType.DEBIT, "1000.0000");
        JournalEntry cashCredit = createEntry(assetAccount, EntryType.CREDIT, "600.0000");

        when(entryRepository.findPostedEntriesByTenantId("tenant-1"))
                .thenReturn(List.of(revCredit, expDebit, cashDebit, cashCredit));

        ProfitAndLossReport report = profitAndLossService.generateProfitAndLoss("tenant-1");

        assertEquals(1, report.revenueLines().size());
        assertEquals(1, report.expenseLines().size());
        assertEquals(new BigDecimal("1000.0000"), report.totalRevenue());
        assertEquals(new BigDecimal("600.0000"), report.totalExpenses());
        assertEquals(new BigDecimal("400.0000"), report.netIncome());
    }

    @Test
    void generateProfitAndLoss_excludesNonPnlAccounts() {
        JournalEntry assetDebit = createEntry(assetAccount, EntryType.DEBIT, "500.0000");

        when(entryRepository.findPostedEntriesByTenantId("tenant-1"))
                .thenReturn(List.of(assetDebit));

        ProfitAndLossReport report = profitAndLossService.generateProfitAndLoss("tenant-1");

        assertTrue(report.revenueLines().isEmpty());
        assertTrue(report.expenseLines().isEmpty());
        assertEquals(BigDecimal.ZERO, report.totalRevenue());
        assertEquals(BigDecimal.ZERO, report.totalExpenses());
    }

    private JournalEntry createEntry(LedgerAccount account, EntryType type, String amount) {
        JournalEntry entry = new JournalEntry(
                "tenant-1", account, type, new BigDecimal(amount), "Test entry");
        entry.setTransaction(postedTransaction);
        return entry;
    }
}
