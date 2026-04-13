package com.nexus.onebook.ingestion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for syncing a corporate card transaction.
 */
public record CardTransactionRequest(

        @NotBlank(message = "Tenant ID is required")
        String tenantId,

        @NotBlank(message = "External ID is required")
        String externalId,

        String cardLastFour,

        String merchantName,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.0001", message = "Amount must be greater than zero")
        BigDecimal amount,

        String currency,

        @NotNull(message = "Transaction date is required")
        LocalDate transactionDate,

        String category,

        String description,

        String metadata
) {}
