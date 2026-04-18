package com.nexus.onebook.education.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a student fee enquiry.
 * Created when a student enquires about admission; captures fee breakdown before finalization.
 */
@Entity
@Table(name = "student_enquiries")
public class StudentEnquiry {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private EducationProgram program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private EducationCourse course;

    @Enumerated(EnumType.STRING)
    @Column(name = "student_type", nullable = false, length = 20)
    private StudentType studentType;

    @Column(name = "generic_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal genericTotal;

    @Column(name = "additional_fee", nullable = false, precision = 15, scale = 2)
    private BigDecimal additionalFee;

    @Column(name = "total_fees", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalFees;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnquiryStatus status = EnquiryStatus.OPEN;

    @Column(name = "enquiry_date", nullable = false)
    private LocalDate enquiryDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public StudentEnquiry() {}

    public StudentEnquiry(String tenantId, String studentName, String email, String phone,
                          EducationProgram program, EducationCourse course,
                          StudentType studentType, BigDecimal genericTotal,
                          BigDecimal additionalFee, BigDecimal totalFees, String academicYear) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.studentName = studentName;
        this.email = email;
        this.phone = phone;
        this.program = program;
        this.course = course;
        this.studentType = studentType;
        this.genericTotal = genericTotal;
        this.additionalFee = additionalFee;
        this.totalFees = totalFees;
        this.academicYear = academicYear;
        this.status = EnquiryStatus.OPEN;
        this.enquiryDate = LocalDate.now();
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = Instant.now();
        if (enquiryDate == null) {
            enquiryDate = LocalDate.now();
        }
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public EducationProgram getProgram() { return program; }
    public void setProgram(EducationProgram program) { this.program = program; }

    public EducationCourse getCourse() { return course; }
    public void setCourse(EducationCourse course) { this.course = course; }

    public StudentType getStudentType() { return studentType; }
    public void setStudentType(StudentType studentType) { this.studentType = studentType; }

    public BigDecimal getGenericTotal() { return genericTotal; }
    public void setGenericTotal(BigDecimal genericTotal) { this.genericTotal = genericTotal; }

    public BigDecimal getAdditionalFee() { return additionalFee; }
    public void setAdditionalFee(BigDecimal additionalFee) { this.additionalFee = additionalFee; }

    public BigDecimal getTotalFees() { return totalFees; }
    public void setTotalFees(BigDecimal totalFees) { this.totalFees = totalFees; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public EnquiryStatus getStatus() { return status; }
    public void setStatus(EnquiryStatus status) { this.status = status; }

    public LocalDate getEnquiryDate() { return enquiryDate; }
    public void setEnquiryDate(LocalDate enquiryDate) { this.enquiryDate = enquiryDate; }

    public Instant getCreatedAt() { return createdAt; }
}
