package com.nexus.onebook.intelligence.controller;

import com.nexus.onebook.intelligence.dto.CorporateActionRequest;
import com.nexus.onebook.intelligence.dto.InvestmentHoldingRequest;
import com.nexus.onebook.intelligence.dto.MarketSentiment;
import com.nexus.onebook.intelligence.dto.MarketValuation;
import com.nexus.onebook.intelligence.model.CorporateAction;
import com.nexus.onebook.intelligence.model.InvestmentHolding;
import com.nexus.onebook.intelligence.service.CorporateActionService;
import com.nexus.onebook.intelligence.service.MarkToMarketService;
import com.nexus.onebook.intelligence.service.MarketSentimentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    private final MarkToMarketService markToMarketService;
    private final CorporateActionService corporateActionService;
    private final MarketSentimentService marketSentimentService;

    public MarketController(MarkToMarketService markToMarketService,
                            CorporateActionService corporateActionService,
                            MarketSentimentService marketSentimentService) {
        this.markToMarketService = markToMarketService;
        this.corporateActionService = corporateActionService;
        this.marketSentimentService = marketSentimentService;
    }

    @GetMapping("/valuation")
    public ResponseEntity<MarketValuation> getValuation(@RequestParam String tenantId) {
        return ResponseEntity.ok(markToMarketService.valuatePortfolio(tenantId));
    }

    @PutMapping("/holdings/{symbol}/price")
    public ResponseEntity<InvestmentHolding> updatePrice(
            @PathVariable String symbol,
            @RequestParam String tenantId,
            @RequestParam BigDecimal price) {
        return ResponseEntity.ok(markToMarketService.updateMarketPrice(tenantId, symbol, price));
    }

    @PostMapping("/holdings")
    public ResponseEntity<InvestmentHolding> createHolding(
            @Valid @RequestBody InvestmentHoldingRequest request) {
        InvestmentHolding holding = markToMarketService.createHolding(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(holding);
    }

    @GetMapping("/holdings")
    public ResponseEntity<List<InvestmentHolding>> getHoldings(@RequestParam String tenantId) {
        return ResponseEntity.ok(markToMarketService.getHoldingsByTenant(tenantId));
    }

    @PostMapping("/corporate-actions")
    public ResponseEntity<CorporateAction> createAction(
            @Valid @RequestBody CorporateActionRequest request) {
        CorporateAction action = corporateActionService.createAction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(action);
    }

    @PostMapping("/corporate-actions/{id}/process")
    public ResponseEntity<CorporateAction> processAction(@PathVariable Long id) {
        return ResponseEntity.ok(corporateActionService.processAction(id));
    }

    @GetMapping("/corporate-actions/pending")
    public ResponseEntity<List<CorporateAction>> getPendingActions(@RequestParam String tenantId) {
        return ResponseEntity.ok(corporateActionService.getPendingActions(tenantId));
    }

    @GetMapping("/sentiment")
    public ResponseEntity<List<MarketSentiment>> getPortfolioSentiment(
            @RequestParam String tenantId) {
        return ResponseEntity.ok(marketSentimentService.getSentimentForPortfolio(tenantId));
    }

    @GetMapping("/sentiment/{symbol}")
    public ResponseEntity<MarketSentiment> getSymbolSentiment(@PathVariable String symbol) {
        return ResponseEntity.ok(marketSentimentService.getSentimentForSymbol(symbol));
    }
}
