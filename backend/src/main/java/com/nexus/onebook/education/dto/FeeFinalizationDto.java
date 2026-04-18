package com.nexus.onebook.education.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for a fee finalization.
 */
public record FeeFinalizationDto(
        UUID id,
        UUID enquiryId,
        BigDecimal genericTotal,
        BigDecimal additionalFee,
        BigDecimal discountAmount,
        BigDecimal finalPayable,
        String finalizedBy,
        Instant finalizedAt
) {}
