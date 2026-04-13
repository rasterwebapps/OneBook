package com.nexus.onebook.ledger.operations.controller;

import com.nexus.onebook.ledger.operations.service.ObservabilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for observability endpoints.
 * Provides structured metrics and tracing data for dashboards.
 */
@RestController
@RequestMapping("/api/observability")
public class ObservabilityController {

    private final ObservabilityService observabilityService;

    public ObservabilityController(ObservabilityService observabilityService) {
        this.observabilityService = observabilityService;
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        return ResponseEntity.ok(observabilityService.getHealthMetrics());
    }

    @GetMapping("/tracing")
    public ResponseEntity<Map<String, Object>> getTracing(
            @RequestParam String traceId, @RequestParam String spanId) {
        return ResponseEntity.ok(observabilityService.getTracingInfo(traceId, spanId));
    }
}
