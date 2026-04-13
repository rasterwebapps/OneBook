package com.nexus.onebook.ledger.foundation.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "advances")
public class Advance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "advance_number", nullable = false, length = 50)
    private String advanceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id")
    private Payer payer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payee_id")
    private Payee payee;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "settled_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal settledAmount = BigDecimal.ZERO;

    @Column(name = "unsettled_amount", precision = 19, scale = 4)
    private BigDecimal unsettledAmount;

    @Column(name = "description")
    private String description;

    @Column(name = "advance_date")
    private Instant advanceDate;

    @Column(name = "is_settled", nullable = false)
    private boolean settled = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Advance() {}

    public Advance(String tenantId, String advanceNumber, BigDecimal amount) {
        this.tenantId = tenantId;
        this.advanceNumber = advanceNumber;
        this.amount = amount;
        this.unsettledAmount = amount;
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

    public String getAdvanceNumber() { return advanceNumber; }
    public void setAdvanceNumber(String advanceNumber) { this.advanceNumber = advanceNumber; }

    public Payer getPayer() { return payer; }
    public void setPayer(Payer payer) { this.payer = payer; }

    public Payee getPayee() { return payee; }
    public void setPayee(Payee payee) { this.payee = payee; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getSettledAmount() { return settledAmount; }
    public void setSettledAmount(BigDecimal settledAmount) { this.settledAmount = settledAmount; }

    public BigDecimal getUnsettledAmount() { return unsettledAmount; }
    public void setUnsettledAmount(BigDecimal unsettledAmount) { this.unsettledAmount = unsettledAmount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getAdvanceDate() { return advanceDate; }
    public void setAdvanceDate(Instant advanceDate) { this.advanceDate = advanceDate; }

    public boolean isSettled() { return settled; }
    public void setSettled(boolean settled) { this.settled = settled; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
