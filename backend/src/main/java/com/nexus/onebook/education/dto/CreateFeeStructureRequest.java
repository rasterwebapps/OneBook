package com.nexus.onebook.education.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating or updating a fee structure.
 */
public record CreateFeeStructureRequest(
        @NotNull UUID courseId,
        @NotBlank String academicYear,
        @NotNull @Valid List<FeeStructureItemRequest> items
) {}
