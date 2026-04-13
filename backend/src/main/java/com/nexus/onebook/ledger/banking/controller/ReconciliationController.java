package com.nexus.onebook.ledger.banking.controller;

import com.nexus.onebook.ledger.banking.dto.BankReconciliationResult;
import com.nexus.onebook.ledger.banking.model.BankFeedTransaction;
import com.nexus.onebook.ledger.banking.service.BankReconciliationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for automated bank reconciliation.
 * Supports bank feed ingestion, matching, and reconciliation status.
 */
@RestController
@RequestMapping("/api/reconciliation")
public class ReconciliationController {

    private final BankReconciliationService reconciliationService;

    public ReconciliationController(BankReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @PostMapping("/bank-feeds")
    public ResponseEntity<BankFeedTransaction> ingestBankFeed(
            @RequestParam String tenantId,
            @RequestParam Long bankAccountId,
            @RequestParam String externalTransactionId,
            @RequestParam LocalDate transactionDate,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description,
            @RequestParam(required = false, defaultValue = "MANUAL") String source) {
        BankFeedTransaction txn = reconciliationService.ingestBankFeed(
                tenantId, bankAccountId, externalTransactionId,
                transactionDate, amount, description, source);
        return ResponseEntity.status(HttpStatus.CREATED).body(txn);
    }

    @PostMapping("/bank-feeds/{feedId}/match/{journalEntryId}")
    public ResponseEntity<BankFeedTransaction> matchTransaction(
            @PathVariable Long feedId, @PathVariable Long journalEntryId) {
        return ResponseEntity.ok(reconciliationService.matchTransaction(feedId, journalEntryId));
    }

    @GetMapping("/status")
    public ResponseEntity<BankReconciliationResult> getReconciliationStatus(
            @RequestParam String tenantId) {
        return ResponseEntity.ok(reconciliationService.getReconciliationStatus(tenantId));
    }

    @GetMapping("/bank-feeds/unmatched")
    public ResponseEntity<List<BankFeedTransaction>> getUnmatchedTransactions(
            @RequestParam String tenantId) {
        return ResponseEntity.ok(reconciliationService.getUnmatchedTransactions(tenantId));
    }
}
