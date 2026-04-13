package com.nexus.onebook.ledger.fixedasset.service;

import com.nexus.onebook.ledger.fixedasset.dto.DepreciationSchedule;
import com.nexus.onebook.ledger.fixedasset.dto.FixedAssetRequest;
import com.nexus.onebook.ledger.accounts.model.*;
import com.nexus.onebook.ledger.auditor.model.*;
import com.nexus.onebook.ledger.banking.model.*;
import com.nexus.onebook.ledger.clientaccount.model.*;
import com.nexus.onebook.ledger.compliance.model.*;
import com.nexus.onebook.ledger.credit.model.*;
import com.nexus.onebook.ledger.currency.model.*;
import com.nexus.onebook.ledger.entitlement.model.*;
import com.nexus.onebook.ledger.fixedasset.model.*;
import com.nexus.onebook.ledger.foundation.model.*;
import com.nexus.onebook.ledger.intelligence.model.*;
import com.nexus.onebook.ledger.inventory.model.*;
import com.nexus.onebook.ledger.operations.model.*;
import com.nexus.onebook.ledger.payroll.model.*;
import com.nexus.onebook.ledger.reporting.model.*;
import com.nexus.onebook.ledger.tenant.model.*;
import com.nexus.onebook.ledger.accounts.repository.BranchRepository;
import com.nexus.onebook.ledger.fixedasset.repository.FixedAssetRepository;
import com.nexus.onebook.ledger.accounts.repository.LedgerAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FixedAssetServiceTest {

    @Mock
    private FixedAssetRepository fixedAssetRepository;
    @Mock
    private LedgerAccountRepository ledgerAccountRepository;
    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private FixedAssetService fixedAssetService;

    private LedgerAccount assetAccount;
    private LedgerAccount depreciationAccount;

    @BeforeEach
    void setUp() {
        assetAccount = new LedgerAccount();
        assetAccount.setId(1L);
        assetAccount.setAccountCode("1500");
        assetAccount.setAccountName("Equipment");
        assetAccount.setAccountType(AccountType.ASSET);

        depreciationAccount = new LedgerAccount();
        depreciationAccount.setId(2L);
        depreciationAccount.setAccountCode("5200");
        depreciationAccount.setAccountName("Depreciation Expense");
        depreciationAccount.setAccountType(AccountType.EXPENSE);
    }

    @Test
    void createAsset_validRequest_succeeds() {
        FixedAssetRequest request = new FixedAssetRequest(
                "tenant-1", "EQUIP-001", "Office Desk", "A desk",
                1L, 2L, LocalDate.of(2024, 1, 1),
                new BigDecimal("10000.0000"), new BigDecimal("1000.0000"),
                60, "STRAIGHT_LINE", null);

        when(fixedAssetRepository.findByTenantIdAndAssetCode("tenant-1", "EQUIP-001"))
                .thenReturn(Optional.empty());
        when(ledgerAccountRepository.findById(1L)).thenReturn(Optional.of(assetAccount));
        when(ledgerAccountRepository.findById(2L)).thenReturn(Optional.of(depreciationAccount));
        when(fixedAssetRepository.save(any(FixedAsset.class))).thenAnswer(inv -> inv.getArgument(0));

        FixedAsset result = fixedAssetService.createAsset(request);

        assertNotNull(result);
        assertEquals("EQUIP-001", result.getAssetCode());
        assertEquals("Office Desk", result.getAssetName());
        verify(fixedAssetRepository).save(any(FixedAsset.class));
    }

    @Test
    void createAsset_duplicateCode_throws() {
        FixedAssetRequest request = new FixedAssetRequest(
                "tenant-1", "EQUIP-001", "Office Desk", null,
                1L, 2L, LocalDate.of(2024, 1, 1),
                new BigDecimal("10000.0000"), null, 60, null, null);

        when(fixedAssetRepository.findByTenantIdAndAssetCode("tenant-1", "EQUIP-001"))
                .thenReturn(Optional.of(new FixedAsset()));

        assertThrows(IllegalArgumentException.class, () -> fixedAssetService.createAsset(request));
    }

    @Test
    void computeDepreciation_straightLine_calculatesCorrectly() {
        FixedAsset asset = createTestAsset(DepreciationMethod.STRAIGHT_LINE);
        when(fixedAssetRepository.findById(1L)).thenReturn(Optional.of(asset));

        DepreciationSchedule schedule = fixedAssetService.computeDepreciation(1L);

        // (10000 - 1000) / 60 = 150.0000
        assertEquals(new BigDecimal("150.0000"), schedule.monthlyDepreciation());
        assertEquals(new BigDecimal("10000.0000"), schedule.netBookValue());
    }

    @Test
    void disposeAsset_activeAsset_succeeds() {
        FixedAsset asset = createTestAsset(DepreciationMethod.STRAIGHT_LINE);
        when(fixedAssetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(fixedAssetRepository.save(any(FixedAsset.class))).thenAnswer(inv -> inv.getArgument(0));

        FixedAsset disposed = fixedAssetService.disposeAsset(
                1L, LocalDate.of(2024, 6, 1), new BigDecimal("5000.0000"));

        assertEquals(AssetStatus.DISPOSED, disposed.getStatus());
        assertEquals(LocalDate.of(2024, 6, 1), disposed.getDisposalDate());
    }

    @Test
    void disposeAsset_alreadyDisposed_throws() {
        FixedAsset asset = createTestAsset(DepreciationMethod.STRAIGHT_LINE);
        asset.setStatus(AssetStatus.DISPOSED);
        when(fixedAssetRepository.findById(1L)).thenReturn(Optional.of(asset));

        assertThrows(IllegalArgumentException.class, () ->
                fixedAssetService.disposeAsset(1L, LocalDate.of(2024, 6, 1), BigDecimal.ZERO));
    }

    @Test
    void runMonthlyDepreciation_activeAsset_updatesAccumulated() {
        FixedAsset asset = createTestAsset(DepreciationMethod.STRAIGHT_LINE);
        when(fixedAssetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(fixedAssetRepository.save(any(FixedAsset.class))).thenAnswer(inv -> inv.getArgument(0));

        FixedAsset result = fixedAssetService.runMonthlyDepreciation(1L);

        assertEquals(new BigDecimal("150.0000"), result.getAccumulatedDepreciation());
    }

    private FixedAsset createTestAsset(DepreciationMethod method) {
        FixedAsset asset = new FixedAsset(
                "tenant-1", "EQUIP-001", "Office Desk",
                assetAccount, depreciationAccount,
                LocalDate.of(2024, 1, 1), new BigDecimal("10000.0000"), 60);
        asset.setId(1L);
        asset.setSalvageValue(new BigDecimal("1000.0000"));
        asset.setDepreciationMethod(method);
        asset.setAccumulatedDepreciation(BigDecimal.ZERO);
        return asset;
    }
}
