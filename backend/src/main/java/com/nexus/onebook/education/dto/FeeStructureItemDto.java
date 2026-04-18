package com.nexus.onebook.education.dto;

import com.nexus.onebook.education.model.AdditionalFeeType;
import com.nexus.onebook.education.model.FeeCategory;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for a single fee structure line item.
 */
public record FeeStructureItemDto(
        UUID id,
        UUID feeTypeId,
        String feeTypeName,
        FeeCategory category,
        AdditionalFeeType additionalType,
        BigDecimal amount
) {}
