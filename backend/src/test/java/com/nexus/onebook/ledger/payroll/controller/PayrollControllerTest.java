package com.nexus.onebook.ledger.payroll.controller;

import com.nexus.onebook.ledger.payroll.dto.PayrollSummary;
import com.nexus.onebook.ledger.exception.GlobalExceptionHandler;
import com.nexus.onebook.ledger.payroll.model.PayrollComponent;
import com.nexus.onebook.ledger.payroll.model.PayrollComponentType;
import com.nexus.onebook.ledger.payroll.model.PayrollEmployee;
import com.nexus.onebook.ledger.payroll.service.PayrollService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PayrollController.class)
@Import(GlobalExceptionHandler.class)
class PayrollControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PayrollService payrollService;

    @Test
    void createEmployee_validRequest_returns201() throws Exception {
        PayrollEmployee employee = new PayrollEmployee();
        employee.setId(1L);
        employee.setTenantId("t1");
        employee.setEmployeeCode("EMP001");
        employee.setEmployeeName("John Doe");

        when(payrollService.createEmployee(any())).thenReturn(employee);

        mockMvc.perform(post("/api/payroll/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "t1",
                                    "employeeCode": "EMP001",
                                    "employeeName": "John Doe",
                                    "dateOfJoining": "2024-01-15"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeCode").value("EMP001"))
                .andExpect(jsonPath("$.employeeName").value("John Doe"));
    }

    @Test
    void getEmployees_returnsList() throws Exception {
        PayrollEmployee employee = new PayrollEmployee();
        employee.setId(1L);
        employee.setTenantId("t1");
        employee.setEmployeeCode("EMP001");
        employee.setEmployeeName("John Doe");

        when(payrollService.getEmployees("t1")).thenReturn(List.of(employee));

        mockMvc.perform(get("/api/payroll/employees")
                        .param("tenantId", "t1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeCode").value("EMP001"));
    }

    @Test
    void addComponent_validRequest_returns201() throws Exception {
        PayrollComponent component = new PayrollComponent();
        component.setId(1L);
        component.setTenantId("t1");
        component.setComponentName("Basic Salary");
        component.setComponentType(PayrollComponentType.EARNING);
        component.setAmount(new BigDecimal("30000"));

        when(payrollService.addComponent(eq("t1"), eq(1L), eq("Basic Salary"),
                eq(PayrollComponentType.EARNING), any(BigDecimal.class), isNull()))
                .thenReturn(component);

        mockMvc.perform(post("/api/payroll/employees/1/components")
                        .param("tenantId", "t1")
                        .param("componentName", "Basic Salary")
                        .param("componentType", "EARNING")
                        .param("amount", "30000"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.componentName").value("Basic Salary"))
                .andExpect(jsonPath("$.componentType").value("EARNING"));
    }

    @Test
    void getPayslip_returnsSummary() throws Exception {
        PayrollSummary summary = new PayrollSummary(
                1L, "EMP001", "John Doe",
                new BigDecimal("50000"), new BigDecimal("5000"),
                new BigDecimal("3000"), new BigDecimal("45000"),
                List.of(new PayrollSummary.ComponentDetail(
                        "Basic Salary", "EARNING", new BigDecimal("30000"))));

        when(payrollService.computePayroll("t1", 1L)).thenReturn(summary);

        mockMvc.perform(get("/api/payroll/employees/1/payslip")
                        .param("tenantId", "t1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeCode").value("EMP001"))
                .andExpect(jsonPath("$.netPay").value(45000))
                .andExpect(jsonPath("$.components[0].componentName").value("Basic Salary"));
    }
}
