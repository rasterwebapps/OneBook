package com.nexus.onebook.ledger.compliance.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * Request DTO for creating or updating a compliance certification.
 */
public record ComplianceCertificationRequest(
        @NotBlank String tenantId,
        @NotBlank String certificationName,
        @NotBlank String issuingBody,
        @NotBlank String industry,
        LocalDate issuedDate,
        LocalDate expiryDate,
        String certificateReference,
        String notes
) {}
