package com.nexus.onebook.ledger.intelligence.dto;

import java.time.LocalDate;

/**
 * Response DTO for market sentiment analysis of a security.
 */
public record MarketSentiment(
        String symbol,
        String headline,
        String source,
        String sentimentScore,
        String summary,
        LocalDate publishedDate
) {}
