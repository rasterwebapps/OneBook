package com.nexus.onebook.ledger.accounts.controller;

import com.nexus.onebook.ledger.accounts.dto.JournalTransactionRequest;
import com.nexus.onebook.ledger.accounts.model.JournalTransaction;
import com.nexus.onebook.ledger.accounts.service.JournalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for journal transaction operations.
 */
@RestController
@RequestMapping("/api/journal")
public class JournalController {

    private final JournalService journalService;

    public JournalController(JournalService journalService) {
        this.journalService = journalService;
    }

    @PostMapping("/transactions")
    public ResponseEntity<JournalTransaction> createTransaction(
            @Valid @RequestBody JournalTransactionRequest request) {
        JournalTransaction transaction = journalService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<JournalTransaction>> getTransactionsByTenant(
            @RequestParam String tenantId) {
        return ResponseEntity.ok(journalService.getTransactionsByTenant(tenantId));
    }

    @GetMapping("/transactions/{uuid}")
    public ResponseEntity<JournalTransaction> getTransaction(@PathVariable UUID uuid) {
        JournalTransaction transaction = journalService.getTransactionByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transaction not found: " + uuid));
        return ResponseEntity.ok(transaction);
    }

    @PutMapping("/transactions/{uuid}")
    public ResponseEntity<JournalTransaction> updateTransaction(
            @PathVariable UUID uuid,
            @Valid @RequestBody JournalTransactionRequest request) {
        JournalTransaction updated = journalService.updateTransaction(uuid, request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/transactions/{uuid}/post")
    public ResponseEntity<JournalTransaction> postTransaction(@PathVariable UUID uuid) {
        JournalTransaction posted = journalService.postTransaction(uuid);
        return ResponseEntity.ok(posted);
    }

    @DeleteMapping("/transactions/{uuid}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID uuid) {
        journalService.deleteTransaction(uuid);
        return ResponseEntity.noContent().build();
    }
}
