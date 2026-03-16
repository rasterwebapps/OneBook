package com.nexus.onebook.ledger.ingestion.externalapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.onebook.ledger.dto.DocumentUploadRequest;
import com.nexus.onebook.ledger.ingestion.dto.*;
import com.nexus.onebook.ledger.ingestion.gateway.FinancialEventGateway;
import com.nexus.onebook.ledger.ingestion.model.AdapterType;
import com.nexus.onebook.ledger.ingestion.model.EventStatus;
import com.nexus.onebook.ledger.ingestion.model.FinancialEvent;
import com.nexus.onebook.ledger.ingestion.repository.FinancialEventRepository;
import com.nexus.onebook.ledger.model.VaultDocument;
import com.nexus.onebook.ledger.service.DocumentVaultService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalAppIngestionServiceTest {

    @Mock
    private FinancialEventGateway gateway;

    @Mock
    private FinancialEventRepository eventRepository;

    @Mock
    private DocumentVaultService documentVaultService;

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private ExternalAppIngestionService service;

    // --- Helpers ---

    private static ExternalAppPaymentRequest buildRequest(String applicationName) {
        ExternalAppPaymentRequest.Amounts amounts = new ExternalAppPaymentRequest.Amounts(
                new BigDecimal("15000.00"),
                new BigDecimal("14250.00"),
                new BigDecimal("750.00"),
                BigDecimal.ZERO,
                new BigDecimal("14250.00")
        );
        ExternalAppPaymentRequest.BankDetails bank = new ExternalAppPaymentRequest.BankDetails(
                "1234567890", "XYZ Supplies", "SBI", "Medical District", "SBIN0001234");
        ExternalAppPaymentRequest.PaymentData paymentData = new ExternalAppPaymentRequest.PaymentData(
                "INV-001", "2026-03-16", "ABC Org", "VENDOR",
                "XYZ Supplies", bank, amounts, "NEFT", "PURCHASE_PAYMENT", "2026-03-20");
        ExternalAppPaymentRequest.PaymentMetadata metadata = new ExternalAppPaymentRequest.PaymentMetadata(
                "BR-001", "ABC-ORG", "ext-system", "ERP", "BATCH-001");
        return new ExternalAppPaymentRequest("branch-001", applicationName, paymentData, null, metadata);
    }

    private static FinancialEvent buildEvent(EventStatus status) {
        FinancialEvent event = new FinancialEvent("branch-001", AdapterType.EXTERNAL_APP, "PURCHASE_PAYMENT");
        event.setStatus(status);
        return event;
    }

    // --- Single ingestion ---

    @Test
    void ingestPaymentRequest_pharmacy_returnsReceived() {
        when(gateway.ingest(eq("branch-001"), eq(AdapterType.EXTERNAL_APP), anyString()))
                .thenReturn(buildEvent(EventStatus.RECEIVED));

        ExternalAppPaymentResponse response = service.ingestPaymentRequest(buildRequest("PHARMACY"));

        assertNotNull(response);
        assertEquals("RECEIVED", response.status());
        assertEquals("Payment request received successfully", response.message());
        assertNull(response.documentId());
    }

    @Test
    void ingestPaymentRequest_lab_returnsReceived() {
        when(gateway.ingest(eq("branch-001"), eq(AdapterType.EXTERNAL_APP), anyString()))
                .thenReturn(buildEvent(EventStatus.RECEIVED));

        ExternalAppPaymentResponse response = service.ingestPaymentRequest(buildRequest("LAB"));

        assertNotNull(response);
        assertEquals("RECEIVED", response.status());
    }

    @Test
    void ingestPaymentRequest_store_returnsReceived() {
        when(gateway.ingest(anyString(), any(), anyString()))
                .thenReturn(buildEvent(EventStatus.RECEIVED));

        ExternalAppPaymentResponse response = service.ingestPaymentRequest(buildRequest("STORE"));

        assertEquals("RECEIVED", response.status());
    }

    @Test
    void ingestPaymentRequest_his_returnsReceived() {
        when(gateway.ingest(anyString(), any(), anyString()))
                .thenReturn(buildEvent(EventStatus.RECEIVED));

        ExternalAppPaymentResponse response = service.ingestPaymentRequest(buildRequest("HIS"));

        assertEquals("RECEIVED", response.status());
    }

    @Test
    void ingestPaymentRequest_withDocumentInfo_storesDocument() {
        ExternalAppPaymentRequest.DocumentInfo docInfo = new ExternalAppPaymentRequest.DocumentInfo(
                "/minio-bucket/pharmacy/invoices/2026/inv.pdf",
                "inv.pdf", "application/pdf", 2048576L, "abc123checksum");
        ExternalAppPaymentRequest request = new ExternalAppPaymentRequest(
                "branch-001", "PHARMACY", buildRequest("PHARMACY").paymentData(),
                docInfo, buildRequest("PHARMACY").metadata());

        when(gateway.ingest(anyString(), any(), anyString()))
                .thenReturn(buildEvent(EventStatus.RECEIVED));

        VaultDocument doc = new VaultDocument("branch-001", "inv.pdf", "application/pdf",
                2048576L, "vault/branch-001/uuid/inv.pdf", "abc123checksum");
        doc.setId(42L);
        when(documentVaultService.storeDocumentWithOriginalPath(
                any(DocumentUploadRequest.class), anyString())).thenReturn(doc);

        ExternalAppPaymentResponse response = service.ingestPaymentRequest(request);

        assertEquals("42", response.documentId());
        verify(documentVaultService).storeDocumentWithOriginalPath(
                any(), eq("/minio-bucket/pharmacy/invoices/2026/inv.pdf"));
    }

    // --- Bulk ingestion ---

    @Test
    void ingestBulkPaymentRequests_mixedSources_allSucceed() {
        when(gateway.ingest(anyString(), any(), anyString()))
                .thenReturn(buildEvent(EventStatus.RECEIVED));

        List<ExternalAppPaymentRequest> requests = List.of(
                buildRequest("PHARMACY"),
                buildRequest("LAB"),
                buildRequest("STORE")
        );
        BulkExternalAppPaymentRequest bulkRequest = new BulkExternalAppPaymentRequest("branch-001", requests);

        BulkExternalAppPaymentResponse response = service.ingestBulkPaymentRequests(bulkRequest);

        assertEquals(3, response.totalReceived());
        assertEquals(3, response.totalSucceeded());
        assertEquals(0, response.totalFailed());
    }

    @Test
    void ingestBulkPaymentRequests_oneFailedEvent_countedAsFailed() {
        FinancialEvent failedEvent = buildEvent(EventStatus.FAILED);
        failedEvent.setErrorMessage("Parse error");

        when(gateway.ingest(anyString(), any(), anyString()))
                .thenReturn(buildEvent(EventStatus.RECEIVED))
                .thenReturn(failedEvent);

        List<ExternalAppPaymentRequest> requests = List.of(buildRequest("PHARMACY"), buildRequest("LAB"));
        BulkExternalAppPaymentRequest bulkRequest = new BulkExternalAppPaymentRequest("branch-001", requests);

        BulkExternalAppPaymentResponse response = service.ingestBulkPaymentRequests(bulkRequest);

        assertEquals(2, response.totalReceived());
        assertEquals(1, response.totalSucceeded());
        assertEquals(1, response.totalFailed());
    }

    // --- Status lookup ---

    @Test
    void getPaymentStatus_receivedEvent_returnsPendingWorkflowStage() {
        UUID eventUuid = UUID.randomUUID();
        FinancialEvent event = buildEvent(EventStatus.RECEIVED);
        when(eventRepository.findByEventUuid(eventUuid)).thenReturn(Optional.of(event));

        PaymentStatusResponse status = service.getPaymentStatus(eventUuid.toString());

        assertEquals("RECEIVED", status.status());
        assertEquals("PENDING", status.workflowStage());
    }

    @Test
    void getPaymentStatus_mappedEvent_returnsPendingWorkflowStage() {
        UUID eventUuid = UUID.randomUUID();
        FinancialEvent event = buildEvent(EventStatus.MAPPED);
        when(eventRepository.findByEventUuid(eventUuid)).thenReturn(Optional.of(event));

        PaymentStatusResponse status = service.getPaymentStatus(eventUuid.toString());

        assertEquals("PROCESSED", status.status());
        assertEquals("PENDING", status.workflowStage());
    }

    @Test
    void getPaymentStatus_postedEvent_returnsPendingPaymentWorkflowStage() {
        UUID eventUuid = UUID.randomUUID();
        FinancialEvent event = buildEvent(EventStatus.POSTED);
        when(eventRepository.findByEventUuid(eventUuid)).thenReturn(Optional.of(event));

        PaymentStatusResponse status = service.getPaymentStatus(eventUuid.toString());

        assertEquals("APPROVED", status.status());
        assertEquals("PENDING_PAYMENT", status.workflowStage());
    }

    @Test
    void getPaymentStatus_failedEvent_returnsRejectedWorkflowStage() {
        UUID eventUuid = UUID.randomUUID();
        FinancialEvent event = buildEvent(EventStatus.FAILED);
        when(eventRepository.findByEventUuid(eventUuid)).thenReturn(Optional.of(event));

        PaymentStatusResponse status = service.getPaymentStatus(eventUuid.toString());

        assertEquals("FAILED", status.status());
        assertEquals("REJECTED", status.workflowStage());
    }

    @Test
    void getPaymentStatus_rejectedEvent_returnsRejectedWorkflowStage() {
        UUID eventUuid = UUID.randomUUID();
        FinancialEvent event = buildEvent(EventStatus.REJECTED);
        when(eventRepository.findByEventUuid(eventUuid)).thenReturn(Optional.of(event));

        PaymentStatusResponse status = service.getPaymentStatus(eventUuid.toString());

        assertEquals("REJECTED", status.status());
        assertEquals("REJECTED", status.workflowStage());
    }

    @Test
    void getPaymentStatus_notFound_throws() {
        UUID eventUuid = UUID.randomUUID();
        when(eventRepository.findByEventUuid(eventUuid)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.getPaymentStatus(eventUuid.toString()));
    }

    @Test
    void getPaymentStatus_invalidUuid_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getPaymentStatus("not-a-uuid"));
    }

    // --- OCR ---

    @Test
    void processDocumentOcr_validDocumentId_returnsCompletedResponse() {
        VaultDocument doc = new VaultDocument("branch-001", "inv.pdf", "application/pdf",
                2048L, "vault/branch-001/uuid/inv.pdf", "abc123");
        doc.setId(1L);

        when(documentVaultService.getDocument(1L)).thenReturn(doc);

        OcrProcessingResponse response = service.processDocumentOcr(1L);

        assertEquals("1", response.documentId());
        assertEquals("COMPLETED", response.ocrStatus());
        assertNotNull(response.extractedData());
        assertTrue(response.extractedData().contains("inv.pdf"));
    }
}
