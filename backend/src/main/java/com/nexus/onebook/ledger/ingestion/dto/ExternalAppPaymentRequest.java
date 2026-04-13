package com.nexus.onebook.ledger.ingestion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import com.nexus.onebook.ledger.foundation.model.Application;

/**
 * Common request DTO for ingesting a payment request from any integrated external application
 * (Pharmacy, Lab, Stores, HIS, or any other system).
 * <p>
 * The {@code applicationName} field identifies the source system (e.g. {@code PHARMACY},
 * {@code LAB}, {@code STORE}, {@code HIS}), while the rest of the structure is shared
 * across all external integrations. This common type replaces the need for per-application
 * request DTOs and allows the OneBook ingestion layer to scale to new integrations
 * without changing the API contract.
 * <p>
 * Supports the complete maker-checker-approver-payer workflow.
 */
public record ExternalAppPaymentRequest(

        @NotBlank(message = "Tenant ID is required")
        String tenantId,

        @NotBlank(message = "Application name is required (e.g. PHARMACY, LAB, STORE, HIS)")
        String applicationName,

        @NotNull(message = "Payment data is required")
        @Valid
        PaymentData paymentData,

        DocumentInfo documentInfo,

        PaymentMetadata metadata

) {

    public record PaymentData(
            String invoiceNumber,
            String invoiceDate,
            String payerName,
            String payeeType,
            String payeeName,
            BankDetails bankDetails,
            Amounts amounts,
            String paymentMode,
            String transactionType,
            String dueDate
    ) {}

    public record BankDetails(
            String accountNumber,
            String accountName,
            String bankName,
            String branchName,
            String ifscCode
    ) {}

    public record Amounts(
            BigDecimal grossAmount,
            BigDecimal netBillAmount,
            BigDecimal tdsAmount,
            BigDecimal deductions,
            BigDecimal payableAmount
    ) {}

    public record DocumentInfo(
            String invoiceFilePath,
            String fileName,
            String contentType,
            Long fileSize,
            String checksum
    ) {}

    public record PaymentMetadata(
            String branchId,
            String organizationId,
            String createdBy,
            String sourceSystem,
            String batchNumber
    ) {}
}
