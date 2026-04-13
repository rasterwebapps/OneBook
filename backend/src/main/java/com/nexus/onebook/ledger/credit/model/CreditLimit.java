package com.nexus.onebook.ledger.credit.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import com.nexus.onebook.ledger.accounts.model.LedgerAccount;

@Entity
@Table(name = "credit_limits")
public class CreditLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private LedgerAccount account;

    @Column(name = "credit_limit", nullable = false, precision = 19, scale = 4)
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Column(name = "current_outstanding", nullable = false, precision = 19, scale = 4)
    private BigDecimal currentOutstanding = BigDecimal.ZERO;

    @Column(name = "credit_period_days", nullable = false)
    private int creditPeriodDays = 30;

    @Column(name = "is_blocked", nullable = false)
    private boolean blocked = false;

    @Column(name = "last_reviewed_at")
    private Instant lastReviewedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public CreditLimit() {}

    public CreditLimit(String tenantId, LedgerAccount account, BigDecimal creditLimit, int creditPeriodDays) {
        this.tenantId = tenantId;
        this.account = account;
        this.creditLimit = creditLimit;
        this.creditPeriodDays = creditPeriodDays;
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

    public LedgerAccount getAccount() { return account; }
    public void setAccount(LedgerAccount account) { this.account = account; }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    public BigDecimal getCurrentOutstanding() { return currentOutstanding; }
    public void setCurrentOutstanding(BigDecimal currentOutstanding) { this.currentOutstanding = currentOutstanding; }

    public int getCreditPeriodDays() { return creditPeriodDays; }
    public void setCreditPeriodDays(int creditPeriodDays) { this.creditPeriodDays = creditPeriodDays; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public Instant getLastReviewedAt() { return lastReviewedAt; }
    public void setLastReviewedAt(Instant lastReviewedAt) { this.lastReviewedAt = lastReviewedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
