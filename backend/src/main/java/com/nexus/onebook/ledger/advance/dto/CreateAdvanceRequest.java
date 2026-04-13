package com.nexus.onebook.ledger.advance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating an employee advance.
 */
public record CreateAdvanceRequest(
    String tenantId,
    Long employeeId,
    Long departmentId,
    BigDecimal amount,
    String purpose,
    LocalDate voucherDate,
    String createdBy,
    boolean override,
    String overrideReason
) {}
