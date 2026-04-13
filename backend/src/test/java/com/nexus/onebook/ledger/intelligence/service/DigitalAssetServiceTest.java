package com.nexus.onebook.ledger.intelligence.service;

import com.nexus.onebook.ledger.intelligence.dto.DigitalAssetRequest;
import com.nexus.onebook.ledger.intelligence.dto.MarketValuation;
import com.nexus.onebook.ledger.intelligence.model.DigitalAsset;
import com.nexus.onebook.ledger.intelligence.model.DigitalAssetType;
import com.nexus.onebook.ledger.intelligence.repository.DigitalAssetRepository;
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
class DigitalAssetServiceTest {

    @Mock
    private DigitalAssetRepository digitalAssetRepository;

    @InjectMocks
    private DigitalAssetService digitalAssetService;

    @Test
    void createAsset_validRequest_succeeds() {
        DigitalAssetRequest request = new DigitalAssetRequest(
                "tenant-1", "BTC", "Bitcoin", "CRYPTOCURRENCY",
                new BigDecimal("2.00000000"), new BigDecimal("60000.0000"),
                "0xABC123", null);

        when(digitalAssetRepository.save(any(DigitalAsset.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DigitalAsset asset = digitalAssetService.createAsset(request);

        assertEquals("tenant-1", asset.getTenantId());
        assertEquals("BTC", asset.getSymbol());
        assertEquals("Bitcoin", asset.getAssetName());
        assertEquals(DigitalAssetType.CRYPTOCURRENCY, asset.getAssetType());
        assertEquals(0, new BigDecimal("2.00000000").compareTo(asset.getQuantity()));
        assertEquals("0xABC123", asset.getWalletAddress());
    }

    @Test
    void getAsset_found_returnsAsset() {
        DigitalAsset asset = createAsset("BTC", "Bitcoin",
                new BigDecimal("2.00000000"), new BigDecimal("60000.0000"));
        asset.setId(1L);

        when(digitalAssetRepository.findById(1L)).thenReturn(Optional.of(asset));

        DigitalAsset result = digitalAssetService.getAsset(1L);

        assertEquals("BTC", result.getSymbol());
        assertEquals(1L, result.getId());
    }

    @Test
    void getAsset_notFound_throws() {
        when(digitalAssetRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                digitalAssetService.getAsset(999L));
    }

    @Test
    void updateMarketPrice_validSymbol_updatesValues() {
        DigitalAsset asset = createAsset("BTC", "Bitcoin",
                new BigDecimal("2.00000000"), new BigDecimal("60000.0000"));

        when(digitalAssetRepository.findByTenantIdAndSymbol("tenant-1", "BTC"))
                .thenReturn(Optional.of(asset));
        when(digitalAssetRepository.save(any(DigitalAsset.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DigitalAsset updated = digitalAssetService.updateMarketPrice(
                "tenant-1", "BTC", new BigDecimal("35000.0000"));

        assertEquals(0, new BigDecimal("35000.0000").compareTo(updated.getCurrentPrice()));
        assertEquals(0, new BigDecimal("70000.00000000").compareTo(updated.getMarketValue()));
        assertEquals(0, new BigDecimal("10000.00000000").compareTo(updated.getUnrealizedGainLoss()));
    }

    @Test
    void getPortfolioValuation_calculatesCorrectly() {
        DigitalAsset asset = createAsset("ETH", "Ethereum",
                new BigDecimal("10.00000000"), new BigDecimal("20000.0000"));
        asset.setCurrentPrice(new BigDecimal("2500.0000"));
        asset.setMarketValue(new BigDecimal("25000.00000000"));
        asset.setUnrealizedGainLoss(new BigDecimal("5000.00000000"));

        when(digitalAssetRepository.findByTenantId("tenant-1"))
                .thenReturn(List.of(asset));

        MarketValuation valuation = digitalAssetService.getPortfolioValuation("tenant-1");

        assertEquals("tenant-1", valuation.tenantId());
        assertEquals(1, valuation.totalHoldings());
        assertEquals(0, new BigDecimal("20000.0000").compareTo(valuation.totalCostBasis()));
        assertEquals(0, new BigDecimal("25000.00000000").compareTo(valuation.totalMarketValue()));
        assertEquals(0, new BigDecimal("5000.00000000").compareTo(valuation.totalUnrealizedGainLoss()));
        assertTrue(valuation.gainLossPercent().compareTo(BigDecimal.ZERO) > 0);
    }

    private DigitalAsset createAsset(String symbol, String name,
                                     BigDecimal quantity, BigDecimal costBasis) {
        return new DigitalAsset(
                "tenant-1", symbol, name, DigitalAssetType.CRYPTOCURRENCY, quantity, costBasis);
    }
}
