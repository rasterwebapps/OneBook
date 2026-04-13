package com.nexus.onebook.dashboard.service;

import com.nexus.onebook.dashboard.dto.DashboardSummaryDTO;
import com.nexus.onebook.dashboard.dto.DashboardSummaryDTO.BalanceSheetSummary;
import com.nexus.onebook.dashboard.dto.DashboardSummaryDTO.CashFlowSummary;
import com.nexus.onebook.dashboard.dto.DashboardSummaryDTO.ProfitAndLossSummary;
import com.nexus.onebook.dashboard.dto.DashboardSummaryDTO.TrialBalanceSummary;
import com.nexus.onebook.accounts.dto.BalanceSheetReport;
import com.nexus.onebook.accounts.dto.CashFlowReport;
import com.nexus.onebook.accounts.dto.ProfitAndLossReport;
import com.nexus.onebook.accounts.dto.TrialBalanceReport;
import com.nexus.onebook.accounts.service.BalanceSheetService;
import com.nexus.onebook.accounts.service.CashFlowService;
import com.nexus.onebook.accounts.service.ProfitAndLossService;
import com.nexus.onebook.accounts.service.TrialBalanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cross-domain dashboard service that aggregates data from multiple
 * domain services into a single unified summary.
 *
 * <p>This service demonstrates the sub-module collaboration pattern:
 * <ul>
 *   <li>Dashboard owns no data (no models, repositories, or migrations)</li>
 *   <li>Depends on domain services, not repositories (respects encapsulation)</li>
 *   <li>One-way dependency: Dashboard → domains, never the reverse</li>
 *   <li>Read-only: all operations are transactional read-only</li>
 * </ul>
 */
@Service
public class DashboardService {

    private final TrialBalanceService trialBalanceService;
    private final BalanceSheetService balanceSheetService;
    private final ProfitAndLossService profitAndLossService;
    private final CashFlowService cashFlowService;

    public DashboardService(
            TrialBalanceService trialBalanceService,
            BalanceSheetService balanceSheetService,
            ProfitAndLossService profitAndLossService,
            CashFlowService cashFlowService) {
        this.trialBalanceService = trialBalanceService;
        this.balanceSheetService = balanceSheetService;
        this.profitAndLossService = profitAndLossService;
        this.cashFlowService = cashFlowService;
    }

    /**
     * Returns a unified dashboard summary aggregating trial balance,
     * balance sheet, P&amp;L, and cash flow data for the given tenant.
     */
    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummary(String tenantId) {
        TrialBalanceReport tb = trialBalanceService.generateTrialBalance(tenantId);
        BalanceSheetReport bs = balanceSheetService.generateBalanceSheet(tenantId);
        ProfitAndLossReport pl = profitAndLossService.generateProfitAndLoss(tenantId);
        CashFlowReport cf = cashFlowService.generateCashFlow(tenantId);

        return new DashboardSummaryDTO(
            tenantId,
            new TrialBalanceSummary(
                tb.totalDebits(),
                tb.totalCredits(),
                tb.balanced(),
                tb.lines().size()
            ),
            new BalanceSheetSummary(
                bs.totalAssets(),
                bs.totalLiabilities(),
                bs.totalEquity(),
                bs.balanced()
            ),
            new ProfitAndLossSummary(
                pl.totalRevenue(),
                pl.totalExpenses(),
                pl.netIncome()
            ),
            new CashFlowSummary(
                cf.netCashFromOperating(),
                cf.netCashFromInvesting(),
                cf.netCashFromFinancing(),
                cf.netCashChange()
            )
        );
    }
}
