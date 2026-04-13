package com.nexus.onebook.ledger.payroll.service;

import com.nexus.onebook.ledger.payroll.dto.PayrollEmployeeRequest;
import com.nexus.onebook.ledger.payroll.dto.PayrollSummary;
import com.nexus.onebook.ledger.accounts.model.*;
import com.nexus.onebook.ledger.auditor.model.*;
import com.nexus.onebook.ledger.banking.model.*;
import com.nexus.onebook.ledger.clientaccount.model.*;
import com.nexus.onebook.ledger.compliance.model.*;
import com.nexus.onebook.ledger.credit.model.*;
import com.nexus.onebook.ledger.currency.model.*;
import com.nexus.onebook.ledger.entitlement.model.*;
import com.nexus.onebook.ledger.fixedasset.model.*;
import com.nexus.onebook.ledger.foundation.model.*;
import com.nexus.onebook.ledger.intelligence.model.*;
import com.nexus.onebook.ledger.inventory.model.*;
import com.nexus.onebook.ledger.operations.model.*;
import com.nexus.onebook.ledger.payroll.model.*;
import com.nexus.onebook.ledger.reporting.model.*;
import com.nexus.onebook.ledger.tenant.model.*;
import com.nexus.onebook.ledger.accounts.repository.LedgerAccountRepository;
import com.nexus.onebook.ledger.payroll.repository.PayrollComponentRepository;
import com.nexus.onebook.ledger.payroll.repository.PayrollEmployeeRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollServiceTest {

    @Mock
    private PayrollEmployeeRepository payrollEmployeeRepository;
    @Mock
    private PayrollComponentRepository payrollComponentRepository;
    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    @InjectMocks
    private PayrollService payrollService;

    @Test
    void createEmployee_validRequest_succeeds() {
        PayrollEmployeeRequest request = new PayrollEmployeeRequest(
                "tenant-1", "EMP-001", "John Doe", "Manager", "Finance",
                LocalDate.of(2024, 1, 15), "ABCDE1234F", "1234567890",
                1L, "Group A");

        LedgerAccount salaryAccount = new LedgerAccount();
        salaryAccount.setId(1L);

        when(payrollEmployeeRepository.findByTenantIdAndEmployeeCode("tenant-1", "EMP-001"))
                .thenReturn(Optional.empty());
        when(ledgerAccountRepository.findById(1L)).thenReturn(Optional.of(salaryAccount));
        when(payrollEmployeeRepository.save(any(PayrollEmployee.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PayrollEmployee result = payrollService.createEmployee(request);

        assertNotNull(result);
        assertEquals("EMP-001", result.getEmployeeCode());
        assertEquals("John Doe", result.getEmployeeName());
        verify(payrollEmployeeRepository).save(any(PayrollEmployee.class));
    }

    @Test
    void createEmployee_duplicateCode_throws() {
        PayrollEmployeeRequest request = new PayrollEmployeeRequest(
                "tenant-1", "EMP-001", "John Doe", "Manager", "Finance",
                LocalDate.of(2024, 1, 15), "ABCDE1234F", "1234567890",
                1L, "Group A");

        when(payrollEmployeeRepository.findByTenantIdAndEmployeeCode("tenant-1", "EMP-001"))
                .thenReturn(Optional.of(new PayrollEmployee()));

        assertThrows(IllegalArgumentException.class, () ->
                payrollService.createEmployee(request));
    }

    @Test
    void addComponent_validEmployee_addsSuccessfully() {
        PayrollEmployee employee = new PayrollEmployee(
                "tenant-1", "EMP-001", "John Doe", LocalDate.of(2024, 1, 15));

        when(payrollEmployeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(payrollComponentRepository.save(any(PayrollComponent.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PayrollComponent result = payrollService.addComponent(
                "tenant-1", 1L, "Basic Salary",
                PayrollComponentType.EARNING, new BigDecimal("30000"), null);

        assertNotNull(result);
        assertEquals("Basic Salary", result.getComponentName());
        assertEquals(PayrollComponentType.EARNING, result.getComponentType());
        assertEquals(new BigDecimal("30000"), result.getAmount());
    }

    @Test
    void computePayroll_earningsAndDeductions_calculatesNetPay() {
        PayrollEmployee employee = new PayrollEmployee(
                "tenant-1", "EMP-001", "John Doe", LocalDate.of(2024, 1, 15));

        PayrollComponent earning = new PayrollComponent(
                "tenant-1", employee, "Basic Salary",
                PayrollComponentType.EARNING, new BigDecimal("30000"));
        PayrollComponent deduction = new PayrollComponent(
                "tenant-1", employee, "PF Deduction",
                PayrollComponentType.DEDUCTION, new BigDecimal("3600"));

        when(payrollEmployeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(payrollComponentRepository.findByTenantIdAndEmployeeId("tenant-1", 1L))
                .thenReturn(List.of(earning, deduction));

        PayrollSummary result = payrollService.computePayroll("tenant-1", 1L);

        assertNotNull(result);
        assertEquals(new BigDecimal("30000"), result.totalEarnings());
        assertEquals(new BigDecimal("3600"), result.totalDeductions());
        // netPay = 30000 - 3600 = 26400
        assertEquals(new BigDecimal("26400"), result.netPay());
    }

    @Test
    void computePayroll_withPercentageOfBasic_calculatesFromBasic() {
        PayrollEmployee employee = new PayrollEmployee(
                "tenant-1", "EMP-002", "Jane Smith", LocalDate.of(2024, 2, 1));

        PayrollComponent basic = new PayrollComponent(
                "tenant-1", employee, "Basic",
                PayrollComponentType.EARNING, new BigDecimal("40000"));

        PayrollComponent hra = new PayrollComponent(
                "tenant-1", employee, "HRA",
                PayrollComponentType.EARNING, BigDecimal.ZERO);
        hra.setPercentageOfBasic(new BigDecimal("50"));

        when(payrollEmployeeRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(payrollComponentRepository.findByTenantIdAndEmployeeId("tenant-1", 2L))
                .thenReturn(List.of(basic, hra));

        PayrollSummary result = payrollService.computePayroll("tenant-1", 2L);

        assertNotNull(result);
        // Basic 40000 + HRA (50% of 40000 = 20000.0000) = 60000.0000
        assertEquals(new BigDecimal("60000.0000"), result.totalEarnings());
    }
}
