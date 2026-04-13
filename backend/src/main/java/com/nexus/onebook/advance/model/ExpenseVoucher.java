package com.nexus.onebook.advance.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Expense voucher entity.
 * Submitted by employee, approved by HOD.
 * On approval, settles against outstanding advance and/or creates payment advice.
 */
@Entity
@Table(name = "expense_vouchers")
public class ExpenseVoucher {

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

    @Column(name = "expense_type", nullable = false, length = 100)
    private String expenseType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "voucher_date", nullable = false)
    private LocalDate voucherDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ExpenseVoucherStatus status = ExpenseVoucherStatus.DRAFT;

    @Column(name = "supporting_doc_ref", columnDefinition = "TEXT")
    private String supportingDocRef;

    // Settlement tracking (populated on HOD approval)
    @Column(name = "advance_settlement", precision = 19, scale = 4)
    private BigDecimal advanceSettlement;

    @Column(name = "reimbursement_amount", precision = 19, scale = 4)
    private BigDecimal reimbursementAmount;

    @Column(name = "payment_advice_id")
    private Long paymentAdviceId;

    @Column(name = "journal_entry_id")
    private Long journalEntryId;

    // Approval tracking
    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "rejected_by")
    private String rejectedBy;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ExpenseVoucher() {}

    public ExpenseVoucher(String tenantId, Long employeeId, Long departmentId, BigDecimal amount,
                          String expenseType, String description, LocalDate voucherDate, String createdBy) {
        this.tenantId = tenantId;
        this.employeeId = employeeId;
        this.departmentId = departmentId;
        this.amount = amount;
        this.expenseType = expenseType;
        this.description = description;
        this.voucherDate = voucherDate;
        this.createdBy = createdBy;
        this.status = ExpenseVoucherStatus.DRAFT;
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

    public String getExpenseType() { return expenseType; }
    public void setExpenseType(String expenseType) { this.expenseType = expenseType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getVoucherDate() { return voucherDate; }
    public void setVoucherDate(LocalDate voucherDate) { this.voucherDate = voucherDate; }

    public ExpenseVoucherStatus getStatus() { return status; }
    public void setStatus(ExpenseVoucherStatus status) { this.status = status; }

    public String getSupportingDocRef() { return supportingDocRef; }
    public void setSupportingDocRef(String supportingDocRef) { this.supportingDocRef = supportingDocRef; }

    public BigDecimal getAdvanceSettlement() { return advanceSettlement; }
    public void setAdvanceSettlement(BigDecimal advanceSettlement) { this.advanceSettlement = advanceSettlement; }

    public BigDecimal getReimbursementAmount() { return reimbursementAmount; }
    public void setReimbursementAmount(BigDecimal reimbursementAmount) { this.reimbursementAmount = reimbursementAmount; }

    public Long getPaymentAdviceId() { return paymentAdviceId; }
    public void setPaymentAdviceId(Long paymentAdviceId) { this.paymentAdviceId = paymentAdviceId; }

    public Long getJournalEntryId() { return journalEntryId; }
    public void setJournalEntryId(Long journalEntryId) { this.journalEntryId = journalEntryId; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }

    public String getRejectedBy() { return rejectedBy; }
    public void setRejectedBy(String rejectedBy) { this.rejectedBy = rejectedBy; }

    public Instant getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(Instant rejectedAt) { this.rejectedAt = rejectedAt; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
