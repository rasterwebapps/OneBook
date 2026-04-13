package com.nexus.onebook.ledger.currency.dto;

import java.math.BigDecimal;

public record CurrencyConversionResult(
    String fromCurrency,
    String toCurrency,
    BigDecimal originalAmount,
    BigDecimal convertedAmount,
    BigDecimal exchangeRate,
    java.time.LocalDate rateDate
) {}
