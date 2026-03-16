package com.nexus.onebook.ledger.ingestion.controller;

import com.nexus.onebook.ledger.ingestion.automation.OcrInvoiceService;
import com.nexus.onebook.ledger.ingestion.automation.ThreeWayMatchingService;
import com.nexus.onebook.ledger.ingestion.connector.CorporateCardService;
import com.nexus.onebook.ledger.ingestion.connector.HrmPayrollConnector;
import com.nexus.onebook.ledger.ingestion.connector.InventoryEventListener;
import com.nexus.onebook.ledger.ingestion.dto.*;
import com.nexus.onebook.ledger.ingestion.externalapp.ExternalAppIngestionService;
import com.nexus.onebook.ledger.ingestion.gateway.AdapterRegistry;
import com.nexus.onebook.ledger.ingestion.gateway.FinancialEventGateway;
import com.nexus.onebook.ledger.ingestion.model.*;
import com.nexus.onebook.ledger.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IngestionController.class)
@Import(GlobalExceptionHandler.class)
class IngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FinancialEventGateway gateway;

    @MockitoBean
    private AdapterRegistry adapterRegistry;

    @MockitoBean
    private OcrInvoiceService ocrInvoiceService;

    @MockitoBean
    private ThreeWayMatchingService matchingService;

    @MockitoBean
    private CorporateCardService corporateCardService;

    @MockitoBean
    private HrmPayrollConnector hrmPayrollConnector;

    @MockitoBean
    private InventoryEventListener inventoryEventListener;

    @MockitoBean
    private ExternalAppIngestionService externalAppIngestionService;

    @Test
    void ingestEvent_validRequest_returns201() throws Exception {
        FinancialEvent event = new FinancialEvent("tenant-1", AdapterType.HL7, "CHARGE");
        event.setStatus(EventStatus.POSTED);

        when(gateway.ingest(eq("tenant-1"), eq(AdapterType.HL7), any())).thenReturn(event);

        mockMvc.perform(post("/api/ingestion/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "tenant-1",
                                    "adapterType": "HL7",
                                    "payload": "DFT|CHARGE|P-12345|1500|USD|2026-03-10|Lab Test|4100|2100"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("POSTED"));
    }

    @Test
    void ingestEvent_missingTenantId_returns400() throws Exception {
        mockMvc.perform(post("/api/ingestion/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "adapterType": "HL7",
                                    "payload": "DFT|CHARGE|P-12345"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listAdapters_returnsRegisteredTypes() throws Exception {
        when(adapterRegistry.getRegisteredTypes())
                .thenReturn(List.of(AdapterType.HL7, AdapterType.DMS));

        mockMvc.perform(get("/api/ingestion/adapters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("HL7"))
                .andExpect(jsonPath("$[1]").value("DMS"));
    }

    @Test
    void processOcrInvoice_validRequest_returns201() throws Exception {
        VendorInvoice invoice = new VendorInvoice(
                "tenant-1", "INV-001", "PO-100", "Supplier Inc",
                new BigDecimal("5000.00"), LocalDate.of(2026, 3, 10));

        when(ocrInvoiceService.processOcrInvoice(any())).thenReturn(invoice);

        mockMvc.perform(post("/api/ingestion/invoices/ocr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "tenant-1",
                                    "invoiceNumber": "INV-001",
                                    "vendorName": "Supplier Inc",
                                    "poNumber": "PO-100",
                                    "totalAmount": 5000.00,
                                    "invoiceDate": "2026-03-10"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.invoiceNumber").value("INV-001"));
    }

    @Test
    void performThreeWayMatch_returns200() throws Exception {
        ThreeWayMatchResult result = new ThreeWayMatchResult(
                MatchStatus.MATCHED, "PO-100",
                new BigDecimal("5000"), new BigDecimal("5000"), new BigDecimal("5000"),
                true, List.of(), "All matched");

        when(matchingService.match("tenant-1", "PO-100")).thenReturn(result);

        mockMvc.perform(post("/api/ingestion/match/tenant-1/PO-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MATCHED"))
                .andExpect(jsonPath("$.amountsMatch").value(true));
    }

    @Test
    void syncCardTransaction_validRequest_returns201() throws Exception {
        CardTransaction txn = new CardTransaction(
                "tenant-1", "EXT-001", "Coffee Shop",
                new BigDecimal("15.50"), LocalDate.of(2026, 3, 10));

        when(corporateCardService.syncTransaction(any())).thenReturn(txn);

        mockMvc.perform(post("/api/ingestion/cards/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "tenant-1",
                                    "externalId": "EXT-001",
                                    "merchantName": "Coffee Shop",
                                    "amount": 15.50,
                                    "transactionDate": "2026-03-10"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.externalId").value("EXT-001"));
    }

    @Test
    void getUnpostedCardTransactions_returns200() throws Exception {
        when(corporateCardService.getUnpostedTransactions("tenant-1"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/ingestion/cards/unposted/tenant-1"))
                .andExpect(status().isOk());
    }

    // --- External Application Payment Request API Tests ---
    // These tests use applicationName to distinguish different source systems.
    // The same endpoints serve Pharmacy, Lab, Stores, HIS, and any future integration.

    @Test
    void ingestExternalAppPaymentRequest_pharmacySource_returns201() throws Exception {
        ExternalAppPaymentResponse response = new ExternalAppPaymentResponse(
                UUID.randomUUID().toString(), "RECEIVED",
                "Payment request received successfully",
                UUID.randomUUID().toString(), null);

        when(externalAppIngestionService.ingestPaymentRequest(any())).thenReturn(response);

        mockMvc.perform(post("/api/ingestion/payment-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "branch-001",
                                    "applicationName": "PHARMACY",
                                    "paymentData": {
                                        "invoiceNumber": "PH-INV-2026-001",
                                        "invoiceDate": "2026-03-16",
                                        "payerName": "ABC Hospital Pharmacy",
                                        "payeeType": "VENDOR",
                                        "payeeName": "XYZ Medical Supplies",
                                        "amounts": {
                                            "grossAmount": 15000.00,
                                            "payableAmount": 14250.00
                                        },
                                        "paymentMode": "NEFT",
                                        "transactionType": "PURCHASE_PAYMENT"
                                    }
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.message").value("Payment request received successfully"));
    }

    @Test
    void ingestExternalAppPaymentRequest_labSource_returns201() throws Exception {
        ExternalAppPaymentResponse response = new ExternalAppPaymentResponse(
                UUID.randomUUID().toString(), "RECEIVED",
                "Payment request received successfully",
                UUID.randomUUID().toString(), null);

        when(externalAppIngestionService.ingestPaymentRequest(any())).thenReturn(response);

        mockMvc.perform(post("/api/ingestion/payment-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "lab-branch-001",
                                    "applicationName": "LAB",
                                    "paymentData": {
                                        "invoiceNumber": "LAB-INV-2026-001",
                                        "invoiceDate": "2026-03-16",
                                        "payeeName": "Lab Reagents Pvt Ltd",
                                        "amounts": { "payableAmount": 8500.00 },
                                        "paymentMode": "RTGS",
                                        "transactionType": "PURCHASE_PAYMENT"
                                    }
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    void ingestExternalAppPaymentRequest_missingTenantId_returns400() throws Exception {
        mockMvc.perform(post("/api/ingestion/payment-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "applicationName": "PHARMACY",
                                    "paymentData": {
                                        "invoiceNumber": "PH-INV-001"
                                    }
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ingestBulkExternalAppPaymentRequests_mixedSources_returns201() throws Exception {
        BulkExternalAppPaymentResponse bulkResponse = new BulkExternalAppPaymentResponse(
                List.of(
                        new ExternalAppPaymentResponse(UUID.randomUUID().toString(), "RECEIVED",
                                "Payment request received successfully", UUID.randomUUID().toString(), null),
                        new ExternalAppPaymentResponse(UUID.randomUUID().toString(), "RECEIVED",
                                "Payment request received successfully", UUID.randomUUID().toString(), null)
                ), 2, 2, 0);

        when(externalAppIngestionService.ingestBulkPaymentRequests(any())).thenReturn(bulkResponse);

        mockMvc.perform(post("/api/ingestion/payment-requests/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "branch-001",
                                    "requests": [
                                        {
                                            "tenantId": "branch-001",
                                            "applicationName": "PHARMACY",
                                            "paymentData": {
                                                "invoiceNumber": "PH-INV-001",
                                                "amounts": { "payableAmount": 5000.00 }
                                            }
                                        },
                                        {
                                            "tenantId": "branch-001",
                                            "applicationName": "LAB",
                                            "paymentData": {
                                                "invoiceNumber": "LAB-INV-001",
                                                "amounts": { "payableAmount": 3000.00 }
                                            }
                                        }
                                    ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalReceived").value(2))
                .andExpect(jsonPath("$.totalSucceeded").value(2))
                .andExpect(jsonPath("$.totalFailed").value(0));
    }

    @Test
    void getExternalAppPaymentStatus_validRequestId_returns200() throws Exception {
        UUID requestId = UUID.randomUUID();
        PaymentStatusResponse statusResponse = new PaymentStatusResponse(
                requestId.toString(), "APPROVED", "PENDING_PAYMENT", null, null, null);

        when(externalAppIngestionService.getPaymentStatus(requestId.toString())).thenReturn(statusResponse);

        mockMvc.perform(get("/api/ingestion/payment-requests/{requestId}/status", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(requestId.toString()))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.workflowStage").value("PENDING_PAYMENT"));
    }

    @Test
    void processDocumentOcr_validDocumentId_returns200() throws Exception {
        OcrProcessingResponse ocrResponse = new OcrProcessingResponse(
                "42", "COMPLETED",
                "{\"fileName\":\"inv.pdf\"}",
                "OCR processing completed for document: inv.pdf");

        when(externalAppIngestionService.processDocumentOcr(42L)).thenReturn(ocrResponse);

        mockMvc.perform(post("/api/ingestion/documents/42/ocr"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value("42"))
                .andExpect(jsonPath("$.ocrStatus").value("COMPLETED"));
    }
}
