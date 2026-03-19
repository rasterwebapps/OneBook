package com.nexus.onebook.ledger.payment.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payment_batches")
public class PaymentBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "vendor_account_id")
    private Long vendorAccountId;

    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "total_purchases", precision = 19, scale = 4)
    private BigDecimal totalPurchases;

    @Column(name = "total_returns", precision = 19, scale = 4)
    private BigDecimal totalReturns;

    @Column(name = "total_credit_notes", precision = 19, scale = 4)
    private BigDecimal totalCreditNotes;

    @Column(name = "net_payable", precision = 19, scale = 4)
    private BigDecimal netPayable;

    @Column(name = "bank_account_id")
    private Long bankAccountId;

    @Column(name = "payment_mode")
    private String paymentMode;

    @Enumerated(EnumType.STRING)
    private PaymentBatchStatus status;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "rejected_by")
    private String rejectedBy;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "payment_journal_id")
    private Long paymentJournalId;

    @Column(name = "payment_file_generated")
    private boolean paymentFileGenerated = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PaymentBatchItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public Long getVendorAccountId() { return vendorAccountId; }
    public void setVendorAccountId(Long vendorAccountId) { this.vendorAccountId = vendorAccountId; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public BigDecimal getTotalPurchases() { return totalPurchases; }
    public void setTotalPurchases(BigDecimal totalPurchases) { this.totalPurchases = totalPurchases; }

    public BigDecimal getTotalReturns() { return totalReturns; }
    public void setTotalReturns(BigDecimal totalReturns) { this.totalReturns = totalReturns; }

    public BigDecimal getTotalCreditNotes() { return totalCreditNotes; }
    public void setTotalCreditNotes(BigDecimal totalCreditNotes) { this.totalCreditNotes = totalCreditNotes; }

    public BigDecimal getNetPayable() { return netPayable; }
    public void setNetPayable(BigDecimal netPayable) { this.netPayable = netPayable; }

    public Long getBankAccountId() { return bankAccountId; }
    public void setBankAccountId(Long bankAccountId) { this.bankAccountId = bankAccountId; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public PaymentBatchStatus getStatus() { return status; }
    public void setStatus(PaymentBatchStatus status) { this.status = status; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }

    public String getRejectedBy() { return rejectedBy; }
    public void setRejectedBy(String rejectedBy) { this.rejectedBy = rejectedBy; }

    public Instant getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(Instant rejectedAt) { this.rejectedAt = rejectedAt; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public Long getPaymentJournalId() { return paymentJournalId; }
    public void setPaymentJournalId(Long paymentJournalId) { this.paymentJournalId = paymentJournalId; }

    public boolean isPaymentFileGenerated() { return paymentFileGenerated; }
    public void setPaymentFileGenerated(boolean paymentFileGenerated) { this.paymentFileGenerated = paymentFileGenerated; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public List<PaymentBatchItem> getItems() { return items; }
    public void setItems(List<PaymentBatchItem> items) { this.items = items; }
}
