package com.nexus.onebook.advance.service;

import com.nexus.onebook.advance.dto.*;
import com.nexus.onebook.advance.model.*;
import com.nexus.onebook.advance.repository.*;
import com.nexus.onebook.security.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import com.nexus.onebook.foundation.model.Advance;

/**
 * Service for managing advance receipts.
 * Records employee return of unspent cash/bank funds.
 */
@Service
public class AdvanceReceiptService {

    private final AdvanceReceiptRepository receiptRepository;
    private final EmployeeAdvanceBalanceRepository balanceRepository;
    private final AuditLogService auditLogService;

    public AdvanceReceiptService(
            AdvanceReceiptRepository receiptRepository,
            EmployeeAdvanceBalanceRepository balanceRepository,
            AuditLogService auditLogService) {
        this.receiptRepository = receiptRepository;
        this.balanceRepository = balanceRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public AdvanceReceiptResponse createReceipt(CreateAdvanceReceiptRequest request) {
        // Validate amount is positive
        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Receipt amount must be positive");
        }

        // Get outstanding advance balance
        BigDecimal outstanding = balanceRepository.findByTenantIdAndEmployeeId(request.tenantId(), request.employeeId())
                .map(EmployeeAdvanceBalance::getOutstandingAdvance)
                .orElse(BigDecimal.ZERO);

        // Validate receipt doesn't exceed outstanding (unless override)
        if (request.amount().compareTo(outstanding) > 0 && !request.override()) {
            throw new IllegalStateException(
                    "Receipt amount exceeds outstanding advance. Outstanding: " + outstanding +
                    ", Receipt: " + request.amount());
        }

        if (request.override() && (request.overrideReason() == null || request.overrideReason().isBlank())) {
            throw new IllegalArgumentException("Override reason is required when receipt exceeds outstanding");
        }

        PaymentMode paymentMode = PaymentMode.valueOf(request.paymentMode().toUpperCase());

        AdvanceReceipt receipt = new AdvanceReceipt(
                request.tenantId(),
                request.employeeId(),
                request.departmentId(),
                request.amount(),
                paymentMode,
                request.receiptDate(),
                request.createdBy()
        );

        if (request.override()) {
            receipt.setOverrideFlag(true);
            receipt.setOverrideReason(request.overrideReason());
        }

        // Auto-post receipt
        receipt.setStatus("POSTED");

        // Reduce outstanding balance
        EmployeeAdvanceBalance balance = balanceRepository
                .findByTenantIdAndEmployeeId(request.tenantId(), request.employeeId())
                .orElseGet(() -> {
                    EmployeeAdvanceBalance b = new EmployeeAdvanceBalance(request.tenantId(), request.employeeId());
                    return balanceRepository.save(b);
                });

        balance.reduceAdvance(request.amount());
        balanceRepository.save(balance);

        AdvanceReceipt saved = receiptRepository.save(receipt);

        auditLogService.logInsert(request.tenantId(), "advance_receipts", saved.getId(),
                "employeeId=" + request.employeeId() + ", amount=" + request.amount() +
                ", paymentMode=" + request.paymentMode() + ", status=POSTED" +
                (request.override() ? ", override=true" : ""));

        return AdvanceReceiptResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public AdvanceReceiptResponse getReceipt(String tenantId, Long receiptId) {
        AdvanceReceipt receipt = receiptRepository.findByIdAndTenantId(receiptId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Advance receipt not found: " + receiptId));
        return AdvanceReceiptResponse.from(receipt);
    }

    @Transactional(readOnly = true)
    public List<AdvanceReceiptResponse> getReceiptsByEmployee(String tenantId, Long employeeId) {
        return receiptRepository.findByTenantIdAndEmployeeId(tenantId, employeeId)
                .stream()
                .map(AdvanceReceiptResponse::from)
                .collect(Collectors.toList());
    }
}
