package com.nexus.onebook.ledger.intelligence.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for cash flow forecasting with 30/60/90 day projections.
 */
public record CashFlowForecast(
        String tenantId,
        BigDecimal currentCashPosition,
        BigDecimal forecast30Day,
        BigDecimal forecast60Day,
        BigDecimal forecast90Day,
        BigDecimal avgDailyInflow,
        BigDecimal avgDailyOutflow,
        String riskLevel,
        LocalDate generatedDate
) {}
