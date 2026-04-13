package com.nexus.onebook.compliance.controller;

import com.nexus.onebook.compliance.dto.EInvoiceRequest;
import com.nexus.onebook.compliance.dto.EWayBillRequest;
import com.nexus.onebook.compliance.model.EInvoice;
import com.nexus.onebook.compliance.service.ComplianceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for tax compliance operations.
 * Provides e-Invoicing and e-Way Bill generation endpoints.
 */
@RestController
@RequestMapping("/api/compliance")
public class ComplianceController {

    private final ComplianceService complianceService;

    public ComplianceController(ComplianceService complianceService) {
        this.complianceService = complianceService;
    }

    @PostMapping("/e-invoices")
    public ResponseEntity<EInvoice> createEInvoice(@Valid @RequestBody EInvoiceRequest request) {
        EInvoice invoice = complianceService.createEInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
    }

    @PostMapping("/e-invoices/{id}/generate")
    public ResponseEntity<EInvoice> generateEInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(complianceService.generateEInvoice(id));
    }

    @PostMapping("/e-way-bills")
    public ResponseEntity<EInvoice> generateEWayBill(@Valid @RequestBody EWayBillRequest request) {
        return ResponseEntity.ok(complianceService.generateEWayBill(request));
    }

    @PostMapping("/e-invoices/{id}/cancel")
    public ResponseEntity<EInvoice> cancelEInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(complianceService.cancelEInvoice(id));
    }

    @GetMapping("/e-invoices")
    public ResponseEntity<List<EInvoice>> getInvoices(@RequestParam String tenantId) {
        return ResponseEntity.ok(complianceService.getInvoicesByTenant(tenantId));
    }

    @GetMapping("/e-invoices/{id}")
    public ResponseEntity<EInvoice> getInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(complianceService.getInvoice(id));
    }
}
