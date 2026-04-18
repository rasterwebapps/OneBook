package com.nexus.onebook.education.dto;

import com.nexus.onebook.education.model.EnquiryStatus;
import com.nexus.onebook.education.model.StudentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Response DTO for a student fee enquiry.
 */
public record StudentEnquiryDto(
        UUID id,
        String studentName,
        String email,
        String phone,
        UUID programId,
        String programName,
        UUID courseId,
        String courseName,
        StudentType studentType,
        BigDecimal genericTotal,
        BigDecimal additionalFee,
        BigDecimal totalFees,
        String academicYear,
        EnquiryStatus status,
        LocalDate enquiryDate
) {}
