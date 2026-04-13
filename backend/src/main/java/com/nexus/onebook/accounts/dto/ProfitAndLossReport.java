package com.nexus.onebook.accounts.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProfitAndLossReport(
    String tenantId,
    List<TrialBalanceLine> revenueLines,
    List<TrialBalanceLine> expenseLines,
    BigDecimal totalRevenue,
    BigDecimal totalExpenses,
    BigDecimal netIncome
) {}
