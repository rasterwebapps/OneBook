package com.nexus.onebook.ledger.intelligence.dto;

import java.math.BigDecimal;

/**
 * Response DTO representing the valuation of a single holding.
 */
public record HoldingValuation(
        String symbol,
        String holdingName,
        String holdingType,
        BigDecimal quantity,
        BigDecimal costBasis,
        BigDecimal currentPrice,
        BigDecimal marketValue,
        BigDecimal unrealizedGainLoss,
        BigDecimal gainLossPercent
) {}
