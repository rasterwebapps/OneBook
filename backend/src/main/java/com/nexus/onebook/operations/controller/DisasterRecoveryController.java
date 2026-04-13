package com.nexus.onebook.operations.controller;

import com.nexus.onebook.operations.dto.DisasterRecoveryRequest;
import com.nexus.onebook.operations.model.DisasterRecoveryEvent;
import com.nexus.onebook.operations.service.DisasterRecoveryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for disaster recovery operations.
 * Manages backup, point-in-time recovery, and failover procedures.
 */
@RestController
@RequestMapping("/api/disaster-recovery")
public class DisasterRecoveryController {

    private final DisasterRecoveryService disasterRecoveryService;

    public DisasterRecoveryController(DisasterRecoveryService disasterRecoveryService) {
        this.disasterRecoveryService = disasterRecoveryService;
    }

    @PostMapping("/events")
    public ResponseEntity<DisasterRecoveryEvent> initiateEvent(
            @Valid @RequestBody DisasterRecoveryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(disasterRecoveryService.initiateEvent(request));
    }

    @GetMapping("/events")
    public ResponseEntity<List<DisasterRecoveryEvent>> getEvents(@RequestParam String tenantId) {
        return ResponseEntity.ok(disasterRecoveryService.getEvents(tenantId));
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<DisasterRecoveryEvent> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(disasterRecoveryService.getEvent(id));
    }

    @PostMapping("/events/{id}/complete")
    public ResponseEntity<DisasterRecoveryEvent> completeEvent(
            @PathVariable Long id, @RequestParam Long fileSizeBytes) {
        return ResponseEntity.ok(disasterRecoveryService.completeEvent(id, fileSizeBytes));
    }

    @PostMapping("/events/{id}/fail")
    public ResponseEntity<DisasterRecoveryEvent> failEvent(
            @PathVariable Long id, @RequestBody String errorMessage) {
        return ResponseEntity.ok(disasterRecoveryService.failEvent(id, errorMessage));
    }
}
