package com.nexus.onebook.reporting.service;

import com.nexus.onebook.reporting.dto.ConsolidatedReport;
import com.nexus.onebook.accounts.dto.BalanceSheetReport;
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
import com.nexus.onebook.accounts.repository.BranchRepository;
import com.nexus.onebook.reporting.repository.IntercompanyEliminationRepository;
import com.nexus.onebook.accounts.repository.JournalTransactionRepository;
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
import com.nexus.onebook.accounts.service.BalanceSheetService;
import com.nexus.onebook.accounts.service.ProfitAndLossService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntercompanyServiceTest {

    @Mock
    private IntercompanyEliminationRepository eliminationRepository;
    @Mock
    private JournalTransactionRepository transactionRepository;
    @Mock
    private BranchRepository branchRepository;
    @Mock
    private BalanceSheetService balanceSheetService;
    @Mock
    private ProfitAndLossService profitAndLossService;

    @InjectMocks
    private IntercompanyService intercompanyService;

    private Branch sourceBranch;
    private Branch targetBranch;
    private JournalTransaction transaction;

    @BeforeEach
    void setUp() {
        sourceBranch = new Branch();
        sourceBranch.setId(1L);
        sourceBranch.setName("Branch A");

        targetBranch = new Branch();
        targetBranch.setId(2L);
        targetBranch.setName("Branch B");

        transaction = new JournalTransaction();
        transaction.setId(1L);
    }

    @Test
    void recordIntercompanyTransaction_validData_succeeds() {
        when(branchRepository.findById(1L)).thenReturn(Optional.of(sourceBranch));
        when(branchRepository.findById(2L)).thenReturn(Optional.of(targetBranch));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(eliminationRepository.save(any(IntercompanyElimination.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        IntercompanyElimination result = intercompanyService.recordIntercompanyTransaction(
                "tenant-1", 1L, 2L, 1L, new BigDecimal("50000.0000"));

        assertNotNull(result);
        assertFalse(result.isEliminated());
        assertEquals(new BigDecimal("50000.0000"), result.getEliminationAmount());
    }

    @Test
    void recordIntercompanyTransaction_sameBranch_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                intercompanyService.recordIntercompanyTransaction(
                        "tenant-1", 1L, 1L, 1L, new BigDecimal("50000.0000")));
    }

    @Test
    void eliminateTransaction_pendingElimination_succeeds() {
        IntercompanyElimination elimination = new IntercompanyElimination(
                "tenant-1", sourceBranch, targetBranch, transaction, new BigDecimal("50000.0000"));
        elimination.setId(1L);

        when(eliminationRepository.findById(1L)).thenReturn(Optional.of(elimination));
        when(eliminationRepository.save(any(IntercompanyElimination.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        IntercompanyElimination result = intercompanyService.eliminateTransaction(1L);

        assertTrue(result.isEliminated());
        assertNotNull(result.getEliminationDate());
    }

    @Test
    void eliminateTransaction_alreadyEliminated_throws() {
        IntercompanyElimination elimination = new IntercompanyElimination(
                "tenant-1", sourceBranch, targetBranch, transaction, new BigDecimal("50000.0000"));
        elimination.setId(1L);
        elimination.setEliminated(true);

        when(eliminationRepository.findById(1L)).thenReturn(Optional.of(elimination));

        assertThrows(IllegalArgumentException.class, () ->
                intercompanyService.eliminateTransaction(1L));
    }

    @Test
    void generateConsolidatedReport_returnsReportWithEliminations() {
        BalanceSheetReport bs = new BalanceSheetReport(
                "tenant-1", List.of(), List.of(), List.of(),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, true);
        ProfitAndLossReport pl = new ProfitAndLossReport(
                "tenant-1", List.of(), List.of(),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        when(balanceSheetService.generateBalanceSheet("tenant-1")).thenReturn(bs);
        when(profitAndLossService.generateProfitAndLoss("tenant-1")).thenReturn(pl);
        when(eliminationRepository.findByTenantId("tenant-1")).thenReturn(Collections.emptyList());

        ConsolidatedReport report = intercompanyService.generateConsolidatedReport("tenant-1");

        assertNotNull(report);
        assertEquals("tenant-1", report.tenantId());
        assertTrue(report.eliminations().isEmpty());
        assertEquals(BigDecimal.ZERO, report.totalEliminationAmount());
    }
}
