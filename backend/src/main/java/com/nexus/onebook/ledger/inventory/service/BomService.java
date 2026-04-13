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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Bill of Materials (BOM) service — define raw material compositions
 * for finished goods manufacturing.
 */
@Service
public class BomService {

    private final BillOfMaterialsRepository bomRepository;
    private final BomComponentRepository componentRepository;
    private final StockItemRepository stockItemRepository;
    private final UnitOfMeasureRepository uomRepository;

    public BomService(BillOfMaterialsRepository bomRepository,
                      BomComponentRepository componentRepository,
                      StockItemRepository stockItemRepository,
                      UnitOfMeasureRepository uomRepository) {
        this.bomRepository = bomRepository;
        this.componentRepository = componentRepository;
        this.stockItemRepository = stockItemRepository;
        this.uomRepository = uomRepository;
    }

    @Transactional
    public BillOfMaterials createBom(String tenantId, String bomCode, Long finishedItemId,
                                      BigDecimal quantityProduced) {
        bomRepository.findByTenantIdAndBomCode(tenantId, bomCode)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("BOM '" + bomCode + "' already exists");
                });

        StockItem finishedItem = stockItemRepository.findById(finishedItemId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Finished item not found: " + finishedItemId));

        BillOfMaterials bom = new BillOfMaterials(tenantId, bomCode, finishedItem);
        bom.setQuantityProduced(quantityProduced);
        return bomRepository.save(bom);
    }

    @Transactional
    public BomComponent addComponent(String tenantId, Long bomId, Long componentItemId,
                                      BigDecimal quantityRequired, Long uomId) {
        BillOfMaterials bom = bomRepository.findById(bomId)
                .orElseThrow(() -> new IllegalArgumentException("BOM not found: " + bomId));

        StockItem component = stockItemRepository.findById(componentItemId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Component item not found: " + componentItemId));

        UnitOfMeasure uom = uomRepository.findById(uomId)
                .orElseThrow(() -> new IllegalArgumentException("UoM not found: " + uomId));

        BomComponent bomComponent = new BomComponent(tenantId, bom, component, quantityRequired, uom);
        return componentRepository.save(bomComponent);
    }

    @Transactional(readOnly = true)
    public List<BillOfMaterials> getBoms(String tenantId) {
        return bomRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public BillOfMaterials getBom(Long id) {
        return bomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("BOM not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<BomComponent> getBomComponents(Long bomId) {
        return componentRepository.findByBomId(bomId);
    }
}
