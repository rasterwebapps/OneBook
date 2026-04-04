package com.nexus.onebook.ledger.voucher.model;

import com.nexus.onebook.ledger.model.Advance;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "advance_voucher_item_settlements")
public class AdvanceVoucherItemSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advance_id", nullable = false)
    private Advance advance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_item_id", nullable = false)
    private VoucherItem voucherItem;

    @Column(name = "settled_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal settledAmount;

    @Column(name = "settlement_date", nullable = false)
    private Instant settlementDate;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public AdvanceVoucherItemSettlement() {}

    public AdvanceVoucherItemSettlement(String tenantId, Advance advance, VoucherItem voucherItem,
                                        BigDecimal settledAmount, Instant settlementDate) {
        this.tenantId = tenantId;
        this.advance = advance;
        this.voucherItem = voucherItem;
        this.settledAmount = settledAmount;
        this.settlementDate = settlementDate;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public Advance getAdvance() { return advance; }
    public void setAdvance(Advance advance) { this.advance = advance; }

    public VoucherItem getVoucherItem() { return voucherItem; }
    public void setVoucherItem(VoucherItem voucherItem) { this.voucherItem = voucherItem; }

    public BigDecimal getSettledAmount() { return settledAmount; }
    public void setSettledAmount(BigDecimal settledAmount) { this.settledAmount = settledAmount; }

    public Instant getSettlementDate() { return settlementDate; }
    public void setSettlementDate(Instant settlementDate) { this.settlementDate = settlementDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Instant getCreatedAt() { return createdAt; }
}
