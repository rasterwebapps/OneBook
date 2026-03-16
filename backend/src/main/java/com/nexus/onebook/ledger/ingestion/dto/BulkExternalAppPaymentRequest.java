package com.nexus.onebook.ledger.ingestion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO for bulk ingestion of external application payment requests in a single API call.
 * Common across all source applications (Pharmacy, Lab, Stores, HIS, etc.).
 */
public record BulkExternalAppPaymentRequest(

        @NotBlank(message = "Tenant ID is required")
        String tenantId,

        @NotNull
        @NotEmpty(message = "At least one payment request is required")
        @Valid
        List<ExternalAppPaymentRequest> requests
) {}
