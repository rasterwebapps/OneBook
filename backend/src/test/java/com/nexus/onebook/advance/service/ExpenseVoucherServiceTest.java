package com.nexus.onebook.advance.service;

import com.nexus.onebook.advance.dto.CreateExpenseVoucherRequest;
import com.nexus.onebook.advance.dto.ExpenseVoucherResponse;
import com.nexus.onebook.advance.model.*;
import com.nexus.onebook.advance.repository.*;
import com.nexus.onebook.security.AuditLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseVoucherServiceTest {

    @Mock private ExpenseVoucherRepository voucherRepository;
    @Mock private EmployeeAdvanceBalanceRepository balanceRepository;
    @Mock private EmployeePaymentAdviceRepository paymentAdviceRepository;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private ExpenseVoucherService voucherService;

    private static final String TENANT = "tenant-1";
    private static final Long EMPLOYEE_ID = 100L;
    private static final Long DEPARTMENT_ID = 10L;

    @Test
    void createExpenseVoucher_createsSuccessfully() {
        CreateExpenseVoucherRequest request = new CreateExpenseVoucherRequest(
                TENANT, EMPLOYEE_ID, DEPARTMENT_ID,
                new BigDecimal("2000.00"), "Travel", "Business trip to Mumbai",
                LocalDate.now(), "receipt.pdf", null, "user1"
        );

        when(voucherRepository.save(any(ExpenseVoucher.class))).thenAnswer(inv -> {
            ExpenseVoucher v = inv.getArgument(0);
            v.setId(1L);
            return v;
        });

        ExpenseVoucherResponse response = voucherService.createExpenseVoucher(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("2000.00"), response.amount());
        assertEquals("Travel", response.expenseType());
        assertEquals("PENDING_HOD_APPROVAL", response.status());
    }

    @Test
    void createExpenseVoucher_negativeAmount_throwsException() {
        CreateExpenseVoucherRequest request = new CreateExpenseVoucherRequest(
                TENANT, EMPLOYEE_ID, DEPARTMENT_ID,
                new BigDecimal("-100.00"), "Travel", "Invalid", LocalDate.now(),
                null, null, "user1"
        );

        assertThrows(IllegalArgumentException.class, () -> voucherService.createExpenseVoucher(request));
    }

    @Test
    void approveExpenseVoucher_fullSettlement_settlesAgainstAdvance() {
        ExpenseVoucher voucher = buildVoucher(1L, new BigDecimal("3000.00"));

        EmployeeAdvanceBalance balance = new EmployeeAdvanceBalance(TENANT, EMPLOYEE_ID);
        balance.setOutstandingAdvance(new BigDecimal("5000.00"));

        when(voucherRepository.findByIdAndTenantId(1L, TENANT)).thenReturn(Optional.of(voucher));
        when(balanceRepository.findByTenantIdAndEmployeeId(TENANT, EMPLOYEE_ID)).thenReturn(Optional.of(balance));
        when(voucherRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ExpenseVoucherResponse response = voucherService.approveExpenseVoucher(TENANT, 1L, "hod_user");

        assertEquals("POSTED", response.status());
        assertEquals(new BigDecimal("3000.00"), response.advanceSettlement());
        assertNull(response.reimbursementAmount());
        assertNull(response.paymentAdviceId());

        // Balance should be reduced
        assertEquals(new BigDecimal("2000.00"), balance.getOutstandingAdvance());
    }

    @Test
    void approveExpenseVoucher_partialSettlement_createsPaymentAdvice() {
        ExpenseVoucher voucher = buildVoucher(1L, new BigDecimal("8000.00"));

        EmployeeAdvanceBalance balance = new EmployeeAdvanceBalance(TENANT, EMPLOYEE_ID);
        balance.setOutstandingAdvance(new BigDecimal("3000.00"));

        when(voucherRepository.findByIdAndTenantId(1L, TENANT)).thenReturn(Optional.of(voucher));
        when(balanceRepository.findByTenantIdAndEmployeeId(TENANT, EMPLOYEE_ID)).thenReturn(Optional.of(balance));
        when(voucherRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentAdviceRepository.save(any(EmployeePaymentAdvice.class))).thenAnswer(inv -> {
            EmployeePaymentAdvice p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        ExpenseVoucherResponse response = voucherService.approveExpenseVoucher(TENANT, 1L, "hod_user");

        assertEquals("POSTED", response.status());
        assertEquals(new BigDecimal("3000.00"), response.advanceSettlement());
        assertEquals(new BigDecimal("5000.00"), response.reimbursementAmount());
        assertEquals(10L, response.paymentAdviceId());

        // Balance should be zero
        assertEquals(0, balance.getOutstandingAdvance().compareTo(BigDecimal.ZERO));
    }

    @Test
    void approveExpenseVoucher_noAdvance_fullReimbursement() {
        ExpenseVoucher voucher = buildVoucher(1L, new BigDecimal("5000.00"));

        when(voucherRepository.findByIdAndTenantId(1L, TENANT)).thenReturn(Optional.of(voucher));
        when(balanceRepository.findByTenantIdAndEmployeeId(TENANT, EMPLOYEE_ID)).thenReturn(Optional.empty());
        when(voucherRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentAdviceRepository.save(any(EmployeePaymentAdvice.class))).thenAnswer(inv -> {
            EmployeePaymentAdvice p = inv.getArgument(0);
            p.setId(20L);
            return p;
        });

        ExpenseVoucherResponse response = voucherService.approveExpenseVoucher(TENANT, 1L, "hod_user");

        assertEquals("POSTED", response.status());
        assertNull(response.advanceSettlement());
        assertEquals(new BigDecimal("5000.00"), response.reimbursementAmount());
        assertEquals(20L, response.paymentAdviceId());
    }

    @Test
    void approveExpenseVoucher_selfApproval_throwsException() {
        ExpenseVoucher voucher = buildVoucher(1L, new BigDecimal("2000.00"));
        voucher.setCreatedBy("same_user");

        when(voucherRepository.findByIdAndTenantId(1L, TENANT)).thenReturn(Optional.of(voucher));

        assertThrows(IllegalStateException.class, () ->
                voucherService.approveExpenseVoucher(TENANT, 1L, "same_user"));
    }

    @Test
    void approveExpenseVoucher_alreadyPosted_throwsException() {
        ExpenseVoucher voucher = buildVoucher(1L, new BigDecimal("2000.00"));
        voucher.setStatus(ExpenseVoucherStatus.POSTED);

        when(voucherRepository.findByIdAndTenantId(1L, TENANT)).thenReturn(Optional.of(voucher));

        assertThrows(IllegalStateException.class, () ->
                voucherService.approveExpenseVoucher(TENANT, 1L, "hod_user"));
    }

    @Test
    void rejectExpenseVoucher_setsRejectedStatus() {
        ExpenseVoucher voucher = buildVoucher(1L, new BigDecimal("2000.00"));

        when(voucherRepository.findByIdAndTenantId(1L, TENANT)).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ExpenseVoucherResponse response = voucherService.rejectExpenseVoucher(TENANT, 1L, "hod_user", "Missing receipts");

        assertEquals("REJECTED", response.status());
        assertEquals("hod_user", response.rejectedBy());
        assertEquals("Missing receipts", response.rejectionReason());
    }

    @Test
    void rejectExpenseVoucher_withoutReason_throwsException() {
        ExpenseVoucher voucher = buildVoucher(1L, new BigDecimal("2000.00"));

        when(voucherRepository.findByIdAndTenantId(1L, TENANT)).thenReturn(Optional.of(voucher));

        assertThrows(IllegalArgumentException.class, () ->
                voucherService.rejectExpenseVoucher(TENANT, 1L, "hod_user", null));
    }

    private ExpenseVoucher buildVoucher(Long id, BigDecimal amount) {
        ExpenseVoucher voucher = new ExpenseVoucher(
                TENANT, EMPLOYEE_ID, DEPARTMENT_ID,
                amount, "Travel", "Business expense", LocalDate.now(), "user1"
        );
        voucher.setId(id);
        voucher.setStatus(ExpenseVoucherStatus.PENDING_HOD_APPROVAL);
        return voucher;
    }
}
