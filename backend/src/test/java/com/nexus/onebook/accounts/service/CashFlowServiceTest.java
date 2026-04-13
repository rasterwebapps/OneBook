package com.nexus.onebook.accounts.service;

import com.nexus.onebook.accounts.dto.CashFlowReport;
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
class CashFlowServiceTest {

    @Mock
    private JournalEntryRepository entryRepository;

    @InjectMocks
    private CashFlowService cashFlowService;

    private LedgerAccount revenueAccount;
    private LedgerAccount expenseAccount;
    private LedgerAccount equipmentAccount;
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

        equipmentAccount = new LedgerAccount();
        equipmentAccount.setId(3L);
        equipmentAccount.setAccountCode("1500");
        equipmentAccount.setAccountName("Equipment");
        equipmentAccount.setAccountType(AccountType.ASSET);

        cashAccount = new LedgerAccount();
        cashAccount.setId(4L);
        cashAccount.setAccountCode("1000");
        cashAccount.setAccountName("Cash");
        cashAccount.setAccountType(AccountType.ASSET);

        postedTransaction = new JournalTransaction();
        postedTransaction.setPosted(true);
    }

    @Test
    void generateCashFlow_noEntries_returnsZeros() {
        when(entryRepository.findPostedEntriesByTenantId("tenant-1"))
                .thenReturn(Collections.emptyList());

        CashFlowReport report = cashFlowService.generateCashFlow("tenant-1");

        assertEquals("tenant-1", report.tenantId());
        assertEquals(BigDecimal.ZERO, report.netCashChange());
    }

    @Test
    void generateCashFlow_revenueAndExpense_showsOperating() {
        JournalEntry revCredit = createEntry(revenueAccount, EntryType.CREDIT, "1000.0000");
        JournalEntry expDebit = createEntry(expenseAccount, EntryType.DEBIT, "400.0000");

        when(entryRepository.findPostedEntriesByTenantId("tenant-1"))
                .thenReturn(List.of(revCredit, expDebit));

        CashFlowReport report = cashFlowService.generateCashFlow("tenant-1");

        assertFalse(report.operatingActivities().isEmpty());
        assertEquals(new BigDecimal("600.0000"), report.netCashFromOperating());
    }

    @Test
    void generateCashFlow_cashAccountExcludedFromInvesting() {
        JournalEntry cashDebit = createEntry(cashAccount, EntryType.DEBIT, "500.0000");

        when(entryRepository.findPostedEntriesByTenantId("tenant-1"))
                .thenReturn(List.of(cashDebit));

        CashFlowReport report = cashFlowService.generateCashFlow("tenant-1");

        // Cash account should NOT appear in investing activities
        assertEquals(BigDecimal.ZERO, report.netCashFromInvesting());
    }

    private JournalEntry createEntry(LedgerAccount account, EntryType type, String amount) {
        JournalEntry entry = new JournalEntry(
                "tenant-1", account, type, new BigDecimal(amount), "Test entry");
        entry.setTransaction(postedTransaction);
        return entry;
    }
}
