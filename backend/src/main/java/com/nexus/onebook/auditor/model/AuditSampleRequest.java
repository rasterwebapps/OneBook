package com.nexus.onebook.auditor.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

/**
 * External auditor sample request.
 * Auditors can request random samples of transactions/records
 * for independent verification.
 */
@Entity
@Table(name = "audit_sample_requests")
public class AuditSampleRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "auditor_name", nullable = false)
    private String auditorName;

    @Column(name = "auditor_email", nullable = false)
    private String auditorEmail;

    @Column(name = "request_description", nullable = false, columnDefinition = "text")
    private String requestDescription;

    @Column(name = "table_name", nullable = false, length = 100)
    private String tableName;

    @Column(name = "sample_size", nullable = false)
    private int sampleSize = 10;

    @Column(name = "date_from")
    private LocalDate dateFrom;

    @Column(name = "date_to")
    private LocalDate dateTo;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "response_data", columnDefinition = "text")
    private String responseData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public AuditSampleRequest() {}

    public AuditSampleRequest(String tenantId, String auditorName, String auditorEmail,
                               String requestDescription, String tableName, int sampleSize) {
        this.tenantId = tenantId;
        this.auditorName = auditorName;
        this.auditorEmail = auditorEmail;
        this.requestDescription = requestDescription;
        this.tableName = tableName;
        this.sampleSize = sampleSize;
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

    public String getAuditorEmail() { return auditorEmail; }
    public void setAuditorEmail(String auditorEmail) { this.auditorEmail = auditorEmail; }

    public String getRequestDescription() { return requestDescription; }
    public void setRequestDescription(String requestDescription) { this.requestDescription = requestDescription; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public int getSampleSize() { return sampleSize; }
    public void setSampleSize(int sampleSize) { this.sampleSize = sampleSize; }

    public LocalDate getDateFrom() { return dateFrom; }
    public void setDateFrom(LocalDate dateFrom) { this.dateFrom = dateFrom; }

    public LocalDate getDateTo() { return dateTo; }
    public void setDateTo(LocalDate dateTo) { this.dateTo = dateTo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResponseData() { return responseData; }
    public void setResponseData(String responseData) { this.responseData = responseData; }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
}
