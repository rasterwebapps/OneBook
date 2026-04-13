package com.nexus.onebook.entitlement.dto;

import jakarta.validation.constraints.*;

public record FeatureEntitlementRequest(
    @NotBlank String tenantId,
    @NotBlank String featureCode,
    boolean enabled
) {}
