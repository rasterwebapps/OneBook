package com.nexus.onebook.intelligence.controller;

import com.nexus.onebook.intelligence.dto.CashFlowForecast;
import com.nexus.onebook.intelligence.dto.ScenarioRequest;
import com.nexus.onebook.intelligence.dto.ScenarioResult;
import com.nexus.onebook.intelligence.service.ForecastingService;
import com.nexus.onebook.intelligence.service.ScenarioModelingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forecast")
public class ForecastController {

    private final ForecastingService forecastingService;
    private final ScenarioModelingService scenarioModelingService;

    public ForecastController(ForecastingService forecastingService,
                              ScenarioModelingService scenarioModelingService) {
        this.forecastingService = forecastingService;
        this.scenarioModelingService = scenarioModelingService;
    }

    @GetMapping
    public ResponseEntity<CashFlowForecast> getForecast(@RequestParam String tenantId) {
        return ResponseEntity.ok(forecastingService.generateForecast(tenantId));
    }

    @PostMapping("/scenario")
    public ResponseEntity<ScenarioResult> runScenario(@Valid @RequestBody ScenarioRequest request) {
        return ResponseEntity.ok(scenarioModelingService.runScenario(request));
    }
}
