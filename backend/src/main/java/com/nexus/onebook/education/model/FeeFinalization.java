package com.nexus.onebook.education.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents the finalized fee agreement for a student enquiry.
 * Captures any discount applied and the final payable amount.
 * One-to-one with StudentEnquiry.
 */
@Entity
@Table(name = "fee_finalizations")
public class FeeFinalization {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enquiry_id", nullable = false, unique = true)
    private StudentEnquiry enquiry;

    @Column(name = "generic_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal genericTotal;

    @Column(name = "additional_fee", nullable = false, precision = 15, scale = 2)
    private BigDecimal additionalFee;

    @Column(name = "discount_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "final_payable", nullable = false, precision = 15, scale = 2)
    private BigDecimal finalPayable;

    @Column(name = "finalized_by", nullable = false)
    private String finalizedBy;

    @Column(name = "finalized_at", nullable = false)
    private Instant finalizedAt;

    public FeeFinalization() {}

    public FeeFinalization(String tenantId, StudentEnquiry enquiry, BigDecimal genericTotal,
                           BigDecimal additionalFee, BigDecimal discountAmount,
                           BigDecimal finalPayable, String finalizedBy) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.enquiry = enquiry;
        this.genericTotal = genericTotal;
        this.additionalFee = additionalFee;
        this.discountAmount = discountAmount;
        this.finalPayable = finalPayable;
        this.finalizedBy = finalizedBy;
        this.finalizedAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (finalizedAt == null) {
            finalizedAt = Instant.now();
        }
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public StudentEnquiry getEnquiry() { return enquiry; }
    public void setEnquiry(StudentEnquiry enquiry) { this.enquiry = enquiry; }

    public BigDecimal getGenericTotal() { return genericTotal; }
    public void setGenericTotal(BigDecimal genericTotal) { this.genericTotal = genericTotal; }

    public BigDecimal getAdditionalFee() { return additionalFee; }
    public void setAdditionalFee(BigDecimal additionalFee) { this.additionalFee = additionalFee; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getFinalPayable() { return finalPayable; }
    public void setFinalPayable(BigDecimal finalPayable) { this.finalPayable = finalPayable; }

    public String getFinalizedBy() { return finalizedBy; }
    public void setFinalizedBy(String finalizedBy) { this.finalizedBy = finalizedBy; }

    public Instant getFinalizedAt() { return finalizedAt; }
    public void setFinalizedAt(Instant finalizedAt) { this.finalizedAt = finalizedAt; }
}
