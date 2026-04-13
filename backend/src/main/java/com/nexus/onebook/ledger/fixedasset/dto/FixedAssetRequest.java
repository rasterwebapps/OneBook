package com.nexus.onebook.ledger.fixedasset.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record FixedAssetRequest(
    @NotBlank String tenantId,
    @NotBlank String assetCode,
    @NotBlank String assetName,
    String description,
    @NotNull Long assetAccountId,
    @NotNull Long depreciationAccountId,
    @NotNull LocalDate purchaseDate,
    @NotNull @DecimalMin("0.01") BigDecimal purchaseCost,
    BigDecimal salvageValue,
    @Min(1) int usefulLifeMonths,
    String depreciationMethod,
    Long branchId
) {}
