package com.nexus.onebook.fixedasset.service;

import com.nexus.onebook.fixedasset.dto.DepreciationSchedule;
import com.nexus.onebook.fixedasset.dto.FixedAssetRequest;
import com.nexus.onebook.accounts.model.*;
import com.nexus.onebook.auditor.model.*;
import com.nexus.onebook.banking.model.*;
import com.nexus.onebook.clientaccount.model.*;
import com.nexus.onebook.compliance.model.*;
import com.nexus.onebook.credit.model.*;
import com.nexus.onebook.currency.model.*;
import com.nexus.onebook.entitlement.model.*;
import com.nexus.onebook.fixedasset.model.*;
import com.nexus.onebook.foundation.model.*;
import com.nexus.onebook.intelligence.model.*;
import com.nexus.onebook.inventory.model.*;
import com.nexus.onebook.operations.model.*;
import com.nexus.onebook.payroll.model.*;
import com.nexus.onebook.reporting.model.*;
import com.nexus.onebook.tenant.model.*;
import com.nexus.onebook.accounts.repository.BranchRepository;
import com.nexus.onebook.fixedasset.repository.FixedAssetRepository;
import com.nexus.onebook.accounts.repository.LedgerAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class FixedAssetService {

    private final FixedAssetRepository fixedAssetRepository;
    private final LedgerAccountRepository ledgerAccountRepository;
    private final BranchRepository branchRepository;

    public FixedAssetService(FixedAssetRepository fixedAssetRepository,
                              LedgerAccountRepository ledgerAccountRepository,
                              BranchRepository branchRepository) {
        this.fixedAssetRepository = fixedAssetRepository;
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.branchRepository = branchRepository;
    }

    @Transactional
    public FixedAsset createAsset(FixedAssetRequest request) {
        // Validate uniqueness
        fixedAssetRepository.findByTenantIdAndAssetCode(request.tenantId(), request.assetCode())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Asset with code '" + request.assetCode() + "' already exists");
                });

        LedgerAccount assetAccount = ledgerAccountRepository.findById(request.assetAccountId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Asset account not found: " + request.assetAccountId()));

        LedgerAccount depreciationAccount = ledgerAccountRepository.findById(request.depreciationAccountId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Depreciation account not found: " + request.depreciationAccountId()));

        FixedAsset asset = new FixedAsset(
                request.tenantId(), request.assetCode(), request.assetName(),
                assetAccount, depreciationAccount,
                request.purchaseDate(), request.purchaseCost(), request.usefulLifeMonths());

        if (request.description() != null) {
            asset.setDescription(request.description());
        }
        if (request.salvageValue() != null) {
            asset.setSalvageValue(request.salvageValue());
        }
        if (request.depreciationMethod() != null) {
            asset.setDepreciationMethod(DepreciationMethod.valueOf(request.depreciationMethod()));
        }
        if (request.branchId() != null) {
            Branch branch = branchRepository.findById(request.branchId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Branch not found: " + request.branchId()));
            asset.setBranch(branch);
        }

        return fixedAssetRepository.save(asset);
    }

    @Transactional(readOnly = true)
    public List<FixedAsset> getAssetsByTenant(String tenantId) {
        return fixedAssetRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public FixedAsset getAsset(Long id) {
        return fixedAssetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fixed asset not found: " + id));
    }

    @Transactional(readOnly = true)
    public DepreciationSchedule computeDepreciation(Long assetId) {
        FixedAsset asset = getAsset(assetId);

        BigDecimal depreciableAmount = asset.getPurchaseCost().subtract(asset.getSalvageValue());
        BigDecimal monthlyDepreciation;

        if (asset.getDepreciationMethod() == DepreciationMethod.STRAIGHT_LINE) {
            // SLM: (Cost - Salvage) / Useful Life in months
            monthlyDepreciation = depreciableAmount.divide(
                    BigDecimal.valueOf(asset.getUsefulLifeMonths()), 4, RoundingMode.HALF_UP);
        } else {
            // WDV: Rate = 1 - (Salvage/Cost)^(1/years), then applied to NBV monthly
            BigDecimal netBookValue = asset.getPurchaseCost().subtract(asset.getAccumulatedDepreciation());
            if (netBookValue.compareTo(asset.getSalvageValue()) <= 0) {
                monthlyDepreciation = BigDecimal.ZERO;
            } else {
                double years = asset.getUsefulLifeMonths() / 12.0;
                double salvageRatio = asset.getSalvageValue().doubleValue() / asset.getPurchaseCost().doubleValue();
                double annualRate = 1 - Math.pow(Math.max(salvageRatio, 0.001), 1.0 / years);
                double monthlyRate = annualRate / 12.0;
                monthlyDepreciation = netBookValue.multiply(BigDecimal.valueOf(monthlyRate))
                        .setScale(4, RoundingMode.HALF_UP);
            }
        }

        BigDecimal netBookValue = asset.getPurchaseCost()
                .subtract(asset.getAccumulatedDepreciation());

        return new DepreciationSchedule(
                asset.getId(), asset.getAssetCode(), asset.getAssetName(),
                monthlyDepreciation, asset.getAccumulatedDepreciation(), netBookValue);
    }

    @Transactional
    public FixedAsset runMonthlyDepreciation(Long assetId) {
        FixedAsset asset = getAsset(assetId);
        if (asset.getStatus() != AssetStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot depreciate non-active asset: " + assetId);
        }

        DepreciationSchedule schedule = computeDepreciation(assetId);
        BigDecimal newAccumulated = asset.getAccumulatedDepreciation()
                .add(schedule.monthlyDepreciation());

        // Cap at depreciable amount
        BigDecimal maxDepreciation = asset.getPurchaseCost().subtract(asset.getSalvageValue());
        if (newAccumulated.compareTo(maxDepreciation) > 0) {
            newAccumulated = maxDepreciation;
        }

        asset.setAccumulatedDepreciation(newAccumulated);
        return fixedAssetRepository.save(asset);
    }

    @Transactional
    public FixedAsset disposeAsset(Long assetId, LocalDate disposalDate, BigDecimal disposalAmount) {
        FixedAsset asset = getAsset(assetId);
        if (asset.getStatus() != AssetStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot dispose non-active asset: " + assetId);
        }
        asset.setStatus(AssetStatus.DISPOSED);
        asset.setDisposalDate(disposalDate);
        asset.setDisposalAmount(disposalAmount);
        return fixedAssetRepository.save(asset);
    }
}
