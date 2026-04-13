package com.nexus.onebook.payroll.service;

import com.nexus.onebook.payroll.dto.PayrollEmployeeRequest;
import com.nexus.onebook.payroll.dto.PayrollSummary;
import com.nexus.onebook.accounts.model.*;
import com.nexus.onebook.auditor.model.*;
import com.nexus.onebook.banking.model.*;
import com.nexus.onebook.clientaccount.model.*;
import com.nexus.onebook.compliance.model.*;
import com.nexus.onebook.credit.model.*;
import com.nexus.onebook.currency.model.*;
import com.nexus.onebook.entitlement.model.*;
import com.nexus.onebook.fixedasset.model.*;
import com.nexus.onebook.foundation.model.*;
import com.nexus.onebook.intelligence.model.*;
import com.nexus.onebook.inventory.model.*;
import com.nexus.onebook.operations.model.*;
import com.nexus.onebook.payroll.model.*;
import com.nexus.onebook.reporting.model.*;
import com.nexus.onebook.tenant.model.*;
import com.nexus.onebook.accounts.repository.LedgerAccountRepository;
import com.nexus.onebook.payroll.repository.PayrollComponentRepository;
import com.nexus.onebook.payroll.repository.PayrollEmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Payroll service — manage employees, salary components (Basic, HRA, PF, ESI, Gratuity),
 * and compute net pay with earnings, deductions, and employer contributions.
 */
@Service
public class PayrollService {

    private final PayrollEmployeeRepository employeeRepository;
    private final PayrollComponentRepository componentRepository;
    private final LedgerAccountRepository ledgerAccountRepository;

    public PayrollService(PayrollEmployeeRepository employeeRepository,
                          PayrollComponentRepository componentRepository,
                          LedgerAccountRepository ledgerAccountRepository) {
        this.employeeRepository = employeeRepository;
        this.componentRepository = componentRepository;
        this.ledgerAccountRepository = ledgerAccountRepository;
    }

    @Transactional
    public PayrollEmployee createEmployee(PayrollEmployeeRequest request) {
        employeeRepository.findByTenantIdAndEmployeeCode(request.tenantId(), request.employeeCode())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Employee '" + request.employeeCode() + "' already exists");
                });

        PayrollEmployee employee = new PayrollEmployee(request.tenantId(),
                request.employeeCode(), request.employeeName(), request.dateOfJoining());

        if (request.designation() != null) employee.setDesignation(request.designation());
        if (request.department() != null) employee.setDepartment(request.department());
        if (request.panNumber() != null) employee.setPanNumber(request.panNumber());
        if (request.bankAccount() != null) employee.setBankAccount(request.bankAccount());
        if (request.employeeGroup() != null) employee.setEmployeeGroup(request.employeeGroup());
        if (request.salaryAccountId() != null) {
            LedgerAccount salaryAccount = ledgerAccountRepository.findById(request.salaryAccountId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Salary account not found: " + request.salaryAccountId()));
            employee.setSalaryAccount(salaryAccount);
        }

        return employeeRepository.save(employee);
    }

    @Transactional(readOnly = true)
    public List<PayrollEmployee> getEmployees(String tenantId) {
        return employeeRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public List<PayrollEmployee> getEmployeesByGroup(String tenantId, String group) {
        return employeeRepository.findByTenantIdAndEmployeeGroup(tenantId, group);
    }

    @Transactional
    public PayrollComponent addComponent(String tenantId, Long employeeId, String componentName,
                                          PayrollComponentType componentType, BigDecimal amount,
                                          BigDecimal percentageOfBasic) {
        PayrollEmployee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        PayrollComponent component = new PayrollComponent(tenantId, employee,
                componentName, componentType, amount);
        if (percentageOfBasic != null) component.setPercentageOfBasic(percentageOfBasic);
        return componentRepository.save(component);
    }

    @Transactional(readOnly = true)
    public PayrollSummary computePayroll(String tenantId, Long employeeId) {
        PayrollEmployee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        List<PayrollComponent> components = componentRepository
                .findByTenantIdAndEmployeeId(tenantId, employeeId);

        // Find basic salary for percentage-based calculations
        BigDecimal basicSalary = components.stream()
                .filter(c -> "Basic".equalsIgnoreCase(c.getComponentName())
                        && c.getComponentType() == PayrollComponentType.EARNING)
                .map(PayrollComponent::getAmount)
                .findFirst()
                .orElse(BigDecimal.ZERO);

        BigDecimal totalEarnings = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;
        BigDecimal employerContributions = BigDecimal.ZERO;

        List<PayrollSummary.ComponentDetail> details = new java.util.ArrayList<>();

        for (PayrollComponent comp : components) {
            BigDecimal effectiveAmount = comp.getAmount();
            if (comp.getPercentageOfBasic() != null && basicSalary.compareTo(BigDecimal.ZERO) > 0) {
                effectiveAmount = basicSalary.multiply(comp.getPercentageOfBasic())
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            }

            details.add(new PayrollSummary.ComponentDetail(
                    comp.getComponentName(), comp.getComponentType().name(), effectiveAmount));

            switch (comp.getComponentType()) {
                case EARNING -> totalEarnings = totalEarnings.add(effectiveAmount);
                case DEDUCTION -> totalDeductions = totalDeductions.add(effectiveAmount);
                case EMPLOYER_CONTRIBUTION -> employerContributions = employerContributions.add(effectiveAmount);
            }
        }

        BigDecimal netPay = totalEarnings.subtract(totalDeductions);

        return new PayrollSummary(employee.getId(), employee.getEmployeeCode(),
                employee.getEmployeeName(), totalEarnings, totalDeductions,
                employerContributions, netPay, details);
    }
}
