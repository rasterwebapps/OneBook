package com.nexus.onebook.accounts.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * A single debit or credit line within a journal transaction.
 * Amount is stored as DECIMAL(19,4) for precision.
 * The FK to journal_transactions prevents orphan entries.
 * The JSONB metadata column stores industry-specific tags (Patient ID, VIN, etc.).
 */
@Entity
@Table(name = "journal_entries")
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private JournalTransaction transaction;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private LedgerAccount account;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 6)
    private EntryType entryType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "description")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata = "{}";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public JournalEntry() {}

    public JournalEntry(String tenantId, LedgerAccount account,
                        EntryType entryType, BigDecimal amount, String description) {
        this.tenantId = tenantId;
        this.account = account;
        this.entryType = entryType;
        this.amount = amount;
        this.description = description;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public JournalTransaction getTransaction() { return transaction; }
    public void setTransaction(JournalTransaction transaction) { this.transaction = transaction; }

    public LedgerAccount getAccount() { return account; }
    public void setAccount(LedgerAccount account) { this.account = account; }

    public EntryType getEntryType() { return entryType; }
    public void setEntryType(EntryType entryType) { this.entryType = entryType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public Instant getCreatedAt() { return createdAt; }
}
