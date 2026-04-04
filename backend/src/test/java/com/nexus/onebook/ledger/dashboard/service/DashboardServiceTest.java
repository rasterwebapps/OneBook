package com.nexus.onebook.ledger.dashboard.service;

import com.nexus.onebook.ledger.dashboard.dto.DashboardSummaryDTO;
import com.nexus.onebook.ledger.dto.BalanceSheetReport;
import com.nexus.onebook.ledger.dto.CashFlowLine;
import com.nexus.onebook.ledger.dto.CashFlowReport;
import com.nexus.onebook.ledger.dto.ProfitAndLossReport;
import com.nexus.onebook.ledger.dto.TrialBalanceLine;
import com.nexus.onebook.ledger.dto.TrialBalanceReport;
import com.nexus.onebook.ledger.service.BalanceSheetService;
import com.nexus.onebook.ledger.service.CashFlowService;
import com.nexus.onebook.ledger.service.ProfitAndLossService;
import com.nexus.onebook.ledger.service.TrialBalanceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private TrialBalanceService trialBalanceService;
    @Mock private BalanceSheetService balanceSheetService;
    @Mock private ProfitAndLossService profitAndLossService;
    @Mock private CashFlowService cashFlowService;

    @InjectMocks
    private DashboardService dashboardService;

    private static final String TENANT = "tenant-1";

    @Test
    void getSummary_aggregatesAllDomainReports() {
        TrialBalanceLine tbLine = new TrialBalanceLine(
            1L, "1000", "Cash", "ASSET",
            new BigDecimal("5000.00"), new BigDecimal("2000.00"));
        TrialBalanceReport tbReport = new TrialBalanceReport(
            TENANT, List.of(tbLine),
            new BigDecimal("5000.00"), new BigDecimal("5000.00"), true);

        BalanceSheetReport bsReport = new BalanceSheetReport(
            TENANT, List.of(tbLine), List.of(), List.of(),
            new BigDecimal("10000.00"), new BigDecimal("6000.00"),
            new BigDecimal("4000.00"), true);

        ProfitAndLossReport plReport = new ProfitAndLossReport(
            TENANT, List.of(), List.of(),
            new BigDecimal("8000.00"), new BigDecimal("3000.00"),
            new BigDecimal("5000.00"));

        CashFlowReport cfReport = new CashFlowReport(
            TENANT,
            List.of(new CashFlowLine("Net Income", new BigDecimal("5000.00"))),
            List.of(new CashFlowLine("Investing", new BigDecimal("-1000.00"))),
            List.of(new CashFlowLine("Financing", new BigDecimal("500.00"))),
            new BigDecimal("5000.00"), new BigDecimal("-1000.00"),
            new BigDecimal("500.00"), new BigDecimal("4500.00"));

        when(trialBalanceService.generateTrialBalance(TENANT)).thenReturn(tbReport);
        when(balanceSheetService.generateBalanceSheet(TENANT)).thenReturn(bsReport);
        when(profitAndLossService.generateProfitAndLoss(TENANT)).thenReturn(plReport);
        when(cashFlowService.generateCashFlow(TENANT)).thenReturn(cfReport);

        DashboardSummaryDTO summary = dashboardService.getSummary(TENANT);

        assertNotNull(summary);
        assertEquals(TENANT, summary.tenantId());

        // Trial Balance
        assertEquals(new BigDecimal("5000.00"), summary.trialBalance().totalDebits());
        assertEquals(new BigDecimal("5000.00"), summary.trialBalance().totalCredits());
        assertTrue(summary.trialBalance().balanced());
        assertEquals(1, summary.trialBalance().accountCount());

        // Balance Sheet
        assertEquals(new BigDecimal("10000.00"), summary.balanceSheet().totalAssets());
        assertEquals(new BigDecimal("6000.00"), summary.balanceSheet().totalLiabilities());
        assertEquals(new BigDecimal("4000.00"), summary.balanceSheet().totalEquity());
        assertTrue(summary.balanceSheet().balanced());

        // P&L
        assertEquals(new BigDecimal("8000.00"), summary.profitAndLoss().totalRevenue());
        assertEquals(new BigDecimal("3000.00"), summary.profitAndLoss().totalExpenses());
        assertEquals(new BigDecimal("5000.00"), summary.profitAndLoss().netIncome());

        // Cash Flow
        assertEquals(new BigDecimal("5000.00"), summary.cashFlow().netCashFromOperating());
        assertEquals(new BigDecimal("-1000.00"), summary.cashFlow().netCashFromInvesting());
        assertEquals(new BigDecimal("500.00"), summary.cashFlow().netCashFromFinancing());
        assertEquals(new BigDecimal("4500.00"), summary.cashFlow().netCashChange());

        verify(trialBalanceService).generateTrialBalance(TENANT);
        verify(balanceSheetService).generateBalanceSheet(TENANT);
        verify(profitAndLossService).generateProfitAndLoss(TENANT);
        verify(cashFlowService).generateCashFlow(TENANT);
    }

    @Test
    void getSummary_emptyData_returnsZeroValues() {
        TrialBalanceReport tbReport = new TrialBalanceReport(
            TENANT, List.of(),
            BigDecimal.ZERO, BigDecimal.ZERO, true);

        BalanceSheetReport bsReport = new BalanceSheetReport(
            TENANT, List.of(), List.of(), List.of(),
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, true);

        ProfitAndLossReport plReport = new ProfitAndLossReport(
            TENANT, List.of(), List.of(),
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        CashFlowReport cfReport = new CashFlowReport(
            TENANT, List.of(), List.of(), List.of(),
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        when(trialBalanceService.generateTrialBalance(TENANT)).thenReturn(tbReport);
        when(balanceSheetService.generateBalanceSheet(TENANT)).thenReturn(bsReport);
        when(profitAndLossService.generateProfitAndLoss(TENANT)).thenReturn(plReport);
        when(cashFlowService.generateCashFlow(TENANT)).thenReturn(cfReport);

        DashboardSummaryDTO summary = dashboardService.getSummary(TENANT);

        assertNotNull(summary);
        assertEquals(0, summary.trialBalance().accountCount());
        assertEquals(0, summary.trialBalance().totalDebits().signum());
        assertEquals(0, summary.balanceSheet().totalAssets().signum());
        assertEquals(0, summary.profitAndLoss().netIncome().signum());
        assertEquals(0, summary.cashFlow().netCashChange().signum());
    }

    @Test
    void getSummary_callsEachDomainServiceExactlyOnce() {
        TrialBalanceReport tbReport = new TrialBalanceReport(
            TENANT, List.of(), BigDecimal.ZERO, BigDecimal.ZERO, true);
        BalanceSheetReport bsReport = new BalanceSheetReport(
            TENANT, List.of(), List.of(), List.of(),
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, true);
        ProfitAndLossReport plReport = new ProfitAndLossReport(
            TENANT, List.of(), List.of(),
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        CashFlowReport cfReport = new CashFlowReport(
            TENANT, List.of(), List.of(), List.of(),
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        when(trialBalanceService.generateTrialBalance(TENANT)).thenReturn(tbReport);
        when(balanceSheetService.generateBalanceSheet(TENANT)).thenReturn(bsReport);
        when(profitAndLossService.generateProfitAndLoss(TENANT)).thenReturn(plReport);
        when(cashFlowService.generateCashFlow(TENANT)).thenReturn(cfReport);

        dashboardService.getSummary(TENANT);

        verify(trialBalanceService, times(1)).generateTrialBalance(TENANT);
        verify(balanceSheetService, times(1)).generateBalanceSheet(TENANT);
        verify(profitAndLossService, times(1)).generateProfitAndLoss(TENANT);
        verify(cashFlowService, times(1)).generateCashFlow(TENANT);
        verifyNoMoreInteractions(trialBalanceService, balanceSheetService,
            profitAndLossService, cashFlowService);
    }
}
