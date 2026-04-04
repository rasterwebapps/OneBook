package com.nexus.onebook.ledger.dashboard.dto;

import java.math.BigDecimal;

/**
 * Aggregated dashboard summary DTO.
 * This is a cross-domain read-only view that combines data from
 * multiple domain services: Trial Balance, Balance Sheet, P&amp;L, and Cash Flow.
 * The dashboard module owns no data — it only reads from other domains.
 */
public record DashboardSummaryDTO(
    String tenantId,
    TrialBalanceSummary trialBalance,
    BalanceSheetSummary balanceSheet,
    ProfitAndLossSummary profitAndLoss,
    CashFlowSummary cashFlow
) {

    public record TrialBalanceSummary(
        BigDecimal totalDebits,
        BigDecimal totalCredits,
        boolean balanced,
        int accountCount
    ) {}

    public record BalanceSheetSummary(
        BigDecimal totalAssets,
        BigDecimal totalLiabilities,
        BigDecimal totalEquity,
        boolean balanced
    ) {}

    public record ProfitAndLossSummary(
        BigDecimal totalRevenue,
        BigDecimal totalExpenses,
        BigDecimal netIncome
    ) {}

    public record CashFlowSummary(
        BigDecimal netCashFromOperating,
        BigDecimal netCashFromInvesting,
        BigDecimal netCashFromFinancing,
        BigDecimal netCashChange
    ) {}
}
