package com.nexus.onebook.banking.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ConnectedPaymentRequest(
    @NotBlank String tenantId,
    @NotNull Long bankAccountId,
    @NotBlank String beneficiaryName,
    @NotBlank String beneficiaryAccount,
    String ifscCode,
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    @NotBlank String paymentMode
) {}
