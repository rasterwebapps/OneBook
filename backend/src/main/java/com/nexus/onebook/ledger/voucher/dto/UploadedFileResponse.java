package com.nexus.onebook.ledger.voucher.dto;

import java.time.Instant;

public record UploadedFileResponse(
        Long id,
        String tenantId,
        String fileName,
        String originalFileName,
        String contentType,
        Long fileSize,
        String status,
        Instant processedAt,
        String errorMessage,
        String uploadedBy,
        Instant createdAt
) {}
