package com.nexus.onebook.education.dto;

import com.nexus.onebook.education.model.StudentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO for creating a new student fee enquiry.
 */
public record CreateEnquiryRequest(
        @NotBlank String studentName,
        @NotBlank String email,
        @NotBlank String phone,
        @NotNull UUID programId,
        @NotNull UUID courseId,
        @NotNull StudentType studentType,
        @NotBlank String academicYear
) {}
