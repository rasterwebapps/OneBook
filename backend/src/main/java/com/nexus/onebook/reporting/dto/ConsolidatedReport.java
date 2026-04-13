package com.nexus.onebook.reporting.dto;

import java.math.BigDecimal;
import java.util.List;
import com.nexus.onebook.accounts.dto.BalanceSheetReport;
import com.nexus.onebook.accounts.dto.ProfitAndLossReport;

public record ConsolidatedReport(
    String tenantId,
    BalanceSheetReport consolidatedBalanceSheet,
    ProfitAndLossReport consolidatedProfitAndLoss,
    List<IntercompanyEliminationLine> eliminations,
    BigDecimal totalEliminationAmount
) {}
