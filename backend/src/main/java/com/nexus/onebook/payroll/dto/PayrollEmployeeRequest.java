package com.nexus.onebook.payroll.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record PayrollEmployeeRequest(
    @NotBlank String tenantId,
    @NotBlank String employeeCode,
    @NotBlank String employeeName,
    String designation,
    String department,
    @NotNull LocalDate dateOfJoining,
    String panNumber,
    String bankAccount,
    Long salaryAccountId,
    String employeeGroup
) {}
