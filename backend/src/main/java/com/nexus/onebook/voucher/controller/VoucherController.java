package com.nexus.onebook.voucher.controller;

import com.nexus.onebook.voucher.dto.*;
import com.nexus.onebook.voucher.service.VoucherService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {

    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @PostMapping
    public ResponseEntity<VoucherResponse> createVoucher(@Valid @RequestBody CreateVoucherRequest request) {
        return ResponseEntity.status(201).body(voucherService.createVoucher(request));
    }

    @GetMapping
    public ResponseEntity<List<VoucherResponse>> listVouchers(@RequestParam String tenantId) {
        return ResponseEntity.ok(voucherService.getVouchersByTenant(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VoucherResponse> getVoucher(@PathVariable Long id) {
        return ResponseEntity.ok(voucherService.getVoucherById(id));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<VoucherResponse> approveVoucher(
            @PathVariable Long id, @RequestParam String approvedBy) {
        return ResponseEntity.ok(voucherService.approveVoucher(id, approvedBy));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<VoucherResponse> cancelVoucher(
            @PathVariable Long id, @RequestParam String cancelledBy,
            @RequestParam String reason) {
        return ResponseEntity.ok(voucherService.cancelVoucher(id, cancelledBy, reason));
    }

    @PostMapping("/items")
    public ResponseEntity<VoucherItemResponse> createVoucherItem(
            @Valid @RequestBody CreateVoucherItemRequest request) {
        return ResponseEntity.status(201).body(voucherService.createVoucherItem(request));
    }

    @GetMapping("/{voucherId}/items")
    public ResponseEntity<List<VoucherItemResponse>> getVoucherItems(
            @PathVariable Long voucherId, @RequestParam String tenantId) {
        return ResponseEntity.ok(voucherService.getVoucherItems(tenantId, voucherId));
    }
}
