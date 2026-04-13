package com.nexus.onebook.ledger.advance.dto;

import com.nexus.onebook.ledger.advance.model.AdvanceReceipt;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Response DTO for advance receipt details.
 */
public record AdvanceReceiptResponse(
    Long id,
    String tenantId,
    Long employeeId,
    Long departmentId,
    BigDecimal amount,
    String paymentMode,
    LocalDate receiptDate,
    boolean overrideFlag,
    String overrideReason,
    Long journalEntryId,
    String status,
    String createdBy,
    Instant createdAt,
    Instant updatedAt
) {
    public static AdvanceReceiptResponse from(AdvanceReceipt r) {
        return new AdvanceReceiptResponse(
            r.getId(),
            r.getTenantId(),
            r.getEmployeeId(),
            r.getDepartmentId(),
            r.getAmount(),
            r.getPaymentMode().name(),
            r.getReceiptDate(),
            r.isOverrideFlag(),
            r.getOverrideReason(),
            r.getJournalEntryId(),
            r.getStatus(),
            r.getCreatedBy(),
            r.getCreatedAt(),
            r.getUpdatedAt()
        );
    }
}
