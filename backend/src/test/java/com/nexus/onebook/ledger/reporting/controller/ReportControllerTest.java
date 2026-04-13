package com.nexus.onebook.ledger.reporting.controller;

import com.nexus.onebook.ledger.accounts.dto.TrialBalanceLine;
import com.nexus.onebook.ledger.accounts.dto.TrialBalanceReport;
import com.nexus.onebook.ledger.exception.GlobalExceptionHandler;
import com.nexus.onebook.ledger.accounts.service.BalanceSheetService;
import com.nexus.onebook.ledger.accounts.service.CashFlowService;
import com.nexus.onebook.ledger.accounts.service.ProfitAndLossService;
import com.nexus.onebook.ledger.accounts.service.TrialBalanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@Import(GlobalExceptionHandler.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrialBalanceService trialBalanceService;

    @MockitoBean
    private ProfitAndLossService profitAndLossService;

    @MockitoBean
    private BalanceSheetService balanceSheetService;

    @MockitoBean
    private CashFlowService cashFlowService;

    @Test
    void getTrialBalance_noDateRange_returnsReport() throws Exception {
        TrialBalanceReport report = new TrialBalanceReport(
                "tenant-1",
                List.of(new TrialBalanceLine(1L, "1000", "Cash", "ASSET",
                        new BigDecimal("500.0000"), BigDecimal.ZERO)),
                new BigDecimal("500.0000"),
                new BigDecimal("500.0000"),
                true
        );

        when(trialBalanceService.generateTrialBalance("tenant-1", null, null)).thenReturn(report);

        mockMvc.perform(get("/api/reports/trial-balance")
                        .param("tenantId", "tenant-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("tenant-1"))
                .andExpect(jsonPath("$.balanced").value(true))
                .andExpect(jsonPath("$.lines.length()").value(1));
    }

    @Test
    void getTrialBalance_withDateRange_returnsReport() throws Exception {
        TrialBalanceReport report = new TrialBalanceReport(
                "tenant-1",
                List.of(new TrialBalanceLine(1L, "1000", "Cash", "ASSET",
                        new BigDecimal("200.0000"), BigDecimal.ZERO)),
                new BigDecimal("200.0000"),
                new BigDecimal("200.0000"),
                true
        );
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 3, 31);

        when(trialBalanceService.generateTrialBalance("tenant-1", from, to)).thenReturn(report);

        mockMvc.perform(get("/api/reports/trial-balance")
                        .param("tenantId", "tenant-1")
                        .param("fromDate", "2025-01-01")
                        .param("toDate", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("tenant-1"))
                .andExpect(jsonPath("$.lines.length()").value(1));
    }
}
