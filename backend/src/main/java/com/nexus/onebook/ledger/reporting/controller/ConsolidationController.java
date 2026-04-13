package com.nexus.onebook.ledger.reporting.controller;

import com.nexus.onebook.ledger.reporting.dto.ConsolidatedReport;
import com.nexus.onebook.ledger.reporting.model.IntercompanyElimination;
import com.nexus.onebook.ledger.reporting.service.IntercompanyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for intercompany accounting and consolidation.
 * Provides consolidated reports and elimination management.
 */
@RestController
@RequestMapping("/api/consolidation")
public class ConsolidationController {

    private final IntercompanyService intercompanyService;

    public ConsolidationController(IntercompanyService intercompanyService) {
        this.intercompanyService = intercompanyService;
    }

    @PostMapping("/intercompany")
    public ResponseEntity<IntercompanyElimination> recordIntercompany(
            @RequestParam String tenantId,
            @RequestParam Long sourceBranchId,
            @RequestParam Long targetBranchId,
            @RequestParam Long journalTransactionId,
            @RequestParam BigDecimal amount) {
        IntercompanyElimination elimination = intercompanyService.recordIntercompanyTransaction(
                tenantId, sourceBranchId, targetBranchId, journalTransactionId, amount);
        return ResponseEntity.status(HttpStatus.CREATED).body(elimination);
    }

    @PostMapping("/intercompany/{id}/eliminate")
    public ResponseEntity<IntercompanyElimination> eliminateTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(intercompanyService.eliminateTransaction(id));
    }

    @GetMapping("/report")
    public ResponseEntity<ConsolidatedReport> getConsolidatedReport(@RequestParam String tenantId) {
        return ResponseEntity.ok(intercompanyService.generateConsolidatedReport(tenantId));
    }

    @GetMapping("/intercompany/pending")
    public ResponseEntity<List<IntercompanyElimination>> getPendingEliminations(
            @RequestParam String tenantId) {
        return ResponseEntity.ok(intercompanyService.getPendingEliminations(tenantId));
    }
}
