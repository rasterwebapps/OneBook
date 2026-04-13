package com.nexus.onebook.ledger.payroll.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import com.nexus.onebook.ledger.accounts.model.LedgerAccount;

@Entity
@Table(name = "payroll_employees")
public class PayrollEmployee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "employee_code", nullable = false, length = 50)
    private String employeeCode;

    @Column(name = "employee_name", nullable = false, length = 255)
    private String employeeName;

    @Column(name = "designation", length = 100)
    private String designation;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "date_of_joining", nullable = false)
    private LocalDate dateOfJoining;

    @Column(name = "pan_number", length = 20)
    private String panNumber;

    @Column(name = "bank_account", length = 50)
    private String bankAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salary_account_id")
    private LedgerAccount salaryAccount;

    @Column(name = "employee_group", length = 100)
    private String employeeGroup = "DEFAULT";

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public PayrollEmployee() {}

    public PayrollEmployee(String tenantId, String employeeCode, String employeeName,
                           LocalDate dateOfJoining) {
        this.tenantId = tenantId;
        this.employeeCode = employeeCode;
        this.employeeName = employeeName;
        this.dateOfJoining = dateOfJoining;
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

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public LocalDate getDateOfJoining() { return dateOfJoining; }
    public void setDateOfJoining(LocalDate dateOfJoining) { this.dateOfJoining = dateOfJoining; }

    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }

    public LedgerAccount getSalaryAccount() { return salaryAccount; }
    public void setSalaryAccount(LedgerAccount salaryAccount) { this.salaryAccount = salaryAccount; }

    public String getEmployeeGroup() { return employeeGroup; }
    public void setEmployeeGroup(String employeeGroup) { this.employeeGroup = employeeGroup; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
