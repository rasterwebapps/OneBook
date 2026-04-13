package com.nexus.onebook.ledger.advance.service;

import com.nexus.onebook.ledger.advance.dto.AdvanceReceiptResponse;
import com.nexus.onebook.ledger.advance.dto.CreateAdvanceReceiptRequest;
import com.nexus.onebook.ledger.advance.model.*;
import com.nexus.onebook.ledger.advance.repository.*;
import com.nexus.onebook.ledger.security.AuditLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvanceReceiptServiceTest {

    @Mock private AdvanceReceiptRepository receiptRepository;
    @Mock private EmployeeAdvanceBalanceRepository balanceRepository;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private AdvanceReceiptService receiptService;

    private static final String TENANT = "tenant-1";
    private static final Long EMPLOYEE_ID = 100L;
    private static final Long DEPARTMENT_ID = 10L;

    @Test
    void createReceipt_withinOutstanding_createsSuccessfully() {
        CreateAdvanceReceiptRequest request = new CreateAdvanceReceiptRequest(
                TENANT, EMPLOYEE_ID, DEPARTMENT_ID,
                new BigDecimal("2000.00"), "CASH", LocalDate.now(),
                "user1", false, null
        );

        EmployeeAdvanceBalance balance = new EmployeeAdvanceBalance(TENANT, EMPLOYEE_ID);
        balance.setOutstandingAdvance(new BigDecimal("5000.00"));

        when(balanceRepository.findByTenantIdAndEmployeeId(TENANT, EMPLOYEE_ID))
                .thenReturn(Optional.of(balance));
        when(receiptRepository.save(any(AdvanceReceipt.class))).thenAnswer(inv -> {
            AdvanceReceipt r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AdvanceReceiptResponse response = receiptService.createReceipt(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("2000.00"), response.amount());
        assertEquals("CASH", response.paymentMode());
        assertEquals("POSTED", response.status());

        // Balance should be reduced
        assertEquals(new BigDecimal("3000.00"), balance.getOutstandingAdvance());
    }

    @Test
    void createReceipt_exceedsOutstanding_throwsException() {
        CreateAdvanceReceiptRequest request = new CreateAdvanceReceiptRequest(
                TENANT, EMPLOYEE_ID, DEPARTMENT_ID,
                new BigDecimal("10000.00"), "BANK", LocalDate.now(),
                "user1", false, null
        );

        EmployeeAdvanceBalance balance = new EmployeeAdvanceBalance(TENANT, EMPLOYEE_ID);
        balance.setOutstandingAdvance(new BigDecimal("5000.00"));

        when(balanceRepository.findByTenantIdAndEmployeeId(TENANT, EMPLOYEE_ID))
                .thenReturn(Optional.of(balance));

        assertThrows(IllegalStateException.class, () -> receiptService.createReceipt(request));
    }

    @Test
    void createReceipt_withOverride_createsSuccessfully() {
        CreateAdvanceReceiptRequest request = new CreateAdvanceReceiptRequest(
                TENANT, EMPLOYEE_ID, DEPARTMENT_ID,
                new BigDecimal("10000.00"), "BANK", LocalDate.now(),
                "user1", true, "Advance from previous month"
        );

        EmployeeAdvanceBalance balance = new EmployeeAdvanceBalance(TENANT, EMPLOYEE_ID);
        balance.setOutstandingAdvance(new BigDecimal("5000.00"));

        when(balanceRepository.findByTenantIdAndEmployeeId(TENANT, EMPLOYEE_ID))
                .thenReturn(Optional.of(balance));
        when(receiptRepository.save(any(AdvanceReceipt.class))).thenAnswer(inv -> {
            AdvanceReceipt r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AdvanceReceiptResponse response = receiptService.createReceipt(request);

        assertNotNull(response);
        assertTrue(response.overrideFlag());
        assertEquals("Advance from previous month", response.overrideReason());
    }

    @Test
    void createReceipt_overrideWithoutReason_throwsException() {
        CreateAdvanceReceiptRequest request = new CreateAdvanceReceiptRequest(
                TENANT, EMPLOYEE_ID, DEPARTMENT_ID,
                new BigDecimal("10000.00"), "BANK", LocalDate.now(),
                "user1", true, null
        );

        EmployeeAdvanceBalance balance = new EmployeeAdvanceBalance(TENANT, EMPLOYEE_ID);
        balance.setOutstandingAdvance(new BigDecimal("5000.00"));

        when(balanceRepository.findByTenantIdAndEmployeeId(TENANT, EMPLOYEE_ID))
                .thenReturn(Optional.of(balance));

        assertThrows(IllegalArgumentException.class, () -> receiptService.createReceipt(request));
    }

    @Test
    void createReceipt_negativeAmount_throwsException() {
        CreateAdvanceReceiptRequest request = new CreateAdvanceReceiptRequest(
                TENANT, EMPLOYEE_ID, DEPARTMENT_ID,
                new BigDecimal("-100.00"), "CASH", LocalDate.now(),
                "user1", false, null
        );

        assertThrows(IllegalArgumentException.class, () -> receiptService.createReceipt(request));
    }

    @Test
    void createReceipt_upiPaymentMode_createsSuccessfully() {
        CreateAdvanceReceiptRequest request = new CreateAdvanceReceiptRequest(
                TENANT, EMPLOYEE_ID, DEPARTMENT_ID,
                new BigDecimal("1500.00"), "UPI", LocalDate.now(),
                "user1", false, null
        );

        EmployeeAdvanceBalance balance = new EmployeeAdvanceBalance(TENANT, EMPLOYEE_ID);
        balance.setOutstandingAdvance(new BigDecimal("5000.00"));

        when(balanceRepository.findByTenantIdAndEmployeeId(TENANT, EMPLOYEE_ID))
                .thenReturn(Optional.of(balance));
        when(receiptRepository.save(any(AdvanceReceipt.class))).thenAnswer(inv -> {
            AdvanceReceipt r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AdvanceReceiptResponse response = receiptService.createReceipt(request);

        assertEquals("UPI", response.paymentMode());
    }
}
