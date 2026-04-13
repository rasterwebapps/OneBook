package com.nexus.onebook.ledger.intelligence.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import com.nexus.onebook.ledger.accounts.model.LedgerAccount;

@Entity
@Table(name = "investment_holdings")
public class InvestmentHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    @Column(name = "holding_name", nullable = false, length = 255)
    private String holdingName;

    @Enumerated(EnumType.STRING)
    @Column(name = "holding_type", nullable = false, length = 30)
    private HoldingType holdingType;

    @Column(name = "quantity", precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "cost_basis", precision = 19, scale = 4)
    private BigDecimal costBasis;

    @Column(name = "current_market_price", precision = 19, scale = 4)
    private BigDecimal currentMarketPrice;

    @Column(name = "market_value", precision = 19, scale = 4)
    private BigDecimal marketValue;

    @Column(name = "unrealized_gain_loss", precision = 19, scale = 4)
    private BigDecimal unrealizedGainLoss;

    @Column(name = "last_valuation_date")
    private LocalDate lastValuationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ledger_account_id")
    private LedgerAccount ledgerAccount;

    @Column(name = "metadata", columnDefinition = "text")
    private String metadata = "{}";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public InvestmentHolding() {}

    public InvestmentHolding(String tenantId, String symbol, String holdingName,
                             HoldingType holdingType, BigDecimal quantity, BigDecimal costBasis) {
        this.tenantId = tenantId;
        this.symbol = symbol;
        this.holdingName = holdingName;
        this.holdingType = holdingType;
        this.quantity = quantity;
        this.costBasis = costBasis;
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

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getHoldingName() { return holdingName; }
    public void setHoldingName(String holdingName) { this.holdingName = holdingName; }

    public HoldingType getHoldingType() { return holdingType; }
    public void setHoldingType(HoldingType holdingType) { this.holdingType = holdingType; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getCostBasis() { return costBasis; }
    public void setCostBasis(BigDecimal costBasis) { this.costBasis = costBasis; }

    public BigDecimal getCurrentMarketPrice() { return currentMarketPrice; }
    public void setCurrentMarketPrice(BigDecimal currentMarketPrice) { this.currentMarketPrice = currentMarketPrice; }

    public BigDecimal getMarketValue() { return marketValue; }
    public void setMarketValue(BigDecimal marketValue) { this.marketValue = marketValue; }

    public BigDecimal getUnrealizedGainLoss() { return unrealizedGainLoss; }
    public void setUnrealizedGainLoss(BigDecimal unrealizedGainLoss) { this.unrealizedGainLoss = unrealizedGainLoss; }

    public LocalDate getLastValuationDate() { return lastValuationDate; }
    public void setLastValuationDate(LocalDate lastValuationDate) { this.lastValuationDate = lastValuationDate; }

    public LedgerAccount getLedgerAccount() { return ledgerAccount; }
    public void setLedgerAccount(LedgerAccount ledgerAccount) { this.ledgerAccount = ledgerAccount; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
