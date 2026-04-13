package com.nexus.onebook.currency.service;

import com.nexus.onebook.currency.dto.CurrencyConversionResult;
import com.nexus.onebook.currency.dto.CurrencyExchangeRateRequest;
import com.nexus.onebook.currency.model.CurrencyExchangeRate;
import com.nexus.onebook.currency.repository.CurrencyExchangeRateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Multi-currency service — handles exchange rate management,
 * currency conversion, and exchange rate gain/loss calculation.
 */
@Service
public class MultiCurrencyService {

    private final CurrencyExchangeRateRepository exchangeRateRepository;

    public MultiCurrencyService(CurrencyExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    @Transactional
    public CurrencyExchangeRate addExchangeRate(CurrencyExchangeRateRequest request) {
        CurrencyExchangeRate rate = new CurrencyExchangeRate(
                request.tenantId(), request.fromCurrency(), request.toCurrency(),
                request.exchangeRate(), request.effectiveDate());
        if (request.source() != null) {
            rate.setSource(request.source());
        }
        return exchangeRateRepository.save(rate);
    }

    @Transactional(readOnly = true)
    public List<CurrencyExchangeRate> getExchangeRates(String tenantId) {
        return exchangeRateRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public CurrencyConversionResult convert(String tenantId, String fromCurrency,
                                             String toCurrency, BigDecimal amount, LocalDate date) {
        CurrencyExchangeRate rate = exchangeRateRepository
                .findFirstByTenantIdAndFromCurrencyAndToCurrencyAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                        tenantId, fromCurrency, toCurrency, date)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No exchange rate found for " + fromCurrency + " → " + toCurrency));

        BigDecimal convertedAmount = amount.multiply(rate.getExchangeRate())
                .setScale(4, RoundingMode.HALF_UP);

        return new CurrencyConversionResult(
                fromCurrency, toCurrency, amount, convertedAmount,
                rate.getExchangeRate(), rate.getEffectiveDate());
    }

    /**
     * Calculates exchange rate gain or loss between booking rate and settlement rate.
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateExchangeGainLoss(BigDecimal originalAmount,
                                                 BigDecimal bookingRate,
                                                 BigDecimal settlementRate) {
        BigDecimal bookedValue = originalAmount.multiply(bookingRate);
        BigDecimal settledValue = originalAmount.multiply(settlementRate);
        return settledValue.subtract(bookedValue).setScale(4, RoundingMode.HALF_UP);
    }
}
