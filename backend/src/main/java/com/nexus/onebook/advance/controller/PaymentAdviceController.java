package com.nexus.onebook.advance.controller;

import com.nexus.onebook.advance.dto.PaymentAdviceResponse;
import com.nexus.onebook.advance.service.PaymentAdviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for payment advices.
 * Manages employee reimbursement payments.
 */
@RestController("advancePaymentAdviceController")
@RequestMapping("/api/advance/payment-advices")
public class PaymentAdviceController {

    private final PaymentAdviceService paymentAdviceService;

    public PaymentAdviceController(PaymentAdviceService paymentAdviceService) {
        this.paymentAdviceService = paymentAdviceService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentAdviceResponse> getPaymentAdvice(
            @PathVariable Long id,
            @RequestParam String tenantId) {
        return ResponseEntity.ok(paymentAdviceService.getPaymentAdvice(tenantId, id));
    }

    @GetMapping
    public ResponseEntity<List<PaymentAdviceResponse>> listPaymentAdvices(
            @RequestParam String tenantId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String status) {
        if (employeeId != null) {
            return ResponseEntity.ok(paymentAdviceService.getPaymentAdvicesByEmployee(tenantId, employeeId));
        }
        if ("PENDING_PAYMENT".equalsIgnoreCase(status)) {
            return ResponseEntity.ok(paymentAdviceService.getPendingPaymentAdvices(tenantId));
        }
        // Default: return pending payment advices
        return ResponseEntity.ok(paymentAdviceService.getPendingPaymentAdvices(tenantId));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<PaymentAdviceResponse> payAdvice(
            @PathVariable Long id,
            @RequestParam String tenantId,
            @RequestParam String paidBy,
            @RequestParam Long paymentVoucherId) {
        PaymentAdviceResponse response = paymentAdviceService.payAdvice(tenantId, id, paidBy, paymentVoucherId);
        return ResponseEntity.ok(response);
    }
}
