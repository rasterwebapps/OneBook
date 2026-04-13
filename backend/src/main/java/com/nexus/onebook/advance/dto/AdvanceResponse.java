package com.nexus.onebook.advance.dto;

import com.nexus.onebook.advance.model.AdvanceStatus;
import com.nexus.onebook.advance.model.ApproverRole;
import com.nexus.onebook.advance.model.EmployeeAdvance;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Response DTO for employee advance details.
 */
public record AdvanceResponse(
    Long id,
    String tenantId,
    Long employeeId,
    Long departmentId,
    BigDecimal amount,
    String purpose,
    LocalDate voucherDate,
    String status,
    String currentApproverRole,
    boolean overrideFlag,
    String overrideReason,
    BigDecimal approvedAmount,
    Long journalEntryId,
    String hodApprovedBy,
    Instant hodApprovedAt,
    String ceoApprovedBy,
    Instant ceoApprovedAt,
    String mdApprovedBy,
    Instant mdApprovedAt,
    String rejectedBy,
    Instant rejectedAt,
    String rejectionReason,
    String createdBy,
    Instant createdAt,
    Instant updatedAt
) {
    public static AdvanceResponse from(EmployeeAdvance a) {
        return new AdvanceResponse(
            a.getId(),
            a.getTenantId(),
            a.getEmployeeId(),
            a.getDepartmentId(),
            a.getAmount(),
            a.getPurpose(),
            a.getVoucherDate(),
            a.getStatus().name(),
            a.getCurrentApproverRole() != null ? a.getCurrentApproverRole().name() : null,
            a.isOverrideFlag(),
            a.getOverrideReason(),
            a.getApprovedAmount(),
            a.getJournalEntryId(),
            a.getHodApprovedBy(),
            a.getHodApprovedAt(),
            a.getCeoApprovedBy(),
            a.getCeoApprovedAt(),
            a.getMdApprovedBy(),
            a.getMdApprovedAt(),
            a.getRejectedBy(),
            a.getRejectedAt(),
            a.getRejectionReason(),
            a.getCreatedBy(),
            a.getCreatedAt(),
            a.getUpdatedAt()
        );
    }
}
