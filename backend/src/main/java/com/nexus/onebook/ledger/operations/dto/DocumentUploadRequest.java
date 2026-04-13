package com.nexus.onebook.ledger.operations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for uploading a document to the vault.
 */
public record DocumentUploadRequest(
        @NotBlank String tenantId,
        @NotBlank String fileName,
        @NotBlank String contentType,
        @NotNull @Positive Long fileSize,
        @NotBlank String checksum,
        Long journalTransactionId,
        String uploadedBy
) {}
