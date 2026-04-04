package com.nexus.onebook.ledger.voucher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public record CreateReceiptRequest(
        @NotBlank(message = "Tenant ID is required")
        String tenantId,
        @NotBlank(message = "Receipt number is required")
        String receiptNumber,
        Long voucherId,
        Long payerId,
        Long payerBankAccountId,
        Long payeeId,
        Long payeeBankAccountId,
        Long fromLedgerAccountId,
        Long toLedgerAccountId,
        @NotNull(message = "Amount is required")
        BigDecimal amount,
        String paymentMode,
        String referenceNumber,
        Instant receiptDate,
        String description,
        @NotBlank(message = "Created by is required")
        String createdBy
) {}
