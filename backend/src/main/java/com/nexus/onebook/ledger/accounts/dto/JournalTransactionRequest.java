package com.nexus.onebook.ledger.accounts.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for creating a complete journal transaction with its entries.
 * Must contain at least one debit and one credit entry that balance.
 */
public record JournalTransactionRequest(

        @NotBlank(message = "Tenant ID is required")
        String tenantId,

        @NotNull(message = "Transaction date is required")
        LocalDate transactionDate,

        String description,

        String metadata,

        @NotEmpty(message = "At least two journal entries are required")
        @Valid
        List<JournalEntryRequest> entries
) {}
