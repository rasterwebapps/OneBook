package com.nexus.onebook.voucher.controller;

import com.nexus.onebook.voucher.dto.*;
import com.nexus.onebook.voucher.service.PaymentAdviceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payment-advices")
public class PaymentAdviceController {
    private final PaymentAdviceService paymentAdviceService;

    public PaymentAdviceController(PaymentAdviceService paymentAdviceService) {
        this.paymentAdviceService = paymentAdviceService;
    }

    @PostMapping
    public ResponseEntity<PaymentAdviceResponse> create(@Valid @RequestBody CreatePaymentAdviceRequest request) {
        return ResponseEntity.status(201).body(paymentAdviceService.createPaymentAdvice(request));
    }

    @GetMapping
    public ResponseEntity<List<PaymentAdviceResponse>> list(@RequestParam String tenantId) {
        return ResponseEntity.ok(paymentAdviceService.getByTenant(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentAdviceResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(paymentAdviceService.getById(id));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<PaymentAdviceResponse> approve(@PathVariable Long id, @RequestParam String approvedBy) {
        return ResponseEntity.ok(paymentAdviceService.approve(id, approvedBy));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<PaymentAdviceResponse> reject(@PathVariable Long id, @RequestParam String rejectedBy, @RequestParam String reason) {
        return ResponseEntity.ok(paymentAdviceService.reject(id, rejectedBy, reason));
    }

    @PostMapping("/{id}/mark-paid")
    public ResponseEntity<PaymentAdviceResponse> markPaid(@PathVariable Long id, @RequestParam String transactionReference) {
        return ResponseEntity.ok(paymentAdviceService.markPaid(id, transactionReference));
    }
}
