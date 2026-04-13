package com.nexus.onebook.compliance.controller;

import com.nexus.onebook.compliance.dto.ComplianceCertificationRequest;
import com.nexus.onebook.compliance.model.ComplianceCertification;
import com.nexus.onebook.compliance.service.ComplianceCertificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for compliance certification management.
 * Tracks certifications for target industries (SOC 2, ISO 27001, HIPAA, PCI-DSS).
 */
@RestController
@RequestMapping("/api/compliance-certifications")
public class ComplianceCertificationController {

    private final ComplianceCertificationService certificationService;

    public ComplianceCertificationController(ComplianceCertificationService certificationService) {
        this.certificationService = certificationService;
    }

    @PostMapping
    public ResponseEntity<ComplianceCertification> createCertification(
            @Valid @RequestBody ComplianceCertificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(certificationService.createCertification(request));
    }

    @GetMapping
    public ResponseEntity<List<ComplianceCertification>> getCertifications(
            @RequestParam String tenantId) {
        return ResponseEntity.ok(certificationService.getCertifications(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComplianceCertification> getCertification(@PathVariable Long id) {
        return ResponseEntity.ok(certificationService.getCertification(id));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ComplianceCertification> startCertification(@PathVariable Long id) {
        return ResponseEntity.ok(certificationService.startCertification(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ComplianceCertification> completeCertification(
            @PathVariable Long id, @RequestBody String certificateReference) {
        return ResponseEntity.ok(certificationService.completeCertification(id, certificateReference));
    }

    @PostMapping("/{id}/revoke")
    public ResponseEntity<ComplianceCertification> revokeCertification(@PathVariable Long id) {
        return ResponseEntity.ok(certificationService.revokeCertification(id));
    }
}
