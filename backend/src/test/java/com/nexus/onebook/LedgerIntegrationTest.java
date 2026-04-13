package com.nexus.onebook;

import com.nexus.onebook.accounts.dto.TrialBalanceReport;
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
import com.nexus.onebook.accounts.repository.*;
import com.nexus.onebook.auditor.repository.*;
import com.nexus.onebook.banking.repository.*;
import com.nexus.onebook.clientaccount.repository.*;
import com.nexus.onebook.compliance.repository.*;
import com.nexus.onebook.credit.repository.*;
import com.nexus.onebook.currency.repository.*;
import com.nexus.onebook.entitlement.repository.*;
import com.nexus.onebook.fixedasset.repository.*;
import com.nexus.onebook.foundation.repository.*;
import com.nexus.onebook.intelligence.repository.*;
import com.nexus.onebook.inventory.repository.*;
import com.nexus.onebook.operations.repository.*;
import com.nexus.onebook.payroll.repository.*;
import com.nexus.onebook.reporting.repository.*;
import com.nexus.onebook.tenant.repository.*;
import com.nexus.onebook.accounts.service.JournalService;
import com.nexus.onebook.accounts.service.LedgerAccountService;
import com.nexus.onebook.accounts.service.TrialBalanceService;
import com.nexus.onebook.accounts.dto.JournalEntryRequest;
import com.nexus.onebook.accounts.dto.JournalTransactionRequest;
import com.nexus.onebook.accounts.dto.LedgerAccountRequest;
import com.nexus.onebook.exception.UnbalancedTransactionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test that validates the complete Milestone 2 flow:
 * 1. Create organizational hierarchy (Enterprise → Branch → Cost Center)
 * 2. Create Chart of Accounts (ledger accounts)
 * 3. Post balanced journal transactions
 * 4. Verify trial balance correctness
 * 5. Assert double-entry integrity (balance assertions, orphan detection)
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LedgerIntegrationTest {

    @Autowired
    private EnterpriseRepository enterpriseRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private CostCenterRepository costCenterRepository;

    @Autowired
    private LedgerAccountService accountService;

    @Autowired
    private JournalService journalService;

    @Autowired
    private TrialBalanceService trialBalanceService;

    @Autowired
    private JournalTransactionRepository transactionRepository;

    @Autowired
    private JournalEntryRepository entryRepository;

    private static final String TENANT = "integration-test-tenant";

    private CostCenter costCenter;
    private LedgerAccount cashAccount;
    private LedgerAccount revenueAccount;
    private LedgerAccount expenseAccount;
    private LedgerAccount apAccount;

    @BeforeEach
    void setUp() {
        // 1. Create organizational hierarchy
        Enterprise enterprise = new Enterprise(TENANT, "ENT-INT", "Integration Test Enterprise");
        enterprise = enterpriseRepository.save(enterprise);

        Branch branch = new Branch(TENANT, enterprise, "BR-INT", "Integration Branch");
        branch = branchRepository.save(branch);

        costCenter = new CostCenter(TENANT, branch, "CC-INT", "Integration Cost Center");
        costCenter = costCenterRepository.save(costCenter);

        // 2. Create Chart of Accounts
        cashAccount = accountService.createAccount(new LedgerAccountRequest(
                TENANT, costCenter.getId(), "1000", "Cash", "ASSET", null, null));

        revenueAccount = accountService.createAccount(new LedgerAccountRequest(
                TENANT, costCenter.getId(), "4000", "Sales Revenue", "REVENUE", null, null));

        expenseAccount = accountService.createAccount(new LedgerAccountRequest(
                TENANT, costCenter.getId(), "5000", "Rent Expense", "EXPENSE", null, null));

        apAccount = accountService.createAccount(new LedgerAccountRequest(
                TENANT, costCenter.getId(), "2000", "Accounts Payable", "LIABILITY", null, null));
    }

    @Test
    void fullFlow_createAccountsPostEntriesVerifyTrialBalance() {
        // Post transaction 1: Cash received from sales (Debit Cash, Credit Revenue)
        JournalTransaction tx1 = journalService.createTransaction(new JournalTransactionRequest(
                TENANT,
                LocalDate.of(2026, 3, 1),
                "Sales receipt",
                null,
                List.of(
                        new JournalEntryRequest(cashAccount.getId(), "DEBIT",
                                new BigDecimal("1000.0000"), "Cash in", null),
                        new JournalEntryRequest(revenueAccount.getId(), "CREDIT",
                                new BigDecimal("1000.0000"), "Sales", null)
                )
        ));
        assertNotNull(tx1.getId());
        assertNotNull(tx1.getTransactionUuid());
        assertTrue(tx1.isPosted(), "Transaction should be auto-posted on save");

        // Post transaction 2: Rent payment (Debit Expense, Credit Cash)
        JournalTransaction tx2 = journalService.createTransaction(new JournalTransactionRequest(
                TENANT,
                LocalDate.of(2026, 3, 5),
                "Rent payment",
                null,
                List.of(
                        new JournalEntryRequest(expenseAccount.getId(), "DEBIT",
                                new BigDecimal("300.0000"), "Rent", null),
                        new JournalEntryRequest(cashAccount.getId(), "CREDIT",
                                new BigDecimal("300.0000"), "Cash out", null)
                )
        ));
        assertNotNull(tx2.getId());
        assertTrue(tx2.isPosted(), "Transaction should be auto-posted on save");

        // Verify trial balance
        TrialBalanceReport report = trialBalanceService.generateTrialBalance(TENANT);

        assertTrue(report.balanced(), "Trial balance should be balanced");
        assertEquals(new BigDecimal("1300.0000"), report.totalDebits());
        assertEquals(new BigDecimal("1300.0000"), report.totalCredits());
        assertEquals(3, report.lines().size());
    }

    @Test
    void unbalancedTransaction_isRejected() {
        assertThrows(UnbalancedTransactionException.class, () ->
                journalService.createTransaction(new JournalTransactionRequest(
                        TENANT,
                        LocalDate.of(2026, 3, 1),
                        "Unbalanced",
                        null,
                        List.of(
                                new JournalEntryRequest(cashAccount.getId(), "DEBIT",
                                        new BigDecimal("100.0000"), "Debit", null),
                                new JournalEntryRequest(revenueAccount.getId(), "CREDIT",
                                        new BigDecimal("50.0000"), "Credit", null)
                        )
                ))
        );
    }

    @Test
    void duplicateAccountCode_isRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                accountService.createAccount(new LedgerAccountRequest(
                        TENANT, costCenter.getId(), "1000", "Duplicate Cash", "ASSET", null, null))
        );
    }

    @Test
    void hierarchicalAccounts_parentChildRelationship() {
        LedgerAccount pettyCash = accountService.createAccount(new LedgerAccountRequest(
                TENANT, costCenter.getId(), "1010", "Petty Cash", "ASSET",
                cashAccount.getId(), null));

        assertNotNull(pettyCash.getParentAccount());
        assertEquals(cashAccount.getId(), pettyCash.getParentAccount().getId());
    }

    @Test
    void multiEntryTransaction_balances() {
        // Compound entry: Split expense between two accounts
        JournalTransaction tx = journalService.createTransaction(new JournalTransactionRequest(
                TENANT,
                LocalDate.of(2026, 3, 10),
                "Compound entry",
                null,
                List.of(
                        new JournalEntryRequest(expenseAccount.getId(), "DEBIT",
                                new BigDecimal("200.0000"), "Rent portion", null),
                        new JournalEntryRequest(apAccount.getId(), "DEBIT",
                                new BigDecimal("100.0000"), "AP portion", null),
                        new JournalEntryRequest(cashAccount.getId(), "CREDIT",
                                new BigDecimal("300.0000"), "Cash out", null)
                )
        ));

        assertNotNull(tx.getId());
        assertEquals(3, tx.getEntries().size());
    }

    @Test
    void trialBalance_emptyTenant_returnsEmpty() {
        TrialBalanceReport report = trialBalanceService.generateTrialBalance("nonexistent-tenant");
        assertTrue(report.lines().isEmpty());
        assertTrue(report.balanced());
    }

    @Test
    void metadataSupport_industrySpecificTags() {
        // Create account with industry metadata
        LedgerAccount pharmacyAccount = accountService.createAccount(new LedgerAccountRequest(
                TENANT, costCenter.getId(), "1300", "Pharmacy Inventory", "ASSET",
                null, "{\"department\": \"pharmacy\", \"regulated\": true}"));

        assertEquals("{\"department\": \"pharmacy\", \"regulated\": true}",
                pharmacyAccount.getMetadata());

        // Create transaction with metadata
        JournalTransaction tx = journalService.createTransaction(new JournalTransactionRequest(
                TENANT,
                LocalDate.of(2026, 3, 10),
                "Pharmacy purchase",
                "{\"purchaseOrder\": \"PO-12345\"}",
                List.of(
                        new JournalEntryRequest(pharmacyAccount.getId(), "DEBIT",
                                new BigDecimal("750.0000"), "Inventory in",
                                "{\"batchNo\": \"B-001\"}"),
                        new JournalEntryRequest(apAccount.getId(), "CREDIT",
                                new BigDecimal("750.0000"), "Payable", null)
                )
        ));

        assertEquals("{\"purchaseOrder\": \"PO-12345\"}", tx.getMetadata());
        assertEquals("{\"batchNo\": \"B-001\"}", tx.getEntries().get(0).getMetadata());
    }

    @Test
    void orphanDetection_entriesAlwaysLinkedToTransaction() {
        JournalTransaction tx = journalService.createTransaction(new JournalTransactionRequest(
                TENANT,
                LocalDate.of(2026, 3, 10),
                "Orphan check",
                null,
                List.of(
                        new JournalEntryRequest(cashAccount.getId(), "DEBIT",
                                new BigDecimal("100.0000"), "Debit", null),
                        new JournalEntryRequest(revenueAccount.getId(), "CREDIT",
                                new BigDecimal("100.0000"), "Credit", null)
                )
        ));

        // All entries must reference the transaction
        for (JournalEntry entry : tx.getEntries()) {
            assertNotNull(entry.getTransaction());
            assertEquals(tx, entry.getTransaction());
        }
    }
}
