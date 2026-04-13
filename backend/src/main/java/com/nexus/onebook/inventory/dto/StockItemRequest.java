package com.nexus.onebook.inventory.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record StockItemRequest(
    @NotBlank String tenantId,
    @NotBlank String itemCode,
    @NotBlank String itemName,
    String description,
    Long stockGroupId,
    @NotNull Long primaryUomId,
    Long secondaryUomId,
    BigDecimal conversionFactor,
    BigDecimal openingBalance,
    BigDecimal ratePerUnit,
    String hsnCode
) {}
