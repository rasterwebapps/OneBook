package com.nexus.onebook.voucher.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record VoucherResponse(
        Long id,
        String tenantId,
        String voucherNumber,
        String voucherTypeName,
        String departmentName,
        String payerName,
        Instant voucherDate,
        String status,
        String closureType,
        BigDecimal totalAmount,
        BigDecimal approvedAmount,
        BigDecimal tdsAmount,
        BigDecimal netAmount,
        String paymentMode,
        String description,
        String remarks,
        String approvedBy,
        Instant approvedAt,
        boolean cancelled,
        String createdBy,
        Instant createdAt
) {}
