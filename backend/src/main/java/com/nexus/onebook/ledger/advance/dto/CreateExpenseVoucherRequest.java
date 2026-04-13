package com.nexus.onebook.ledger.advance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for creating an expense voucher.
 */
public record CreateExpenseVoucherRequest(
    String tenantId,
    Long employeeId,
    Long departmentId,
    BigDecimal amount,
    String expenseType,
    String description,
    LocalDate voucherDate,
    String supportingDocRef,
    List<Long> linkedAdvanceIds,
    String createdBy
) {}
