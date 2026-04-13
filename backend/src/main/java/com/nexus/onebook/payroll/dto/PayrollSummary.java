package com.nexus.onebook.payroll.dto;

import java.math.BigDecimal;
import java.util.List;

public record PayrollSummary(
    Long employeeId,
    String employeeCode,
    String employeeName,
    BigDecimal totalEarnings,
    BigDecimal totalDeductions,
    BigDecimal employerContributions,
    BigDecimal netPay,
    List<ComponentDetail> components
) {
    public record ComponentDetail(
        String componentName,
        String componentType,
        BigDecimal amount
    ) {}
}
