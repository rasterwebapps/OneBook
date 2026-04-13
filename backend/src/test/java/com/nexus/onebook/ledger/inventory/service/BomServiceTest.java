package com.nexus.onebook.ledger.inventory.service;

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
import com.nexus.onebook.ledger.inventory.repository.BillOfMaterialsRepository;
import com.nexus.onebook.ledger.inventory.repository.BomComponentRepository;
import com.nexus.onebook.ledger.inventory.repository.StockItemRepository;
import com.nexus.onebook.ledger.inventory.repository.UnitOfMeasureRepository;
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
class BomServiceTest {

    @Mock
    private BillOfMaterialsRepository bomRepository;

    @Mock
    private BomComponentRepository componentRepository;

    @Mock
    private StockItemRepository stockItemRepository;

    @Mock
    private UnitOfMeasureRepository uomRepository;

    @InjectMocks
    private BomService bomService;

    @Test
    void createBom_validRequest_createsBom() {
        UnitOfMeasure uom = new UnitOfMeasure("tenant-1", "PCS", "Pieces");
        StockItem finishedItem = new StockItem("tenant-1", "FG001", "Finished Good", uom);

        when(bomRepository.findByTenantIdAndBomCode("tenant-1", "BOM001"))
                .thenReturn(Optional.empty());
        when(stockItemRepository.findById(1L)).thenReturn(Optional.of(finishedItem));
        when(bomRepository.save(any(BillOfMaterials.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        BillOfMaterials result = bomService.createBom(
                "tenant-1", "BOM001", 1L, new BigDecimal("10"));

        assertNotNull(result);
        assertEquals("BOM001", result.getBomCode());
        assertEquals(new BigDecimal("10"), result.getQuantityProduced());
    }

    @Test
    void createBom_duplicateCode_throwsException() {
        UnitOfMeasure uom = new UnitOfMeasure("tenant-1", "PCS", "Pieces");
        StockItem item = new StockItem("tenant-1", "FG001", "Finished Good", uom);
        BillOfMaterials existing = new BillOfMaterials("tenant-1", "BOM001", item);

        when(bomRepository.findByTenantIdAndBomCode("tenant-1", "BOM001"))
                .thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () ->
                bomService.createBom("tenant-1", "BOM001", 1L, new BigDecimal("10")));
    }

    @Test
    void addComponent_validRequest_addsComponent() {
        UnitOfMeasure uom = new UnitOfMeasure("tenant-1", "KG", "Kilogram");
        StockItem finishedItem = new StockItem("tenant-1", "FG001", "Finished Good", uom);
        StockItem componentItem = new StockItem("tenant-1", "RM001", "Raw Material", uom);
        BillOfMaterials bom = new BillOfMaterials("tenant-1", "BOM001", finishedItem);

        when(bomRepository.findById(1L)).thenReturn(Optional.of(bom));
        when(stockItemRepository.findById(2L)).thenReturn(Optional.of(componentItem));
        when(uomRepository.findById(1L)).thenReturn(Optional.of(uom));
        when(componentRepository.save(any(BomComponent.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        BomComponent result = bomService.addComponent(
                "tenant-1", 1L, 2L, new BigDecimal("5"), 1L);

        assertNotNull(result);
        assertEquals(new BigDecimal("5"), result.getQuantityRequired());
    }

    @Test
    void addComponent_bomNotFound_throwsException() {
        when(bomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                bomService.addComponent("tenant-1", 99L, 2L, new BigDecimal("5"), 1L));
    }

    @Test
    void getBom_notFound_throwsException() {
        when(bomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                bomService.getBom(99L));
    }
}
