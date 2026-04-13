package com.nexus.onebook.ledger.operations.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for initiating a disaster recovery operation.
 */
public record DisasterRecoveryRequest(
        @NotBlank String tenantId,
        @NotBlank String eventType,
        String backupLocation
) {}
