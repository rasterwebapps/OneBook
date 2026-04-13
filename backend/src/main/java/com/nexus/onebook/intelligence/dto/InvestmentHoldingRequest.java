package com.nexus.onebook.intelligence.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Request DTO for creating an investment holding (equity, mutual fund, bond, etc.).
 */
public record InvestmentHoldingRequest(

        @NotBlank(message = "Tenant ID is required")
        String tenantId,

        @NotBlank(message = "Symbol is required")
        String symbol,

        @NotBlank(message = "Holding name is required")
        String holdingName,

        @NotBlank(message = "Holding type is required")
        String holdingType,

        @NotNull(message = "Quantity is required")
        BigDecimal quantity,

        @NotNull(message = "Cost basis is required")
        BigDecimal costBasis,

        Long ledgerAccountId
) {}
