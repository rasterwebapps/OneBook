package com.nexus.onebook.payment.controller;

import com.nexus.onebook.payment.dto.BatchApprovalRequest;
import com.nexus.onebook.payment.dto.CreateBatchRequest;
import com.nexus.onebook.payment.dto.PaymentBatchResponse;
import com.nexus.onebook.payment.service.PaymentBatchService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payment-batches")
public class PaymentBatchController {

    private final PaymentBatchService batchService;

    public PaymentBatchController(PaymentBatchService batchService) {
        this.batchService = batchService;
    }

    @PostMapping
    public ResponseEntity<PaymentBatchResponse> createBatch(
            @RequestParam String tenantId,
            @RequestParam String createdBy,
            @Valid @RequestBody CreateBatchRequest request) {
        PaymentBatchResponse response = batchService.createBatch(tenantId, request, createdBy);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PaymentBatchResponse>> listBatches(
            @RequestParam String tenantId,
            @RequestParam(required = false) String status) {
        List<PaymentBatchResponse> batches;
        if ("APPROVED".equalsIgnoreCase(status)) {
            batches = batchService.getApprovedBatches(tenantId);
        } else {
            batches = batchService.getPendingBatches(tenantId);
        }
        return ResponseEntity.ok(batches);
    }

    @GetMapping("/{batchId}")
    public ResponseEntity<PaymentBatchResponse> getBatch(
            @PathVariable Long batchId,
            @RequestParam String tenantId) {
        return ResponseEntity.ok(batchService.getBatchById(tenantId, batchId));
    }

    @PostMapping("/{batchId}/approve")
    public ResponseEntity<PaymentBatchResponse> approveBatch(
            @PathVariable Long batchId,
            @RequestParam String tenantId,
            @RequestParam String actorId,
            @Valid @RequestBody BatchApprovalRequest request) {
        PaymentBatchResponse response;
        if ("APPROVE".equalsIgnoreCase(request.action())) {
            response = batchService.approveBatch(tenantId, batchId, actorId);
        } else if ("REJECT".equalsIgnoreCase(request.action())) {
            response = batchService.rejectBatch(tenantId, batchId, actorId, request.rejectionReason());
        } else {
            throw new IllegalArgumentException("Invalid action: " + request.action());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{batchId}/generate-file")
    public ResponseEntity<byte[]> generateFile(
            @PathVariable Long batchId,
            @RequestParam String tenantId) {
        byte[] fileBytes = batchService.generatePaymentFile(tenantId, batchId);
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=payment-batch-" + batchId + ".csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(fileBytes);
    }
}
