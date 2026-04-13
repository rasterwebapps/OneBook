package com.nexus.onebook.ledger.intelligence.controller;

import com.nexus.onebook.ledger.intelligence.dto.CashFlowForecast;
import com.nexus.onebook.ledger.intelligence.dto.ScenarioResult;
import com.nexus.onebook.ledger.exception.GlobalExceptionHandler;
import com.nexus.onebook.ledger.intelligence.service.ForecastingService;
import com.nexus.onebook.ledger.intelligence.service.ScenarioModelingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ForecastController.class)
@Import(GlobalExceptionHandler.class)
class ForecastControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ForecastingService forecastingService;

    @MockitoBean
    private ScenarioModelingService scenarioModelingService;

    @Test
    void getForecast_returnsOk() throws Exception {
        CashFlowForecast forecast = new CashFlowForecast(
                "t1",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "LOW",
                LocalDate.now()
        );

        when(forecastingService.generateForecast("t1")).thenReturn(forecast);

        mockMvc.perform(get("/api/forecast").param("tenantId", "t1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("t1"))
                .andExpect(jsonPath("$.riskLevel").value("LOW"));
    }

    @Test
    void runScenario_returnsOk() throws Exception {
        ScenarioResult result = new ScenarioResult(
                "t1",
                "Revenue Drop",
                new BigDecimal("1000.00"),
                new BigDecimal("800.00"),
                new BigDecimal("750.00"),
                new BigDecimal("-200.00"),
                new BigDecimal("-20.00"),
                "Projected net income decreases by 20%"
        );

        when(scenarioModelingService.runScenario(any())).thenReturn(result);

        mockMvc.perform(post("/api/forecast/scenario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "t1",
                                    "scenarioName": "Revenue Drop",
                                    "revenueChangePercent": -20,
                                    "expenseChangePercent": 0,
                                    "interestRateChange": 0,
                                    "projectionMonths": 12
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("t1"))
                .andExpect(jsonPath("$.scenarioName").value("Revenue Drop"));
    }
}
