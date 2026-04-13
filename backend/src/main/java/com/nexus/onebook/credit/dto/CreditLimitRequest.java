package com.nexus.onebook.credit.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreditLimitRequest(
    @NotBlank String tenantId,
    @NotNull Long accountId,
    @NotNull @DecimalMin("0") BigDecimal creditLimit,
    @Min(1) int creditPeriodDays
) {}
