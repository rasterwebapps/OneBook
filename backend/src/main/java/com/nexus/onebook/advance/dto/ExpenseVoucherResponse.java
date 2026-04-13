package com.nexus.onebook.advance.dto;

import com.nexus.onebook.advance.model.ExpenseVoucher;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Response DTO for expense voucher details.
 */
public record ExpenseVoucherResponse(
    Long id,
    String tenantId,
    Long employeeId,
    Long departmentId,
    BigDecimal amount,
    String expenseType,
    String description,
    LocalDate voucherDate,
    String status,
    String supportingDocRef,
    BigDecimal advanceSettlement,
    BigDecimal reimbursementAmount,
    Long paymentAdviceId,
    Long journalEntryId,
    String approvedBy,
    Instant approvedAt,
    String rejectedBy,
    Instant rejectedAt,
    String rejectionReason,
    String createdBy,
    Instant createdAt,
    Instant updatedAt
) {
    public static ExpenseVoucherResponse from(ExpenseVoucher e) {
        return new ExpenseVoucherResponse(
            e.getId(),
            e.getTenantId(),
            e.getEmployeeId(),
            e.getDepartmentId(),
            e.getAmount(),
            e.getExpenseType(),
            e.getDescription(),
            e.getVoucherDate(),
            e.getStatus().name(),
            e.getSupportingDocRef(),
            e.getAdvanceSettlement(),
            e.getReimbursementAmount(),
            e.getPaymentAdviceId(),
            e.getJournalEntryId(),
            e.getApprovedBy(),
            e.getApprovedAt(),
            e.getRejectedBy(),
            e.getRejectedAt(),
            e.getRejectionReason(),
            e.getCreatedBy(),
            e.getCreatedAt(),
            e.getUpdatedAt()
        );
    }
}
