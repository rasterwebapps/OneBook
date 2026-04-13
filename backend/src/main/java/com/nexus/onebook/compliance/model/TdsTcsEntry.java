package com.nexus.onebook.compliance.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import com.nexus.onebook.accounts.model.JournalTransaction;

@Entity
@Table(name = "tds_tcs_entries")
public class TdsTcsEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 3)
    private TdsTcsType entryType;

    @Column(name = "section_code", nullable = false, length = 20)
    private String sectionCode;

    @Column(name = "party_name", nullable = false, length = 255)
    private String partyName;

    @Column(name = "party_pan", length = 20)
    private String partyPan;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "taxable_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal taxableAmount;

    @Column(name = "tax_rate", nullable = false, precision = 8, scale = 4)
    private BigDecimal taxRate;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal taxAmount;

    @Column(name = "certificate_number", length = 100)
    private String certificateNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TdsTcsStatus status = TdsTcsStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_transaction_id")
    private JournalTransaction journalTransaction;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public TdsTcsEntry() {}

    public TdsTcsEntry(String tenantId, TdsTcsType entryType, String sectionCode,
                       String partyName, LocalDate transactionDate,
                       BigDecimal taxableAmount, BigDecimal taxRate, BigDecimal taxAmount) {
        this.tenantId = tenantId;
        this.entryType = entryType;
        this.sectionCode = sectionCode;
        this.partyName = partyName;
        this.transactionDate = transactionDate;
        this.taxableAmount = taxableAmount;
        this.taxRate = taxRate;
        this.taxAmount = taxAmount;
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

    public TdsTcsType getEntryType() { return entryType; }
    public void setEntryType(TdsTcsType entryType) { this.entryType = entryType; }

    public String getSectionCode() { return sectionCode; }
    public void setSectionCode(String sectionCode) { this.sectionCode = sectionCode; }

    public String getPartyName() { return partyName; }
    public void setPartyName(String partyName) { this.partyName = partyName; }

    public String getPartyPan() { return partyPan; }
    public void setPartyPan(String partyPan) { this.partyPan = partyPan; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public BigDecimal getTaxableAmount() { return taxableAmount; }
    public void setTaxableAmount(BigDecimal taxableAmount) { this.taxableAmount = taxableAmount; }

    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public String getCertificateNumber() { return certificateNumber; }
    public void setCertificateNumber(String certificateNumber) { this.certificateNumber = certificateNumber; }

    public TdsTcsStatus getStatus() { return status; }
    public void setStatus(TdsTcsStatus status) { this.status = status; }

    public JournalTransaction getJournalTransaction() { return journalTransaction; }
    public void setJournalTransaction(JournalTransaction journalTransaction) { this.journalTransaction = journalTransaction; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
