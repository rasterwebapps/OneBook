package com.nexus.onebook.advance.controller;

import com.nexus.onebook.advance.dto.*;
import com.nexus.onebook.advance.service.AdvanceReceiptService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for advance receipts.
 * Records employee return of unspent cash/bank funds.
 */
@RestController
@RequestMapping("/api/advance-receipts")
public class AdvanceReceiptController {

    private final AdvanceReceiptService receiptService;

    public AdvanceReceiptController(AdvanceReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @PostMapping
    public ResponseEntity<AdvanceReceiptResponse> createReceipt(@Valid @RequestBody CreateAdvanceReceiptRequest request) {
        AdvanceReceiptResponse response = receiptService.createReceipt(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdvanceReceiptResponse> getReceipt(
            @PathVariable Long id,
            @RequestParam String tenantId) {
        return ResponseEntity.ok(receiptService.getReceipt(tenantId, id));
    }

    @GetMapping
    public ResponseEntity<List<AdvanceReceiptResponse>> listReceipts(
            @RequestParam String tenantId,
            @RequestParam Long employeeId) {
        return ResponseEntity.ok(receiptService.getReceiptsByEmployee(tenantId, employeeId));
    }
}
