package com.nexus.onebook.ledger.ingestion.externalapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.onebook.ledger.dto.DocumentUploadRequest;
import com.nexus.onebook.ledger.ingestion.dto.*;
import com.nexus.onebook.ledger.ingestion.gateway.FinancialEventGateway;
import com.nexus.onebook.ledger.ingestion.model.AdapterType;
import com.nexus.onebook.ledger.payment.model.PaymentRegisterStatus;
import com.nexus.onebook.ledger.payment.model.PaymentRegisterEntry;
import com.nexus.onebook.ledger.payment.repository.PaymentRegisterRepository;
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
    private DocumentVaultService documentVaultService;

    @Mock
    private PaymentRegisterRepository paymentRegisterRepository;

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

    private static PaymentRegisterEntry buildEvent(PaymentRegisterStatus status) {
        PaymentRegisterEntry event = new PaymentRegisterEntry("branch-001", AdapterType.EXTERNAL_APP, "PURCHASE_PAYMENT");
        event.setId(1L);
        event.setEventUuid(UUID.randomUUID());
        event.setStatus(status);
        return event;
    }

    // --- Single ingestion ---

    @Test
    void ingestPaymentRequest_pharmacy_returnsAvailableForProcessing() {
        when(gateway.ingest(eq("branch-001"), eq(AdapterType.EXTERNAL_APP), anyString()))
                .thenReturn(buildEvent(PaymentRegisterStatus.VALIDATED));

        ExternalAppPaymentResponse response = service.ingestPaymentRequest(buildRequest("PHARMACY"));

        assertNotNull(response);
        assertEquals("AVAILABLE_FOR_PROCESSING", response.status());
        assertEquals("Payment request received successfully", response.message());
        assertNull(response.documentId());
    }

    @Test
    void ingestPaymentRequest_lab_returnsAvailableForProcessing() {
        when(gateway.ingest(eq("branch-001"), eq(AdapterType.EXTERNAL_APP), anyString()))
                .thenReturn(buildEvent(PaymentRegisterStatus.VALIDATED));

        ExternalAppPaymentResponse response = service.ingestPaymentRequest(buildRequest("LAB"));

        assertNotNull(response);
        assertEquals("AVAILABLE_FOR_PROCESSING", response.status());
    }

    @Test
    void ingestPaymentRequest_store_returnsAvailableForProcessing() {
        when(gateway.ingest(anyString(), any(), anyString()))
                .thenReturn(buildEvent(PaymentRegisterStatus.VALIDATED));

        ExternalAppPaymentResponse response = service.ingestPaymentRequest(buildRequest("STORE"));

        assertEquals("AVAILABLE_FOR_PROCESSING", response.status());
    }

    @Test
    void ingestPaymentRequest_his_returnsAvailableForProcessing() {
        when(gateway.ingest(anyString(), any(), anyString()))
                .thenReturn(buildEvent(PaymentRegisterStatus.VALIDATED));

        ExternalAppPaymentResponse response = service.ingestPaymentRequest(buildRequest("HIS"));

        assertEquals("AVAILABLE_FOR_PROCESSING", response.status());
    }

    @Test
    void ingestPaymentRequest_enrichesAndSavesEntry() {
        PaymentRegisterEntry entry = buildEvent(PaymentRegisterStatus.VALIDATED);
        when(gateway.ingest(eq("branch-001"), eq(AdapterType.EXTERNAL_APP), anyString()))
                .thenReturn(entry);

        service.ingestPaymentRequest(buildRequest("PHARMACY"));

        // Verify the entry was enriched with payment details and saved
        verify(paymentRegisterRepository).save(argThat((PaymentRegisterEntry e) ->
                "PHARMACY".equals(e.getSourceType())
                && "PURCHASE_PAYMENT".equals(e.getTransactionType())
                && "XYZ Supplies".equals(e.getVendorName())
                && new BigDecimal("14250.00").compareTo(e.getAmount()) == 0
                && "NEFT".equals(e.getPaymentMode())
                && "1234567890".equals(e.getBankAccountNumber())
                && "SBIN0001234".equals(e.getBankIfscCode())
                && PaymentRegisterStatus.AVAILABLE_FOR_PROCESSING == e.getStatus()
        ));
    }

    @Test
    void ingestPaymentRequest_failedEvent_doesNotCreateRegisterEntry() {
        PaymentRegisterEntry failedEvent = buildEvent(PaymentRegisterStatus.FAILED);
        failedEvent.setErrorMessage("Parse error");

        when(gateway.ingest(anyString(), any(), anyString()))
                .thenReturn(failedEvent);

        service.ingestPaymentRequest(buildRequest("PHARMACY"));

        verify(paymentRegisterRepository, never()).save(any());
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
                .thenReturn(buildEvent(PaymentRegisterStatus.VALIDATED));

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
                .thenReturn(buildEvent(PaymentRegisterStatus.VALIDATED));

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
        PaymentRegisterEntry failedEvent = buildEvent(PaymentRegisterStatus.FAILED);
        failedEvent.setErrorMessage("Parse error");

        when(gateway.ingest(anyString(), any(), anyString()))
                .thenReturn(buildEvent(PaymentRegisterStatus.VALIDATED))
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
        PaymentRegisterEntry event = buildEvent(PaymentRegisterStatus.RECEIVED);
        when(paymentRegisterRepository.findByEventUuid(eventUuid)).thenReturn(Optional.of(event));

        PaymentStatusResponse status = service.getPaymentStatus(eventUuid.toString());

        assertEquals("RECEIVED", status.status());
        assertEquals("PENDING", status.workflowStage());
    }

    @Test
    void getPaymentStatus_validatedEvent_returnsPendingWorkflowStage() {
        UUID eventUuid = UUID.randomUUID();
        PaymentRegisterEntry event = buildEvent(PaymentRegisterStatus.VALIDATED);
        when(paymentRegisterRepository.findByEventUuid(eventUuid)).thenReturn(Optional.of(event));

        PaymentStatusResponse status = service.getPaymentStatus(eventUuid.toString());

        assertEquals("VALIDATED", status.status());
        assertEquals("PENDING", status.workflowStage());
    }

    @Test
    void getPaymentStatus_postedEvent_returnsPendingPaymentWorkflowStage() {
        UUID eventUuid = UUID.randomUUID();
        PaymentRegisterEntry event = buildEvent(PaymentRegisterStatus.POSTED);
        when(paymentRegisterRepository.findByEventUuid(eventUuid)).thenReturn(Optional.of(event));

        PaymentStatusResponse status = service.getPaymentStatus(eventUuid.toString());

        assertEquals("APPROVED", status.status());
        assertEquals("PENDING_PAYMENT", status.workflowStage());
    }

    @Test
    void getPaymentStatus_failedEvent_returnsRejectedWorkflowStage() {
        UUID eventUuid = UUID.randomUUID();
        PaymentRegisterEntry event = buildEvent(PaymentRegisterStatus.FAILED);
        when(paymentRegisterRepository.findByEventUuid(eventUuid)).thenReturn(Optional.of(event));

        PaymentStatusResponse status = service.getPaymentStatus(eventUuid.toString());

        assertEquals("FAILED", status.status());
        assertEquals("REJECTED", status.workflowStage());
    }

    @Test
    void getPaymentStatus_rejectedEvent_returnsRejectedWorkflowStage() {
        UUID eventUuid = UUID.randomUUID();
        PaymentRegisterEntry event = buildEvent(PaymentRegisterStatus.REJECTED);
        when(paymentRegisterRepository.findByEventUuid(eventUuid)).thenReturn(Optional.of(event));

        PaymentStatusResponse status = service.getPaymentStatus(eventUuid.toString());

        assertEquals("REJECTED", status.status());
        assertEquals("REJECTED", status.workflowStage());
    }

    @Test
    void getPaymentStatus_notFound_throws() {
        UUID eventUuid = UUID.randomUUID();
        when(paymentRegisterRepository.findByEventUuid(eventUuid)).thenReturn(Optional.empty());

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
