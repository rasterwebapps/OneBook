package com.nexus.onebook.intelligence.controller;

import com.nexus.onebook.intelligence.dto.DigitalAssetRequest;
import com.nexus.onebook.intelligence.dto.MarketValuation;
import com.nexus.onebook.intelligence.model.DigitalAsset;
import com.nexus.onebook.intelligence.service.DigitalAssetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/digital-assets")
public class DigitalAssetController {

    private final DigitalAssetService digitalAssetService;

    public DigitalAssetController(DigitalAssetService digitalAssetService) {
        this.digitalAssetService = digitalAssetService;
    }

    @PostMapping
    public ResponseEntity<DigitalAsset> createAsset(@Valid @RequestBody DigitalAssetRequest request) {
        DigitalAsset asset = digitalAssetService.createAsset(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(asset);
    }

    @GetMapping
    public ResponseEntity<List<DigitalAsset>> getAssets(@RequestParam String tenantId) {
        return ResponseEntity.ok(digitalAssetService.getAssetsByTenant(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DigitalAsset> getAsset(@PathVariable Long id) {
        return ResponseEntity.ok(digitalAssetService.getAsset(id));
    }

    @PutMapping("/{symbol}/price")
    public ResponseEntity<DigitalAsset> updatePrice(
            @PathVariable String symbol,
            @RequestParam String tenantId,
            @RequestParam BigDecimal price) {
        return ResponseEntity.ok(digitalAssetService.updateMarketPrice(tenantId, symbol, price));
    }

    @GetMapping("/valuation")
    public ResponseEntity<MarketValuation> getValuation(@RequestParam String tenantId) {
        return ResponseEntity.ok(digitalAssetService.getPortfolioValuation(tenantId));
    }
}
