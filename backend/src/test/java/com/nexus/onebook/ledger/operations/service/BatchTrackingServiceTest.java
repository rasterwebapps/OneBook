package com.nexus.onebook.ledger.operations.service;

import com.nexus.onebook.ledger.operations.dto.BatchTrackingRequest;
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
import com.nexus.onebook.ledger.operations.repository.BatchTrackingRepository;
import com.nexus.onebook.ledger.inventory.repository.GodownRepository;
import com.nexus.onebook.ledger.inventory.repository.StockItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchTrackingServiceTest {

    @Mock
    private BatchTrackingRepository batchRepository;

    @Mock
    private StockItemRepository stockItemRepository;

    @Mock
    private GodownRepository godownRepository;

    @InjectMocks
    private BatchTrackingService batchTrackingService;

    @Test
    void createBatch_validRequest_createsBatch() {
        UnitOfMeasure uom = new UnitOfMeasure("tenant-1", "PCS", "Pieces");
        StockItem item = new StockItem("tenant-1", "MED001", "Paracetamol", uom);
        BatchTrackingRequest request = new BatchTrackingRequest(
                "tenant-1", 1L, "BATCH-2024-001",
                LocalDate.of(2024, 1, 1), LocalDate.of(2025, 1, 1),
                null, new BigDecimal("500"), new BigDecimal("10.50"));

        when(stockItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(batchRepository.findByTenantIdAndStockItemIdAndBatchNumber(
                "tenant-1", 1L, "BATCH-2024-001"))
                .thenReturn(Optional.empty());
        when(batchRepository.save(any(BatchTracking.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        BatchTracking result = batchTrackingService.createBatch(request);

        assertNotNull(result);
        assertEquals("BATCH-2024-001", result.getBatchNumber());
        assertEquals(new BigDecimal("500"), result.getQuantity());
        assertEquals(new BigDecimal("10.50"), result.getCostPerUnit());
    }

    @Test
    void createBatch_duplicateBatchNumber_throwsException() {
        UnitOfMeasure uom = new UnitOfMeasure("tenant-1", "PCS", "Pieces");
        StockItem item = new StockItem("tenant-1", "MED001", "Paracetamol", uom);
        BatchTracking existing = new BatchTracking("tenant-1", item,
                "BATCH-2024-001", new BigDecimal("500"));
        BatchTrackingRequest request = new BatchTrackingRequest(
                "tenant-1", 1L, "BATCH-2024-001",
                null, null, null, new BigDecimal("100"), null);

        when(stockItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(batchRepository.findByTenantIdAndStockItemIdAndBatchNumber(
                "tenant-1", 1L, "BATCH-2024-001"))
                .thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () ->
                batchTrackingService.createBatch(request));
    }

    @Test
    void getBatchesByItem_returnsList() {
        UnitOfMeasure uom = new UnitOfMeasure("tenant-1", "PCS", "Pieces");
        StockItem item = new StockItem("tenant-1", "MED001", "Paracetamol", uom);
        BatchTracking b1 = new BatchTracking("tenant-1", item, "B001", new BigDecimal("100"));
        BatchTracking b2 = new BatchTracking("tenant-1", item, "B002", new BigDecimal("200"));

        when(batchRepository.findByTenantIdAndStockItemId("tenant-1", 1L))
                .thenReturn(List.of(b1, b2));

        List<BatchTracking> result = batchTrackingService.getBatchesByItem("tenant-1", 1L);

        assertEquals(2, result.size());
    }

    @Test
    void getExpiringBatches_returnsList() {
        UnitOfMeasure uom = new UnitOfMeasure("tenant-1", "PCS", "Pieces");
        StockItem item = new StockItem("tenant-1", "MED001", "Paracetamol", uom);
        BatchTracking b1 = new BatchTracking("tenant-1", item, "B001", new BigDecimal("100"));
        b1.setExpiryDate(LocalDate.of(2024, 6, 1));
        LocalDate cutoff = LocalDate.of(2024, 7, 1);

        when(batchRepository.findByTenantIdAndExpiryDateBefore("tenant-1", cutoff))
                .thenReturn(List.of(b1));

        List<BatchTracking> result = batchTrackingService.getExpiringBatches("tenant-1", cutoff);

        assertEquals(1, result.size());
    }

    @Test
    void markExpired_validBatch_setsStatusExpired() {
        UnitOfMeasure uom = new UnitOfMeasure("tenant-1", "PCS", "Pieces");
        StockItem item = new StockItem("tenant-1", "MED001", "Paracetamol", uom);
        BatchTracking batch = new BatchTracking("tenant-1", item, "B001", new BigDecimal("100"));

        when(batchRepository.findById(1L)).thenReturn(Optional.of(batch));
        when(batchRepository.save(any(BatchTracking.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        BatchTracking result = batchTrackingService.markExpired(1L);

        assertEquals(BatchStatus.EXPIRED, result.getStatus());
    }
}
