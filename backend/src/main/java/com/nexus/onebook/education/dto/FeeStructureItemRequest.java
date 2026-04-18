package com.nexus.onebook.education.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for a single fee structure item.
 */
public record FeeStructureItemRequest(
        @NotNull UUID feeTypeId,
        @NotNull @Positive BigDecimal amount
) {}
