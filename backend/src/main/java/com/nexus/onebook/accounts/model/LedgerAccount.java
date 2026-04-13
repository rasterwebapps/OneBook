package com.nexus.onebook.accounts.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;

/**
 * Chart of Accounts entry. Each account belongs to a Cost Center
 * and has a type (ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE).
 * Supports hierarchical parent-child relationships via parent_account_id.
 * The JSONB metadata column stores industry-specific tags (Patient ID, VIN, etc.).
 */
@Entity
@Table(name = "ledger_accounts")
public class LedgerAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cost_center_id", nullable = false)
    private CostCenter costCenter;

    @Column(name = "account_code", nullable = false, length = 50)
    private String accountCode;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Column(name = "account_name_encrypted")
    private String accountNameEncrypted;

    @Column(name = "account_name_blind_index", length = 64)
    private String accountNameBlindIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_account_id")
    private LedgerAccount parentAccount;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata = "{}";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public LedgerAccount() {}

    public LedgerAccount(String tenantId, CostCenter costCenter, String accountCode,
                         String accountName, AccountType accountType) {
        this.tenantId = tenantId;
        this.costCenter = costCenter;
        this.accountCode = accountCode;
        this.accountName = accountName;
        this.accountType = accountType;
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

    public CostCenter getCostCenter() { return costCenter; }
    public void setCostCenter(CostCenter costCenter) { this.costCenter = costCenter; }

    public String getAccountCode() { return accountCode; }
    public void setAccountCode(String accountCode) { this.accountCode = accountCode; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getAccountNameEncrypted() { return accountNameEncrypted; }
    public void setAccountNameEncrypted(String accountNameEncrypted) { this.accountNameEncrypted = accountNameEncrypted; }

    public String getAccountNameBlindIndex() { return accountNameBlindIndex; }
    public void setAccountNameBlindIndex(String accountNameBlindIndex) { this.accountNameBlindIndex = accountNameBlindIndex; }

    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }

    public LedgerAccount getParentAccount() { return parentAccount; }
    public void setParentAccount(LedgerAccount parentAccount) { this.parentAccount = parentAccount; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
