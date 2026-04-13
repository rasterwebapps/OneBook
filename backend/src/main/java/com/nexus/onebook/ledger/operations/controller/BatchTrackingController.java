package com.nexus.onebook.ledger.operations.controller;

import com.nexus.onebook.ledger.operations.dto.BatchTrackingRequest;
import com.nexus.onebook.ledger.operations.model.BatchTracking;
import com.nexus.onebook.ledger.operations.service.BatchTrackingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for Batch & Expiry Tracking.
 */
@RestController
@RequestMapping("/api/batch-tracking")
public class BatchTrackingController {

    private final BatchTrackingService batchTrackingService;

    public BatchTrackingController(BatchTrackingService batchTrackingService) {
        this.batchTrackingService = batchTrackingService;
    }

    @PostMapping
    public ResponseEntity<BatchTracking> createBatch(@Valid @RequestBody BatchTrackingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(batchTrackingService.createBatch(request));
    }

    @GetMapping
    public ResponseEntity<List<BatchTracking>> getBatches(@RequestParam String tenantId) {
        return ResponseEntity.ok(batchTrackingService.getBatches(tenantId));
    }

    @GetMapping("/by-item")
    public ResponseEntity<List<BatchTracking>> getBatchesByItem(@RequestParam String tenantId,
                                                                  @RequestParam Long stockItemId) {
        return ResponseEntity.ok(batchTrackingService.getBatchesByItem(tenantId, stockItemId));
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<BatchTracking>> getExpiringBatches(@RequestParam String tenantId,
                                                                    @RequestParam LocalDate beforeDate) {
        return ResponseEntity.ok(batchTrackingService.getExpiringBatches(tenantId, beforeDate));
    }

    @PostMapping("/{id}/expire")
    public ResponseEntity<BatchTracking> markExpired(@PathVariable Long id) {
        return ResponseEntity.ok(batchTrackingService.markExpired(id));
    }
}
