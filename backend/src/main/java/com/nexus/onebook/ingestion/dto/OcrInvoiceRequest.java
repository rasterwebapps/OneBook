package com.nexus.onebook.ingestion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for an OCR-extracted invoice submission.
 */
public record OcrInvoiceRequest(

        @NotBlank(message = "Tenant ID is required")
        String tenantId,

        @NotBlank(message = "Invoice number is required")
        String invoiceNumber,

        @NotBlank(message = "Vendor name is required")
        String vendorName,

        String poNumber,

        @NotNull(message = "Total amount is required")
        BigDecimal totalAmount,

        String currency,

        @NotNull(message = "Invoice date is required")
        LocalDate invoiceDate,

        String lineItems,

        String rawOcrText
) {}
