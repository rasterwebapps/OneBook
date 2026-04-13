package com.nexus.onebook.ledger.inventory.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "units_of_measure")
public class UnitOfMeasure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "uom_code", nullable = false, length = 20)
    private String uomCode;

    @Column(name = "uom_name", nullable = false, length = 100)
    private String uomName;

    @Column(name = "decimal_places", nullable = false)
    private int decimalPlaces = 2;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public UnitOfMeasure() {}

    public UnitOfMeasure(String tenantId, String uomCode, String uomName) {
        this.tenantId = tenantId;
        this.uomCode = uomCode;
        this.uomName = uomName;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getUomCode() { return uomCode; }
    public void setUomCode(String uomCode) { this.uomCode = uomCode; }

    public String getUomName() { return uomName; }
    public void setUomName(String uomName) { this.uomName = uomName; }

    public int getDecimalPlaces() { return decimalPlaces; }
    public void setDecimalPlaces(int decimalPlaces) { this.decimalPlaces = decimalPlaces; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
}
