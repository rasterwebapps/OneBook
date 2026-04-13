package com.nexus.onebook.banking.service;

import com.nexus.onebook.banking.dto.ConnectedPaymentRequest;
import com.nexus.onebook.banking.model.ConnectedPayment;
import com.nexus.onebook.accounts.model.LedgerAccount;
import com.nexus.onebook.foundation.model.PaymentMode;
import com.nexus.onebook.banking.model.PaymentStatus;
import com.nexus.onebook.banking.repository.ConnectedPaymentRepository;
import com.nexus.onebook.accounts.repository.LedgerAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectedPaymentServiceTest {

    @Mock
    private ConnectedPaymentRepository connectedPaymentRepository;
    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    @InjectMocks
    private ConnectedPaymentService connectedPaymentService;

    private LedgerAccount bankAccount;

    @BeforeEach
    void setUp() {
        bankAccount = new LedgerAccount();
        bankAccount.setId(1L);
    }

    @Test
    void initiatePayment_validRequest_succeeds() {
        ConnectedPaymentRequest request = new ConnectedPaymentRequest(
                "tenant-1", 1L, "Vendor A", "9876543210",
                "HDFC0001234", new BigDecimal("50000"), "NEFT");

        when(ledgerAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccount));
        when(connectedPaymentRepository.save(any(ConnectedPayment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ConnectedPayment result = connectedPaymentService.initiatePayment(request);

        assertNotNull(result);
        verify(connectedPaymentRepository).save(any(ConnectedPayment.class));
    }

    @Test
    void initiatePayment_bankAccountNotFound_throws() {
        ConnectedPaymentRequest request = new ConnectedPaymentRequest(
                "tenant-1", 99L, "Vendor A", "9876543210",
                "HDFC0001234", new BigDecimal("50000"), "NEFT");

        when(ledgerAccountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                connectedPaymentService.initiatePayment(request));
    }

    @Test
    void completePayment_validPayment_setsCompletedStatus() {
        ConnectedPayment payment = new ConnectedPayment(
                "tenant-1", bankAccount, "Vendor A", "9876543210",
                new BigDecimal("50000"), PaymentMode.NEFT);

        when(connectedPaymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(connectedPaymentRepository.save(any(ConnectedPayment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ConnectedPayment result = connectedPaymentService.completePayment(1L, "REF-001");

        assertEquals(PaymentStatus.COMPLETED, result.getStatus());
        assertEquals("REF-001", result.getReferenceNumber());
        assertNotNull(result.getCompletedAt());
    }

    @Test
    void failPayment_validPayment_setsFailedStatus() {
        ConnectedPayment payment = new ConnectedPayment(
                "tenant-1", bankAccount, "Vendor B", "1234567890",
                new BigDecimal("25000"), PaymentMode.RTGS);

        when(connectedPaymentRepository.findById(2L)).thenReturn(Optional.of(payment));
        when(connectedPaymentRepository.save(any(ConnectedPayment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ConnectedPayment result = connectedPaymentService.failPayment(2L, "Account closed");

        assertEquals(PaymentStatus.FAILED, result.getStatus());
        assertEquals("Account closed", result.getFailureReason());
    }

    @Test
    void getPaymentsByStatus_completed_returnsFilteredList() {
        ConnectedPayment payment = new ConnectedPayment(
                "tenant-1", bankAccount, "Vendor A", "9876543210",
                new BigDecimal("50000"), PaymentMode.NEFT);
        payment.setStatus(PaymentStatus.COMPLETED);

        when(connectedPaymentRepository.findByTenantIdAndStatus("tenant-1", PaymentStatus.COMPLETED))
                .thenReturn(List.of(payment));

        List<ConnectedPayment> result = connectedPaymentService.getPaymentsByStatus(
                "tenant-1", PaymentStatus.COMPLETED);

        assertEquals(1, result.size());
        assertEquals(PaymentStatus.COMPLETED, result.get(0).getStatus());
    }
}
