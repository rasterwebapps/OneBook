package com.nexus.onebook.ledger.entitlement.service;

import com.nexus.onebook.ledger.entitlement.dto.FeatureEntitlementRequest;
import com.nexus.onebook.ledger.entitlement.model.FeatureEntitlement;
import com.nexus.onebook.ledger.entitlement.repository.FeatureEntitlementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FeatureEntitlementService {

    private final FeatureEntitlementRepository entitlementRepository;

    public FeatureEntitlementService(FeatureEntitlementRepository entitlementRepository) {
        this.entitlementRepository = entitlementRepository;
    }

    @Transactional
    public FeatureEntitlement setEntitlement(FeatureEntitlementRequest request) {
        // Upsert: update if exists, create if not
        FeatureEntitlement entitlement = entitlementRepository
                .findByTenantIdAndFeatureCode(request.tenantId(), request.featureCode())
                .orElse(new FeatureEntitlement(request.tenantId(), request.featureCode(), request.enabled()));

        entitlement.setEnabled(request.enabled());
        return entitlementRepository.save(entitlement);
    }

    @Transactional(readOnly = true)
    public List<FeatureEntitlement> getEntitlements(String tenantId) {
        return entitlementRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public boolean isFeatureEnabled(String tenantId, String featureCode) {
        return entitlementRepository.findByTenantIdAndFeatureCode(tenantId, featureCode)
                .map(FeatureEntitlement::isEnabled)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<FeatureEntitlement> getEnabledFeatures(String tenantId) {
        return entitlementRepository.findByTenantIdAndEnabled(tenantId, true);
    }
}
