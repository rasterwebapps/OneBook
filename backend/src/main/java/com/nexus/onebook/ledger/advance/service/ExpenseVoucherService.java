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
 * Service for managing expense vouchers.
 * Implements expense settlement logic (BR-014.7):
 * - If expense ≤ outstanding: full settlement against advance
 * - If expense > outstanding: partial settlement + payment advice for excess
 */
@Service
public class ExpenseVoucherService {

    private final ExpenseVoucherRepository voucherRepository;
    private final EmployeeAdvanceBalanceRepository balanceRepository;
    private final EmployeePaymentAdviceRepository paymentAdviceRepository;
    private final AuditLogService auditLogService;

    public ExpenseVoucherService(
            ExpenseVoucherRepository voucherRepository,
            EmployeeAdvanceBalanceRepository balanceRepository,
            EmployeePaymentAdviceRepository paymentAdviceRepository,
            AuditLogService auditLogService) {
        this.voucherRepository = voucherRepository;
        this.balanceRepository = balanceRepository;
        this.paymentAdviceRepository = paymentAdviceRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public ExpenseVoucherResponse createExpenseVoucher(CreateExpenseVoucherRequest request) {
        // Validate amount is positive
        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Expense amount must be positive");
        }

        ExpenseVoucher voucher = new ExpenseVoucher(
                request.tenantId(),
                request.employeeId(),
                request.departmentId(),
                request.amount(),
                request.expenseType(),
                request.description(),
                request.voucherDate(),
                request.createdBy()
        );

        voucher.setSupportingDocRef(request.supportingDocRef());

        // Submit for HOD approval immediately
        voucher.setStatus(ExpenseVoucherStatus.PENDING_HOD_APPROVAL);

        ExpenseVoucher saved = voucherRepository.save(voucher);

        auditLogService.logInsert(request.tenantId(), "expense_vouchers", saved.getId(),
                "employeeId=" + request.employeeId() + ", amount=" + request.amount() +
                ", expenseType=" + request.expenseType() + ", status=PENDING_HOD_APPROVAL");

        return ExpenseVoucherResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ExpenseVoucherResponse getExpenseVoucher(String tenantId, Long voucherId) {
        ExpenseVoucher voucher = voucherRepository.findByIdAndTenantId(voucherId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Expense voucher not found: " + voucherId));
        return ExpenseVoucherResponse.from(voucher);
    }

    @Transactional(readOnly = true)
    public List<ExpenseVoucherResponse> getExpensesByEmployee(String tenantId, Long employeeId) {
        return voucherRepository.findByTenantIdAndEmployeeId(tenantId, employeeId)
                .stream()
                .map(ExpenseVoucherResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExpenseVoucherResponse> getPendingApprovals(String tenantId) {
        return voucherRepository.findByTenantIdAndStatus(tenantId, ExpenseVoucherStatus.PENDING_HOD_APPROVAL)
                .stream()
                .map(ExpenseVoucherResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExpenseVoucherResponse> getPendingApprovalsByDepartments(String tenantId, List<Long> departmentIds) {
        return voucherRepository.findByTenantIdAndDepartmentIdInAndStatus(tenantId, departmentIds, ExpenseVoucherStatus.PENDING_HOD_APPROVAL)
                .stream()
                .map(ExpenseVoucherResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * HOD approves expense voucher.
     * Executes settlement logic (BR-014.7):
     * - Settles against outstanding advance
     * - Creates payment advice for any excess (reimbursement)
     */
    @Transactional
    public ExpenseVoucherResponse approveExpenseVoucher(String tenantId, Long voucherId, String approverId) {
        ExpenseVoucher voucher = voucherRepository.findByIdAndTenantId(voucherId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Expense voucher not found: " + voucherId));

        if (voucher.getStatus() != ExpenseVoucherStatus.PENDING_HOD_APPROVAL) {
            throw new IllegalStateException("Expense voucher is not pending HOD approval");
        }

        // Prevent self-approval
        if (voucher.getCreatedBy().equals(approverId)) {
            throw new IllegalStateException("Cannot approve your own expense voucher");
        }

        // Get outstanding advance balance
        BigDecimal outstanding = balanceRepository.findByTenantIdAndEmployeeId(tenantId, voucher.getEmployeeId())
                .map(EmployeeAdvanceBalance::getOutstandingAdvance)
                .orElse(BigDecimal.ZERO);

        BigDecimal expenseAmount = voucher.getAmount();
        BigDecimal advancePortion = outstanding.min(expenseAmount);
        BigDecimal reimbursePortion = expenseAmount.subtract(advancePortion);

        // Settle against advance
        if (advancePortion.compareTo(BigDecimal.ZERO) > 0) {
            voucher.setAdvanceSettlement(advancePortion);

            // Reduce outstanding balance
            EmployeeAdvanceBalance balance = balanceRepository
                    .findByTenantIdAndEmployeeId(tenantId, voucher.getEmployeeId())
                    .orElseThrow();
            balance.reduceAdvance(advancePortion);
            balanceRepository.save(balance);
        }

        // Create payment advice for reimbursement if needed
        if (reimbursePortion.compareTo(BigDecimal.ZERO) > 0) {
            voucher.setReimbursementAmount(reimbursePortion);

            EmployeePaymentAdvice paymentAdvice = new EmployeePaymentAdvice(
                    tenantId,
                    voucher.getEmployeeId(),
                    voucher.getDepartmentId(),
                    reimbursePortion,
                    voucher.getId()
            );
            EmployeePaymentAdvice savedAdvice = paymentAdviceRepository.save(paymentAdvice);
            voucher.setPaymentAdviceId(savedAdvice.getId());

            auditLogService.logInsert(tenantId, "payment_advices_m12", savedAdvice.getId(),
                    "employeeId=" + voucher.getEmployeeId() + ", amount=" + reimbursePortion +
                    ", expenseVoucherId=" + voucherId);
        }

        voucher.setStatus(ExpenseVoucherStatus.POSTED);
        voucher.setApprovedBy(approverId);
        voucher.setApprovedAt(Instant.now());

        ExpenseVoucher saved = voucherRepository.save(voucher);

        auditLogService.logUpdate(tenantId, "expense_vouchers", voucherId,
                "status=PENDING_HOD_APPROVAL",
                "status=POSTED, approvedBy=" + approverId +
                ", advanceSettlement=" + advancePortion +
                ", reimbursement=" + reimbursePortion);

        return ExpenseVoucherResponse.from(saved);
    }

    @Transactional
    public ExpenseVoucherResponse rejectExpenseVoucher(String tenantId, Long voucherId, String rejectorId, String reason) {
        ExpenseVoucher voucher = voucherRepository.findByIdAndTenantId(voucherId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Expense voucher not found: " + voucherId));

        if (voucher.getStatus() != ExpenseVoucherStatus.PENDING_HOD_APPROVAL) {
            throw new IllegalStateException("Expense voucher is not pending HOD approval");
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }

        voucher.setStatus(ExpenseVoucherStatus.REJECTED);
        voucher.setRejectedBy(rejectorId);
        voucher.setRejectedAt(Instant.now());
        voucher.setRejectionReason(reason);

        ExpenseVoucher saved = voucherRepository.save(voucher);

        auditLogService.logUpdate(tenantId, "expense_vouchers", voucherId,
                "status=PENDING_HOD_APPROVAL",
                "status=REJECTED, rejectedBy=" + rejectorId + ", reason=" + reason);

        return ExpenseVoucherResponse.from(saved);
    }
}
