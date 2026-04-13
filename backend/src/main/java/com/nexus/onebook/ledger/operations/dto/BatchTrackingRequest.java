package com.nexus.onebook.ledger.operations.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record BatchTrackingRequest(
    @NotBlank String tenantId,
    @NotNull Long stockItemId,
    @NotBlank String batchNumber,
    LocalDate manufacturingDate,
    LocalDate expiryDate,
    Long godownId,
    @NotNull @DecimalMin("0") BigDecimal quantity,
    BigDecimal costPerUnit
) {}
