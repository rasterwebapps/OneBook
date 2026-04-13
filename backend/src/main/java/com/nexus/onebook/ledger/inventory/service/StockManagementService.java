package com.nexus.onebook.ledger.inventory.service;

import com.nexus.onebook.ledger.inventory.dto.StockItemRequest;
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
import com.nexus.onebook.ledger.accounts.repository.*;
import com.nexus.onebook.ledger.auditor.repository.*;
import com.nexus.onebook.ledger.banking.repository.*;
import com.nexus.onebook.ledger.clientaccount.repository.*;
import com.nexus.onebook.ledger.compliance.repository.*;
import com.nexus.onebook.ledger.credit.repository.*;
import com.nexus.onebook.ledger.currency.repository.*;
import com.nexus.onebook.ledger.entitlement.repository.*;
import com.nexus.onebook.ledger.fixedasset.repository.*;
import com.nexus.onebook.ledger.foundation.repository.*;
import com.nexus.onebook.ledger.intelligence.repository.*;
import com.nexus.onebook.ledger.inventory.repository.*;
import com.nexus.onebook.ledger.operations.repository.*;
import com.nexus.onebook.ledger.payroll.repository.*;
import com.nexus.onebook.ledger.reporting.repository.*;
import com.nexus.onebook.ledger.tenant.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Stock Management service — track stock items, groups, godowns,
 * and godown-wise stock allocations. Supports multiple Units of Measure.
 */
@Service
public class StockManagementService {

    private final StockItemRepository stockItemRepository;
    private final StockGroupRepository stockGroupRepository;
    private final UnitOfMeasureRepository uomRepository;
    private final GodownRepository godownRepository;
    private final StockGodownAllocationRepository allocationRepository;

    public StockManagementService(StockItemRepository stockItemRepository,
                                   StockGroupRepository stockGroupRepository,
                                   UnitOfMeasureRepository uomRepository,
                                   GodownRepository godownRepository,
                                   StockGodownAllocationRepository allocationRepository) {
        this.stockItemRepository = stockItemRepository;
        this.stockGroupRepository = stockGroupRepository;
        this.uomRepository = uomRepository;
        this.godownRepository = godownRepository;
        this.allocationRepository = allocationRepository;
    }

    @Transactional
    public StockItem createStockItem(StockItemRequest request) {
        stockItemRepository.findByTenantIdAndItemCode(request.tenantId(), request.itemCode())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Stock item with code '" + request.itemCode() + "' already exists");
                });

        UnitOfMeasure primaryUom = uomRepository.findById(request.primaryUomId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Primary UoM not found: " + request.primaryUomId()));

        StockItem item = new StockItem(request.tenantId(), request.itemCode(),
                request.itemName(), primaryUom);

        if (request.description() != null) item.setDescription(request.description());
        if (request.stockGroupId() != null) {
            StockGroup group = stockGroupRepository.findById(request.stockGroupId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Stock group not found: " + request.stockGroupId()));
            item.setStockGroup(group);
        }
        if (request.secondaryUomId() != null) {
            UnitOfMeasure secondaryUom = uomRepository.findById(request.secondaryUomId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Secondary UoM not found: " + request.secondaryUomId()));
            item.setSecondaryUom(secondaryUom);
        }
        if (request.conversionFactor() != null) item.setConversionFactor(request.conversionFactor());
        if (request.openingBalance() != null) {
            item.setOpeningBalance(request.openingBalance());
            item.setCurrentBalance(request.openingBalance());
        }
        if (request.ratePerUnit() != null) item.setRatePerUnit(request.ratePerUnit());
        if (request.hsnCode() != null) item.setHsnCode(request.hsnCode());

        return stockItemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<StockItem> getStockItems(String tenantId) {
        return stockItemRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public StockItem getStockItem(Long id) {
        return stockItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stock item not found: " + id));
    }

    @Transactional
    public StockItem adjustStock(Long stockItemId, BigDecimal quantityChange) {
        StockItem item = getStockItem(stockItemId);
        item.setCurrentBalance(item.getCurrentBalance().add(quantityChange));
        return stockItemRepository.save(item);
    }

    @Transactional
    public StockGodownAllocation allocateToGodown(String tenantId, Long stockItemId,
                                                   Long godownId, BigDecimal quantity) {
        StockItem item = getStockItem(stockItemId);
        Godown godown = godownRepository.findById(godownId)
                .orElseThrow(() -> new IllegalArgumentException("Godown not found: " + godownId));

        StockGodownAllocation allocation = allocationRepository
                .findByTenantIdAndStockItemIdAndGodownId(tenantId, stockItemId, godownId)
                .orElse(new StockGodownAllocation(tenantId, item, godown, BigDecimal.ZERO));

        allocation.setQuantity(allocation.getQuantity().add(quantity));
        return allocationRepository.save(allocation);
    }

    @Transactional(readOnly = true)
    public List<StockGodownAllocation> getGodownAllocations(String tenantId, Long stockItemId) {
        return allocationRepository.findByTenantIdAndStockItemId(tenantId, stockItemId);
    }

    // --- Stock Group operations ---

    @Transactional
    public StockGroup createStockGroup(String tenantId, String groupCode, String groupName,
                                        Long parentGroupId) {
        stockGroupRepository.findByTenantIdAndGroupCode(tenantId, groupCode)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Stock group '" + groupCode + "' already exists");
                });
        StockGroup group = new StockGroup(tenantId, groupCode, groupName);
        if (parentGroupId != null) {
            StockGroup parent = stockGroupRepository.findById(parentGroupId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Parent group not found: " + parentGroupId));
            group.setParentGroup(parent);
        }
        return stockGroupRepository.save(group);
    }

    @Transactional(readOnly = true)
    public List<StockGroup> getStockGroups(String tenantId) {
        return stockGroupRepository.findByTenantId(tenantId);
    }

    // --- UoM operations ---

    @Transactional
    public UnitOfMeasure createUom(String tenantId, String uomCode, String uomName, int decimalPlaces) {
        uomRepository.findByTenantIdAndUomCode(tenantId, uomCode)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("UoM '" + uomCode + "' already exists");
                });
        UnitOfMeasure uom = new UnitOfMeasure(tenantId, uomCode, uomName);
        uom.setDecimalPlaces(decimalPlaces);
        return uomRepository.save(uom);
    }

    @Transactional(readOnly = true)
    public List<UnitOfMeasure> getUoms(String tenantId) {
        return uomRepository.findByTenantId(tenantId);
    }

    // --- Godown operations ---

    @Transactional
    public Godown createGodown(String tenantId, String godownCode, String godownName, String address) {
        godownRepository.findByTenantIdAndGodownCode(tenantId, godownCode)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Godown '" + godownCode + "' already exists");
                });
        Godown godown = new Godown(tenantId, godownCode, godownName);
        if (address != null) godown.setAddress(address);
        return godownRepository.save(godown);
    }

    @Transactional(readOnly = true)
    public List<Godown> getGodowns(String tenantId) {
        return godownRepository.findByTenantId(tenantId);
    }
}
