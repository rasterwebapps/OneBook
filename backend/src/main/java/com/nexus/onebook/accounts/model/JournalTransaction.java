package com.nexus.onebook.accounts.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Transaction header that groups related journal entries via a unique
 * transaction_uuid. No journal entry can exist without referencing
 * a JournalTransaction (FK constraint prevents orphan entries).
 */
@Entity
@Table(name = "journal_transactions")
public class JournalTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "transaction_uuid", nullable = false, unique = true)
    private UUID transactionUuid;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "description")
    private String description;

    @Column(name = "description_encrypted")
    private String descriptionEncrypted;

    @Column(name = "description_blind_index", length = 64)
    private String descriptionBlindIndex;

    @Column(name = "posted", nullable = false)
    private boolean posted = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata = "{}";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @JsonManagedReference
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JournalEntry> entries = new ArrayList<>();

    public JournalTransaction() {
    }

    public JournalTransaction(String tenantId, LocalDate transactionDate, String description) {
        this.tenantId = tenantId;
        this.transactionUuid = UUID.randomUUID();
        this.transactionDate = transactionDate;
        this.description = description;
    }

    @PrePersist
    protected void onCreate() {
        if (transactionUuid == null) {
            transactionUuid = UUID.randomUUID();
        }
        createdAt = Instant.now();
    }

    public void addEntry(JournalEntry entry) {
        entries.add(entry);
        entry.setTransaction(this);
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getTransactionUuid() {
        return transactionUuid;
    }

    public void setTransactionUuid(UUID transactionUuid) {
        this.transactionUuid = transactionUuid;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionEncrypted() {
        return descriptionEncrypted;
    }

    public void setDescriptionEncrypted(String descriptionEncrypted) {
        this.descriptionEncrypted = descriptionEncrypted;
    }

    public String getDescriptionBlindIndex() {
        return descriptionBlindIndex;
    }

    public void setDescriptionBlindIndex(String descriptionBlindIndex) {
        this.descriptionBlindIndex = descriptionBlindIndex;
    }

    public boolean isPosted() {
        return posted;
    }

    public void setPosted(boolean posted) {
        this.posted = posted;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<JournalEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<JournalEntry> entries) {
        this.entries.clear();
        if (entries != null) {
            for (JournalEntry entry : entries) {
                entry.setTransaction(this);
                this.entries.add(entry);
            }
        }
    }
}
