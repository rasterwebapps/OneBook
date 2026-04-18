package com.nexus.onebook.education.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a fee structure for a specific course and academic year.
 * Contains multiple fee structure items defining individual amounts.
 */
@Entity
@Table(name = "fee_structures")
public class FeeStructure {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private EducationCourse course;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public FeeStructure() {}

    public FeeStructure(String tenantId, EducationCourse course, String academicYear) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.course = course;
        this.academicYear = academicYear;
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

    public EducationCourse getCourse() { return course; }
    public void setCourse(EducationCourse course) { this.course = course; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Instant getCreatedAt() { return createdAt; }
}
