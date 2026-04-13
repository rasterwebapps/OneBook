package com.nexus.onebook.ledger.intelligence.service;

import com.nexus.onebook.ledger.intelligence.dto.MarketValuation;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkToMarketServiceTest {

    @Mock
    private InvestmentHoldingRepository holdingRepository;

    @InjectMocks
    private MarkToMarketService markToMarketService;

    @Test
    void valuatePortfolio_noHoldings_returnsEmpty() {
        when(holdingRepository.findByTenantId("tenant-1"))
                .thenReturn(Collections.emptyList());

        MarketValuation valuation = markToMarketService.valuatePortfolio("tenant-1");

        assertEquals("tenant-1", valuation.tenantId());
        assertEquals(0, valuation.totalHoldings());
        assertEquals(0, BigDecimal.ZERO.compareTo(valuation.totalCostBasis()));
        assertEquals(0, BigDecimal.ZERO.compareTo(valuation.totalMarketValue()));
        assertTrue(valuation.holdings().isEmpty());
    }

    @Test
    void valuatePortfolio_withHoldings_calculatesCorrectly() {
        InvestmentHolding holding = createHolding("AAPL", "Apple Inc",
                new BigDecimal("100"), new BigDecimal("10000"),
                new BigDecimal("150"), new BigDecimal("15000"),
                new BigDecimal("5000"));

        when(holdingRepository.findByTenantId("tenant-1"))
                .thenReturn(List.of(holding));

        MarketValuation valuation = markToMarketService.valuatePortfolio("tenant-1");

        assertEquals(1, valuation.totalHoldings());
        assertEquals(0, new BigDecimal("10000").compareTo(valuation.totalCostBasis()));
        assertEquals(0, new BigDecimal("15000").compareTo(valuation.totalMarketValue()));
        assertEquals(0, new BigDecimal("5000").compareTo(valuation.totalUnrealizedGainLoss()));
        assertTrue(valuation.gainLossPercent().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void updateMarketPrice_validSymbol_updatesValues() {
        InvestmentHolding holding = createHolding("AAPL", "Apple Inc",
                new BigDecimal("100"), new BigDecimal("10000"),
                new BigDecimal("100"), new BigDecimal("10000"),
                BigDecimal.ZERO);

        when(holdingRepository.findByTenantIdAndSymbol("tenant-1", "AAPL"))
                .thenReturn(Optional.of(holding));
        when(holdingRepository.save(any(InvestmentHolding.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InvestmentHolding updated = markToMarketService.updateMarketPrice(
                "tenant-1", "AAPL", new BigDecimal("150"));

        assertEquals(0, new BigDecimal("15000").compareTo(updated.getMarketValue()));
        assertEquals(0, new BigDecimal("5000").compareTo(updated.getUnrealizedGainLoss()));
        assertEquals(0, new BigDecimal("150").compareTo(updated.getCurrentMarketPrice()));
    }

    @Test
    void updateMarketPrice_unknownSymbol_throws() {
        when(holdingRepository.findByTenantIdAndSymbol("tenant-1", "UNKNOWN"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                markToMarketService.updateMarketPrice("tenant-1", "UNKNOWN", new BigDecimal("100")));
    }

    private InvestmentHolding createHolding(String symbol, String name,
                                            BigDecimal quantity, BigDecimal costBasis,
                                            BigDecimal marketPrice, BigDecimal marketValue,
                                            BigDecimal unrealizedGainLoss) {
        InvestmentHolding holding = new InvestmentHolding(
                "tenant-1", symbol, name, HoldingType.EQUITY_SHARE, quantity, costBasis);
        holding.setCurrentMarketPrice(marketPrice);
        holding.setMarketValue(marketValue);
        holding.setUnrealizedGainLoss(unrealizedGainLoss);
        return holding;
    }
}
