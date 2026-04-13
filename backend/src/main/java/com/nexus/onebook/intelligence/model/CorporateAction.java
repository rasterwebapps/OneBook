package com.nexus.onebook.intelligence.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "corporate_actions")
public class CorporateAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holding_id", nullable = false)
    private InvestmentHolding holding;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private CorporateActionType actionType;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "execution_date")
    private LocalDate executionDate;

    @Column(name = "ratio", precision = 10, scale = 4)
    private BigDecimal ratio;

    @Column(name = "amount_per_unit", precision = 19, scale = 4)
    private BigDecimal amountPerUnit;

    @Column(name = "processed", nullable = false)
    private boolean processed = false;

    @Column(name = "metadata", columnDefinition = "text")
    private String metadata = "{}";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public CorporateAction() {}

    public CorporateAction(String tenantId, InvestmentHolding holding,
                           CorporateActionType actionType, LocalDate recordDate) {
        this.tenantId = tenantId;
        this.holding = holding;
        this.actionType = actionType;
        this.recordDate = recordDate;
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

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public InvestmentHolding getHolding() { return holding; }
    public void setHolding(InvestmentHolding holding) { this.holding = holding; }

    public CorporateActionType getActionType() { return actionType; }
    public void setActionType(CorporateActionType actionType) { this.actionType = actionType; }

    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }

    public LocalDate getExecutionDate() { return executionDate; }
    public void setExecutionDate(LocalDate executionDate) { this.executionDate = executionDate; }

    public BigDecimal getRatio() { return ratio; }
    public void setRatio(BigDecimal ratio) { this.ratio = ratio; }

    public BigDecimal getAmountPerUnit() { return amountPerUnit; }
    public void setAmountPerUnit(BigDecimal amountPerUnit) { this.amountPerUnit = amountPerUnit; }

    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
