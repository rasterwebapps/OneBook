package com.nexus.onebook.ledger.foundation.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "payee_bank_accounts")
public class PayeeBankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payee_id", nullable = false)
    private Payee payee;

    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber;

    @Column(name = "bank_name", nullable = false, length = 255)
    private String bankName;

    @Column(name = "branch_name", length = 255)
    private String branchName;

    @Column(name = "ifsc_code", length = 20)
    private String ifscCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "bank_account_type", length = 30)
    private BankAccountType bankAccountType;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public PayeeBankAccount() {}

    public PayeeBankAccount(String tenantId, Payee payee, String accountNumber, String bankName) {
        this.tenantId = tenantId;
        this.payee = payee;
        this.accountNumber = accountNumber;
        this.bankName = bankName;
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

    public Payee getPayee() { return payee; }
    public void setPayee(Payee payee) { this.payee = payee; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public String getIfscCode() { return ifscCode; }
    public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }

    public BankAccountType getBankAccountType() { return bankAccountType; }
    public void setBankAccountType(BankAccountType bankAccountType) { this.bankAccountType = bankAccountType; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
