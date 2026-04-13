package com.nexus.onebook.ledger.fixedasset.dto;

import java.math.BigDecimal;

public record DepreciationSchedule(
    Long assetId,
    String assetCode,
    String assetName,
    BigDecimal monthlyDepreciation,
    BigDecimal accumulatedDepreciation,
    BigDecimal netBookValue
) {}
