package com.nexus.onebook.intelligence.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for corporate actions such as stock splits, dividends, or bonus issues.
 */
public record CorporateActionRequest(

        @NotBlank(message = "Tenant ID is required")
        String tenantId,

        @NotNull(message = "Holding ID is required")
        Long holdingId,

        @NotBlank(message = "Action type is required")
        String actionType,

        @NotNull(message = "Record date is required")
        LocalDate recordDate,

        BigDecimal ratio,

        BigDecimal amountPerUnit
) {}
