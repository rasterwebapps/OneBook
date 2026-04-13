package com.nexus.onebook.intelligence.service;

import com.nexus.onebook.intelligence.dto.DigitalAssetRequest;
import com.nexus.onebook.intelligence.dto.HoldingValuation;
import com.nexus.onebook.intelligence.dto.MarketValuation;
import com.nexus.onebook.intelligence.model.DigitalAsset;
import com.nexus.onebook.intelligence.model.DigitalAssetType;
import com.nexus.onebook.intelligence.repository.DigitalAssetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class DigitalAssetService {

    private final DigitalAssetRepository digitalAssetRepository;

    public DigitalAssetService(DigitalAssetRepository digitalAssetRepository) {
        this.digitalAssetRepository = digitalAssetRepository;
    }

    @Transactional
    public DigitalAsset createAsset(DigitalAssetRequest request) {
        DigitalAsset asset = new DigitalAsset(
                request.tenantId(),
                request.symbol(),
                request.assetName(),
                DigitalAssetType.valueOf(request.assetType()),
                request.quantity(),
                request.costBasis());

        asset.setWalletAddress(request.walletAddress());
        asset.setMarketValue(BigDecimal.ZERO);
        asset.setUnrealizedGainLoss(BigDecimal.ZERO);

        return digitalAssetRepository.save(asset);
    }

    @Transactional(readOnly = true)
    public List<DigitalAsset> getAssetsByTenant(String tenantId) {
        return digitalAssetRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public DigitalAsset getAsset(Long id) {
        return digitalAssetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Digital asset not found: " + id));
    }

    @Transactional
    public DigitalAsset updateMarketPrice(String tenantId, String symbol, BigDecimal newPrice) {
        DigitalAsset asset = digitalAssetRepository.findByTenantIdAndSymbol(tenantId, symbol)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Digital asset not found for tenant " + tenantId + " and symbol " + symbol));

        asset.setCurrentPrice(newPrice);
        asset.setMarketValue(asset.getQuantity().multiply(newPrice));
        asset.setUnrealizedGainLoss(asset.getMarketValue().subtract(asset.getCostBasis()));
        asset.setLastValuationDate(LocalDate.now());

        return digitalAssetRepository.save(asset);
    }

    @Transactional(readOnly = true)
    public MarketValuation getPortfolioValuation(String tenantId) {
        List<DigitalAsset> assets = digitalAssetRepository.findByTenantId(tenantId);

        List<HoldingValuation> valuations = assets.stream()
                .map(this::toHoldingValuation)
                .toList();

        BigDecimal totalCostBasis = valuations.stream()
                .map(HoldingValuation::costBasis)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalMarketValue = valuations.stream()
                .map(HoldingValuation::marketValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUnrealizedGainLoss = valuations.stream()
                .map(HoldingValuation::unrealizedGainLoss)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal gainLossPercent = totalCostBasis.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : totalUnrealizedGainLoss.divide(totalCostBasis, 10, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);

        return new MarketValuation(
                tenantId,
                assets.size(),
                totalCostBasis,
                totalMarketValue,
                totalUnrealizedGainLoss,
                gainLossPercent,
                LocalDate.now(),
                valuations);
    }

    private HoldingValuation toHoldingValuation(DigitalAsset a) {
        BigDecimal costBasis = a.getCostBasis() != null ? a.getCostBasis() : BigDecimal.ZERO;
        BigDecimal marketValue = a.getMarketValue() != null ? a.getMarketValue() : BigDecimal.ZERO;
        BigDecimal unrealizedGainLoss = a.getUnrealizedGainLoss() != null
                ? a.getUnrealizedGainLoss() : BigDecimal.ZERO;

        BigDecimal gainLossPercent = costBasis.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : unrealizedGainLoss.divide(costBasis, 10, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);

        return new HoldingValuation(
                a.getSymbol(),
                a.getAssetName(),
                a.getAssetType().name(),
                a.getQuantity(),
                costBasis,
                a.getCurrentPrice(),
                marketValue,
                unrealizedGainLoss,
                gainLossPercent);
    }
}
