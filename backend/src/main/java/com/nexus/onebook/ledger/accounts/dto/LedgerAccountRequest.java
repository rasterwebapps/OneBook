package com.nexus.onebook.ledger.accounts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a ledger account in the Chart of Accounts.
 */
public record LedgerAccountRequest(

        @NotBlank(message = "Tenant ID is required")
        String tenantId,

        @NotNull(message = "Cost Center ID is required")
        Long costCenterId,

        @NotBlank(message = "Account code is required")
        String accountCode,

        @NotBlank(message = "Account name is required")
        String accountName,

        @NotBlank(message = "Account type is required (ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE)")
        String accountType,

        Long parentAccountId,

        String metadata
) {}
