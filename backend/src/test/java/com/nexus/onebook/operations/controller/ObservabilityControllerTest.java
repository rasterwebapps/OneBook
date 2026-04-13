package com.nexus.onebook.operations.controller;

import com.nexus.onebook.exception.GlobalExceptionHandler;
import com.nexus.onebook.operations.service.ObservabilityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ObservabilityController.class)
@Import(GlobalExceptionHandler.class)
class ObservabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ObservabilityService observabilityService;

    @Test
    void getMetrics_returnsMetrics() throws Exception {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("jvmMemoryUsedMb", 128L);
        metrics.put("jvmMemoryMaxMb", 512L);
        metrics.put("availableProcessors", 4);

        when(observabilityService.getHealthMetrics()).thenReturn(metrics);

        mockMvc.perform(get("/api/observability/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jvmMemoryUsedMb").value(128))
                .andExpect(jsonPath("$.availableProcessors").value(4));
    }

    @Test
    void getTracing_returnsTracingInfo() throws Exception {
        Map<String, Object> tracing = new LinkedHashMap<>();
        tracing.put("traceId", "trace-123");
        tracing.put("spanId", "span-456");
        tracing.put("service", "onebook-backend");

        when(observabilityService.getTracingInfo("trace-123", "span-456")).thenReturn(tracing);

        mockMvc.perform(get("/api/observability/tracing")
                        .param("traceId", "trace-123")
                        .param("spanId", "span-456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.traceId").value("trace-123"))
                .andExpect(jsonPath("$.service").value("onebook-backend"));
    }
}
