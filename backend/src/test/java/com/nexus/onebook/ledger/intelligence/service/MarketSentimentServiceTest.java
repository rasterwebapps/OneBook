package com.nexus.onebook.ledger.intelligence.service;

import com.nexus.onebook.ledger.intelligence.dto.MarketSentiment;
import com.nexus.onebook.ledger.intelligence.model.HoldingType;
import com.nexus.onebook.ledger.intelligence.model.InvestmentHolding;
import com.nexus.onebook.ledger.intelligence.repository.InvestmentHoldingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketSentimentServiceTest {

    @Mock
    private InvestmentHoldingRepository holdingRepository;

    @InjectMocks
    private MarketSentimentService marketSentimentService;

    @Test
    void getSentimentForPortfolio_noHoldings_returnsEmpty() {
        when(holdingRepository.findByTenantId("tenant-1"))
                .thenReturn(Collections.emptyList());

        List<MarketSentiment> sentiments = marketSentimentService.getSentimentForPortfolio("tenant-1");

        assertTrue(sentiments.isEmpty());
    }

    @Test
    void getSentimentForPortfolio_withHoldings_returnsForEach() {
        InvestmentHolding gainHolding = new InvestmentHolding(
                "tenant-1", "AAPL", "Apple Inc", HoldingType.EQUITY_SHARE,
                new BigDecimal("100"), new BigDecimal("10000"));
        gainHolding.setUnrealizedGainLoss(new BigDecimal("5000"));

        InvestmentHolding lossHolding = new InvestmentHolding(
                "tenant-1", "GOOG", "Alphabet Inc", HoldingType.EQUITY_SHARE,
                new BigDecimal("50"), new BigDecimal("8000"));
        lossHolding.setUnrealizedGainLoss(new BigDecimal("-2000"));

        when(holdingRepository.findByTenantId("tenant-1"))
                .thenReturn(List.of(gainHolding, lossHolding));

        List<MarketSentiment> sentiments = marketSentimentService.getSentimentForPortfolio("tenant-1");

        assertEquals(2, sentiments.size());
        assertEquals("POSITIVE", sentiments.get(0).sentimentScore());
        assertEquals("NEGATIVE", sentiments.get(1).sentimentScore());
    }

    @Test
    void getSentimentForSymbol_returnsNeutral() {
        MarketSentiment sentiment = marketSentimentService.getSentimentForSymbol("AAPL");

        assertEquals("AAPL", sentiment.symbol());
        assertEquals("NEUTRAL", sentiment.sentimentScore());
        assertEquals("OneBook Market Intelligence", sentiment.source());
        assertNotNull(sentiment.publishedDate());
    }
}
