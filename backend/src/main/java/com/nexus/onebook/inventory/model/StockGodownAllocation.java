package com.nexus.onebook.inventory.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "stock_godown_allocations")
public class StockGodownAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_item_id", nullable = false)
    private StockItem stockItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "godown_id", nullable = false)
    private Godown godown;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "rate", precision = 19, scale = 4)
    private BigDecimal rate = BigDecimal.ZERO;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public StockGodownAllocation() {}

    public StockGodownAllocation(String tenantId, StockItem stockItem, Godown godown, BigDecimal quantity) {
        this.tenantId = tenantId;
        this.stockItem = stockItem;
        this.godown = godown;
        this.quantity = quantity;
    }

    @PrePersist
    protected void onCreate() {
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

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }

    public Instant getUpdatedAt() { return updatedAt; }
}
