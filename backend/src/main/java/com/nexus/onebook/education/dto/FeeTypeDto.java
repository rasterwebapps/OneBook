package com.nexus.onebook.education.dto;

import com.nexus.onebook.education.model.AdditionalFeeType;
import com.nexus.onebook.education.model.FeeCategory;

import java.util.UUID;

/**
 * Response DTO for a fee type.
 */
public record FeeTypeDto(
        UUID id,
        String name,
        FeeCategory category,
        AdditionalFeeType additionalType,
        boolean isActive
) {}
