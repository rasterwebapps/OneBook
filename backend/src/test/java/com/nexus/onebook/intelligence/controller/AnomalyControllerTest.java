package com.nexus.onebook.intelligence.controller;

import com.nexus.onebook.intelligence.dto.TransactionAnomaly;
import com.nexus.onebook.exception.GlobalExceptionHandler;
import com.nexus.onebook.intelligence.service.AnomalyDetectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnomalyController.class)
@Import(GlobalExceptionHandler.class)
class AnomalyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnomalyDetectionService anomalyDetectionService;

    @Test
    void detectAnomalies_returnsOk() throws Exception {
        TransactionAnomaly anomaly = new TransactionAnomaly(
                1L,
                "t1",
                "DUPLICATE",
                "Duplicate transaction detected",
                new BigDecimal("500.00"),
                new BigDecimal("100.00"),
                0.95,
                "HIGH"
        );

        when(anomalyDetectionService.detectAnomalies("t1")).thenReturn(List.of(anomaly));

        mockMvc.perform(get("/api/anomalies").param("tenantId", "t1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].anomalyType").value("DUPLICATE"))
                .andExpect(jsonPath("$[0].severity").value("HIGH"))
                .andExpect(jsonPath("$[0].confidenceScore").value(0.95));
    }
}
