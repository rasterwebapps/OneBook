package com.nexus.onebook.ledger.auditor.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Audit workflow approval entity.
 * Tracks workflow approvals requested by external auditors (CPAs).
 */
@Entity
@Table(name = "audit_workflows")
public class AuditWorkflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "workflow_name", nullable = false)
    private String workflowName;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "auditor_name", nullable = false)
    private String auditorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AuditWorkflowStatus status = AuditWorkflowStatus.PENDING;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    @Column(name = "rejection_reason", columnDefinition = "text")
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public AuditWorkflow() {}

    public AuditWorkflow(String tenantId, String workflowName, String auditorName) {
        this.tenantId = tenantId;
        this.workflowName = workflowName;
        this.auditorName = auditorName;
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

    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAuditorName() { return auditorName; }
    public void setAuditorName(String auditorName) { this.auditorName = auditorName; }

    public AuditWorkflowStatus getStatus() { return status; }
    public void setStatus(AuditWorkflowStatus status) { this.status = status; }

    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }

    public Instant getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(Instant rejectedAt) { this.rejectedAt = rejectedAt; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
}
