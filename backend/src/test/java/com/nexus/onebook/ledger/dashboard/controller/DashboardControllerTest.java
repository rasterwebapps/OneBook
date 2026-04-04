package com.nexus.onebook.ledger.dashboard.controller;

import com.nexus.onebook.ledger.dashboard.dto.DashboardSummaryDTO;
import com.nexus.onebook.ledger.dashboard.dto.DashboardSummaryDTO.BalanceSheetSummary;
import com.nexus.onebook.ledger.dashboard.dto.DashboardSummaryDTO.CashFlowSummary;
import com.nexus.onebook.ledger.dashboard.dto.DashboardSummaryDTO.ProfitAndLossSummary;
import com.nexus.onebook.ledger.dashboard.dto.DashboardSummaryDTO.TrialBalanceSummary;
import com.nexus.onebook.ledger.dashboard.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController).build();
    }

    private DashboardSummaryDTO buildSummary() {
        return new DashboardSummaryDTO(
            "tenant-1",
            new TrialBalanceSummary(
                new BigDecimal("5000.00"), new BigDecimal("5000.00"), true, 3),
            new BalanceSheetSummary(
                new BigDecimal("10000.00"), new BigDecimal("6000.00"),
                new BigDecimal("4000.00"), true),
            new ProfitAndLossSummary(
                new BigDecimal("8000.00"), new BigDecimal("3000.00"),
                new BigDecimal("5000.00")),
            new CashFlowSummary(
                new BigDecimal("5000.00"), new BigDecimal("-1000.00"),
                new BigDecimal("500.00"), new BigDecimal("4500.00"))
        );
    }

    @Test
    void getSummary_returns200WithAggregatedData() throws Exception {
        DashboardSummaryDTO summary = buildSummary();
        when(dashboardService.getSummary("tenant-1")).thenReturn(summary);

        mockMvc.perform(get("/api/dashboard/summary")
                .param("tenantId", "tenant-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tenantId").value("tenant-1"))
            .andExpect(jsonPath("$.trialBalance.totalDebits").value(5000.00))
            .andExpect(jsonPath("$.trialBalance.balanced").value(true))
            .andExpect(jsonPath("$.trialBalance.accountCount").value(3))
            .andExpect(jsonPath("$.balanceSheet.totalAssets").value(10000.00))
            .andExpect(jsonPath("$.balanceSheet.balanced").value(true))
            .andExpect(jsonPath("$.profitAndLoss.netIncome").value(5000.00))
            .andExpect(jsonPath("$.cashFlow.netCashChange").value(4500.00));

        verify(dashboardService).getSummary("tenant-1");
    }

    @Test
    void getSummary_missingTenantId_returns400() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
            .andExpect(status().isBadRequest());
    }
}
