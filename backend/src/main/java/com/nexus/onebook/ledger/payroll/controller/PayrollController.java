package com.nexus.onebook.ledger.payroll.controller;

import com.nexus.onebook.ledger.payroll.dto.PayrollEmployeeRequest;
import com.nexus.onebook.ledger.payroll.dto.PayrollSummary;
import com.nexus.onebook.ledger.payroll.model.PayrollComponent;
import com.nexus.onebook.ledger.payroll.model.PayrollComponentType;
import com.nexus.onebook.ledger.payroll.model.PayrollEmployee;
import com.nexus.onebook.ledger.payroll.service.PayrollService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for Payroll — employee management, salary components, and payroll computation.
 */
@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @PostMapping("/employees")
    public ResponseEntity<PayrollEmployee> createEmployee(@Valid @RequestBody PayrollEmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(payrollService.createEmployee(request));
    }

    @GetMapping("/employees")
    public ResponseEntity<List<PayrollEmployee>> getEmployees(@RequestParam String tenantId) {
        return ResponseEntity.ok(payrollService.getEmployees(tenantId));
    }

    @GetMapping("/employees/by-group")
    public ResponseEntity<List<PayrollEmployee>> getByGroup(@RequestParam String tenantId,
                                                              @RequestParam String group) {
        return ResponseEntity.ok(payrollService.getEmployeesByGroup(tenantId, group));
    }

    @PostMapping("/employees/{employeeId}/components")
    public ResponseEntity<PayrollComponent> addComponent(
            @PathVariable Long employeeId,
            @RequestParam String tenantId,
            @RequestParam String componentName,
            @RequestParam PayrollComponentType componentType,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) BigDecimal percentageOfBasic) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                payrollService.addComponent(tenantId, employeeId, componentName,
                        componentType, amount, percentageOfBasic));
    }

    @GetMapping("/employees/{employeeId}/payslip")
    public ResponseEntity<PayrollSummary> getPayslip(@PathVariable Long employeeId,
                                                       @RequestParam String tenantId) {
        return ResponseEntity.ok(payrollService.computePayroll(tenantId, employeeId));
    }
}
