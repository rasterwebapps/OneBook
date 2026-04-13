package com.nexus.onebook.operations.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;

/**
 * Disaster recovery event tracking.
 * Records backups, point-in-time recovery attempts, and failover events.
 */
@Entity
@Table(name = "disaster_recovery_events")
public class DisasterRecoveryEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "STARTED";

    @Column(name = "backup_location", length = 500)
    private String backupLocation;

    @Column(name = "point_in_time")
    private Instant pointInTime;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata = "{}";

    public DisasterRecoveryEvent() {}

    public DisasterRecoveryEvent(String tenantId, String eventType) {
        this.tenantId = tenantId;
        this.eventType = eventType;
    }

    @PrePersist
    protected void onCreate() {
        startedAt = Instant.now();
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBackupLocation() { return backupLocation; }
    public void setBackupLocation(String backupLocation) { this.backupLocation = backupLocation; }

    public Instant getPointInTime() { return pointInTime; }
    public void setPointInTime(Instant pointInTime) { this.pointInTime = pointInTime; }

    public Instant getStartedAt() { return startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}
