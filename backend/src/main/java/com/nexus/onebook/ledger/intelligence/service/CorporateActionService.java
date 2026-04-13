package com.nexus.onebook.ledger.intelligence.service;

import com.nexus.onebook.ledger.intelligence.dto.CorporateActionRequest;
import com.nexus.onebook.ledger.intelligence.model.CorporateAction;
import com.nexus.onebook.ledger.intelligence.model.CorporateActionType;
import com.nexus.onebook.ledger.intelligence.model.InvestmentHolding;
import com.nexus.onebook.ledger.intelligence.repository.CorporateActionRepository;
import com.nexus.onebook.ledger.intelligence.repository.InvestmentHoldingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class CorporateActionService {

    private final CorporateActionRepository actionRepository;
    private final InvestmentHoldingRepository holdingRepository;

    public CorporateActionService(CorporateActionRepository actionRepository,
                                  InvestmentHoldingRepository holdingRepository) {
        this.actionRepository = actionRepository;
        this.holdingRepository = holdingRepository;
    }

    @Transactional
    public CorporateAction createAction(CorporateActionRequest request) {
        InvestmentHolding holding = holdingRepository.findById(request.holdingId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Holding not found: " + request.holdingId()));

        CorporateAction action = new CorporateAction(
                request.tenantId(),
                holding,
                CorporateActionType.valueOf(request.actionType()),
                request.recordDate());

        if (request.ratio() != null) {
            action.setRatio(request.ratio());
        }
        if (request.amountPerUnit() != null) {
            action.setAmountPerUnit(request.amountPerUnit());
        }

        return actionRepository.save(action);
    }

    @Transactional
    public CorporateAction processAction(Long actionId) {
        CorporateAction action = actionRepository.findById(actionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Corporate action not found: " + actionId));

        if (action.isProcessed()) {
            throw new IllegalStateException(
                    "Corporate action already processed: " + actionId);
        }

        InvestmentHolding holding = action.getHolding();

        switch (action.getActionType()) {
            case STOCK_SPLIT -> applySplit(holding, action.getRatio());
            case BONUS_ISSUE -> applyBonusIssue(holding, action.getRatio());
            case DIVIDEND -> { /* no quantity change; could generate journal entry in future */ }
            case RIGHTS_ISSUE -> throw new UnsupportedOperationException(
                    "RIGHTS_ISSUE processing not yet implemented");
            case BUYBACK -> throw new UnsupportedOperationException(
                    "BUYBACK processing not yet implemented");
        }

        action.setProcessed(true);
        action.setExecutionDate(LocalDate.now());

        holdingRepository.save(holding);
        return actionRepository.save(action);
    }

    @Transactional(readOnly = true)
    public List<CorporateAction> getPendingActions(String tenantId) {
        return actionRepository.findByTenantIdAndProcessed(tenantId, false);
    }

    @Transactional(readOnly = true)
    public List<CorporateAction> getActionsByHolding(Long holdingId) {
        return actionRepository.findByHoldingId(holdingId);
    }

    private void applySplit(InvestmentHolding holding, BigDecimal ratio) {
        BigDecimal originalQuantity = holding.getQuantity();
        BigDecimal newQuantity = originalQuantity.multiply(ratio);
        holding.setQuantity(newQuantity);

        // Total cost basis stays the same; per-unit cost adjusts
        holding.setCurrentMarketPrice(
                holding.getCurrentMarketPrice().divide(ratio, 4, RoundingMode.HALF_UP));

        recalculateMarketValues(holding);
    }

    private void applyBonusIssue(InvestmentHolding holding, BigDecimal ratio) {
        BigDecimal originalQuantity = holding.getQuantity();
        BigDecimal bonusShares = originalQuantity.multiply(ratio);
        holding.setQuantity(originalQuantity.add(bonusShares));

        // Total cost basis stays the same; per-unit cost adjusts
        holding.setCurrentMarketPrice(
                holding.getCurrentMarketPrice().divide(
                        BigDecimal.ONE.add(ratio), 4, RoundingMode.HALF_UP));

        recalculateMarketValues(holding);
    }

    private void recalculateMarketValues(InvestmentHolding holding) {
        BigDecimal marketValue = holding.getQuantity().multiply(holding.getCurrentMarketPrice());
        holding.setMarketValue(marketValue);
        holding.setUnrealizedGainLoss(marketValue.subtract(holding.getCostBasis()));
    }
}
