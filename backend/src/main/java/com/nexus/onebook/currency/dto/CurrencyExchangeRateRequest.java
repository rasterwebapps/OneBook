package com.nexus.onebook.currency.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CurrencyExchangeRateRequest(
    @NotBlank String tenantId,
    @NotBlank String fromCurrency,
    @NotBlank String toCurrency,
    @NotNull @DecimalMin("0.00000001") BigDecimal exchangeRate,
    @NotNull LocalDate effectiveDate,
    String source
) {}
