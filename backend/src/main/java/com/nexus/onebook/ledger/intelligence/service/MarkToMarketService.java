package com.nexus.onebook.ledger.intelligence.service;

import com.nexus.onebook.ledger.intelligence.dto.HoldingValuation;
import com.nexus.onebook.ledger.intelligence.dto.InvestmentHoldingRequest;
import com.nexus.onebook.ledger.intelligence.dto.MarketValuation;
import com.nexus.onebook.ledger.intelligence.model.HoldingType;
import com.nexus.onebook.ledger.intelligence.model.InvestmentHolding;
import com.nexus.onebook.ledger.intelligence.repository.InvestmentHoldingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class MarkToMarketService {

    private final InvestmentHoldingRepository holdingRepository;

    public MarkToMarketService(InvestmentHoldingRepository holdingRepository) {
        this.holdingRepository = holdingRepository;
    }

    @Transactional(readOnly = true)
    public MarketValuation valuatePortfolio(String tenantId) {
        List<InvestmentHolding> holdings = holdingRepository.findByTenantId(tenantId);

        List<HoldingValuation> valuations = holdings.stream()
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
                holdings.size(),
                totalCostBasis,
                totalMarketValue,
                totalUnrealizedGainLoss,
                gainLossPercent,
                LocalDate.now(),
                valuations);
    }

    @Transactional
    public InvestmentHolding createHolding(InvestmentHoldingRequest request) {
        InvestmentHolding holding = new InvestmentHolding(
                request.tenantId(),
                request.symbol(),
                request.holdingName(),
                HoldingType.valueOf(request.holdingType()),
                request.quantity(),
                request.costBasis());

        holding.setMarketValue(BigDecimal.ZERO);
        holding.setUnrealizedGainLoss(BigDecimal.ZERO);

        return holdingRepository.save(holding);
    }

    @Transactional(readOnly = true)
    public List<InvestmentHolding> getHoldingsByTenant(String tenantId) {
        return holdingRepository.findByTenantId(tenantId);
    }

    @Transactional
    public InvestmentHolding updateMarketPrice(String tenantId, String symbol, BigDecimal newPrice) {
        InvestmentHolding holding = holdingRepository.findByTenantIdAndSymbol(tenantId, symbol)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Holding not found for tenant " + tenantId + " and symbol " + symbol));

        holding.setCurrentMarketPrice(newPrice);
        holding.setMarketValue(holding.getQuantity().multiply(newPrice));
        holding.setUnrealizedGainLoss(holding.getMarketValue().subtract(holding.getCostBasis()));
        holding.setLastValuationDate(LocalDate.now());

        return holdingRepository.save(holding);
    }

    private HoldingValuation toHoldingValuation(InvestmentHolding h) {
        BigDecimal costBasis = h.getCostBasis() != null ? h.getCostBasis() : BigDecimal.ZERO;
        BigDecimal marketValue = h.getMarketValue() != null ? h.getMarketValue() : BigDecimal.ZERO;
        BigDecimal unrealizedGainLoss = h.getUnrealizedGainLoss() != null
                ? h.getUnrealizedGainLoss() : BigDecimal.ZERO;

        BigDecimal gainLossPercent = costBasis.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : unrealizedGainLoss.divide(costBasis, 10, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);

        return new HoldingValuation(
                h.getSymbol(),
                h.getHoldingName(),
                h.getHoldingType().name(),
                h.getQuantity(),
                costBasis,
                h.getCurrentMarketPrice(),
                marketValue,
                unrealizedGainLoss,
                gainLossPercent);
    }
}
