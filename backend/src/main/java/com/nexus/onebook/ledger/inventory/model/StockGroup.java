package com.nexus.onebook.ledger.inventory.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "stock_groups")
public class StockGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "group_code", nullable = false, length = 50)
    private String groupCode;

    @Column(name = "group_name", nullable = false, length = 255)
    private String groupName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_group_id")
    private StockGroup parentGroup;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public StockGroup() {}

    public StockGroup(String tenantId, String groupCode, String groupName) {
        this.tenantId = tenantId;
        this.groupCode = groupCode;
        this.groupName = groupName;
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

    public String getGroupCode() { return groupCode; }
    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public StockGroup getParentGroup() { return parentGroup; }
    public void setParentGroup(StockGroup parentGroup) { this.parentGroup = parentGroup; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
