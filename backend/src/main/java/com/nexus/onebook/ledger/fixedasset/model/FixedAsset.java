package com.nexus.onebook.ledger.fixedasset.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import com.nexus.onebook.ledger.accounts.model.Branch;
import com.nexus.onebook.ledger.accounts.model.LedgerAccount;

@Entity
@Table(name = "fixed_assets")
public class FixedAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "asset_code", nullable = false, length = 50)
    private String assetCode;

    @Column(name = "asset_name", nullable = false, length = 255)
    private String assetName;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_account_id", nullable = false)
    private LedgerAccount assetAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depreciation_account_id", nullable = false)
    private LedgerAccount depreciationAccount;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "purchase_cost", nullable = false, precision = 19, scale = 4)
    private BigDecimal purchaseCost;

    @Column(name = "salvage_value", precision = 19, scale = 4)
    private BigDecimal salvageValue = BigDecimal.ZERO;

    @Column(name = "useful_life_months", nullable = false)
    private int usefulLifeMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "depreciation_method", length = 30)
    private DepreciationMethod depreciationMethod = DepreciationMethod.STRAIGHT_LINE;

    @Column(name = "accumulated_depreciation", precision = 19, scale = 4)
    private BigDecimal accumulatedDepreciation = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private AssetStatus status = AssetStatus.ACTIVE;

    @Column(name = "disposal_date")
    private LocalDate disposalDate;

    @Column(name = "disposal_amount", precision = 19, scale = 4)
    private BigDecimal disposalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata = "{}";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public FixedAsset() {}

    public FixedAsset(String tenantId, String assetCode, String assetName,
                      LedgerAccount assetAccount, LedgerAccount depreciationAccount,
                      LocalDate purchaseDate, BigDecimal purchaseCost, int usefulLifeMonths) {
        this.tenantId = tenantId;
        this.assetCode = assetCode;
        this.assetName = assetName;
        this.assetAccount = assetAccount;
        this.depreciationAccount = depreciationAccount;
        this.purchaseDate = purchaseDate;
        this.purchaseCost = purchaseCost;
        this.usefulLifeMonths = usefulLifeMonths;
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

    public String getAssetCode() { return assetCode; }
    public void setAssetCode(String assetCode) { this.assetCode = assetCode; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LedgerAccount getAssetAccount() { return assetAccount; }
    public void setAssetAccount(LedgerAccount assetAccount) { this.assetAccount = assetAccount; }

    public LedgerAccount getDepreciationAccount() { return depreciationAccount; }
    public void setDepreciationAccount(LedgerAccount depreciationAccount) { this.depreciationAccount = depreciationAccount; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

    public BigDecimal getPurchaseCost() { return purchaseCost; }
    public void setPurchaseCost(BigDecimal purchaseCost) { this.purchaseCost = purchaseCost; }

    public BigDecimal getSalvageValue() { return salvageValue; }
    public void setSalvageValue(BigDecimal salvageValue) { this.salvageValue = salvageValue; }

    public int getUsefulLifeMonths() { return usefulLifeMonths; }
    public void setUsefulLifeMonths(int usefulLifeMonths) { this.usefulLifeMonths = usefulLifeMonths; }

    public DepreciationMethod getDepreciationMethod() { return depreciationMethod; }
    public void setDepreciationMethod(DepreciationMethod depreciationMethod) { this.depreciationMethod = depreciationMethod; }

    public BigDecimal getAccumulatedDepreciation() { return accumulatedDepreciation; }
    public void setAccumulatedDepreciation(BigDecimal accumulatedDepreciation) { this.accumulatedDepreciation = accumulatedDepreciation; }

    public AssetStatus getStatus() { return status; }
    public void setStatus(AssetStatus status) { this.status = status; }

    public LocalDate getDisposalDate() { return disposalDate; }
    public void setDisposalDate(LocalDate disposalDate) { this.disposalDate = disposalDate; }

    public BigDecimal getDisposalAmount() { return disposalAmount; }
    public void setDisposalAmount(BigDecimal disposalAmount) { this.disposalAmount = disposalAmount; }

    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
