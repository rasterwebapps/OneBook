package com.nexus.onebook.education.dto;

import java.util.UUID;

/**
 * Response DTO for an education program.
 */
public record ProgramDto(
        UUID id,
        String name,
        String code,
        boolean isActive
) {}
