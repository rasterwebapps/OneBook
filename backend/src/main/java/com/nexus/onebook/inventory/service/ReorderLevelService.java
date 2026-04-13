package com.nexus.onebook.inventory.service;

import com.nexus.onebook.inventory.dto.ReorderAlert;
import com.nexus.onebook.inventory.dto.ReorderLevelRequest;
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
import com.nexus.onebook.inventory.repository.GodownRepository;
import com.nexus.onebook.inventory.repository.ReorderLevelRepository;
import com.nexus.onebook.inventory.repository.StockItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Re-order Level service — automated stock alerts to prevent stockouts.
 * Monitors current stock against configured reorder levels and generates alerts.
 */
@Service
public class ReorderLevelService {

    private final ReorderLevelRepository reorderRepository;
    private final StockItemRepository stockItemRepository;
    private final GodownRepository godownRepository;

    public ReorderLevelService(ReorderLevelRepository reorderRepository,
                                StockItemRepository stockItemRepository,
                                GodownRepository godownRepository) {
        this.reorderRepository = reorderRepository;
        this.stockItemRepository = stockItemRepository;
        this.godownRepository = godownRepository;
    }

    @Transactional
    public ReorderLevel setReorderLevel(ReorderLevelRequest request) {
        StockItem item = stockItemRepository.findById(request.stockItemId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Stock item not found: " + request.stockItemId()));

        ReorderLevel level = reorderRepository
                .findByTenantIdAndStockItemIdAndGodownId(
                        request.tenantId(), request.stockItemId(), request.godownId())
                .orElse(new ReorderLevel(request.tenantId(), item,
                        request.minimumLevel(), request.reorderLevel(),
                        request.maximumLevel(), request.reorderQuantity()));

        level.setMinimumLevel(request.minimumLevel());
        level.setReorderLevel(request.reorderLevel());
        level.setMaximumLevel(request.maximumLevel());
        level.setReorderQuantity(request.reorderQuantity());

        if (request.godownId() != null) {
            Godown godown = godownRepository.findById(request.godownId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Godown not found: " + request.godownId()));
            level.setGodown(godown);
        }

        return reorderRepository.save(level);
    }

    @Transactional(readOnly = true)
    public List<ReorderLevel> getReorderLevels(String tenantId) {
        return reorderRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public List<ReorderAlert> checkReorderAlerts(String tenantId) {
        List<ReorderLevel> levels = reorderRepository.findByTenantIdAndActiveTrue(tenantId);
        List<ReorderAlert> alerts = new ArrayList<>();

        for (ReorderLevel level : levels) {
            StockItem item = level.getStockItem();
            if (item.getCurrentBalance().compareTo(level.getReorderLevel()) <= 0) {
                String godownName = level.getGodown() != null
                        ? level.getGodown().getGodownName() : "All Locations";
                alerts.add(new ReorderAlert(
                        item.getId(), item.getItemCode(), item.getItemName(),
                        item.getCurrentBalance(), level.getReorderLevel(),
                        level.getReorderQuantity(), godownName));
            }
        }
        return alerts;
    }
}
