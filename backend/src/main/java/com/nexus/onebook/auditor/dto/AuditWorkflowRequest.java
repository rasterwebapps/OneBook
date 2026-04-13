package com.nexus.onebook.auditor.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating an audit workflow approval.
 */
public record AuditWorkflowRequest(
        @NotBlank String tenantId,
        @NotBlank String workflowName,
        String description,
        @NotBlank String auditorName
) {}
