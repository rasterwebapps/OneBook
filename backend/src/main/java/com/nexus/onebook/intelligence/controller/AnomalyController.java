package com.nexus.onebook.intelligence.controller;

import com.nexus.onebook.intelligence.dto.TransactionAnomaly;
import com.nexus.onebook.intelligence.service.AnomalyDetectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anomalies")
public class AnomalyController {

    private final AnomalyDetectionService anomalyDetectionService;

    public AnomalyController(AnomalyDetectionService anomalyDetectionService) {
        this.anomalyDetectionService = anomalyDetectionService;
    }

    @GetMapping
    public ResponseEntity<List<TransactionAnomaly>> detectAnomalies(@RequestParam String tenantId) {
        return ResponseEntity.ok(anomalyDetectionService.detectAnomalies(tenantId));
    }
}
