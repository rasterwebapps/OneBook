package com.nexus.onebook.ledger.reporting.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import com.nexus.onebook.ledger.accounts.model.Branch;
import com.nexus.onebook.ledger.accounts.model.JournalTransaction;

@Entity
@Table(name = "intercompany_eliminations")
public class IntercompanyElimination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_branch_id", nullable = false)
    private Branch sourceBranch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_branch_id", nullable = false)
    private Branch targetBranch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_transaction_id", nullable = false)
    private JournalTransaction journalTransaction;

    @Column(name = "elimination_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal eliminationAmount;

    @Column(name = "eliminated", nullable = false)
    private boolean eliminated = false;

    @Column(name = "elimination_date")
    private LocalDate eliminationDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata = "{}";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public IntercompanyElimination() {}

    public IntercompanyElimination(String tenantId, Branch sourceBranch, Branch targetBranch,
                                   JournalTransaction journalTransaction, BigDecimal eliminationAmount) {
        this.tenantId = tenantId;
        this.sourceBranch = sourceBranch;
        this.targetBranch = targetBranch;
        this.journalTransaction = journalTransaction;
        this.eliminationAmount = eliminationAmount;
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

    public Branch getSourceBranch() { return sourceBranch; }
    public void setSourceBranch(Branch sourceBranch) { this.sourceBranch = sourceBranch; }

    public Branch getTargetBranch() { return targetBranch; }
    public void setTargetBranch(Branch targetBranch) { this.targetBranch = targetBranch; }

    public JournalTransaction getJournalTransaction() { return journalTransaction; }
    public void setJournalTransaction(JournalTransaction journalTransaction) { this.journalTransaction = journalTransaction; }

    public BigDecimal getEliminationAmount() { return eliminationAmount; }
    public void setEliminationAmount(BigDecimal eliminationAmount) { this.eliminationAmount = eliminationAmount; }

    public boolean isEliminated() { return eliminated; }
    public void setEliminated(boolean eliminated) { this.eliminated = eliminated; }

    public LocalDate getEliminationDate() { return eliminationDate; }
    public void setEliminationDate(LocalDate eliminationDate) { this.eliminationDate = eliminationDate; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public Instant getCreatedAt() { return createdAt; }
}
