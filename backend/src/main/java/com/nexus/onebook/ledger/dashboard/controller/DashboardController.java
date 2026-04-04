package com.nexus.onebook.ledger.dashboard.controller;

import com.nexus.onebook.ledger.dashboard.dto.DashboardSummaryDTO;
import com.nexus.onebook.ledger.dashboard.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getSummary(@RequestParam String tenantId) {
        return ResponseEntity.ok(dashboardService.getSummary(tenantId));
    }
}
