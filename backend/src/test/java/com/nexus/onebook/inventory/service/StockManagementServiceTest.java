package com.nexus.onebook.inventory.service;

import com.nexus.onebook.inventory.dto.StockItemRequest;
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
import com.nexus.onebook.accounts.repository.*;
import com.nexus.onebook.auditor.repository.*;
import com.nexus.onebook.banking.repository.*;
import com.nexus.onebook.clientaccount.repository.*;
import com.nexus.onebook.compliance.repository.*;
import com.nexus.onebook.credit.repository.*;
import com.nexus.onebook.currency.repository.*;
import com.nexus.onebook.entitlement.repository.*;
import com.nexus.onebook.fixedasset.repository.*;
import com.nexus.onebook.foundation.repository.*;
import com.nexus.onebook.intelligence.repository.*;
import com.nexus.onebook.inventory.repository.*;
import com.nexus.onebook.operations.repository.*;
import com.nexus.onebook.payroll.repository.*;
import com.nexus.onebook.reporting.repository.*;
import com.nexus.onebook.tenant.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockManagementServiceTest {

    @Mock
    private StockItemRepository stockItemRepository;

    @Mock
    private StockGroupRepository stockGroupRepository;

    @Mock
    private UnitOfMeasureRepository uomRepository;

    @Mock
    private GodownRepository godownRepository;

    @Mock
    private StockGodownAllocationRepository allocationRepository;

    @InjectMocks
    private StockManagementService stockManagementService;

    @Test
    void createStockItem_validRequest_createsItem() {
        UnitOfMeasure uom = new UnitOfMeasure("tenant-1", "PCS", "Pieces");
        StockItemRequest request = new StockItemRequest(
                "tenant-1", "ITM001", "Widget", null, null, 1L,
                null, null, null, null, null);

        when(stockItemRepository.findByTenantIdAndItemCode("tenant-1", "ITM001"))
                .thenReturn(Optional.empty());
        when(uomRepository.findById(1L)).thenReturn(Optional.of(uom));
        when(stockItemRepository.save(any(StockItem.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        StockItem result = stockManagementService.createStockItem(request);

        assertNotNull(result);
        assertEquals("ITM001", result.getItemCode());
        assertEquals("Widget", result.getItemName());
    }

    @Test
    void createStockItem_duplicateCode_throwsException() {
        StockItem existing = new StockItem();
        existing.setItemCode("ITM001");
        StockItemRequest request = new StockItemRequest(
                "tenant-1", "ITM001", "Widget", null, null, 1L,
                null, null, null, null, null);

        when(stockItemRepository.findByTenantIdAndItemCode("tenant-1", "ITM001"))
                .thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () ->
                stockManagementService.createStockItem(request));
    }

    @Test
    void getStockItem_notFound_throwsException() {
        when(stockItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                stockManagementService.getStockItem(99L));
    }

    @Test
    void adjustStock_updatesBalance() {
        UnitOfMeasure uom = new UnitOfMeasure("tenant-1", "PCS", "Pieces");
        StockItem item = new StockItem("tenant-1", "ITM001", "Widget", uom);
        item.setCurrentBalance(new BigDecimal("100"));

        when(stockItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(stockItemRepository.save(any(StockItem.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        StockItem result = stockManagementService.adjustStock(1L, new BigDecimal("25"));

        assertEquals(new BigDecimal("125"), result.getCurrentBalance());
    }

    @Test
    void allocateToGodown_newAllocation_createsAllocation() {
        UnitOfMeasure uom = new UnitOfMeasure("tenant-1", "PCS", "Pieces");
        StockItem item = new StockItem("tenant-1", "ITM001", "Widget", uom);
        Godown godown = new Godown("tenant-1", "GDN01", "Main Warehouse");

        when(stockItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(godownRepository.findById(1L)).thenReturn(Optional.of(godown));
        when(allocationRepository.findByTenantIdAndStockItemIdAndGodownId("tenant-1", 1L, 1L))
                .thenReturn(Optional.empty());
        when(allocationRepository.save(any(StockGodownAllocation.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        StockGodownAllocation result = stockManagementService.allocateToGodown(
                "tenant-1", 1L, 1L, new BigDecimal("50"));

        assertNotNull(result);
        assertEquals(new BigDecimal("50"), result.getQuantity());
    }

    @Test
    void createStockGroup_withParent_setsParentGroup() {
        StockGroup parent = new StockGroup("tenant-1", "RAW", "Raw Materials");

        when(stockGroupRepository.findByTenantIdAndGroupCode("tenant-1", "STEEL"))
                .thenReturn(Optional.empty());
        when(stockGroupRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(stockGroupRepository.save(any(StockGroup.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        StockGroup result = stockManagementService.createStockGroup(
                "tenant-1", "STEEL", "Steel Products", 1L);

        assertNotNull(result);
        assertEquals("STEEL", result.getGroupCode());
        assertEquals(parent, result.getParentGroup());
    }

    @Test
    void createUom_duplicateCode_throwsException() {
        UnitOfMeasure existing = new UnitOfMeasure("tenant-1", "KG", "Kilogram");

        when(uomRepository.findByTenantIdAndUomCode("tenant-1", "KG"))
                .thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () ->
                stockManagementService.createUom("tenant-1", "KG", "Kilogram", 2));
    }

    @Test
    void createGodown_newCode_createsGodown() {
        when(godownRepository.findByTenantIdAndGodownCode("tenant-1", "GDN01"))
                .thenReturn(Optional.empty());
        when(godownRepository.save(any(Godown.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Godown result = stockManagementService.createGodown(
                "tenant-1", "GDN01", "Main Warehouse", "123 Main St");

        assertNotNull(result);
        assertEquals("GDN01", result.getGodownCode());
        assertEquals("123 Main St", result.getAddress());
    }
}
