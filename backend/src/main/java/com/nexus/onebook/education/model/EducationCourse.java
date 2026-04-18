package com.nexus.onebook.education.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a course within an educational program (e.g., Computer Science under B.Tech).
 */
@Entity
@Table(name = "education_courses")
public class EducationCourse {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private EducationProgram program;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public EducationCourse() {}

    public EducationCourse(String tenantId, EducationProgram program, String name, String code) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.program = program;
        this.name = name;
        this.code = code;
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

    public EducationProgram getProgram() { return program; }
    public void setProgram(EducationProgram program) { this.program = program; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Instant getCreatedAt() { return createdAt; }
}
