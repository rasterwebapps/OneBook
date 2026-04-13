package com.nexus.onebook.voucher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreatePaymentAdviceRequest(
        @NotBlank(message = "Tenant ID is required")
        String tenantId,
        @NotBlank(message = "Advice number is required")
        String adviceNumber,
        Long voucherId,
        Long applicationId,
        Long departmentId,
        Long payerId,
        Long payerBankAccountId,
        Long payeeId,
        Long payeeBankAccountId,
        @NotNull(message = "Amount is required")
        BigDecimal amount,
        String paymentMode,
        String twoStepVerification,
        String description,
        @NotBlank(message = "Created by is required")
        String createdBy
) {}
