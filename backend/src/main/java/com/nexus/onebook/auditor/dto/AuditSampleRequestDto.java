package com.nexus.onebook.auditor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

/**
 * Request DTO for creating an audit sample request.
 */
public record AuditSampleRequestDto(
        @NotBlank String tenantId,
        @NotBlank String auditorName,
        @NotBlank String auditorEmail,
        @NotBlank String requestDescription,
        @NotBlank String tableName,
        @NotNull @Positive Integer sampleSize,
        LocalDate dateFrom,
        LocalDate dateTo
) {}
