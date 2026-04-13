package com.nexus.onebook.ledger.entitlement.service;

import com.nexus.onebook.ledger.entitlement.dto.FeatureEntitlementRequest;
import com.nexus.onebook.ledger.entitlement.model.FeatureEntitlement;
import com.nexus.onebook.ledger.entitlement.repository.FeatureEntitlementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeatureEntitlementServiceTest {

    @Mock
    private FeatureEntitlementRepository entitlementRepository;

    @InjectMocks
    private FeatureEntitlementService entitlementService;

    @Test
    void setEntitlement_newFeature_createsEntitlement() {
        FeatureEntitlementRequest request = new FeatureEntitlementRequest(
                "tenant-1", "E_INVOICING", true);

        when(entitlementRepository.findByTenantIdAndFeatureCode("tenant-1", "E_INVOICING"))
                .thenReturn(Optional.empty());
        when(entitlementRepository.save(any(FeatureEntitlement.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        FeatureEntitlement result = entitlementService.setEntitlement(request);

        assertNotNull(result);
        assertTrue(result.isEnabled());
        assertEquals("E_INVOICING", result.getFeatureCode());
    }

    @Test
    void setEntitlement_existingFeature_updatesEnabled() {
        FeatureEntitlement existing = new FeatureEntitlement("tenant-1", "E_INVOICING", false);
        FeatureEntitlementRequest request = new FeatureEntitlementRequest(
                "tenant-1", "E_INVOICING", true);

        when(entitlementRepository.findByTenantIdAndFeatureCode("tenant-1", "E_INVOICING"))
                .thenReturn(Optional.of(existing));
        when(entitlementRepository.save(any(FeatureEntitlement.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        FeatureEntitlement result = entitlementService.setEntitlement(request);

        assertTrue(result.isEnabled());
    }

    @Test
    void isFeatureEnabled_enabled_returnsTrue() {
        FeatureEntitlement entitlement = new FeatureEntitlement("tenant-1", "E_INVOICING", true);
        when(entitlementRepository.findByTenantIdAndFeatureCode("tenant-1", "E_INVOICING"))
                .thenReturn(Optional.of(entitlement));

        assertTrue(entitlementService.isFeatureEnabled("tenant-1", "E_INVOICING"));
    }

    @Test
    void isFeatureEnabled_notFound_returnsFalse() {
        when(entitlementRepository.findByTenantIdAndFeatureCode("tenant-1", "UNKNOWN"))
                .thenReturn(Optional.empty());

        assertFalse(entitlementService.isFeatureEnabled("tenant-1", "UNKNOWN"));
    }

    @Test
    void getEnabledFeatures_returnsList() {
        FeatureEntitlement e1 = new FeatureEntitlement("tenant-1", "E_INVOICING", true);
        FeatureEntitlement e2 = new FeatureEntitlement("tenant-1", "EWAY_BILL", true);

        when(entitlementRepository.findByTenantIdAndEnabled("tenant-1", true))
                .thenReturn(List.of(e1, e2));

        List<FeatureEntitlement> result = entitlementService.getEnabledFeatures("tenant-1");

        assertEquals(2, result.size());
    }
}
