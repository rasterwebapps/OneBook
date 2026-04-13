package com.nexus.onebook.inventory.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "stock_items")
public class StockItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "item_code", nullable = false, length = 50)
    private String itemCode;

    @Column(name = "item_name", nullable = false, length = 255)
    private String itemName;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_group_id")
    private StockGroup stockGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_uom_id", nullable = false)
    private UnitOfMeasure primaryUom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secondary_uom_id")
    private UnitOfMeasure secondaryUom;

    @Column(name = "conversion_factor", precision = 19, scale = 6)
    private BigDecimal conversionFactor = BigDecimal.ONE;

    @Column(name = "opening_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Column(name = "current_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(name = "rate_per_unit", precision = 19, scale = 4)
    private BigDecimal ratePerUnit = BigDecimal.ZERO;

    @Column(name = "hsn_code", length = 20)
    private String hsnCode;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "metadata", columnDefinition = "text")
    private String metadata = "{}";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public StockItem() {}

    public StockItem(String tenantId, String itemCode, String itemName, UnitOfMeasure primaryUom) {
        this.tenantId = tenantId;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.primaryUom = primaryUom;
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

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public StockGroup getStockGroup() { return stockGroup; }
    public void setStockGroup(StockGroup stockGroup) { this.stockGroup = stockGroup; }

    public UnitOfMeasure getPrimaryUom() { return primaryUom; }
    public void setPrimaryUom(UnitOfMeasure primaryUom) { this.primaryUom = primaryUom; }

    public UnitOfMeasure getSecondaryUom() { return secondaryUom; }
    public void setSecondaryUom(UnitOfMeasure secondaryUom) { this.secondaryUom = secondaryUom; }

    public BigDecimal getConversionFactor() { return conversionFactor; }
    public void setConversionFactor(BigDecimal conversionFactor) { this.conversionFactor = conversionFactor; }

    public BigDecimal getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(BigDecimal openingBalance) { this.openingBalance = openingBalance; }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }

    public BigDecimal getRatePerUnit() { return ratePerUnit; }
    public void setRatePerUnit(BigDecimal ratePerUnit) { this.ratePerUnit = ratePerUnit; }

    public String getHsnCode() { return hsnCode; }
    public void setHsnCode(String hsnCode) { this.hsnCode = hsnCode; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
