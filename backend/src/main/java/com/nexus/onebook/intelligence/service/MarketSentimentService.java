package com.nexus.onebook.intelligence.service;

import com.nexus.onebook.intelligence.dto.MarketSentiment;
import com.nexus.onebook.intelligence.model.InvestmentHolding;
import com.nexus.onebook.intelligence.repository.InvestmentHoldingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class MarketSentimentService {

    private final InvestmentHoldingRepository holdingRepository;

    public MarketSentimentService(InvestmentHoldingRepository holdingRepository) {
        this.holdingRepository = holdingRepository;
    }

    @Transactional(readOnly = true)
    public List<MarketSentiment> getSentimentForPortfolio(String tenantId) {
        List<InvestmentHolding> holdings = holdingRepository.findByTenantId(tenantId);

        return holdings.stream()
                .map(this::toMarketSentiment)
                .toList();
    }

    @Transactional(readOnly = true)
    public MarketSentiment getSentimentForSymbol(String symbol) {
        return new MarketSentiment(
                symbol,
                "Analysis for " + symbol,
                "OneBook Market Intelligence",
                "NEUTRAL",
                "Market analysis pending for " + symbol,
                LocalDate.now());
    }

    private MarketSentiment toMarketSentiment(InvestmentHolding holding) {
        BigDecimal gainLoss = holding.getUnrealizedGainLoss() != null
                ? holding.getUnrealizedGainLoss() : BigDecimal.ZERO;

        String sentimentScore;
        String summary;
        int comparison = gainLoss.compareTo(BigDecimal.ZERO);

        if (comparison > 0) {
            sentimentScore = "POSITIVE";
            summary = holding.getHoldingName() + " is showing positive returns with an unrealized gain of "
                    + gainLoss.toPlainString();
        } else if (comparison < 0) {
            sentimentScore = "NEGATIVE";
            summary = holding.getHoldingName() + " is showing negative returns with an unrealized loss of "
                    + gainLoss.toPlainString();
        } else {
            sentimentScore = "NEUTRAL";
            summary = holding.getHoldingName() + " is at break-even with no unrealized gain or loss";
        }

        return new MarketSentiment(
                holding.getSymbol(),
                "Market update for " + holding.getHoldingName(),
                "OneBook Market Intelligence",
                sentimentScore,
                summary,
                LocalDate.now());
    }
}
