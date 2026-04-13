package com.nexus.onebook.ledger.accounts.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Request DTO for a single journal entry line (debit or credit).
 */
public record JournalEntryRequest(

        @NotNull(message = "Account ID is required")
        Long accountId,

        @NotBlank(message = "Entry type is required (DEBIT or CREDIT)")
        String entryType,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.0001", message = "Amount must be greater than zero")
        BigDecimal amount,

        String description,

        String metadata
) {}
