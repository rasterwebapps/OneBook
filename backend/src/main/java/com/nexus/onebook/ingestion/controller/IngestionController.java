package com.nexus.onebook.ingestion.controller;

import com.nexus.onebook.ingestion.automation.OcrInvoiceService;
import com.nexus.onebook.ingestion.automation.ThreeWayMatchingService;
import com.nexus.onebook.ingestion.connector.CorporateCardService;
import com.nexus.onebook.ingestion.connector.HrmPayrollConnector;
import com.nexus.onebook.ingestion.connector.InventoryEventListener;
import com.nexus.onebook.ingestion.dto.*;
import com.nexus.onebook.ingestion.externalapp.ExternalAppIngestionService;
import com.nexus.onebook.ingestion.gateway.AdapterRegistry;
import com.nexus.onebook.ingestion.gateway.FinancialEventGateway;
import com.nexus.onebook.ingestion.model.AdapterType;
import com.nexus.onebook.ingestion.model.CardTransaction;
import com.nexus.onebook.payment.model.PaymentRegisterEntry;
import com.nexus.onebook.ingestion.model.VendorInvoice;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.nexus.onebook.foundation.model.Application;

/**
 * REST controller for the Universal Ingestion Layer.
 * Provides endpoints for financial event ingestion, OCR invoice processing,
 * 3-way matching, corporate card sync, connector event handling,
 * and external application payment request ingestion (Pharmacy, Lab, Stores, HIS, etc.).
 */
@RestController
@RequestMapping("/api/ingestion")
public class IngestionController {

    private final FinancialEventGateway gateway;
    private final AdapterRegistry adapterRegistry;
    private final OcrInvoiceService ocrInvoiceService;
    private final ThreeWayMatchingService matchingService;
    private final CorporateCardService corporateCardService;
    private final HrmPayrollConnector hrmPayrollConnector;
    private final InventoryEventListener inventoryEventListener;
    private final ExternalAppIngestionService externalAppIngestionService;

    public IngestionController(FinancialEventGateway gateway,
                               AdapterRegistry adapterRegistry,
                               OcrInvoiceService ocrInvoiceService,
                               ThreeWayMatchingService matchingService,
                               CorporateCardService corporateCardService,
                               HrmPayrollConnector hrmPayrollConnector,
                               InventoryEventListener inventoryEventListener,
                               ExternalAppIngestionService externalAppIngestionService) {
        this.gateway = gateway;
        this.adapterRegistry = adapterRegistry;
        this.ocrInvoiceService = ocrInvoiceService;
        this.matchingService = matchingService;
        this.corporateCardService = corporateCardService;
        this.hrmPayrollConnector = hrmPayrollConnector;
        this.inventoryEventListener = inventoryEventListener;
        this.externalAppIngestionService = externalAppIngestionService;
    }

    // --- Financial Event Gateway ---

    @PostMapping("/events")
    public ResponseEntity<FinancialEventResponse> ingestEvent(
            @Valid @RequestBody FinancialEventRequest request) {
        AdapterType type = AdapterType.valueOf(request.adapterType());
        PaymentRegisterEntry event = gateway.ingest(request.tenantId(), type, request.payload());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new FinancialEventResponse(event.getEventUuid(), event.getStatus(),
                        event.getErrorMessage() != null ? event.getErrorMessage() : "Event processed"));
    }

    @PostMapping("/events/validate")
    public ResponseEntity<FinancialEventResponse> validateEvent(
            @Valid @RequestBody FinancialEventRequest request) {
        AdapterType type = AdapterType.valueOf(request.adapterType());
        PaymentRegisterEntry event = gateway.ingestValidateOnly(request.tenantId(), type, request.payload());
        return ResponseEntity.ok(
                new FinancialEventResponse(event.getEventUuid(), event.getStatus(), "Validation passed"));
    }

    @GetMapping("/adapters")
    public ResponseEntity<List<AdapterType>> listAdapters() {
        return ResponseEntity.ok(adapterRegistry.getRegisteredTypes());
    }

    // --- OCR Invoice Processing ---

    @PostMapping("/invoices/ocr")
    public ResponseEntity<VendorInvoice> processOcrInvoice(
            @Valid @RequestBody OcrInvoiceRequest request) {
        VendorInvoice invoice = ocrInvoiceService.processOcrInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
    }

    // --- 3-Way Matching ---

    @PostMapping("/match/{tenantId}/{poNumber}")
    public ResponseEntity<ThreeWayMatchResult> performThreeWayMatch(
            @PathVariable String tenantId, @PathVariable String poNumber) {
        ThreeWayMatchResult result = matchingService.match(tenantId, poNumber);
        return ResponseEntity.ok(result);
    }

    // --- Corporate Card ---

    @PostMapping("/cards/sync")
    public ResponseEntity<CardTransaction> syncCardTransaction(
            @Valid @RequestBody CardTransactionRequest request) {
        CardTransaction txn = corporateCardService.syncTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(txn);
    }

    @GetMapping("/cards/unposted/{tenantId}")
    public ResponseEntity<List<CardTransaction>> getUnpostedCardTransactions(
            @PathVariable String tenantId) {
        return ResponseEntity.ok(corporateCardService.getUnpostedTransactions(tenantId));
    }

    // --- HRM/Payroll Connector ---

    @PostMapping("/payroll")
    public ResponseEntity<FinancialEventResponse> processPayrollEvent(
            @Valid @RequestBody FinancialEventRequest request) {
        PaymentRegisterEntry event = hrmPayrollConnector.processPayrollEvent(
                request.tenantId(), request.payload());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new FinancialEventResponse(event.getEventUuid(), event.getStatus(),
                        "Payroll event processed"));
    }

    // --- Inventory Event Listener ---

    @PostMapping("/inventory")
    public ResponseEntity<FinancialEventResponse> processInventoryEvent(
            @Valid @RequestBody FinancialEventRequest request) {
        PaymentRegisterEntry event = inventoryEventListener.processInventoryEvent(
                request.tenantId(), request.payload());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new FinancialEventResponse(event.getEventUuid(), event.getStatus(),
                        "Inventory event processed"));
    }

    // --- External Application Payment Request APIs ---
    // Common endpoints for all integrated external apps: Pharmacy, Lab, Stores, HIS, etc.
    // The applicationName field in the request body identifies the source system.

    @PostMapping("/payment-requests")
    public ResponseEntity<ExternalAppPaymentResponse> ingestExternalAppPaymentRequest(
            @Valid @RequestBody ExternalAppPaymentRequest request) {
        ExternalAppPaymentResponse response = externalAppIngestionService.ingestPaymentRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/payment-requests/bulk")
    public ResponseEntity<BulkExternalAppPaymentResponse> ingestBulkExternalAppPaymentRequests(
            @Valid @RequestBody BulkExternalAppPaymentRequest request) {
        BulkExternalAppPaymentResponse response = externalAppIngestionService.ingestBulkPaymentRequests(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/payment-requests/{requestId}/status")
    public ResponseEntity<PaymentStatusResponse> getExternalAppPaymentStatus(
            @PathVariable String requestId) {
        PaymentStatusResponse status = externalAppIngestionService.getPaymentStatus(requestId);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/documents/{documentId}/ocr")
    public ResponseEntity<OcrProcessingResponse> processDocumentOcr(
            @PathVariable Long documentId) {
        OcrProcessingResponse response = externalAppIngestionService.processDocumentOcr(documentId);
        return ResponseEntity.ok(response);
    }
}

