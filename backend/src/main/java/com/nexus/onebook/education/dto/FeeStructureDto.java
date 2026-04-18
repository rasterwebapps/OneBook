package com.nexus.onebook.education.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for a fee structure with computed totals.
 */
public record FeeStructureDto(
        UUID id,
        UUID courseId,
        String academicYear,
        boolean isActive,
        List<FeeStructureItemDto> items,
        BigDecimal genericTotal,
        BigDecimal hostelFee,
        BigDecimal transportationFee
) {}
