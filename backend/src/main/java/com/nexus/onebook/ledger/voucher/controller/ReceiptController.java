package com.nexus.onebook.ledger.voucher.controller;

import com.nexus.onebook.ledger.voucher.dto.*;
import com.nexus.onebook.ledger.voucher.service.ReceiptService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {
    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @PostMapping
    public ResponseEntity<ReceiptResponse> create(@Valid @RequestBody CreateReceiptRequest request) {
        return ResponseEntity.status(201).body(receiptService.createReceipt(request));
    }

    @GetMapping
    public ResponseEntity<List<ReceiptResponse>> list(@RequestParam String tenantId) {
        return ResponseEntity.ok(receiptService.getReceiptsByTenant(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReceiptResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(receiptService.getReceiptById(id));
    }
}
