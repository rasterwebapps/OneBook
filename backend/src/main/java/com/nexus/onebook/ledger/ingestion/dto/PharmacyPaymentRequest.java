package com.nexus.onebook.ledger.ingestion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request DTO for ingesting a payment request from an integrated Pharmacy application.
 * Supports the complete maker-checker-approver-payer workflow.
 */
public record PharmacyPaymentRequest(

        @NotBlank(message = "Tenant ID is required")
        String tenantId,

        @NotBlank(message = "Application name is required")
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
