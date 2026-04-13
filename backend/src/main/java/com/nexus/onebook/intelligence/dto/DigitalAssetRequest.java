package com.nexus.onebook.intelligence.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Request DTO for registering a digital asset (cryptocurrency, token, NFT, etc.).
 */
public record DigitalAssetRequest(

        @NotBlank(message = "Tenant ID is required")
        String tenantId,

        @NotBlank(message = "Symbol is required")
        String symbol,

        @NotBlank(message = "Asset name is required")
        String assetName,

        @NotBlank(message = "Asset type is required")
        String assetType,

        @NotNull(message = "Quantity is required")
        BigDecimal quantity,

        @NotNull(message = "Cost basis is required")
        BigDecimal costBasis,

        String walletAddress,

        Long ledgerAccountId
) {}
