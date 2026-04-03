package com.nexus.onebook.ledger.voucher.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ReceiptResponse(
        Long id,
        String tenantId,
        String receiptNumber,
        Long voucherId,
        String payerName,
        String payeeName,
        BigDecimal amount,
        String paymentMode,
        String referenceNumber,
        String status,
        Instant receiptDate,
        String description,
        String createdBy,
        Instant createdAt
) {}
