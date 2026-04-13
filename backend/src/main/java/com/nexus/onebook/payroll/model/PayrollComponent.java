package com.nexus.onebook.payroll.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payroll_components")
public class PayrollComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private PayrollEmployee employee;

    @Column(name = "component_name", nullable = false, length = 100)
    private String componentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false, length = 20)
    private PayrollComponentType componentType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "percentage_of_basic", precision = 8, scale = 4)
    private BigDecimal percentageOfBasic;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public PayrollComponent() {}

    public PayrollComponent(String tenantId, PayrollEmployee employee, String componentName,
                            PayrollComponentType componentType, BigDecimal amount) {
        this.tenantId = tenantId;
        this.employee = employee;
        this.componentName = componentName;
        this.componentType = componentType;
        this.amount = amount;
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

    public PayrollEmployee getEmployee() { return employee; }
    public void setEmployee(PayrollEmployee employee) { this.employee = employee; }

    public String getComponentName() { return componentName; }
    public void setComponentName(String componentName) { this.componentName = componentName; }

    public PayrollComponentType getComponentType() { return componentType; }
    public void setComponentType(PayrollComponentType componentType) { this.componentType = componentType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getPercentageOfBasic() { return percentageOfBasic; }
    public void setPercentageOfBasic(BigDecimal percentageOfBasic) { this.percentageOfBasic = percentageOfBasic; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
