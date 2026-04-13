package com.nexus.onebook.ledger.banking.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import com.nexus.onebook.ledger.accounts.model.JournalEntry;
import com.nexus.onebook.ledger.accounts.model.LedgerAccount;

@Entity
@Table(name = "bank_feed_transactions")
public class BankFeedTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private LedgerAccount bankAccount;

    @Column(name = "external_transaction_id", nullable = false, length = 100)
    private String externalTransactionId;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "description")
    private String description;

    @Column(name = "matched", nullable = false)
    private boolean matched = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_journal_entry_id")
    private JournalEntry matchedJournalEntry;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 20)
    private BankFeedSource source = BankFeedSource.MANUAL;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata = "{}";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public BankFeedTransaction() {}

    public BankFeedTransaction(String tenantId, LedgerAccount bankAccount, String externalTransactionId,
                               LocalDate transactionDate, BigDecimal amount) {
        this.tenantId = tenantId;
        this.bankAccount = bankAccount;
        this.externalTransactionId = externalTransactionId;
        this.transactionDate = transactionDate;
        this.amount = amount;
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

    public LedgerAccount getBankAccount() { return bankAccount; }
    public void setBankAccount(LedgerAccount bankAccount) { this.bankAccount = bankAccount; }

    public String getExternalTransactionId() { return externalTransactionId; }
    public void setExternalTransactionId(String externalTransactionId) { this.externalTransactionId = externalTransactionId; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isMatched() { return matched; }
    public void setMatched(boolean matched) { this.matched = matched; }

    public JournalEntry getMatchedJournalEntry() { return matchedJournalEntry; }
    public void setMatchedJournalEntry(JournalEntry matchedJournalEntry) { this.matchedJournalEntry = matchedJournalEntry; }

    public BankFeedSource getSource() { return source; }
    public void setSource(BankFeedSource source) { this.source = source; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public Instant getCreatedAt() { return createdAt; }
}
