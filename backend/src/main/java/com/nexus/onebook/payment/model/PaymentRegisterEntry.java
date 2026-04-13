package com.nexus.onebook.payment.model;

import com.nexus.onebook.ingestion.model.AdapterType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Unified payment register entry that tracks a payment request from ingestion
 * through processing, approval, and payment.
 * Lifecycle: RECEIVED → VALIDATED → AVAILABLE_FOR_PROCESSING → IN_BATCH → APPROVED → POSTED → PAYMENT_GENERATED → PAID
 */
@Entity
@Table(name = "payment_register")
public class PaymentRegisterEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    // --- Ingestion fields ---

    @Column(name = "event_uuid", nullable = false, unique = true)
    private UUID eventUuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "adapter_type", length = 30)
    private AdapterType adapterType;

    @Column(name = "event_type", length = 100)
    private String eventType;

    @Column(name = "description")
    private String description;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "source_reference", length = 255)
    private String sourceReference;

    @Column(name = "debit_account_code", length = 50)
    private String debitAccountCode;

    @Column(name = "credit_account_code", length = 50)
    private String creditAccountCode;

    @Column(name = "raw_payload", columnDefinition = "text")
    private String rawPayload;

    @Column(name = "industry_tags", columnDefinition = "text")
    private String industryTags = "{}";

    @Column(name = "error_message")
    private String errorMessage;

    // --- Payment / vendor fields ---

    @Column(name = "vendor_account_id")
    private Long vendorAccountId;

    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "source_reference_id")
    private String sourceReferenceId;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(length = 3)
    private String currency;

    @Column(name = "payment_mode")
    private String paymentMode;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "bank_ifsc_code")
    private String bankIfscCode;

    @Column(name = "bank_name")
    private String bankName;

    @Enumerated(EnumType.STRING)
    private PaymentRegisterStatus status;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public PaymentRegisterEntry() {}

    /** Convenience constructor matching the old FinancialEvent signature. */
    public PaymentRegisterEntry(String tenantId, AdapterType adapterType, String eventType) {
        this.tenantId = tenantId;
        this.eventUuid = UUID.randomUUID();
        this.adapterType = adapterType;
        this.eventType = eventType;
    }

    @PrePersist
    protected void onCreate() {
        if (eventUuid == null) { eventUuid = UUID.randomUUID(); }
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getEventUuid() { return eventUuid; }
    public void setEventUuid(UUID eventUuid) { this.eventUuid = eventUuid; }

    public AdapterType getAdapterType() { return adapterType; }
    public void setAdapterType(AdapterType adapterType) { this.adapterType = adapterType; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public String getSourceReference() { return sourceReference; }
    public void setSourceReference(String sourceReference) { this.sourceReference = sourceReference; }

    public String getDebitAccountCode() { return debitAccountCode; }
    public void setDebitAccountCode(String debitAccountCode) { this.debitAccountCode = debitAccountCode; }

    public String getCreditAccountCode() { return creditAccountCode; }
    public void setCreditAccountCode(String creditAccountCode) { this.creditAccountCode = creditAccountCode; }

    public String getRawPayload() { return rawPayload; }
    public void setRawPayload(String rawPayload) { this.rawPayload = rawPayload; }

    public String getIndustryTags() { return industryTags; }
    public void setIndustryTags(String industryTags) { this.industryTags = industryTags; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Long getVendorAccountId() { return vendorAccountId; }
    public void setVendorAccountId(Long vendorAccountId) { this.vendorAccountId = vendorAccountId; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getSourceReferenceId() { return sourceReferenceId; }
    public void setSourceReferenceId(String sourceReferenceId) { this.sourceReferenceId = sourceReferenceId; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }

    public String getBankIfscCode() { return bankIfscCode; }
    public void setBankIfscCode(String bankIfscCode) { this.bankIfscCode = bankIfscCode; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public PaymentRegisterStatus getStatus() { return status; }
    public void setStatus(PaymentRegisterStatus status) { this.status = status; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
