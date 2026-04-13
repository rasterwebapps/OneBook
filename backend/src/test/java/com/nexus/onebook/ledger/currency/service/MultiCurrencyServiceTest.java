package com.nexus.onebook.ledger.currency.service;

import com.nexus.onebook.ledger.currency.dto.CurrencyConversionResult;
import com.nexus.onebook.ledger.currency.dto.CurrencyExchangeRateRequest;
import com.nexus.onebook.ledger.currency.model.CurrencyExchangeRate;
import com.nexus.onebook.ledger.currency.repository.CurrencyExchangeRateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MultiCurrencyServiceTest {

    @Mock
    private CurrencyExchangeRateRepository exchangeRateRepository;

    @InjectMocks
    private MultiCurrencyService multiCurrencyService;

    @Test
    void addExchangeRate_withSource_setsSource() {
        CurrencyExchangeRateRequest request = new CurrencyExchangeRateRequest(
                "tenant-1", "USD", "INR", new BigDecimal("83.50"),
                LocalDate.of(2024, 1, 15), "RBI");

        when(exchangeRateRepository.save(any(CurrencyExchangeRate.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CurrencyExchangeRate result = multiCurrencyService.addExchangeRate(request);

        assertNotNull(result);
        assertEquals("RBI", result.getSource());
        assertEquals("USD", result.getFromCurrency());
    }

    @Test
    void addExchangeRate_withoutSource_keepsDefault() {
        CurrencyExchangeRateRequest request = new CurrencyExchangeRateRequest(
                "tenant-1", "EUR", "INR", new BigDecimal("90.25"),
                LocalDate.of(2024, 1, 15), null);

        when(exchangeRateRepository.save(any(CurrencyExchangeRate.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CurrencyExchangeRate result = multiCurrencyService.addExchangeRate(request);

        assertNotNull(result);
        assertEquals("MANUAL", result.getSource());
    }

    @Test
    void convert_rateExists_returnsConvertedAmount() {
        CurrencyExchangeRate rate = new CurrencyExchangeRate(
                "tenant-1", "USD", "INR", new BigDecimal("83.50"),
                LocalDate.of(2024, 1, 15));
        LocalDate date = LocalDate.of(2024, 1, 20);

        when(exchangeRateRepository
                .findFirstByTenantIdAndFromCurrencyAndToCurrencyAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                        "tenant-1", "USD", "INR", date))
                .thenReturn(Optional.of(rate));

        CurrencyConversionResult result = multiCurrencyService.convert(
                "tenant-1", "USD", "INR", new BigDecimal("100"), date);

        assertNotNull(result);
        assertEquals("USD", result.fromCurrency());
        assertEquals("INR", result.toCurrency());
        BigDecimal expected = new BigDecimal("100").multiply(new BigDecimal("83.50"))
                .setScale(4, RoundingMode.HALF_UP);
        assertEquals(expected, result.convertedAmount());
    }

    @Test
    void convert_noRateFound_throwsException() {
        LocalDate date = LocalDate.of(2024, 1, 20);

        when(exchangeRateRepository
                .findFirstByTenantIdAndFromCurrencyAndToCurrencyAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                        "tenant-1", "USD", "GBP", date))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                multiCurrencyService.convert("tenant-1", "USD", "GBP",
                        new BigDecimal("100"), date));
    }

    @Test
    void calculateExchangeGainLoss_returnsCorrectValue() {
        BigDecimal originalAmount = new BigDecimal("1000");
        BigDecimal bookingRate = new BigDecimal("83.00");
        BigDecimal settlementRate = new BigDecimal("84.50");

        BigDecimal result = multiCurrencyService.calculateExchangeGainLoss(
                originalAmount, bookingRate, settlementRate);

        // (1000 * 84.50) - (1000 * 83.00) = 84500 - 83000 = 1500.0000
        assertEquals(new BigDecimal("1500.0000"), result);
    }
}
