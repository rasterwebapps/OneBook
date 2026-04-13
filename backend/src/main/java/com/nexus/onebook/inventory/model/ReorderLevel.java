package com.nexus.onebook.inventory.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "reorder_levels")
public class ReorderLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_item_id", nullable = false)
    private StockItem stockItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "godown_id")
    private Godown godown;

    @Column(name = "minimum_level", nullable = false, precision = 19, scale = 4)
    private BigDecimal minimumLevel = BigDecimal.ZERO;

    @Column(name = "reorder_level", nullable = false, precision = 19, scale = 4)
    private BigDecimal reorderLevel = BigDecimal.ZERO;

    @Column(name = "maximum_level", nullable = false, precision = 19, scale = 4)
    private BigDecimal maximumLevel = BigDecimal.ZERO;

    @Column(name = "reorder_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal reorderQuantity = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ReorderLevel() {}

    public ReorderLevel(String tenantId, StockItem stockItem, BigDecimal minimumLevel,
                        BigDecimal reorderLevel, BigDecimal maximumLevel, BigDecimal reorderQuantity) {
        this.tenantId = tenantId;
        this.stockItem = stockItem;
        this.minimumLevel = minimumLevel;
        this.reorderLevel = reorderLevel;
        this.maximumLevel = maximumLevel;
        this.reorderQuantity = reorderQuantity;
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

    public StockItem getStockItem() { return stockItem; }
    public void setStockItem(StockItem stockItem) { this.stockItem = stockItem; }

    public Godown getGodown() { return godown; }
    public void setGodown(Godown godown) { this.godown = godown; }

    public BigDecimal getMinimumLevel() { return minimumLevel; }
    public void setMinimumLevel(BigDecimal minimumLevel) { this.minimumLevel = minimumLevel; }

    public BigDecimal getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(BigDecimal reorderLevel) { this.reorderLevel = reorderLevel; }

    public BigDecimal getMaximumLevel() { return maximumLevel; }
    public void setMaximumLevel(BigDecimal maximumLevel) { this.maximumLevel = maximumLevel; }

    public BigDecimal getReorderQuantity() { return reorderQuantity; }
    public void setReorderQuantity(BigDecimal reorderQuantity) { this.reorderQuantity = reorderQuantity; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
