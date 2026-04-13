package com.nexus.onebook.auditor.dto;

import java.time.Instant;
import java.util.List;

/**
 * Report DTO for security audit results.
 */
public record SecurityAuditReport(
        String tenantId,
        Instant auditTimestamp,
        boolean encryptionVerified,
        boolean keyManagementVerified,
        boolean auditChainIntact,
        int totalChecks,
        int passedChecks,
        int failedChecks,
        List<String> findings
) {}
