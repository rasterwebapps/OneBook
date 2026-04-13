package com.nexus.onebook.intelligence.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for mark-to-market portfolio valuation.
 */
public record MarketValuation(
        String tenantId,
        int totalHoldings,
        BigDecimal totalCostBasis,
        BigDecimal totalMarketValue,
        BigDecimal totalUnrealizedGainLoss,
        BigDecimal gainLossPercent,
        LocalDate valuationDate,
        List<HoldingValuation> holdings
) {}
