package com.nexus.onebook.tenant.service;

import com.nexus.onebook.tenant.dto.TenantLocaleConfigRequest;
import com.nexus.onebook.tenant.model.TenantLocaleConfig;
import com.nexus.onebook.tenant.repository.TenantLocaleConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantLocaleService {

    private final TenantLocaleConfigRepository localeConfigRepository;

    public TenantLocaleService(TenantLocaleConfigRepository localeConfigRepository) {
        this.localeConfigRepository = localeConfigRepository;
    }

    @Transactional
    public TenantLocaleConfig configureTenant(TenantLocaleConfigRequest request) {
        // Upsert: update if exists, create if not
        TenantLocaleConfig config = localeConfigRepository.findByTenantId(request.tenantId())
                .orElse(new TenantLocaleConfig(
                        request.tenantId(), request.countryCode(),
                        request.currencyCode(), request.locale()));

        config.setCountryCode(request.countryCode());
        config.setCurrencyCode(request.currencyCode());
        config.setLocale(request.locale());
        config.setTaxRegime(request.taxRegime());
        config.setFiscalYearStartMonth(request.fiscalYearStartMonth());

        return localeConfigRepository.save(config);
    }

    @Transactional(readOnly = true)
    public TenantLocaleConfig getTenantConfig(String tenantId) {
        return localeConfigRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tenant locale configuration not found for: " + tenantId));
    }
}
