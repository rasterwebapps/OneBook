package com.nexus.onebook.ledger.auditor.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * External auditor comment on a specific record.
 * Auditors can leave read-only comments for review by the finance team.
 */
@Entity
@Table(name = "audit_comments")
public class AuditComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "auditor_name", nullable = false)
    private String auditorName;

    @Column(name = "table_name", nullable = false, length = 100)
    private String tableName;

    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @Column(name = "comment_text", nullable = false, columnDefinition = "text")
    private String commentText;

    @Column(name = "resolved", nullable = false)
    private boolean resolved = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public AuditComment() {}

    public AuditComment(String tenantId, String auditorName, String tableName,
                        Long recordId, String commentText) {
        this.tenantId = tenantId;
        this.auditorName = auditorName;
        this.tableName = tableName;
        this.recordId = recordId;
        this.commentText = commentText;
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

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getAuditorName() { return auditorName; }
    public void setAuditorName(String auditorName) { this.auditorName = auditorName; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }

    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
}
