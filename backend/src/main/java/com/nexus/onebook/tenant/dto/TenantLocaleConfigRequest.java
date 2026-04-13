package com.nexus.onebook.tenant.dto;

import jakarta.validation.constraints.*;

public record TenantLocaleConfigRequest(
    @NotBlank String tenantId,
    @NotBlank @Size(min = 2, max = 3) String countryCode,
    @NotBlank @Size(min = 3, max = 3) String currencyCode,
    @NotBlank String locale,
    String taxRegime,
    @Min(1) @Max(12) int fiscalYearStartMonth
) {}
