package com.nexus.onebook.inventory.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ReorderLevelRequest(
    @NotBlank String tenantId,
    @NotNull Long stockItemId,
    Long godownId,
    @NotNull @DecimalMin("0") BigDecimal minimumLevel,
    @NotNull @DecimalMin("0") BigDecimal reorderLevel,
    @NotNull @DecimalMin("0") BigDecimal maximumLevel,
    @NotNull @DecimalMin("0") BigDecimal reorderQuantity
) {}
