package com.nexus.onebook.ledger.ingestion.pharmacy;

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
import org.mockito.ArgumentCaptor;
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
class PharmacyIngestionServiceTest {

    @Mock
    private FinancialEventGateway gateway;

    @Mock
    private FinancialEventRepository eventRepository;

    @Mock
    private DocumentVaultService documentVaultService;

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private PharmacyIngestionService service;

    private static PharmacyPaymentRequest buildRequest() {
        PharmacyPaymentRequest.Amounts amounts = new PharmacyPaymentRequest.Amounts(
                new BigDecimal("15000.00"),
                new BigDecimal("14250.00"),
                new BigDecimal("750.00"),
                BigDecimal.ZERO,
                new BigDecimal("14250.00")
        );
        PharmacyPaymentRequest.BankDetails bank = new PharmacyPaymentRequest.BankDetails(
                "1234567890", "XYZ Supplies", "SBI", "Medical District", "SBIN0001234");
        PharmacyPaymentRequest.PaymentData paymentData = new PharmacyPaymentRequest.PaymentData(
                "PH-INV-001", "2026-03-16", "ABC Pharmacy", "VENDOR",
                "XYZ Medical Supplies", bank, amounts, "NEFT", "PURCHASE_PAYMENT", "2026-03-20");
        PharmacyPaymentRequest.PaymentMetadata metadata = new PharmacyPaymentRequest.PaymentMetadata(
                "BR-001", "ABC-HOSPITAL", "pharmacy-system", "PHARMACY_ERP", "BATCH-001");
        return new PharmacyPaymentRequest("pharmacy-branch-001", "PHARMACY", paymentData, null, metadata);
    }

    private static FinancialEvent buildEvent() {
        FinancialEvent event = new FinancialEvent("pharmacy-branch-001", AdapterType.PHARMACY, "PURCHASE_PAYMENT");
        event.setStatus(EventStatus.RECEIVED);
        return event;
    }

    @Test
    void ingestPaymentRequest_validRequest_returnsResponse() {
        FinancialEvent event = buildEvent();
        when(gateway.ingest(eq("pharmacy-branch-001"), eq(AdapterType.PHARMACY), anyString()))
                .thenReturn(event);

        PharmacyPaymentResponse response = service.ingestPaymentRequest(buildRequest());

        assertNotNull(response);
        assertEquals("RECEIVED", response.status());
        assertEquals("Payment request received successfully", response.message());
        assertNotNull(response.eventId());
        assertNull(response.documentId());
    }

    @Test
    void ingestPaymentRequest_withDocumentInfo_storesDocument() {
        PharmacyPaymentRequest.DocumentInfo docInfo = new PharmacyPaymentRequest.DocumentInfo(
                "/minio-bucket/pharmacy/invoices/inv.pdf",
                "inv.pdf",
                "application/pdf",
                2048576L,
                "abc123checksum"
        );
        PharmacyPaymentRequest request = new PharmacyPaymentRequest(
                "pharmacy-branch-001", "PHARMACY", buildRequest().paymentData(), docInfo, buildRequest().metadata());

        FinancialEvent event = buildEvent();
        when(gateway.ingest(anyString(), any(), anyString())).thenReturn(event);

        VaultDocument doc = new VaultDocument("pharmacy-branch-001", "inv.pdf", "application/pdf",
                2048576L, "vault/pharmacy-branch-001/uuid/inv.pdf", "abc123checksum");
        doc.setId(42L);
        when(documentVaultService.storeDocumentWithOriginalPath(any(DocumentUploadRequest.class), anyString()))
                .thenReturn(doc);

        PharmacyPaymentResponse response = service.ingestPaymentRequest(request);

        assertNotNull(response.documentId());
        assertEquals("42", response.documentId());
        verify(documentVaultService).storeDocumentWithOriginalPath(any(), eq("/minio-bucket/pharmacy/invoices/inv.pdf"));
    }

    @Test
    void ingestBulkPaymentRequests_multiplRequests_returnsAggregatedResponse() {
        FinancialEvent event = buildEvent();
        when(gateway.ingest(anyString(), any(), anyString())).thenReturn(event);

        List<PharmacyPaymentRequest> requests = List.of(buildRequest(), buildRequest());
        BulkPharmacyPaymentRequest bulkRequest = new BulkPharmacyPaymentRequest(
                "pharmacy-branch-001", requests);

        BulkPharmacyPaymentResponse response = service.ingestBulkPaymentRequests(bulkRequest);

        assertEquals(2, response.totalReceived());
        assertEquals(2, response.totalSucceeded());
        assertEquals(0, response.totalFailed());
        assertEquals(2, response.results().size());
    }

    @Test
    void ingestBulkPaymentRequests_oneFailedEvent_countedAsFailed() {
        FinancialEvent successEvent = buildEvent();
        FinancialEvent failedEvent = new FinancialEvent("pharmacy-branch-001", AdapterType.PHARMACY, "PURCHASE_PAYMENT");
        failedEvent.setStatus(EventStatus.FAILED);
        failedEvent.setErrorMessage("Parse error");

        when(gateway.ingest(anyString(), any(), anyString()))
                .thenReturn(successEvent)
                .thenReturn(failedEvent);

        List<PharmacyPaymentRequest> requests = List.of(buildRequest(), buildRequest());
        BulkPharmacyPaymentRequest bulkRequest = new BulkPharmacyPaymentRequest(
                "pharmacy-branch-001", requests);

        BulkPharmacyPaymentResponse response = service.ingestBulkPaymentRequests(bulkRequest);

        assertEquals(2, response.totalReceived());
        assertEquals(1, response.totalSucceeded());
        assertEquals(1, response.totalFailed());
    }

    @Test
    void getPaymentStatus_receivedEvent_returnsPendingWorkflowStage() {
        UUID eventUuid = UUID.randomUUID();
        FinancialEvent event = buildEvent();
        event.setStatus(EventStatus.RECEIVED);

        when(eventRepository.findByEventUuid(eventUuid)).thenReturn(Optional.of(event));

        PaymentStatusResponse status = service.getPaymentStatus(eventUuid.toString());

        assertEquals("RECEIVED", status.status());
        assertEquals("PENDING", status.workflowStage());
    }

    @Test
    void getPaymentStatus_postedEvent_returnsPendingPaymentWorkflowStage() {
        UUID eventUuid = UUID.randomUUID();
        FinancialEvent event = buildEvent();
        event.setStatus(EventStatus.POSTED);

        when(eventRepository.findByEventUuid(eventUuid)).thenReturn(Optional.of(event));

        PaymentStatusResponse status = service.getPaymentStatus(eventUuid.toString());

        assertEquals("APPROVED", status.status());
        assertEquals("PENDING_PAYMENT", status.workflowStage());
    }

    @Test
    void getPaymentStatus_failedEvent_returnsRejectedWorkflowStage() {
        UUID eventUuid = UUID.randomUUID();
        FinancialEvent event = buildEvent();
        event.setStatus(EventStatus.FAILED);

        when(eventRepository.findByEventUuid(eventUuid)).thenReturn(Optional.of(event));

        PaymentStatusResponse status = service.getPaymentStatus(eventUuid.toString());

        assertEquals("FAILED", status.status());
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

    @Test
    void processDocumentOcr_validDocumentId_returnsCompletedResponse() {
        VaultDocument doc = new VaultDocument("pharmacy-branch-001", "inv.pdf", "application/pdf",
                2048L, "vault/pharmacy-branch-001/uuid/inv.pdf", "abc123");
        doc.setId(1L);

        when(documentVaultService.getDocument(1L)).thenReturn(doc);

        OcrProcessingResponse response = service.processDocumentOcr(1L);

        assertEquals("1", response.documentId());
        assertEquals("COMPLETED", response.ocrStatus());
        assertNotNull(response.extractedData());
        assertTrue(response.extractedData().contains("inv.pdf"));
    }
}
