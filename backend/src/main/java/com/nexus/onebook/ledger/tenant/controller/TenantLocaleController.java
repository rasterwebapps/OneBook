package com.nexus.onebook.ledger.tenant.controller;

import com.nexus.onebook.ledger.tenant.dto.TenantLocaleConfigRequest;
import com.nexus.onebook.ledger.tenant.model.TenantLocaleConfig;
import com.nexus.onebook.ledger.tenant.service.TenantLocaleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for tenant locale and tax regime configuration.
 * Enables per-tenant country/currency/tax setup for multi-locale support.
 */
@RestController
@RequestMapping("/api/tenant-locale")
public class TenantLocaleController {

    private final TenantLocaleService tenantLocaleService;

    public TenantLocaleController(TenantLocaleService tenantLocaleService) {
        this.tenantLocaleService = tenantLocaleService;
    }

    @PostMapping
    public ResponseEntity<TenantLocaleConfig> configureTenant(
            @Valid @RequestBody TenantLocaleConfigRequest request) {
        return ResponseEntity.ok(tenantLocaleService.configureTenant(request));
    }

    @GetMapping
    public ResponseEntity<TenantLocaleConfig> getTenantConfig(@RequestParam String tenantId) {
        return ResponseEntity.ok(tenantLocaleService.getTenantConfig(tenantId));
    }
}
