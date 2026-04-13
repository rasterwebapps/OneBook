package com.nexus.onebook.ledger.tenant.service;

import com.nexus.onebook.ledger.tenant.dto.TenantLocaleConfigRequest;
import com.nexus.onebook.ledger.tenant.model.TenantLocaleConfig;
import com.nexus.onebook.ledger.tenant.repository.TenantLocaleConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantLocaleServiceTest {

    @Mock
    private TenantLocaleConfigRepository localeConfigRepository;

    @InjectMocks
    private TenantLocaleService tenantLocaleService;

    @Test
    void configureTenant_newTenant_createsConfig() {
        TenantLocaleConfigRequest request = new TenantLocaleConfigRequest(
                "tenant-1", "IN", "INR", "en-IN", "GST", 4);

        when(localeConfigRepository.findByTenantId("tenant-1")).thenReturn(Optional.empty());
        when(localeConfigRepository.save(any(TenantLocaleConfig.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TenantLocaleConfig result = tenantLocaleService.configureTenant(request);

        assertNotNull(result);
        assertEquals("IN", result.getCountryCode());
        assertEquals("INR", result.getCurrencyCode());
        assertEquals("GST", result.getTaxRegime());
    }

    @Test
    void configureTenant_existingTenant_updatesConfig() {
        TenantLocaleConfig existing = new TenantLocaleConfig("tenant-1", "US", "USD", "en-US");
        TenantLocaleConfigRequest request = new TenantLocaleConfigRequest(
                "tenant-1", "IN", "INR", "en-IN", "GST", 4);

        when(localeConfigRepository.findByTenantId("tenant-1")).thenReturn(Optional.of(existing));
        when(localeConfigRepository.save(any(TenantLocaleConfig.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TenantLocaleConfig result = tenantLocaleService.configureTenant(request);

        assertEquals("IN", result.getCountryCode());
        assertEquals("INR", result.getCurrencyCode());
    }

    @Test
    void getTenantConfig_notFound_throws() {
        when(localeConfigRepository.findByTenantId("tenant-1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                tenantLocaleService.getTenantConfig("tenant-1"));
    }
}
