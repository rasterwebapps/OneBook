package com.nexus.onebook.ledger.auditor.controller;

import com.nexus.onebook.ledger.auditor.dto.AuditCommentRequest;
import com.nexus.onebook.ledger.auditor.dto.AuditSampleRequestDto;
import com.nexus.onebook.ledger.auditor.dto.AuditWorkflowRequest;
import com.nexus.onebook.ledger.auditor.model.AuditComment;
import com.nexus.onebook.ledger.auditor.model.AuditSampleRequest;
import com.nexus.onebook.ledger.auditor.model.AuditWorkflow;
import com.nexus.onebook.ledger.auditor.service.AuditorPortalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for the External Auditor Portal.
 * Provides secure, read-only access for CPAs to request samples,
 * leave comments, and manage approval workflows.
 */
@RestController
@RequestMapping("/api/auditor-portal")
public class AuditorPortalController {

    private final AuditorPortalService auditorPortalService;

    public AuditorPortalController(AuditorPortalService auditorPortalService) {
        this.auditorPortalService = auditorPortalService;
    }

    // --- Sample Requests ---

    @PostMapping("/sample-requests")
    public ResponseEntity<AuditSampleRequest> createSampleRequest(
            @Valid @RequestBody AuditSampleRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(auditorPortalService.createSampleRequest(request));
    }

    @GetMapping("/sample-requests")
    public ResponseEntity<List<AuditSampleRequest>> getSampleRequests(
            @RequestParam String tenantId) {
        return ResponseEntity.ok(auditorPortalService.getSampleRequests(tenantId));
    }

    @GetMapping("/sample-requests/{id}")
    public ResponseEntity<AuditSampleRequest> getSampleRequest(@PathVariable Long id) {
        return ResponseEntity.ok(auditorPortalService.getSampleRequest(id));
    }

    @PostMapping("/sample-requests/{id}/complete")
    public ResponseEntity<AuditSampleRequest> completeSampleRequest(
            @PathVariable Long id, @RequestBody String responseData) {
        return ResponseEntity.ok(auditorPortalService.completeSampleRequest(id, responseData));
    }

    // --- Audit Comments ---

    @PostMapping("/comments")
    public ResponseEntity<AuditComment> createComment(
            @Valid @RequestBody AuditCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(auditorPortalService.createComment(request));
    }

    @GetMapping("/comments")
    public ResponseEntity<List<AuditComment>> getComments(@RequestParam String tenantId) {
        return ResponseEntity.ok(auditorPortalService.getComments(tenantId));
    }

    @GetMapping("/comments/record")
    public ResponseEntity<List<AuditComment>> getCommentsForRecord(
            @RequestParam String tenantId, @RequestParam String tableName,
            @RequestParam Long recordId) {
        return ResponseEntity.ok(
                auditorPortalService.getCommentsForRecord(tenantId, tableName, recordId));
    }

    @PostMapping("/comments/{id}/resolve")
    public ResponseEntity<AuditComment> resolveComment(@PathVariable Long id) {
        return ResponseEntity.ok(auditorPortalService.resolveComment(id));
    }

    // --- Approval Workflows ---

    @PostMapping("/workflows")
    public ResponseEntity<AuditWorkflow> createWorkflow(
            @Valid @RequestBody AuditWorkflowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(auditorPortalService.createWorkflow(request));
    }

    @GetMapping("/workflows")
    public ResponseEntity<List<AuditWorkflow>> getWorkflows(@RequestParam String tenantId) {
        return ResponseEntity.ok(auditorPortalService.getWorkflows(tenantId));
    }

    @GetMapping("/workflows/{id}")
    public ResponseEntity<AuditWorkflow> getWorkflow(@PathVariable Long id) {
        return ResponseEntity.ok(auditorPortalService.getWorkflow(id));
    }

    @PostMapping("/workflows/{id}/approve")
    public ResponseEntity<AuditWorkflow> approveWorkflow(@PathVariable Long id) {
        return ResponseEntity.ok(auditorPortalService.approveWorkflow(id));
    }

    @PostMapping("/workflows/{id}/reject")
    public ResponseEntity<AuditWorkflow> rejectWorkflow(
            @PathVariable Long id, @RequestBody String reason) {
        return ResponseEntity.ok(auditorPortalService.rejectWorkflow(id, reason));
    }
}
