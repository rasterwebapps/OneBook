package com.nexus.onebook.inventory.controller;

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
import com.nexus.onebook.inventory.service.StockManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for Stock/Inventory Management — items, groups, UoMs, godowns.
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final StockManagementService stockManagementService;

    public InventoryController(StockManagementService stockManagementService) {
        this.stockManagementService = stockManagementService;
    }

    // --- Stock Items ---

    @PostMapping("/items")
    public ResponseEntity<StockItem> createItem(@Valid @RequestBody StockItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockManagementService.createStockItem(request));
    }

    @GetMapping("/items")
    public ResponseEntity<List<StockItem>> getItems(@RequestParam String tenantId) {
        return ResponseEntity.ok(stockManagementService.getStockItems(tenantId));
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<StockItem> getItem(@PathVariable Long id) {
        return ResponseEntity.ok(stockManagementService.getStockItem(id));
    }

    @PostMapping("/items/{id}/adjust")
    public ResponseEntity<StockItem> adjustStock(@PathVariable Long id,
                                                  @RequestParam BigDecimal quantity) {
        return ResponseEntity.ok(stockManagementService.adjustStock(id, quantity));
    }

    // --- Stock Groups ---

    @PostMapping("/groups")
    public ResponseEntity<StockGroup> createGroup(@RequestParam String tenantId,
                                                    @RequestParam String groupCode,
                                                    @RequestParam String groupName,
                                                    @RequestParam(required = false) Long parentGroupId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                stockManagementService.createStockGroup(tenantId, groupCode, groupName, parentGroupId));
    }

    @GetMapping("/groups")
    public ResponseEntity<List<StockGroup>> getGroups(@RequestParam String tenantId) {
        return ResponseEntity.ok(stockManagementService.getStockGroups(tenantId));
    }

    // --- Units of Measure ---

    @PostMapping("/uoms")
    public ResponseEntity<UnitOfMeasure> createUom(@RequestParam String tenantId,
                                                     @RequestParam String uomCode,
                                                     @RequestParam String uomName,
                                                     @RequestParam(defaultValue = "2") int decimalPlaces) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                stockManagementService.createUom(tenantId, uomCode, uomName, decimalPlaces));
    }

    @GetMapping("/uoms")
    public ResponseEntity<List<UnitOfMeasure>> getUoms(@RequestParam String tenantId) {
        return ResponseEntity.ok(stockManagementService.getUoms(tenantId));
    }

    // --- Godowns ---

    @PostMapping("/godowns")
    public ResponseEntity<Godown> createGodown(@RequestParam String tenantId,
                                                 @RequestParam String godownCode,
                                                 @RequestParam String godownName,
                                                 @RequestParam(required = false) String address) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                stockManagementService.createGodown(tenantId, godownCode, godownName, address));
    }

    @GetMapping("/godowns")
    public ResponseEntity<List<Godown>> getGodowns(@RequestParam String tenantId) {
        return ResponseEntity.ok(stockManagementService.getGodowns(tenantId));
    }

    // --- Godown Allocations ---

    @PostMapping("/allocations")
    public ResponseEntity<StockGodownAllocation> allocate(@RequestParam String tenantId,
                                                           @RequestParam Long stockItemId,
                                                           @RequestParam Long godownId,
                                                           @RequestParam BigDecimal quantity) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                stockManagementService.allocateToGodown(tenantId, stockItemId, godownId, quantity));
    }

    @GetMapping("/allocations")
    public ResponseEntity<List<StockGodownAllocation>> getAllocations(@RequestParam String tenantId,
                                                                       @RequestParam Long stockItemId) {
        return ResponseEntity.ok(stockManagementService.getGodownAllocations(tenantId, stockItemId));
    }
}
