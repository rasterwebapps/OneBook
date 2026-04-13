package com.nexus.onebook.advance.dto;

import com.nexus.onebook.advance.model.EmployeePaymentAdvice;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO for payment advice details.
 */
public record PaymentAdviceResponse(
    Long id,
    String tenantId,
    Long employeeId,
    Long departmentId,
    BigDecimal amount,
    Long expenseVoucherId,
    String status,
    Long paymentVoucherId,
    String paidBy,
    Instant paidAt,
    Instant createdAt,
    Instant updatedAt
) {
    public static PaymentAdviceResponse from(EmployeePaymentAdvice p) {
        return new PaymentAdviceResponse(
            p.getId(),
            p.getTenantId(),
            p.getEmployeeId(),
            p.getDepartmentId(),
            p.getAmount(),
            p.getExpenseVoucherId(),
            p.getStatus().name(),
            p.getPaymentVoucherId(),
            p.getPaidBy(),
            p.getPaidAt(),
            p.getCreatedAt(),
            p.getUpdatedAt()
        );
    }
}
