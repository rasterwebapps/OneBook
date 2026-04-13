package com.nexus.onebook.ledger.currency.controller;

import com.nexus.onebook.ledger.currency.dto.CurrencyConversionResult;
import com.nexus.onebook.ledger.exception.GlobalExceptionHandler;
import com.nexus.onebook.ledger.currency.model.CurrencyExchangeRate;
import com.nexus.onebook.ledger.currency.service.MultiCurrencyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CurrencyController.class)
@Import(GlobalExceptionHandler.class)
class CurrencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MultiCurrencyService multiCurrencyService;

    @Test
    void addRate_validRequest_returns201() throws Exception {
        CurrencyExchangeRate rate = new CurrencyExchangeRate(
                "t1", "USD", "INR", new BigDecimal("83.50"), LocalDate.of(2024, 1, 15));
        rate.setId(1L);

        when(multiCurrencyService.addExchangeRate(any())).thenReturn(rate);

        mockMvc.perform(post("/api/currency/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "t1",
                                    "fromCurrency": "USD",
                                    "toCurrency": "INR",
                                    "exchangeRate": 83.50,
                                    "effectiveDate": "2024-01-15"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fromCurrency").value("USD"))
                .andExpect(jsonPath("$.toCurrency").value("INR"))
                .andExpect(jsonPath("$.exchangeRate").value(83.50));
    }

    @Test
    void getRates_returnsList() throws Exception {
        CurrencyExchangeRate rate = new CurrencyExchangeRate(
                "t1", "USD", "INR", new BigDecimal("83.50"), LocalDate.of(2024, 1, 15));
        rate.setId(1L);

        when(multiCurrencyService.getExchangeRates("t1")).thenReturn(List.of(rate));

        mockMvc.perform(get("/api/currency/rates")
                        .param("tenantId", "t1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fromCurrency").value("USD"))
                .andExpect(jsonPath("$[0].toCurrency").value("INR"));
    }

    @Test
    void convert_returnsResult() throws Exception {
        CurrencyConversionResult result = new CurrencyConversionResult(
                "USD", "INR", new BigDecimal("100"), new BigDecimal("8350.0000"),
                new BigDecimal("83.50"), LocalDate.of(2024, 1, 15));

        when(multiCurrencyService.convert("t1", "USD", "INR",
                new BigDecimal("100"), LocalDate.of(2024, 1, 15)))
                .thenReturn(result);

        mockMvc.perform(get("/api/currency/convert")
                        .param("tenantId", "t1")
                        .param("fromCurrency", "USD")
                        .param("toCurrency", "INR")
                        .param("amount", "100")
                        .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromCurrency").value("USD"))
                .andExpect(jsonPath("$.toCurrency").value("INR"))
                .andExpect(jsonPath("$.convertedAmount").value(8350.0000));
    }

    @Test
    void calculateGainLoss_returnsValue() throws Exception {
        when(multiCurrencyService.calculateExchangeGainLoss(
                new BigDecimal("1000"), new BigDecimal("83.00"), new BigDecimal("84.00")))
                .thenReturn(new BigDecimal("1000.0000"));

        mockMvc.perform(get("/api/currency/gain-loss")
                        .param("originalAmount", "1000")
                        .param("bookingRate", "83.00")
                        .param("settlementRate", "84.00"))
                .andExpect(status().isOk())
                .andExpect(content().string("1000.0000"));
    }
}
