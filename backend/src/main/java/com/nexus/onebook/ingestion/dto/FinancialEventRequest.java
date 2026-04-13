package com.nexus.onebook.ingestion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for ingesting a raw financial event through the gateway.
 * The adapter type determines which parser processes the raw payload.
 */
public record FinancialEventRequest(

        @NotBlank(message = "Tenant ID is required")
        String tenantId,

        @NotBlank(message = "Adapter type is required")
        String adapterType,

        @NotNull(message = "Payload is required")
        String payload
) {}
