package com.nexus.onebook.operations.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ObservabilityServiceTest {

    private final ObservabilityService observabilityService = new ObservabilityService();

    @Test
    void getHealthMetrics_returnsRequiredFields() {
        Map<String, Object> metrics = observabilityService.getHealthMetrics();

        assertNotNull(metrics);
        assertTrue(metrics.containsKey("timestamp"));
        assertTrue(metrics.containsKey("jvmMemoryUsedMb"));
        assertTrue(metrics.containsKey("jvmMemoryMaxMb"));
        assertTrue(metrics.containsKey("jvmMemoryUsagePercent"));
        assertTrue(metrics.containsKey("availableProcessors"));
        assertTrue(metrics.containsKey("uptimeMs"));
        assertTrue(metrics.containsKey("threadCount"));
    }

    @Test
    void getHealthMetrics_memoryValuesArePositive() {
        Map<String, Object> metrics = observabilityService.getHealthMetrics();

        long usedMb = (long) metrics.get("jvmMemoryUsedMb");
        long maxMb = (long) metrics.get("jvmMemoryMaxMb");
        assertTrue(usedMb > 0);
        assertTrue(maxMb > 0);
        assertTrue(usedMb <= maxMb);
    }

    @Test
    void getTracingInfo_returnsCorrectFields() {
        Map<String, Object> tracing = observabilityService.getTracingInfo("trace-123", "span-456");

        assertEquals("trace-123", tracing.get("traceId"));
        assertEquals("span-456", tracing.get("spanId"));
        assertEquals("onebook-backend", tracing.get("service"));
        assertNotNull(tracing.get("timestamp"));
    }

    @Test
    void createStructuredLog_returnsCorrectFields() {
        Map<String, Object> context = Map.of("userId", "user-1");
        Map<String, Object> log = observabilityService.createStructuredLog(
                "INFO", "User logged in", "auth", context);

        assertEquals("INFO", log.get("level"));
        assertEquals("User logged in", log.get("message"));
        assertEquals("auth", log.get("component"));
        assertEquals("onebook-backend", log.get("service"));
        assertNotNull(log.get("timestamp"));
        assertEquals(context, log.get("context"));
    }

    @Test
    void createStructuredLog_nullContext_omitsContext() {
        Map<String, Object> log = observabilityService.createStructuredLog(
                "ERROR", "Something failed", "service", null);

        assertFalse(log.containsKey("context"));
    }
}
