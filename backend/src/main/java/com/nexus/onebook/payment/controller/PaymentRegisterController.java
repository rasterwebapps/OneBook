package com.nexus.onebook.payment.controller;

import com.nexus.onebook.payment.dto.VendorGroupResponse;
import com.nexus.onebook.payment.service.PaymentRegisterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payment-register")
public class PaymentRegisterController {

    private final PaymentRegisterService registerService;

    public PaymentRegisterController(PaymentRegisterService registerService) {
        this.registerService = registerService;
    }

    @GetMapping
    public ResponseEntity<List<VendorGroupResponse>> getRegister(@RequestParam String tenantId) {
        return ResponseEntity.ok(registerService.getRegisterGroupedByVendor(tenantId));
    }

    @GetMapping("/vendor/{vendorAccountId}")
    public ResponseEntity<VendorGroupResponse> getRegisterForVendor(
            @PathVariable Long vendorAccountId,
            @RequestParam String tenantId) {
        return ResponseEntity.ok(registerService.getRegisterForVendor(tenantId, vendorAccountId));
    }
}
