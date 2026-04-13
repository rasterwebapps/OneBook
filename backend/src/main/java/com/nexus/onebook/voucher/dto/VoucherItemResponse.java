package com.nexus.onebook.voucher.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record VoucherItemResponse(
        Long id,
        String tenantId,
        Long voucherId,
        Integer itemNumber,
        String payeeName,
        String ledgerAccountName,
        String costCenterName,
        String description,
        BigDecimal amount,
        boolean tdsApplicable,
        BigDecimal tdsPercentage,
        BigDecimal tdsAmount,
        BigDecimal netAmount,
        String status,
        String remarks,
        Instant createdAt
) {}
