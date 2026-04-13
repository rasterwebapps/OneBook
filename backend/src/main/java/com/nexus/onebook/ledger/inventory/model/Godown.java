package com.nexus.onebook.ledger.inventory.model;

import jakarta.persistence.*;
import java.time.Instant;
import com.nexus.onebook.ledger.accounts.model.Branch;

@Entity
@Table(name = "godowns")
public class Godown {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "godown_code", nullable = false, length = 50)
    private String godownCode;

    @Column(name = "godown_name", nullable = false, length = 255)
    private String godownName;

    @Column(name = "address")
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Godown() {}

    public Godown(String tenantId, String godownCode, String godownName) {
        this.tenantId = tenantId;
        this.godownCode = godownCode;
        this.godownName = godownName;
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

    public String getGodownCode() { return godownCode; }
    public void setGodownCode(String godownCode) { this.godownCode = godownCode; }

    public String getGodownName() { return godownName; }
    public void setGodownName(String godownName) { this.godownName = godownName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
