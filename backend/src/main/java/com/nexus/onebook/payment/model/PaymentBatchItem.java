package com.nexus.onebook.payment.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment_batch_items")
public class PaymentBatchItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", insertable = false, updatable = false)
    @JsonIgnore
    private PaymentBatch batch;

    @Column(name = "batch_id")
    private Long batchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "register_entry_id")
    private PaymentRegisterEntry registerEntry;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public PaymentBatch getBatch() { return batch; }
    public void setBatch(PaymentBatch batch) {
        this.batch = batch;
        this.batchId = batch != null ? batch.getId() : null;
    }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public PaymentRegisterEntry getRegisterEntry() { return registerEntry; }
    public void setRegisterEntry(PaymentRegisterEntry registerEntry) { this.registerEntry = registerEntry; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Instant getCreatedAt() { return createdAt; }
}
