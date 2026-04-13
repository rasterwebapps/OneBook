package com.nexus.onebook.ledger.intelligence.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Request DTO for scenario modeling (e.g. "Revenue Drop 20%").
 */
public record ScenarioRequest(

        @NotBlank(message = "Tenant ID is required")
        String tenantId,

        @NotBlank(message = "Scenario name is required")
        String scenarioName,

        @NotNull(message = "Revenue change percent is required")
        BigDecimal revenueChangePercent,

        @NotNull(message = "Expense change percent is required")
        BigDecimal expenseChangePercent,

        BigDecimal interestRateChange,

        int projectionMonths
) {}
