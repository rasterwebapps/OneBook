package com.nexus.onebook.voucher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateVoucherItemRequest(
        @NotBlank(message = "Tenant ID is required")
        String tenantId,
        @NotNull(message = "Voucher ID is required")
        Long voucherId,
        Integer itemNumber,
        Long payeeId,
        Long payeeBankAccountId,
        Long ledgerAccountId,
        Long costCenterId,
        String description,
        @NotNull(message = "Amount is required")
        BigDecimal amount,
        boolean tdsApplicable,
        BigDecimal tdsPercentage,
        BigDecimal tdsAmount,
        @NotNull(message = "Net amount is required")
        BigDecimal netAmount,
        String remarks
) {}
