package com.nexus.onebook.banking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import com.nexus.onebook.accounts.model.JournalTransaction;
import com.nexus.onebook.accounts.model.LedgerAccount;

@Entity
@Table(name = "cheque_entries")
public class ChequeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "cheque_number", nullable = false, length = 50)
    private String chequeNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private LedgerAccount bankAccount;

    @Column(name = "party_name", nullable = false, length = 255)
    private String partyName;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "cheque_date", nullable = false)
    private LocalDate chequeDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ChequeStatus status = ChequeStatus.ISSUED;

    @Enumerated(EnumType.STRING)
    @Column(name = "cheque_type", nullable = false, length = 20)
    private ChequeType chequeType = ChequeType.PAYMENT;

    @Column(name = "clearing_date")
    private LocalDate clearingDate;

    @Column(name = "bounce_reason", length = 255)
    private String bounceReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_transaction_id")
    private JournalTransaction journalTransaction;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ChequeEntry() {}

    public ChequeEntry(String tenantId, String chequeNumber, LedgerAccount bankAccount,
                       String partyName, BigDecimal amount, LocalDate chequeDate, ChequeType chequeType) {
        this.tenantId = tenantId;
        this.chequeNumber = chequeNumber;
        this.bankAccount = bankAccount;
        this.partyName = partyName;
        this.amount = amount;
        this.chequeDate = chequeDate;
        this.chequeType = chequeType;
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

    public String getChequeNumber() { return chequeNumber; }
    public void setChequeNumber(String chequeNumber) { this.chequeNumber = chequeNumber; }

    public LedgerAccount getBankAccount() { return bankAccount; }
    public void setBankAccount(LedgerAccount bankAccount) { this.bankAccount = bankAccount; }

    public String getPartyName() { return partyName; }
    public void setPartyName(String partyName) { this.partyName = partyName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getChequeDate() { return chequeDate; }
    public void setChequeDate(LocalDate chequeDate) { this.chequeDate = chequeDate; }

    public ChequeStatus getStatus() { return status; }
    public void setStatus(ChequeStatus status) { this.status = status; }

    public ChequeType getChequeType() { return chequeType; }
    public void setChequeType(ChequeType chequeType) { this.chequeType = chequeType; }

    public LocalDate getClearingDate() { return clearingDate; }
    public void setClearingDate(LocalDate clearingDate) { this.clearingDate = clearingDate; }

    public String getBounceReason() { return bounceReason; }
    public void setBounceReason(String bounceReason) { this.bounceReason = bounceReason; }

    public JournalTransaction getJournalTransaction() { return journalTransaction; }
    public void setJournalTransaction(JournalTransaction journalTransaction) { this.journalTransaction = journalTransaction; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
