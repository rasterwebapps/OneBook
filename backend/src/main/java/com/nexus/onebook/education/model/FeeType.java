package com.nexus.onebook.education.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a fee type (e.g., Tuition Fee, Hostel Fee, Transport Fee).
 * category: GENERIC or ADDITIONAL.
 * additionalType: HOSTEL or TRANSPORTATION (only when category = ADDITIONAL).
 */
@Entity
@Table(name = "fee_types")
public class FeeType {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeeCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "additional_type", length = 20)
    private AdditionalFeeType additionalType;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public FeeType() {}

    public FeeType(String tenantId, String name, FeeCategory category, AdditionalFeeType additionalType) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.name = name;
        this.category = category;
        this.additionalType = additionalType;
        this.isActive = true;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public FeeCategory getCategory() { return category; }
    public void setCategory(FeeCategory category) { this.category = category; }

    public AdditionalFeeType getAdditionalType() { return additionalType; }
    public void setAdditionalType(AdditionalFeeType additionalType) { this.additionalType = additionalType; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Instant getCreatedAt() { return createdAt; }
}
