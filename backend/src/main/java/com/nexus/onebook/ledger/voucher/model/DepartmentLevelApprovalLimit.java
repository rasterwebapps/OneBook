package com.nexus.onebook.ledger.voucher.model;

import com.nexus.onebook.ledger.model.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "department_level_approval_limits")
public class DepartmentLevelApprovalLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id", nullable = false)
    private Payer payer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_approval_limit_id", nullable = false)
    private PaymentApprovalLimit paymentApprovalLimit;

    @Column(name = "max_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal maxAmount;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public DepartmentLevelApprovalLimit() {}

    public DepartmentLevelApprovalLimit(String tenantId, Department department, Payer payer,
                                         PaymentApprovalLimit paymentApprovalLimit, BigDecimal maxAmount) {
        this.tenantId = tenantId;
        this.department = department;
        this.payer = payer;
        this.paymentApprovalLimit = paymentApprovalLimit;
        this.maxAmount = maxAmount;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public Payer getPayer() { return payer; }
    public void setPayer(Payer payer) { this.payer = payer; }

    public PaymentApprovalLimit getPaymentApprovalLimit() { return paymentApprovalLimit; }
    public void setPaymentApprovalLimit(PaymentApprovalLimit paymentApprovalLimit) { this.paymentApprovalLimit = paymentApprovalLimit; }

    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
