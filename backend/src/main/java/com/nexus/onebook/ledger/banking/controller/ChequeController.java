package com.nexus.onebook.ledger.banking.controller;

import com.nexus.onebook.ledger.banking.dto.ChequeEntryRequest;
import com.nexus.onebook.ledger.banking.model.ChequeEntry;
import com.nexus.onebook.ledger.banking.model.ChequeStatus;
import com.nexus.onebook.ledger.banking.service.ChequeManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for Cheque Management — cheque register with full lifecycle.
 */
@RestController
@RequestMapping("/api/cheques")
public class ChequeController {

    private final ChequeManagementService chequeService;

    public ChequeController(ChequeManagementService chequeService) {
        this.chequeService = chequeService;
    }

    @PostMapping
    public ResponseEntity<ChequeEntry> issueCheque(@Valid @RequestBody ChequeEntryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chequeService.issueCheque(request));
    }

    @GetMapping
    public ResponseEntity<List<ChequeEntry>> getRegister(@RequestParam String tenantId) {
        return ResponseEntity.ok(chequeService.getChequeRegister(tenantId));
    }

    @GetMapping("/by-status")
    public ResponseEntity<List<ChequeEntry>> getByStatus(@RequestParam String tenantId,
                                                           @RequestParam ChequeStatus status) {
        return ResponseEntity.ok(chequeService.getChequesByStatus(tenantId, status));
    }

    @PostMapping("/{id}/clear")
    public ResponseEntity<ChequeEntry> clearCheque(@PathVariable Long id,
                                                     @RequestParam LocalDate clearingDate) {
        return ResponseEntity.ok(chequeService.clearCheque(id, clearingDate));
    }

    @PostMapping("/{id}/bounce")
    public ResponseEntity<ChequeEntry> bounceCheque(@PathVariable Long id,
                                                      @RequestParam String reason) {
        return ResponseEntity.ok(chequeService.bounceCheque(id, reason));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ChequeEntry> cancelCheque(@PathVariable Long id) {
        return ResponseEntity.ok(chequeService.cancelCheque(id));
    }
}
