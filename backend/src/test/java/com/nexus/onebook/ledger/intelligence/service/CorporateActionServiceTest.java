package com.nexus.onebook.ledger.intelligence.service;

import com.nexus.onebook.ledger.intelligence.dto.CorporateActionRequest;
import com.nexus.onebook.ledger.intelligence.model.CorporateAction;
import com.nexus.onebook.ledger.intelligence.model.CorporateActionType;
import com.nexus.onebook.ledger.intelligence.model.HoldingType;
import com.nexus.onebook.ledger.intelligence.model.InvestmentHolding;
import com.nexus.onebook.ledger.intelligence.repository.CorporateActionRepository;
import com.nexus.onebook.ledger.intelligence.repository.InvestmentHoldingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CorporateActionServiceTest {

    @Mock
    private CorporateActionRepository actionRepository;

    @Mock
    private InvestmentHoldingRepository holdingRepository;

    @InjectMocks
    private CorporateActionService corporateActionService;

    private InvestmentHolding holding;

    @BeforeEach
    void setUp() {
        holding = new InvestmentHolding(
                "tenant-1", "AAPL", "Apple Inc", HoldingType.EQUITY_SHARE,
                new BigDecimal("100"), new BigDecimal("10000"));
        holding.setId(1L);
        holding.setCurrentMarketPrice(new BigDecimal("200"));
        holding.setMarketValue(new BigDecimal("20000"));
        holding.setUnrealizedGainLoss(new BigDecimal("10000"));
    }

    @Test
    void createAction_validRequest_succeeds() {
        CorporateActionRequest request = new CorporateActionRequest(
                "tenant-1", 1L, "STOCK_SPLIT", LocalDate.now(),
                new BigDecimal("2"), null);

        when(holdingRepository.findById(1L)).thenReturn(Optional.of(holding));
        when(actionRepository.save(any(CorporateAction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CorporateAction action = corporateActionService.createAction(request);

        assertEquals("tenant-1", action.getTenantId());
        assertEquals(CorporateActionType.STOCK_SPLIT, action.getActionType());
        assertEquals(0, new BigDecimal("2").compareTo(action.getRatio()));
    }

    @Test
    void processAction_stockSplit_doublesQuantity() {
        CorporateAction action = new CorporateAction(
                "tenant-1", holding, CorporateActionType.STOCK_SPLIT, LocalDate.now());
        action.setId(1L);
        action.setRatio(new BigDecimal("2"));

        when(actionRepository.findById(1L)).thenReturn(Optional.of(action));
        when(holdingRepository.save(any(InvestmentHolding.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(actionRepository.save(any(CorporateAction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CorporateAction result = corporateActionService.processAction(1L);

        assertTrue(result.isProcessed());
        assertEquals(0, new BigDecimal("200").compareTo(holding.getQuantity()));
        assertEquals(0, new BigDecimal("100.0000").compareTo(holding.getCurrentMarketPrice()));
    }

    @Test
    void processAction_alreadyProcessed_throws() {
        CorporateAction action = new CorporateAction(
                "tenant-1", holding, CorporateActionType.STOCK_SPLIT, LocalDate.now());
        action.setId(1L);
        action.setProcessed(true);

        when(actionRepository.findById(1L)).thenReturn(Optional.of(action));

        assertThrows(IllegalStateException.class, () ->
                corporateActionService.processAction(1L));
    }

    @Test
    void getPendingActions_returnsPendingOnly() {
        CorporateAction pendingAction = new CorporateAction(
                "tenant-1", holding, CorporateActionType.DIVIDEND, LocalDate.now());

        when(actionRepository.findByTenantIdAndProcessed("tenant-1", false))
                .thenReturn(List.of(pendingAction));

        List<CorporateAction> pending = corporateActionService.getPendingActions("tenant-1");

        assertEquals(1, pending.size());
        assertFalse(pending.get(0).isProcessed());
    }
}
