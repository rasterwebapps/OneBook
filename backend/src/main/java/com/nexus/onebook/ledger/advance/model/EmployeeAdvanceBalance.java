package com.nexus.onebook.ledger.advance.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Cached outstanding advance balance per employee.
 * Updated on every advance posting, expense settlement, or receipt.
 */
@Entity
@Table(name = "employee_advance_balance")
public class EmployeeAdvanceBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "outstanding_advance", precision = 19, scale = 4, nullable = false)
    private BigDecimal outstandingAdvance = BigDecimal.ZERO;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public EmployeeAdvanceBalance() {}

    public EmployeeAdvanceBalance(String tenantId, Long employeeId) {
        this.tenantId = tenantId;
        this.employeeId = employeeId;
        this.outstandingAdvance = BigDecimal.ZERO;
    }

    @PrePersist
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

    public BigDecimal getOutstandingAdvance() { return outstandingAdvance; }
    public void setOutstandingAdvance(BigDecimal outstandingAdvance) { this.outstandingAdvance = outstandingAdvance; }

    public Instant getUpdatedAt() { return updatedAt; }

    public void addAdvance(BigDecimal amount) {
        this.outstandingAdvance = this.outstandingAdvance.add(amount);
    }

    public void reduceAdvance(BigDecimal amount) {
        this.outstandingAdvance = this.outstandingAdvance.subtract(amount);
        if (this.outstandingAdvance.compareTo(BigDecimal.ZERO) < 0) {
            this.outstandingAdvance = BigDecimal.ZERO;
        }
    }
}
