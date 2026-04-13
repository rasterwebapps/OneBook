package com.nexus.onebook.accounts.controller;

import com.nexus.onebook.accounts.model.VoucherCategory;
import com.nexus.onebook.accounts.model.VoucherType;
import com.nexus.onebook.accounts.service.VoucherTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Voucher Types — configurable transaction types.
 */
@RestController
@RequestMapping("/api/voucher-types")
public class VoucherTypeController {

    private final VoucherTypeService voucherTypeService;

    public VoucherTypeController(VoucherTypeService voucherTypeService) {
        this.voucherTypeService = voucherTypeService;
    }

    @PostMapping
    public ResponseEntity<VoucherType> create(@RequestParam String tenantId,
                                                @RequestParam String voucherCode,
                                                @RequestParam String voucherName,
                                                @RequestParam String category) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                voucherTypeService.createVoucherType(tenantId, voucherCode, voucherName, category));
    }

    @GetMapping
    public ResponseEntity<List<VoucherType>> getAll(@RequestParam String tenantId) {
        return ResponseEntity.ok(voucherTypeService.getVoucherTypes(tenantId));
    }

    @GetMapping("/by-category")
    public ResponseEntity<List<VoucherType>> getByCategory(@RequestParam String tenantId,
                                                             @RequestParam VoucherCategory category) {
        return ResponseEntity.ok(voucherTypeService.getVoucherTypesByCategory(tenantId, category));
    }
}
