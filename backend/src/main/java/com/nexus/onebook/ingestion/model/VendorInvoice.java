package com.nexus.onebook.ingestion.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Vendor Invoice document used in 3-way matching.
 * The invoice is matched against PO and Goods Receipt before payment is authorised.
 */
@Entity
@Table(name = "vendor_invoices")
public class VendorInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "invoice_number", nullable = false, length = 50)
    private String invoiceNumber;

    @Column(name = "po_number", nullable = false, length = 50)
    private String poNumber;

    @Column(name = "vendor_name", nullable = false)
    private String vendorName;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "line_items", columnDefinition = "text")
    private String lineItems = "[]";

    @Column(name = "ocr_extracted", nullable = false)
    private boolean ocrExtracted = false;

    @Column(name = "metadata", columnDefinition = "text")
    private String metadata = "{}";

    @Enumerated(EnumType.STRING)
    @Column(name = "match_status", nullable = false, length = 20)
    private MatchStatus matchStatus = MatchStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public VendorInvoice() {}

    public VendorInvoice(String tenantId, String invoiceNumber, String poNumber,
                         String vendorName, BigDecimal totalAmount, LocalDate invoiceDate) {
        this.tenantId = tenantId;
        this.invoiceNumber = invoiceNumber;
        this.poNumber = poNumber;
        this.vendorName = vendorName;
        this.totalAmount = totalAmount;
        this.invoiceDate = invoiceDate;
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

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }

    public String getLineItems() { return lineItems; }
    public void setLineItems(String lineItems) { this.lineItems = lineItems; }

    public boolean isOcrExtracted() { return ocrExtracted; }
    public void setOcrExtracted(boolean ocrExtracted) { this.ocrExtracted = ocrExtracted; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public MatchStatus getMatchStatus() { return matchStatus; }
    public void setMatchStatus(MatchStatus matchStatus) { this.matchStatus = matchStatus; }

    public Instant getCreatedAt() { return createdAt; }
}
