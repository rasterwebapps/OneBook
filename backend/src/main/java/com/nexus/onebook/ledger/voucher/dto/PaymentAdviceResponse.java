package com.nexus.onebook.ledger.voucher.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentAdviceResponse(
        Long id,
        String tenantId,
        String adviceNumber,
        Long voucherId,
        String departmentName,
        String payerName,
        String payeeName,
        BigDecimal amount,
        String paymentMode,
        String status,
        String twoStepVerification,
        String approvedBy,
        Instant approvedAt,
        String rejectedBy,
        String rejectionReason,
        Instant paidAt,
        String transactionReference,
        String description,
        String createdBy,
        Instant createdAt
) {}
