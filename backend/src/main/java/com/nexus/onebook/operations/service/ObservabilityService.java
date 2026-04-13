package com.nexus.onebook.operations.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Observability service.
 * Provides structured logging, distributed tracing support, and metrics
 * dashboard data for production monitoring.
 */
@Service
public class ObservabilityService {

    /**
     * Returns a summary of system health metrics for dashboards.
     */
    public Map<String, Object> getHealthMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("timestamp", Instant.now().toString());
        metrics.put("jvmMemoryUsedMb", getJvmMemoryUsedMb());
        metrics.put("jvmMemoryMaxMb", getJvmMemoryMaxMb());
        metrics.put("jvmMemoryUsagePercent", getJvmMemoryUsagePercent());
        metrics.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        metrics.put("uptimeMs", getUptimeMs());
        metrics.put("threadCount", Thread.activeCount());
        return metrics;
    }

    /**
     * Returns structured tracing information for the current request context.
     */
    public Map<String, Object> getTracingInfo(String traceId, String spanId) {
        Map<String, Object> tracing = new LinkedHashMap<>();
        tracing.put("traceId", traceId);
        tracing.put("spanId", spanId);
        tracing.put("timestamp", Instant.now().toString());
        tracing.put("service", "onebook-backend");
        return tracing;
    }

    /**
     * Returns structured log context for the given event.
     */
    public Map<String, Object> createStructuredLog(String level, String message,
                                                     String component, Map<String, Object> context) {
        Map<String, Object> logEntry = new LinkedHashMap<>();
        logEntry.put("timestamp", Instant.now().toString());
        logEntry.put("level", level);
        logEntry.put("message", message);
        logEntry.put("component", component);
        logEntry.put("service", "onebook-backend");
        if (context != null) {
            logEntry.put("context", context);
        }
        return logEntry;
    }

    private long getJvmMemoryUsedMb() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    }

    private long getJvmMemoryMaxMb() {
        return Runtime.getRuntime().maxMemory() / (1024 * 1024);
    }

    private double getJvmMemoryUsagePercent() {
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        long max = runtime.maxMemory();
        return Math.round((double) used / max * 10000.0) / 100.0;
    }

    private long getUptimeMs() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
    }
}
