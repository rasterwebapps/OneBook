package com.nexus.onebook.ledger.entitlement.controller;

import com.nexus.onebook.ledger.entitlement.dto.FeatureEntitlementRequest;
import com.nexus.onebook.ledger.entitlement.model.FeatureEntitlement;
import com.nexus.onebook.ledger.entitlement.service.FeatureEntitlementService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for feature entitlement management.
 * Enables/disables locale-specific modules per tenant.
 */
@RestController
@RequestMapping("/api/entitlements")
public class FeatureEntitlementController {

    private final FeatureEntitlementService entitlementService;

    public FeatureEntitlementController(FeatureEntitlementService entitlementService) {
        this.entitlementService = entitlementService;
    }

    @PostMapping
    public ResponseEntity<FeatureEntitlement> setEntitlement(
            @Valid @RequestBody FeatureEntitlementRequest request) {
        return ResponseEntity.ok(entitlementService.setEntitlement(request));
    }

    @GetMapping
    public ResponseEntity<List<FeatureEntitlement>> getEntitlements(@RequestParam String tenantId) {
        return ResponseEntity.ok(entitlementService.getEntitlements(tenantId));
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> isFeatureEnabled(
            @RequestParam String tenantId, @RequestParam String featureCode) {
        return ResponseEntity.ok(entitlementService.isFeatureEnabled(tenantId, featureCode));
    }

    @GetMapping("/enabled")
    public ResponseEntity<List<FeatureEntitlement>> getEnabledFeatures(
            @RequestParam String tenantId) {
        return ResponseEntity.ok(entitlementService.getEnabledFeatures(tenantId));
    }
}
