package com.nexus.onebook.ledger.accounts.service;

import com.nexus.onebook.ledger.accounts.dto.BalanceSheetReport;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceSheetServiceTest {

    @Mock
    private JournalEntryRepository entryRepository;

    @InjectMocks
    private BalanceSheetService balanceSheetService;

    private LedgerAccount assetAccount;
    private LedgerAccount liabilityAccount;
    private LedgerAccount equityAccount;
    private JournalTransaction postedTransaction;

    @BeforeEach
    void setUp() {
        assetAccount = new LedgerAccount();
        assetAccount.setId(1L);
        assetAccount.setAccountCode("1000");
        assetAccount.setAccountName("Cash");
        assetAccount.setAccountType(AccountType.ASSET);

        liabilityAccount = new LedgerAccount();
        liabilityAccount.setId(2L);
        liabilityAccount.setAccountCode("2000");
        liabilityAccount.setAccountName("Accounts Payable");
        liabilityAccount.setAccountType(AccountType.LIABILITY);

        equityAccount = new LedgerAccount();
        equityAccount.setId(3L);
        equityAccount.setAccountCode("3000");
        equityAccount.setAccountName("Retained Earnings");
        equityAccount.setAccountType(AccountType.EQUITY);

        postedTransaction = new JournalTransaction();
        postedTransaction.setPosted(true);
    }

    @Test
    void generateBalanceSheet_noEntries_returnsEmptyBalanced() {
        when(entryRepository.findPostedEntriesByTenantId("tenant-1"))
                .thenReturn(Collections.emptyList());

        BalanceSheetReport report = balanceSheetService.generateBalanceSheet("tenant-1");

        assertEquals("tenant-1", report.tenantId());
        assertTrue(report.assetLines().isEmpty());
        assertTrue(report.liabilityLines().isEmpty());
        assertTrue(report.equityLines().isEmpty());
        assertTrue(report.balanced());
    }

    @Test
    void generateBalanceSheet_balancedEntries_isBalanced() {
        JournalEntry assetDebit = createEntry(assetAccount, EntryType.DEBIT, "1000.0000");
        JournalEntry liabilityCredit = createEntry(liabilityAccount, EntryType.CREDIT, "600.0000");
        JournalEntry equityCredit = createEntry(equityAccount, EntryType.CREDIT, "400.0000");

        when(entryRepository.findPostedEntriesByTenantId("tenant-1"))
                .thenReturn(List.of(assetDebit, liabilityCredit, equityCredit));

        BalanceSheetReport report = balanceSheetService.generateBalanceSheet("tenant-1");

        assertEquals(new BigDecimal("1000.0000"), report.totalAssets());
        assertEquals(new BigDecimal("600.0000"), report.totalLiabilities());
        assertEquals(new BigDecimal("400.0000"), report.totalEquity());
        assertTrue(report.balanced());
    }

    @Test
    void generateBalanceSheet_excludesRevenueAndExpense() {
        LedgerAccount revenueAccount = new LedgerAccount();
        revenueAccount.setId(4L);
        revenueAccount.setAccountCode("4000");
        revenueAccount.setAccountName("Revenue");
        revenueAccount.setAccountType(AccountType.REVENUE);

        JournalEntry revEntry = createEntry(revenueAccount, EntryType.CREDIT, "500.0000");

        when(entryRepository.findPostedEntriesByTenantId("tenant-1"))
                .thenReturn(List.of(revEntry));

        BalanceSheetReport report = balanceSheetService.generateBalanceSheet("tenant-1");

        assertTrue(report.assetLines().isEmpty());
        assertTrue(report.liabilityLines().isEmpty());
        assertTrue(report.equityLines().isEmpty());
    }

    private JournalEntry createEntry(LedgerAccount account, EntryType type, String amount) {
        JournalEntry entry = new JournalEntry(
                "tenant-1", account, type, new BigDecimal(amount), "Test entry");
        entry.setTransaction(postedTransaction);
        return entry;
    }
}
