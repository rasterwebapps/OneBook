package com.nexus.onebook.advance.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Configurable per-employee advance limit.
 * Default limit is ₹10,000 unless overridden.
 */
@Entity
@Table(name = "employee_advance_config")
public class EmployeeAdvanceConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "advance_limit", precision = 19, scale = 4, nullable = false)
    private BigDecimal advanceLimit = new BigDecimal("10000.00");

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public EmployeeAdvanceConfig() {}

    public EmployeeAdvanceConfig(String tenantId, Long employeeId, BigDecimal advanceLimit) {
        this.tenantId = tenantId;
        this.employeeId = employeeId;
        this.advanceLimit = advanceLimit;
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

    public BigDecimal getAdvanceLimit() { return advanceLimit; }
    public void setAdvanceLimit(BigDecimal advanceLimit) { this.advanceLimit = advanceLimit; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
