package com.nexus.onebook.ledger.advance.service;

import com.nexus.onebook.ledger.advance.dto.PaymentAdviceResponse;
import com.nexus.onebook.ledger.advance.model.*;
import com.nexus.onebook.ledger.advance.repository.EmployeePaymentAdviceRepository;
import com.nexus.onebook.ledger.security.AuditLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentAdviceServiceTest {

    @Mock private EmployeePaymentAdviceRepository paymentAdviceRepository;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private PaymentAdviceService paymentAdviceService;

    private static final String TENANT = "tenant-1";
    private static final Long EMPLOYEE_ID = 100L;
    private static final Long DEPARTMENT_ID = 10L;

    @Test
    void getPaymentAdvice_returnsAdvice() {
        EmployeePaymentAdvice advice = buildPaymentAdvice(1L, new BigDecimal("5000.00"));

        when(paymentAdviceRepository.findByIdAndTenantId(1L, TENANT)).thenReturn(Optional.of(advice));

        PaymentAdviceResponse response = paymentAdviceService.getPaymentAdvice(TENANT, 1L);

        assertNotNull(response);
        assertEquals(new BigDecimal("5000.00"), response.amount());
        assertEquals("PENDING_PAYMENT", response.status());
    }

    @Test
    void getPaymentAdvice_notFound_throwsException() {
        when(paymentAdviceRepository.findByIdAndTenantId(999L, TENANT)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                paymentAdviceService.getPaymentAdvice(TENANT, 999L));
    }

    @Test
    void getPendingPaymentAdvices_returnsList() {
        List<EmployeePaymentAdvice> advices = List.of(
                buildPaymentAdvice(1L, new BigDecimal("3000.00")),
                buildPaymentAdvice(2L, new BigDecimal("2000.00"))
        );

        when(paymentAdviceRepository.findByTenantIdAndStatus(TENANT, PaymentAdviceStatus.PENDING_PAYMENT))
                .thenReturn(advices);

        List<PaymentAdviceResponse> result = paymentAdviceService.getPendingPaymentAdvices(TENANT);

        assertEquals(2, result.size());
    }

    @Test
    void payAdvice_updatesStatus() {
        EmployeePaymentAdvice advice = buildPaymentAdvice(1L, new BigDecimal("5000.00"));

        when(paymentAdviceRepository.findByIdAndTenantId(1L, TENANT)).thenReturn(Optional.of(advice));
        when(paymentAdviceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentAdviceResponse response = paymentAdviceService.payAdvice(TENANT, 1L, "finance_user", 100L);

        assertEquals("PAID", response.status());
        assertEquals("finance_user", response.paidBy());
        assertEquals(100L, response.paymentVoucherId());
        assertNotNull(response.paidAt());
    }

    @Test
    void payAdvice_alreadyPaid_throwsException() {
        EmployeePaymentAdvice advice = buildPaymentAdvice(1L, new BigDecimal("5000.00"));
        advice.setStatus(PaymentAdviceStatus.PAID);

        when(paymentAdviceRepository.findByIdAndTenantId(1L, TENANT)).thenReturn(Optional.of(advice));

        assertThrows(IllegalStateException.class, () ->
                paymentAdviceService.payAdvice(TENANT, 1L, "finance_user", 100L));
    }

    private EmployeePaymentAdvice buildPaymentAdvice(Long id, BigDecimal amount) {
        EmployeePaymentAdvice advice = new EmployeePaymentAdvice(
                TENANT, EMPLOYEE_ID, DEPARTMENT_ID, amount, 50L
        );
        advice.setId(id);
        return advice;
    }
}
