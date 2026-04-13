package com.nexus.onebook.operations.service;

import com.nexus.onebook.operations.dto.BatchTrackingRequest;
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
import com.nexus.onebook.operations.repository.BatchTrackingRepository;
import com.nexus.onebook.inventory.repository.GodownRepository;
import com.nexus.onebook.inventory.repository.StockItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Batch & Expiry tracking service — critical for pharmaceutical and food industries.
 * Tracks items by batch numbers and expiration dates.
 */
@Service
public class BatchTrackingService {

    private final BatchTrackingRepository batchRepository;
    private final StockItemRepository stockItemRepository;
    private final GodownRepository godownRepository;

    public BatchTrackingService(BatchTrackingRepository batchRepository,
                                 StockItemRepository stockItemRepository,
                                 GodownRepository godownRepository) {
        this.batchRepository = batchRepository;
        this.stockItemRepository = stockItemRepository;
        this.godownRepository = godownRepository;
    }

    @Transactional
    public BatchTracking createBatch(BatchTrackingRequest request) {
        StockItem item = stockItemRepository.findById(request.stockItemId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Stock item not found: " + request.stockItemId()));

        batchRepository.findByTenantIdAndStockItemIdAndBatchNumber(
                        request.tenantId(), request.stockItemId(), request.batchNumber())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Batch '" + request.batchNumber() + "' already exists for this item");
                });

        BatchTracking batch = new BatchTracking(request.tenantId(), item,
                request.batchNumber(), request.quantity());

        if (request.manufacturingDate() != null) batch.setManufacturingDate(request.manufacturingDate());
        if (request.expiryDate() != null) batch.setExpiryDate(request.expiryDate());
        if (request.costPerUnit() != null) batch.setCostPerUnit(request.costPerUnit());
        if (request.godownId() != null) {
            Godown godown = godownRepository.findById(request.godownId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Godown not found: " + request.godownId()));
            batch.setGodown(godown);
        }
        return batchRepository.save(batch);
    }

    @Transactional(readOnly = true)
    public List<BatchTracking> getBatches(String tenantId) {
        return batchRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public List<BatchTracking> getBatchesByItem(String tenantId, Long stockItemId) {
        return batchRepository.findByTenantIdAndStockItemId(tenantId, stockItemId);
    }

    @Transactional(readOnly = true)
    public List<BatchTracking> getExpiringBatches(String tenantId, LocalDate beforeDate) {
        return batchRepository.findByTenantIdAndExpiryDateBefore(tenantId, beforeDate);
    }

    @Transactional
    public BatchTracking markExpired(Long batchId) {
        BatchTracking batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchId));
        batch.setStatus(BatchStatus.EXPIRED);
        return batchRepository.save(batch);
    }
}
