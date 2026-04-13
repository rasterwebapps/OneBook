package com.nexus.onebook.ledger.inventory.controller;

import com.nexus.onebook.ledger.inventory.model.BillOfMaterials;
import com.nexus.onebook.ledger.inventory.model.BomComponent;
import com.nexus.onebook.ledger.inventory.service.BomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for Bill of Materials (BOM) — manufacturing support.
 */
@RestController
@RequestMapping("/api/bom")
public class BomController {

    private final BomService bomService;

    public BomController(BomService bomService) {
        this.bomService = bomService;
    }

    @PostMapping
    public ResponseEntity<BillOfMaterials> createBom(@RequestParam String tenantId,
                                                       @RequestParam String bomCode,
                                                       @RequestParam Long finishedItemId,
                                                       @RequestParam(defaultValue = "1") BigDecimal quantityProduced) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                bomService.createBom(tenantId, bomCode, finishedItemId, quantityProduced));
    }

    @GetMapping
    public ResponseEntity<List<BillOfMaterials>> getBoms(@RequestParam String tenantId) {
        return ResponseEntity.ok(bomService.getBoms(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillOfMaterials> getBom(@PathVariable Long id) {
        return ResponseEntity.ok(bomService.getBom(id));
    }

    @PostMapping("/{bomId}/components")
    public ResponseEntity<BomComponent> addComponent(@PathVariable Long bomId,
                                                       @RequestParam String tenantId,
                                                       @RequestParam Long componentItemId,
                                                       @RequestParam BigDecimal quantityRequired,
                                                       @RequestParam Long uomId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                bomService.addComponent(tenantId, bomId, componentItemId, quantityRequired, uomId));
    }

    @GetMapping("/{bomId}/components")
    public ResponseEntity<List<BomComponent>> getComponents(@PathVariable Long bomId) {
        return ResponseEntity.ok(bomService.getBomComponents(bomId));
    }
}
