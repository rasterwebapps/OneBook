package com.nexus.onebook.advance.service;

import com.nexus.onebook.advance.dto.PaymentAdviceResponse;
import com.nexus.onebook.advance.model.*;
import com.nexus.onebook.advance.repository.EmployeePaymentAdviceRepository;
import com.nexus.onebook.security.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing payment advices.
 * Payment advice represents a liability (Employee Reimbursement Payable).
 */
@Service("advancePaymentAdviceService")
public class PaymentAdviceService {

    private final EmployeePaymentAdviceRepository paymentAdviceRepository;
    private final AuditLogService auditLogService;

    public PaymentAdviceService(
            EmployeePaymentAdviceRepository paymentAdviceRepository,
            AuditLogService auditLogService) {
        this.paymentAdviceRepository = paymentAdviceRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PaymentAdviceResponse getPaymentAdvice(String tenantId, Long adviceId) {
        EmployeePaymentAdvice advice = paymentAdviceRepository.findByIdAndTenantId(adviceId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Payment advice not found: " + adviceId));
        return PaymentAdviceResponse.from(advice);
    }

    @Transactional(readOnly = true)
    public List<PaymentAdviceResponse> getPaymentAdvicesByEmployee(String tenantId, Long employeeId) {
        return paymentAdviceRepository.findByTenantIdAndEmployeeId(tenantId, employeeId)
                .stream()
                .map(PaymentAdviceResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentAdviceResponse> getPendingPaymentAdvices(String tenantId) {
        return paymentAdviceRepository.findByTenantIdAndStatus(tenantId, PaymentAdviceStatus.PENDING_PAYMENT)
                .stream()
                .map(PaymentAdviceResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Finance team pays a payment advice.
     * Posts: Dr Employee Reimbursement Payable / Cr Cash/Bank
     */
    @Transactional
    public PaymentAdviceResponse payAdvice(String tenantId, Long adviceId, String paidBy, Long paymentVoucherId) {
        EmployeePaymentAdvice advice = paymentAdviceRepository.findByIdAndTenantId(adviceId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Payment advice not found: " + adviceId));

        if (advice.getStatus() != PaymentAdviceStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Payment advice is not pending payment");
        }

        advice.setStatus(PaymentAdviceStatus.PAID);
        advice.setPaidBy(paidBy);
        advice.setPaidAt(Instant.now());
        advice.setPaymentVoucherId(paymentVoucherId);

        EmployeePaymentAdvice saved = paymentAdviceRepository.save(advice);

        auditLogService.logUpdate(tenantId, "payment_advices_m12", adviceId,
                "status=PENDING_PAYMENT",
                "status=PAID, paidBy=" + paidBy + ", paymentVoucherId=" + paymentVoucherId);

        return PaymentAdviceResponse.from(saved);
    }
}
