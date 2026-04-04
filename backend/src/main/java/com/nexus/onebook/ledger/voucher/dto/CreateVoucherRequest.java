package com.nexus.onebook.ledger.voucher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public record CreateVoucherRequest(
        @NotBlank(message = "Tenant ID is required")
        String tenantId,
        @NotBlank(message = "Voucher number is required")
        String voucherNumber,
        Long voucherTypeId,
        Long departmentId,
        Long subDepartmentId,
        Long payerId,
        Long payerBankAccountId,
        Instant voucherDate,
        @NotNull(message = "Total amount is required")
        BigDecimal totalAmount,
        BigDecimal tdsAmount,
        @NotNull(message = "Net amount is required")
        BigDecimal netAmount,
        String paymentMode,
        String description,
        String remarks,
        @NotBlank(message = "Created by is required")
        String createdBy
) {}
