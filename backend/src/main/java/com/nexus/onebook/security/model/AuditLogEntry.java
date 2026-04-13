package com.nexus.onebook.security.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Hash-chained audit log entry.
 * <p>
 * Each row is cryptographically linked to its predecessor via
 * {@code prevHash}. Tampering with any record breaks the chain,
 * which is detectable by {@link com.nexus.onebook.security.AuditLogService#verifyChain}.
 */
@Entity
@Table(name = "audit_log")
public class AuditLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "table_name", nullable = false, length = 100)
    private String tableName;

    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "operation", nullable = false, length = 10)
    private String operation;

    @Column(name = "old_values", columnDefinition = "text")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "text")
    private String newValues;

    @Column(name = "hash", nullable = false)
    private String hash;

    @Column(name = "prev_hash")
    private String prevHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private String createdBy;

    public AuditLogEntry() {}

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public String getOldValues() { return oldValues; }
    public void setOldValues(String oldValues) { this.oldValues = oldValues; }

    public String getNewValues() { return newValues; }
    public void setNewValues(String newValues) { this.newValues = newValues; }

    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }

    public String getPrevHash() { return prevHash; }
    public void setPrevHash(String prevHash) { this.prevHash = prevHash; }

    public Instant getCreatedAt() { return createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
