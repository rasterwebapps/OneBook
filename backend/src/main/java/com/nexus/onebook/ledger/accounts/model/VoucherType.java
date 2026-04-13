package com.nexus.onebook.ledger.accounts.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "voucher_types")
public class VoucherType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "voucher_code", nullable = false, length = 20)
    private String voucherCode;

    @Column(name = "voucher_name", nullable = false, length = 100)
    private String voucherName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private VoucherCategory category;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public VoucherType() {}

    public VoucherType(String tenantId, String voucherCode, String voucherName, VoucherCategory category) {
        this.tenantId = tenantId;
        this.voucherCode = voucherCode;
        this.voucherName = voucherName;
        this.category = category;
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

    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }

    public String getVoucherName() { return voucherName; }
    public void setVoucherName(String voucherName) { this.voucherName = voucherName; }

    public VoucherCategory getCategory() { return category; }
    public void setCategory(VoucherCategory category) { this.category = category; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
