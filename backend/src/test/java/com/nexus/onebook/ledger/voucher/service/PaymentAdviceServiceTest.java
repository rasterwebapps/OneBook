package com.nexus.onebook.ledger.voucher.service;

import com.nexus.onebook.ledger.model.*;
import com.nexus.onebook.ledger.repository.*;
import com.nexus.onebook.ledger.voucher.dto.*;
import com.nexus.onebook.ledger.voucher.model.*;
import com.nexus.onebook.ledger.voucher.repository.PaymentAdviceRepository;
import com.nexus.onebook.ledger.voucher.repository.VoucherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentAdviceServiceTest {

    @Mock private PaymentAdviceRepository paymentAdviceRepository;
    @Mock private VoucherRepository voucherRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private PayerRepository payerRepository;
    @Mock private PayerBankAccountRepository payerBankAccountRepository;
    @Mock private PayeeRepository payeeRepository;
    @Mock private PayeeBankAccountRepository payeeBankAccountRepository;

    @InjectMocks
    private PaymentAdviceService paymentAdviceService;

    private PaymentAdvice sampleAdvice;

    @BeforeEach
    void setUp() {
        sampleAdvice = new PaymentAdvice("tenant1", "PA-001", new BigDecimal("8000.0000"), "admin");
        sampleAdvice.setId(1L);
    }

    @Test
    void createPaymentAdvice_shouldPersistAndReturn() {
        CreatePaymentAdviceRequest request = new CreatePaymentAdviceRequest(
                "tenant1", "PA-001", null, null, null, null, null, null, null,
                new BigDecimal("8000.0000"), null, null, "Payment for services", "admin");

        when(paymentAdviceRepository.save(any(PaymentAdvice.class))).thenAnswer(i -> {
            PaymentAdvice pa = i.getArgument(0);
            pa.setId(1L);
            return pa;
        });

        PaymentAdviceResponse response = paymentAdviceService.createPaymentAdvice(request);

        assertNotNull(response);
        assertEquals("PA-001", response.adviceNumber());
        assertEquals("PENDING", response.status());
        assertEquals(new BigDecimal("8000.0000"), response.amount());
    }

    @Test
    void getByTenant_shouldReturnList() {
        when(paymentAdviceRepository.findByTenantId("tenant1")).thenReturn(List.of(sampleAdvice));

        List<PaymentAdviceResponse> results = paymentAdviceService.getByTenant("tenant1");

        assertEquals(1, results.size());
    }

    @Test
    void approve_shouldUpdateStatus() {
        when(paymentAdviceRepository.findById(1L)).thenReturn(Optional.of(sampleAdvice));
        when(paymentAdviceRepository.save(any(PaymentAdvice.class))).thenAnswer(i -> i.getArgument(0));

        PaymentAdviceResponse response = paymentAdviceService.approve(1L, "manager");

        assertEquals("APPROVED", response.status());
        assertEquals("manager", response.approvedBy());
        assertNotNull(response.approvedAt());
    }

    @Test
    void approve_shouldRejectIfNotPending() {
        sampleAdvice.setStatus(PaymentAdviceStatus.APPROVED);
        when(paymentAdviceRepository.findById(1L)).thenReturn(Optional.of(sampleAdvice));

        assertThrows(IllegalStateException.class,
                () -> paymentAdviceService.approve(1L, "manager"));
    }

    @Test
    void reject_shouldUpdateStatus() {
        when(paymentAdviceRepository.findById(1L)).thenReturn(Optional.of(sampleAdvice));
        when(paymentAdviceRepository.save(any(PaymentAdvice.class))).thenAnswer(i -> i.getArgument(0));

        PaymentAdviceResponse response = paymentAdviceService.reject(1L, "manager", "Insufficient docs");

        assertEquals("REJECTED", response.status());
        assertEquals("Insufficient docs", response.rejectionReason());
    }

    @Test
    void markPaid_shouldUpdateStatusAndReference() {
        sampleAdvice.setStatus(PaymentAdviceStatus.APPROVED);
        when(paymentAdviceRepository.findById(1L)).thenReturn(Optional.of(sampleAdvice));
        when(paymentAdviceRepository.save(any(PaymentAdvice.class))).thenAnswer(i -> i.getArgument(0));

        PaymentAdviceResponse response = paymentAdviceService.markPaid(1L, "TXN-12345");

        assertEquals("PAID", response.status());
        assertEquals("TXN-12345", response.transactionReference());
        assertNotNull(response.paidAt());
    }

    @Test
    void markPaid_shouldRejectIfNotApproved() {
        when(paymentAdviceRepository.findById(1L)).thenReturn(Optional.of(sampleAdvice));

        assertThrows(IllegalStateException.class,
                () -> paymentAdviceService.markPaid(1L, "TXN-12345"));
    }
}
