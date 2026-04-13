package com.nexus.onebook.ledger.currency.controller;

import com.nexus.onebook.ledger.currency.dto.CurrencyConversionResult;
import com.nexus.onebook.ledger.currency.dto.CurrencyExchangeRateRequest;
import com.nexus.onebook.ledger.currency.model.CurrencyExchangeRate;
import com.nexus.onebook.ledger.currency.service.MultiCurrencyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for Multi-Currency operations — exchange rates and conversions.
 */
@RestController
@RequestMapping("/api/currency")
public class CurrencyController {

    private final MultiCurrencyService multiCurrencyService;

    public CurrencyController(MultiCurrencyService multiCurrencyService) {
        this.multiCurrencyService = multiCurrencyService;
    }

    @PostMapping("/rates")
    public ResponseEntity<CurrencyExchangeRate> addRate(@Valid @RequestBody CurrencyExchangeRateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(multiCurrencyService.addExchangeRate(request));
    }

    @GetMapping("/rates")
    public ResponseEntity<List<CurrencyExchangeRate>> getRates(@RequestParam String tenantId) {
        return ResponseEntity.ok(multiCurrencyService.getExchangeRates(tenantId));
    }

    @GetMapping("/convert")
    public ResponseEntity<CurrencyConversionResult> convert(
            @RequestParam String tenantId,
            @RequestParam String fromCurrency,
            @RequestParam String toCurrency,
            @RequestParam BigDecimal amount,
            @RequestParam LocalDate date) {
        return ResponseEntity.ok(multiCurrencyService.convert(tenantId, fromCurrency, toCurrency, amount, date));
    }

    @GetMapping("/gain-loss")
    public ResponseEntity<BigDecimal> calculateGainLoss(
            @RequestParam BigDecimal originalAmount,
            @RequestParam BigDecimal bookingRate,
            @RequestParam BigDecimal settlementRate) {
        return ResponseEntity.ok(multiCurrencyService.calculateExchangeGainLoss(
                originalAmount, bookingRate, settlementRate));
    }
}
