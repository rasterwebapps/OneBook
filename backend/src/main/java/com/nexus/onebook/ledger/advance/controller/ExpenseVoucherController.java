package com.nexus.onebook.ledger.advance.controller;

import com.nexus.onebook.ledger.advance.dto.*;
import com.nexus.onebook.ledger.advance.service.ExpenseVoucherService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for expense vouchers.
 * Implements expense submission and HOD approval workflow.
 */
@RestController
@RequestMapping("/api/expense-vouchers")
public class ExpenseVoucherController {

    private final ExpenseVoucherService voucherService;

    public ExpenseVoucherController(ExpenseVoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @PostMapping
    public ResponseEntity<ExpenseVoucherResponse> createExpenseVoucher(@Valid @RequestBody CreateExpenseVoucherRequest request) {
        ExpenseVoucherResponse response = voucherService.createExpenseVoucher(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseVoucherResponse> getExpenseVoucher(
            @PathVariable Long id,
            @RequestParam String tenantId) {
        return ResponseEntity.ok(voucherService.getExpenseVoucher(tenantId, id));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseVoucherResponse>> listExpenseVouchers(
            @RequestParam String tenantId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) List<Long> departmentIds) {
        if (employeeId != null) {
            return ResponseEntity.ok(voucherService.getExpensesByEmployee(tenantId, employeeId));
        }
        if (departmentIds != null && !departmentIds.isEmpty()) {
            return ResponseEntity.ok(voucherService.getPendingApprovalsByDepartments(tenantId, departmentIds));
        }
        return ResponseEntity.ok(voucherService.getPendingApprovals(tenantId));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ExpenseVoucherResponse> approveExpenseVoucher(
            @PathVariable Long id,
            @RequestParam String tenantId,
            @RequestParam String actorId,
            @Valid @RequestBody ApprovalRequest request) {
        if ("APPROVE".equalsIgnoreCase(request.action())) {
            ExpenseVoucherResponse response = voucherService.approveExpenseVoucher(tenantId, id, actorId);
            return ResponseEntity.ok(response);
        } else if ("REJECT".equalsIgnoreCase(request.action())) {
            ExpenseVoucherResponse response = voucherService.rejectExpenseVoucher(tenantId, id, actorId, request.rejectionReason());
            return ResponseEntity.ok(response);
        } else {
            throw new IllegalArgumentException("Invalid action: " + request.action());
        }
    }
}
