package com.nexus.onebook.education.dto;

import java.util.UUID;

/**
 * Response DTO for an education course.
 */
public record CourseDto(
        UUID id,
        UUID programId,
        String name,
        String code,
        boolean isActive
) {}
