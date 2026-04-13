package com.nexus.onebook.accounts.dto;

import java.math.BigDecimal;
import java.util.List;

public record BalanceSheetReport(
    String tenantId,
    List<TrialBalanceLine> assetLines,
    List<TrialBalanceLine> liabilityLines,
    List<TrialBalanceLine> equityLines,
    BigDecimal totalAssets,
    BigDecimal totalLiabilities,
    BigDecimal totalEquity,
    boolean balanced
) {}
