package com.nexus.onebook.advance.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Employee advance voucher entity.
 * Supports tiered approval workflow: HOD (≤₹10k), CEO (₹10k-20k), MD (>₹20k).
 */
@Entity
@Table(name = "employee_advances")
public class EmployeeAdvance {

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

    @Column(nullable = false, columnDefinition = "TEXT")
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AdvanceStatus status = AdvanceStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_approver_role", length = 20)
    private ApproverRole currentApproverRole;

    @Column(name = "override_flag", nullable = false)
    private boolean overrideFlag = false;

    @Column(name = "override_reason", columnDefinition = "TEXT")
    private String overrideReason;

    @Column(name = "voucher_date", nullable = false)
    private LocalDate voucherDate;

    @Column(name = "approved_amount", precision = 19, scale = 4)
    private BigDecimal approvedAmount;

    @Column(name = "journal_entry_id")
    private Long journalEntryId;

    // HOD approval
    @Column(name = "hod_approved_by")
    private String hodApprovedBy;

    @Column(name = "hod_approved_at")
    private Instant hodApprovedAt;

    // CEO approval
    @Column(name = "ceo_approved_by")
    private String ceoApprovedBy;

    @Column(name = "ceo_approved_at")
    private Instant ceoApprovedAt;

    // MD approval
    @Column(name = "md_approved_by")
    private String mdApprovedBy;

    @Column(name = "md_approved_at")
    private Instant mdApprovedAt;

    // Rejection
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

    public EmployeeAdvance() {}

    public EmployeeAdvance(String tenantId, Long employeeId, Long departmentId, 
                           BigDecimal amount, String purpose, LocalDate voucherDate, String createdBy) {
        this.tenantId = tenantId;
        this.employeeId = employeeId;
        this.departmentId = departmentId;
        this.amount = amount;
        this.purpose = purpose;
        this.voucherDate = voucherDate;
        this.createdBy = createdBy;
        this.status = AdvanceStatus.DRAFT;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Determines the required approval tier based on amount.
     * ≤₹10,000: HOD only
     * ₹10,001-₹20,000: HOD + CEO
     * >₹20,000: HOD + CEO + MD
     */
    public ApproverRole getRequiredFinalApprover() {
        BigDecimal tenThousand = new BigDecimal("10000.00");
        BigDecimal twentyThousand = new BigDecimal("20000.00");
        
        if (amount.compareTo(twentyThousand) > 0) {
            return ApproverRole.MD;
        } else if (amount.compareTo(tenThousand) > 0) {
            return ApproverRole.CEO;
        } else {
            return ApproverRole.HOD;
        }
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

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public AdvanceStatus getStatus() { return status; }
    public void setStatus(AdvanceStatus status) { this.status = status; }

    public ApproverRole getCurrentApproverRole() { return currentApproverRole; }
    public void setCurrentApproverRole(ApproverRole currentApproverRole) { this.currentApproverRole = currentApproverRole; }

    public boolean isOverrideFlag() { return overrideFlag; }
    public void setOverrideFlag(boolean overrideFlag) { this.overrideFlag = overrideFlag; }

    public String getOverrideReason() { return overrideReason; }
    public void setOverrideReason(String overrideReason) { this.overrideReason = overrideReason; }

    public LocalDate getVoucherDate() { return voucherDate; }
    public void setVoucherDate(LocalDate voucherDate) { this.voucherDate = voucherDate; }

    public BigDecimal getApprovedAmount() { return approvedAmount; }
    public void setApprovedAmount(BigDecimal approvedAmount) { this.approvedAmount = approvedAmount; }

    public Long getJournalEntryId() { return journalEntryId; }
    public void setJournalEntryId(Long journalEntryId) { this.journalEntryId = journalEntryId; }

    public String getHodApprovedBy() { return hodApprovedBy; }
    public void setHodApprovedBy(String hodApprovedBy) { this.hodApprovedBy = hodApprovedBy; }

    public Instant getHodApprovedAt() { return hodApprovedAt; }
    public void setHodApprovedAt(Instant hodApprovedAt) { this.hodApprovedAt = hodApprovedAt; }

    public String getCeoApprovedBy() { return ceoApprovedBy; }
    public void setCeoApprovedBy(String ceoApprovedBy) { this.ceoApprovedBy = ceoApprovedBy; }

    public Instant getCeoApprovedAt() { return ceoApprovedAt; }
    public void setCeoApprovedAt(Instant ceoApprovedAt) { this.ceoApprovedAt = ceoApprovedAt; }

    public String getMdApprovedBy() { return mdApprovedBy; }
    public void setMdApprovedBy(String mdApprovedBy) { this.mdApprovedBy = mdApprovedBy; }

    public Instant getMdApprovedAt() { return mdApprovedAt; }
    public void setMdApprovedAt(Instant mdApprovedAt) { this.mdApprovedAt = mdApprovedAt; }

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
