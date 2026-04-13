package com.nexus.onebook.advance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating an advance receipt.
 */
public record CreateAdvanceReceiptRequest(
    String tenantId,
    Long employeeId,
    Long departmentId,
    BigDecimal amount,
    String paymentMode,
    LocalDate receiptDate,
    String createdBy,
    boolean override,
    String overrideReason
) {}
