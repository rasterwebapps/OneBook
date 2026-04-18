package com.nexus.onebook.education.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for finalizing a student fee enquiry with a discount.
 */
public record CreateFinalizationRequest(
        @NotNull UUID enquiryId,
        @NotNull @PositiveOrZero BigDecimal discountAmount,
        @NotBlank String finalizedBy
) {}
