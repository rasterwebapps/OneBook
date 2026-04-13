package com.nexus.onebook.ingestion.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Purchase Order document used in 3-way matching.
 * Represents an order placed with a vendor for goods or services.
 */
@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "po_number", nullable = false, length = 50)
    private String poNumber;

    @Column(name = "vendor_name", nullable = false)
    private String vendorName;

    @Column(name = "description")
    private String description;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "line_items", columnDefinition = "text")
    private String lineItems = "[]";

    @Column(name = "metadata", columnDefinition = "text")
    private String metadata = "{}";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public PurchaseOrder() {}

    public PurchaseOrder(String tenantId, String poNumber, String vendorName,
                         BigDecimal totalAmount, LocalDate orderDate) {
        this.tenantId = tenantId;
        this.poNumber = poNumber;
        this.vendorName = vendorName;
        this.totalAmount = totalAmount;
        this.orderDate = orderDate;
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

    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public String getLineItems() { return lineItems; }
    public void setLineItems(String lineItems) { this.lineItems = lineItems; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public Instant getCreatedAt() { return createdAt; }
}
