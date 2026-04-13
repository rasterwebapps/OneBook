package com.nexus.onebook.ledger.advance.service;

import com.nexus.onebook.ledger.advance.dto.*;
import com.nexus.onebook.ledger.advance.model.*;
import com.nexus.onebook.ledger.advance.repository.*;
import com.nexus.onebook.ledger.security.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing employee advances.
 * Implements tiered approval workflow (HOD → CEO → MD) based on amount.
 */
@Service
public class EmployeeAdvanceService {

    private static final BigDecimal TEN_THOUSAND = new BigDecimal("10000.00");
    private static final BigDecimal TWENTY_THOUSAND = new BigDecimal("20000.00");
    private static final BigDecimal DEFAULT_LIMIT = new BigDecimal("10000.00");

    private final EmployeeAdvanceRepository advanceRepository;
    private final EmployeeAdvanceConfigRepository configRepository;
    private final EmployeeAdvanceBalanceRepository balanceRepository;
    private final AuditLogService auditLogService;

    public EmployeeAdvanceService(
            EmployeeAdvanceRepository advanceRepository,
            EmployeeAdvanceConfigRepository configRepository,
            EmployeeAdvanceBalanceRepository balanceRepository,
            AuditLogService auditLogService) {
        this.advanceRepository = advanceRepository;
        this.configRepository = configRepository;
        this.balanceRepository = balanceRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public AdvanceResponse createAdvance(CreateAdvanceRequest request) {
        // Validate amount is positive
        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Advance amount must be positive");
        }

        // Get employee's current outstanding balance
        BigDecimal outstanding = getOutstandingBalance(request.tenantId(), request.employeeId());

        // Get employee's limit
        BigDecimal limit = getAdvanceLimit(request.tenantId(), request.employeeId());

        // Check if new advance would exceed limit
        BigDecimal newTotal = outstanding.add(request.amount());
        if (newTotal.compareTo(limit) > 0 && !request.override()) {
            throw new IllegalStateException(
                    "Advance limit exceeded. Outstanding: " + outstanding +
                    ", Requested: " + request.amount() +
                    ", Limit: " + limit);
        }

        if (request.override() && (request.overrideReason() == null || request.overrideReason().isBlank())) {
            throw new IllegalArgumentException("Override reason is required when exceeding limit");
        }

        EmployeeAdvance advance = new EmployeeAdvance(
                request.tenantId(),
                request.employeeId(),
                request.departmentId(),
                request.amount(),
                request.purpose(),
                request.voucherDate(),
                request.createdBy()
        );

        if (request.override()) {
            advance.setOverrideFlag(true);
            advance.setOverrideReason(request.overrideReason());
        }

        // Submit for approval immediately
        advance.setStatus(AdvanceStatus.PENDING_HOD_APPROVAL);
        advance.setCurrentApproverRole(ApproverRole.HOD);

        EmployeeAdvance saved = advanceRepository.save(advance);

        auditLogService.logInsert(request.tenantId(), "employee_advances", saved.getId(),
                "employeeId=" + request.employeeId() + ", amount=" + request.amount() +
                ", status=PENDING_HOD_APPROVAL" + (request.override() ? ", override=true" : ""));

        return AdvanceResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public AdvanceResponse getAdvance(String tenantId, Long advanceId) {
        EmployeeAdvance advance = advanceRepository.findByIdAndTenantId(advanceId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Advance not found: " + advanceId));
        return AdvanceResponse.from(advance);
    }

    @Transactional(readOnly = true)
    public List<AdvanceResponse> getAdvancesByEmployee(String tenantId, Long employeeId) {
        return advanceRepository.findByTenantIdAndEmployeeId(tenantId, employeeId)
                .stream()
                .map(AdvanceResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdvanceResponse> getPendingApprovals(String tenantId, AdvanceStatus status) {
        return advanceRepository.findByTenantIdAndStatus(tenantId, status)
                .stream()
                .map(AdvanceResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdvanceResponse> getPendingApprovalsByDepartments(String tenantId, List<Long> departmentIds, AdvanceStatus status) {
        return advanceRepository.findByTenantIdAndDepartmentIdInAndStatus(tenantId, departmentIds, status)
                .stream()
                .map(AdvanceResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public AdvanceResponse approveAdvance(String tenantId, Long advanceId, String approverId, ApproverRole approverRole, String comment) {
        EmployeeAdvance advance = advanceRepository.findByIdAndTenantId(advanceId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Advance not found: " + advanceId));

        // Validate current status allows approval by this role
        validateApproval(advance, approverRole);

        // Prevent self-approval
        if (advance.getCreatedBy().equals(approverId)) {
            throw new IllegalStateException("Cannot approve your own advance request");
        }

        String previousStatus = advance.getStatus().name();

        switch (approverRole) {
            case HOD:
                advance.setHodApprovedBy(approverId);
                advance.setHodApprovedAt(Instant.now());
                // Check if higher approval is needed
                if (advance.getAmount().compareTo(TEN_THOUSAND) > 0) {
                    advance.setStatus(AdvanceStatus.PENDING_CEO_APPROVAL);
                    advance.setCurrentApproverRole(ApproverRole.CEO);
                } else {
                    // HOD is final approver
                    finalizeApproval(advance);
                }
                break;

            case CEO:
                advance.setCeoApprovedBy(approverId);
                advance.setCeoApprovedAt(Instant.now());
                // Check if MD approval is needed
                if (advance.getAmount().compareTo(TWENTY_THOUSAND) > 0) {
                    advance.setStatus(AdvanceStatus.PENDING_MD_APPROVAL);
                    advance.setCurrentApproverRole(ApproverRole.MD);
                } else {
                    // CEO is final approver
                    finalizeApproval(advance);
                }
                break;

            case MD:
                advance.setMdApprovedBy(approverId);
                advance.setMdApprovedAt(Instant.now());
                finalizeApproval(advance);
                break;
        }

        EmployeeAdvance saved = advanceRepository.save(advance);

        auditLogService.logUpdate(tenantId, "employee_advances", advanceId,
                "status=" + previousStatus,
                "status=" + saved.getStatus().name() + ", approvedBy=" + approverId + ", role=" + approverRole);

        return AdvanceResponse.from(saved);
    }

    @Transactional
    public AdvanceResponse rejectAdvance(String tenantId, Long advanceId, String rejectorId, ApproverRole rejectorRole, String reason) {
        EmployeeAdvance advance = advanceRepository.findByIdAndTenantId(advanceId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Advance not found: " + advanceId));

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }

        String previousStatus = advance.getStatus().name();

        advance.setStatus(AdvanceStatus.REJECTED);
        advance.setRejectedBy(rejectorId);
        advance.setRejectedAt(Instant.now());
        advance.setRejectionReason(reason);
        advance.setCurrentApproverRole(null);

        EmployeeAdvance saved = advanceRepository.save(advance);

        auditLogService.logUpdate(tenantId, "employee_advances", advanceId,
                "status=" + previousStatus,
                "status=REJECTED, rejectedBy=" + rejectorId + ", role=" + rejectorRole + ", reason=" + reason);

        return AdvanceResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public BigDecimal getOutstandingBalance(String tenantId, Long employeeId) {
        return balanceRepository.findByTenantIdAndEmployeeId(tenantId, employeeId)
                .map(EmployeeAdvanceBalance::getOutstandingAdvance)
                .orElse(BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public BigDecimal getAdvanceLimit(String tenantId, Long employeeId) {
        return configRepository.findByTenantIdAndEmployeeId(tenantId, employeeId)
                .map(EmployeeAdvanceConfig::getAdvanceLimit)
                .orElse(DEFAULT_LIMIT);
    }

    @Transactional
    public void updateAdvanceLimit(String tenantId, Long employeeId, BigDecimal newLimit) {
        EmployeeAdvanceConfig config = configRepository.findByTenantIdAndEmployeeId(tenantId, employeeId)
                .orElseGet(() -> {
                    EmployeeAdvanceConfig c = new EmployeeAdvanceConfig(tenantId, employeeId, newLimit);
                    return configRepository.save(c);
                });

        config.setAdvanceLimit(newLimit);
        configRepository.save(config);
    }

    private void validateApproval(EmployeeAdvance advance, ApproverRole approverRole) {
        switch (approverRole) {
            case HOD:
                if (advance.getStatus() != AdvanceStatus.PENDING_HOD_APPROVAL) {
                    throw new IllegalStateException("Advance is not pending HOD approval");
                }
                break;
            case CEO:
                if (advance.getStatus() != AdvanceStatus.PENDING_CEO_APPROVAL) {
                    throw new IllegalStateException("Advance is not pending CEO approval");
                }
                break;
            case MD:
                if (advance.getStatus() != AdvanceStatus.PENDING_MD_APPROVAL) {
                    throw new IllegalStateException("Advance is not pending MD approval");
                }
                break;
        }
    }

    private void finalizeApproval(EmployeeAdvance advance) {
        advance.setStatus(AdvanceStatus.APPROVED);
        advance.setApprovedAmount(advance.getAmount());
        advance.setCurrentApproverRole(null);

        // Update outstanding balance
        EmployeeAdvanceBalance balance = balanceRepository
                .findByTenantIdAndEmployeeId(advance.getTenantId(), advance.getEmployeeId())
                .orElseGet(() -> {
                    EmployeeAdvanceBalance b = new EmployeeAdvanceBalance(advance.getTenantId(), advance.getEmployeeId());
                    return balanceRepository.save(b);
                });

        balance.addAdvance(advance.getAmount());
        balanceRepository.save(balance);

        // Journal entry would be posted here via JournalService
        // Dr Employee Advance (Asset) / Cr Cash/Bank
    }
}
