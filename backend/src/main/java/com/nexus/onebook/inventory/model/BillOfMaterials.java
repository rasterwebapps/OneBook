package com.nexus.onebook.inventory.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "bills_of_materials")
public class BillOfMaterials {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "bom_code", nullable = false, length = 50)
    private String bomCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finished_item_id", nullable = false)
    private StockItem finishedItem;

    @Column(name = "quantity_produced", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityProduced = BigDecimal.ONE;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public BillOfMaterials() {}

    public BillOfMaterials(String tenantId, String bomCode, StockItem finishedItem) {
        this.tenantId = tenantId;
        this.bomCode = bomCode;
        this.finishedItem = finishedItem;
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

    public String getBomCode() { return bomCode; }
    public void setBomCode(String bomCode) { this.bomCode = bomCode; }

    public StockItem getFinishedItem() { return finishedItem; }
    public void setFinishedItem(StockItem finishedItem) { this.finishedItem = finishedItem; }

    public BigDecimal getQuantityProduced() { return quantityProduced; }
    public void setQuantityProduced(BigDecimal quantityProduced) { this.quantityProduced = quantityProduced; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
