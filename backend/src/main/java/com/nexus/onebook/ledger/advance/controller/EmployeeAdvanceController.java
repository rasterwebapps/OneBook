package com.nexus.onebook.ledger.advance.controller;

import com.nexus.onebook.ledger.advance.dto.*;
import com.nexus.onebook.ledger.advance.model.AdvanceStatus;
import com.nexus.onebook.ledger.advance.model.ApproverRole;
import com.nexus.onebook.ledger.advance.service.EmployeeAdvanceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for employee advances.
 * Implements tiered approval workflow (HOD → CEO → MD).
 */
@RestController
@RequestMapping("/api/advances")
public class EmployeeAdvanceController {

    private final EmployeeAdvanceService advanceService;

    public EmployeeAdvanceController(EmployeeAdvanceService advanceService) {
        this.advanceService = advanceService;
    }

    @PostMapping
    public ResponseEntity<AdvanceResponse> createAdvance(@Valid @RequestBody CreateAdvanceRequest request) {
        AdvanceResponse response = advanceService.createAdvance(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdvanceResponse> getAdvance(
            @PathVariable Long id,
            @RequestParam String tenantId) {
        return ResponseEntity.ok(advanceService.getAdvance(tenantId, id));
    }

    @GetMapping
    public ResponseEntity<List<AdvanceResponse>> listAdvances(
            @RequestParam String tenantId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String status) {
        if (employeeId != null) {
            return ResponseEntity.ok(advanceService.getAdvancesByEmployee(tenantId, employeeId));
        }
        if (status != null) {
            AdvanceStatus advanceStatus = AdvanceStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(advanceService.getPendingApprovals(tenantId, advanceStatus));
        }
        // Default: return pending HOD approvals
        return ResponseEntity.ok(advanceService.getPendingApprovals(tenantId, AdvanceStatus.PENDING_HOD_APPROVAL));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<AdvanceResponse>> getPendingApprovals(
            @RequestParam String tenantId,
            @RequestParam String role,
            @RequestParam(required = false) List<Long> departmentIds) {
        AdvanceStatus status = switch (role.toUpperCase()) {
            case "HOD" -> AdvanceStatus.PENDING_HOD_APPROVAL;
            case "CEO" -> AdvanceStatus.PENDING_CEO_APPROVAL;
            case "MD" -> AdvanceStatus.PENDING_MD_APPROVAL;
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        };

        if (departmentIds != null && !departmentIds.isEmpty()) {
            return ResponseEntity.ok(advanceService.getPendingApprovalsByDepartments(tenantId, departmentIds, status));
        }
        return ResponseEntity.ok(advanceService.getPendingApprovals(tenantId, status));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<AdvanceResponse> approveAdvance(
            @PathVariable Long id,
            @RequestParam String tenantId,
            @RequestParam String actorId,
            @RequestParam String role,
            @Valid @RequestBody ApprovalRequest request) {
        ApproverRole approverRole = ApproverRole.valueOf(role.toUpperCase());

        if ("APPROVE".equalsIgnoreCase(request.action())) {
            AdvanceResponse response = advanceService.approveAdvance(tenantId, id, actorId, approverRole, request.comment());
            return ResponseEntity.ok(response);
        } else if ("REJECT".equalsIgnoreCase(request.action())) {
            AdvanceResponse response = advanceService.rejectAdvance(tenantId, id, actorId, approverRole, request.rejectionReason());
            return ResponseEntity.ok(response);
        } else {
            throw new IllegalArgumentException("Invalid action: " + request.action());
        }
    }

    @GetMapping("/balance/{employeeId}")
    public ResponseEntity<BigDecimal> getOutstandingBalance(
            @PathVariable Long employeeId,
            @RequestParam String tenantId) {
        return ResponseEntity.ok(advanceService.getOutstandingBalance(tenantId, employeeId));
    }

    @GetMapping("/limit/{employeeId}")
    public ResponseEntity<BigDecimal> getAdvanceLimit(
            @PathVariable Long employeeId,
            @RequestParam String tenantId) {
        return ResponseEntity.ok(advanceService.getAdvanceLimit(tenantId, employeeId));
    }

    @PutMapping("/limit/{employeeId}")
    public ResponseEntity<Void> updateAdvanceLimit(
            @PathVariable Long employeeId,
            @RequestParam String tenantId,
            @RequestParam BigDecimal limit) {
        advanceService.updateAdvanceLimit(tenantId, employeeId, limit);
        return ResponseEntity.ok().build();
    }
}
