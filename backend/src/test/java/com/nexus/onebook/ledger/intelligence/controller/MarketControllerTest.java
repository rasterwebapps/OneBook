package com.nexus.onebook.ledger.intelligence.controller;

import com.nexus.onebook.ledger.intelligence.dto.MarketSentiment;
import com.nexus.onebook.ledger.intelligence.dto.MarketValuation;
import com.nexus.onebook.ledger.exception.GlobalExceptionHandler;
import com.nexus.onebook.ledger.intelligence.service.CorporateActionService;
import com.nexus.onebook.ledger.intelligence.service.MarkToMarketService;
import com.nexus.onebook.ledger.intelligence.service.MarketSentimentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MarketController.class)
@Import(GlobalExceptionHandler.class)
class MarketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MarkToMarketService markToMarketService;

    @MockitoBean
    private CorporateActionService corporateActionService;

    @MockitoBean
    private MarketSentimentService marketSentimentService;

    @Test
    void getValuation_returnsOk() throws Exception {
        MarketValuation valuation = new MarketValuation(
                "t1",
                0,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                LocalDate.now(),
                List.of()
        );

        when(markToMarketService.valuatePortfolio("t1")).thenReturn(valuation);

        mockMvc.perform(get("/api/market/valuation").param("tenantId", "t1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("t1"))
                .andExpect(jsonPath("$.totalHoldings").value(0));
    }

    @Test
    void getSentiment_returnsOk() throws Exception {
        MarketSentiment sentiment = new MarketSentiment(
                "AAPL", "Apple rises", "MarketWatch",
                "POSITIVE", "Strong earnings", LocalDate.now()
        );

        when(marketSentimentService.getSentimentForPortfolio("t1"))
                .thenReturn(List.of(sentiment));

        mockMvc.perform(get("/api/market/sentiment").param("tenantId", "t1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$[0].sentimentScore").value("POSITIVE"));
    }

    @Test
    void getSentimentForSymbol_returnsOk() throws Exception {
        MarketSentiment sentiment = new MarketSentiment(
                "AAPL", "Apple rises", "MarketWatch",
                "POSITIVE", "Strong earnings", LocalDate.now()
        );

        when(marketSentimentService.getSentimentForSymbol("AAPL")).thenReturn(sentiment);

        mockMvc.perform(get("/api/market/sentiment/AAPL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.sentimentScore").value("POSITIVE"));
    }
}
