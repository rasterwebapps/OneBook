package com.nexus.onebook.ledger.auditor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating an audit comment on a record.
 */
public record AuditCommentRequest(
        @NotBlank String tenantId,
        @NotBlank String auditorName,
        @NotBlank String tableName,
        @NotNull Long recordId,
        @NotBlank String commentText
) {}
