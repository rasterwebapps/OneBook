package com.nexus.onebook.ledger.inventory.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "bom_components")
public class BomComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bom_id", nullable = false)
    private BillOfMaterials bom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_item_id", nullable = false)
    private StockItem componentItem;

    @Column(name = "quantity_required", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityRequired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uom_id", nullable = false)
    private UnitOfMeasure uom;

    public BomComponent() {}

    public BomComponent(String tenantId, BillOfMaterials bom, StockItem componentItem,
                        BigDecimal quantityRequired, UnitOfMeasure uom) {
        this.tenantId = tenantId;
        this.bom = bom;
        this.componentItem = componentItem;
        this.quantityRequired = quantityRequired;
        this.uom = uom;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public BillOfMaterials getBom() { return bom; }
    public void setBom(BillOfMaterials bom) { this.bom = bom; }

    public StockItem getComponentItem() { return componentItem; }
    public void setComponentItem(StockItem componentItem) { this.componentItem = componentItem; }

    public BigDecimal getQuantityRequired() { return quantityRequired; }
    public void setQuantityRequired(BigDecimal quantityRequired) { this.quantityRequired = quantityRequired; }

    public UnitOfMeasure getUom() { return uom; }
    public void setUom(UnitOfMeasure uom) { this.uom = uom; }
}
