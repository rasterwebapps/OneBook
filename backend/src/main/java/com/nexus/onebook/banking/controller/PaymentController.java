package com.nexus.onebook.banking.controller;

import com.nexus.onebook.banking.dto.ConnectedPaymentRequest;
import com.nexus.onebook.banking.model.ConnectedPayment;
import com.nexus.onebook.banking.model.PaymentStatus;
import com.nexus.onebook.banking.service.ConnectedPaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Connected Payments — initiate bank payments directly.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final ConnectedPaymentService paymentService;

    public PaymentController(ConnectedPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<ConnectedPayment> initiatePayment(@Valid @RequestBody ConnectedPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.initiatePayment(request));
    }

    @GetMapping
    public ResponseEntity<List<ConnectedPayment>> getPayments(@RequestParam String tenantId) {
        return ResponseEntity.ok(paymentService.getPayments(tenantId));
    }

    @GetMapping("/by-status")
    public ResponseEntity<List<ConnectedPayment>> getByStatus(@RequestParam String tenantId,
                                                                @RequestParam PaymentStatus status) {
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(tenantId, status));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ConnectedPayment> complete(@PathVariable Long id,
                                                       @RequestParam String referenceNumber) {
        return ResponseEntity.ok(paymentService.completePayment(id, referenceNumber));
    }

    @PostMapping("/{id}/fail")
    public ResponseEntity<ConnectedPayment> fail(@PathVariable Long id,
                                                   @RequestParam String reason) {
        return ResponseEntity.ok(paymentService.failPayment(id, reason));
    }
}
