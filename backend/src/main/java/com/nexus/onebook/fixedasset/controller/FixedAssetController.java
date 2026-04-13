package com.nexus.onebook.fixedasset.controller;

import com.nexus.onebook.fixedasset.dto.DepreciationSchedule;
import com.nexus.onebook.fixedasset.dto.FixedAssetRequest;
import com.nexus.onebook.fixedasset.model.FixedAsset;
import com.nexus.onebook.fixedasset.service.FixedAssetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for Fixed Asset Register (FAR) operations.
 * Supports asset creation, depreciation computation, and disposal.
 */
@RestController
@RequestMapping("/api/fixed-assets")
public class FixedAssetController {

    private final FixedAssetService fixedAssetService;

    public FixedAssetController(FixedAssetService fixedAssetService) {
        this.fixedAssetService = fixedAssetService;
    }

    @PostMapping
    public ResponseEntity<FixedAsset> createAsset(@Valid @RequestBody FixedAssetRequest request) {
        FixedAsset asset = fixedAssetService.createAsset(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(asset);
    }

    @GetMapping
    public ResponseEntity<List<FixedAsset>> getAssets(@RequestParam String tenantId) {
        return ResponseEntity.ok(fixedAssetService.getAssetsByTenant(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FixedAsset> getAsset(@PathVariable Long id) {
        return ResponseEntity.ok(fixedAssetService.getAsset(id));
    }

    @GetMapping("/{id}/depreciation")
    public ResponseEntity<DepreciationSchedule> getDepreciation(@PathVariable Long id) {
        return ResponseEntity.ok(fixedAssetService.computeDepreciation(id));
    }

    @PostMapping("/{id}/depreciate")
    public ResponseEntity<FixedAsset> runDepreciation(@PathVariable Long id) {
        return ResponseEntity.ok(fixedAssetService.runMonthlyDepreciation(id));
    }

    @PostMapping("/{id}/dispose")
    public ResponseEntity<FixedAsset> disposeAsset(
            @PathVariable Long id,
            @RequestParam LocalDate disposalDate,
            @RequestParam BigDecimal disposalAmount) {
        return ResponseEntity.ok(fixedAssetService.disposeAsset(id, disposalDate, disposalAmount));
    }
}
