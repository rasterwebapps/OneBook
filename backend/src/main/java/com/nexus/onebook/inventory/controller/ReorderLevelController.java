package com.nexus.onebook.inventory.controller;

import com.nexus.onebook.inventory.dto.ReorderAlert;
import com.nexus.onebook.inventory.dto.ReorderLevelRequest;
import com.nexus.onebook.inventory.model.ReorderLevel;
import com.nexus.onebook.inventory.service.ReorderLevelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Re-order Level alerts — automated stock replenishment alerts.
 */
@RestController
@RequestMapping("/api/reorder-levels")
public class ReorderLevelController {

    private final ReorderLevelService reorderLevelService;

    public ReorderLevelController(ReorderLevelService reorderLevelService) {
        this.reorderLevelService = reorderLevelService;
    }

    @PostMapping
    public ResponseEntity<ReorderLevel> setReorderLevel(@Valid @RequestBody ReorderLevelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reorderLevelService.setReorderLevel(request));
    }

    @GetMapping
    public ResponseEntity<List<ReorderLevel>> getLevels(@RequestParam String tenantId) {
        return ResponseEntity.ok(reorderLevelService.getReorderLevels(tenantId));
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<ReorderAlert>> getAlerts(@RequestParam String tenantId) {
        return ResponseEntity.ok(reorderLevelService.checkReorderAlerts(tenantId));
    }
}
