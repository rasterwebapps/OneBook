package com.nexus.onebook.ledger.advance.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Payment advice entity.
 * System-generated when expense exceeds outstanding advance.
 * Represents a liability (Employee Reimbursement Payable).
 */
@Entity
@Table(name = "payment_advices_m12")
public class EmployeePaymentAdvice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Column(name = "expense_voucher_id", nullable = false)
    private Long expenseVoucherId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentAdviceStatus status = PaymentAdviceStatus.PENDING_PAYMENT;

    @Column(name = "payment_voucher_id")
    private Long paymentVoucherId;

    @Column(name = "paid_by")
    private String paidBy;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public EmployeePaymentAdvice() {}

    public EmployeePaymentAdvice(String tenantId, Long employeeId, Long departmentId, 
                                 BigDecimal amount, Long expenseVoucherId) {
        this.tenantId = tenantId;
        this.employeeId = employeeId;
        this.departmentId = departmentId;
        this.amount = amount;
        this.expenseVoucherId = expenseVoucherId;
        this.status = PaymentAdviceStatus.PENDING_PAYMENT;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Long getExpenseVoucherId() { return expenseVoucherId; }
    public void setExpenseVoucherId(Long expenseVoucherId) { this.expenseVoucherId = expenseVoucherId; }

    public PaymentAdviceStatus getStatus() { return status; }
    public void setStatus(PaymentAdviceStatus status) { this.status = status; }

    public Long getPaymentVoucherId() { return paymentVoucherId; }
    public void setPaymentVoucherId(Long paymentVoucherId) { this.paymentVoucherId = paymentVoucherId; }

    public String getPaidBy() { return paidBy; }
    public void setPaidBy(String paidBy) { this.paidBy = paidBy; }

    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
