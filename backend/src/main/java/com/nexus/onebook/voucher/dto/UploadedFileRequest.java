package com.nexus.onebook.voucher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UploadedFileRequest(
        @NotBlank(message = "Tenant ID is required")
        String tenantId,
        @NotBlank(message = "File name is required")
        String fileName,
        @NotBlank(message = "Original file name is required")
        String originalFileName,
        String contentType,
        Long fileSize,
        String storagePath,
        @NotBlank(message = "Uploaded by is required")
        String uploadedBy
) {}
