package com.nexus.onebook.ledger.ingestion.pharmacy;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Pharmacy Integration Service — orchestrates the ingestion of payment requests
 * from Pharmacy and other integrated external applications.
 * <p>
 * Workflow:
 * <ol>
 *   <li>Receive {@link PharmacyPaymentRequest} from the external system</li>
 *   <li>Route through {@link FinancialEventGateway} using the {@code PHARMACY} adapter</li>
 *   <li>Store invoice document metadata (preserving original MinIO path)</li>
 *   <li>Return a {@link PharmacyPaymentResponse} with event/workflow/document IDs</li>
 * </ol>
 */
@Service
public class PharmacyIngestionService {

    private final FinancialEventGateway gateway;
    private final FinancialEventRepository eventRepository;
    private final DocumentVaultService documentVaultService;
    private final ObjectMapper objectMapper;

    public PharmacyIngestionService(FinancialEventGateway gateway,
                                    FinancialEventRepository eventRepository,
                                    DocumentVaultService documentVaultService,
                                    ObjectMapper objectMapper) {
        this.gateway = gateway;
        this.eventRepository = eventRepository;
        this.documentVaultService = documentVaultService;
        this.objectMapper = objectMapper;
    }

    /**
     * Ingests a single pharmacy payment request.
     *
     * @param request the payment request from the external pharmacy application
     * @return response containing event ID, status, workflow ID, and document ID
     */
    public PharmacyPaymentResponse ingestPaymentRequest(PharmacyPaymentRequest request) {
        String payload = serializePayload(request);

        FinancialEvent event = gateway.ingest(request.tenantId(), AdapterType.PHARMACY, payload);

        String documentId = null;
        if (request.documentInfo() != null) {
            documentId = storeInvoiceDocument(request);
        }

        return buildPaymentResponse(event, documentId);
    }

    /**
     * Ingests multiple pharmacy payment requests in a single batch call.
     *
     * @param bulkRequest bulk request containing the tenant ID and list of payment requests
     * @return aggregated response with individual results and summary counts
     */
    public BulkPharmacyPaymentResponse ingestBulkPaymentRequests(BulkPharmacyPaymentRequest bulkRequest) {
        List<PharmacyPaymentResponse> results = new ArrayList<>();

        for (PharmacyPaymentRequest req : bulkRequest.requests()) {
            // Ensure each individual request uses the bulk request's tenant ID if not set
            PharmacyPaymentRequest enriched = enrichWithTenantId(req, bulkRequest.tenantId());
            results.add(ingestPaymentRequest(enriched));
        }

        int failed = (int) results.stream().filter(r -> "FAILED".equals(r.status())).count();
        return new BulkPharmacyPaymentResponse(results, results.size(), results.size() - failed, failed);
    }

    /**
     * Returns the current workflow status of a previously ingested payment request.
     *
     * @param requestId the event UUID (as returned in {@link PharmacyPaymentResponse#eventId()})
     * @return current status and workflow stage of the payment request
     */
    public PaymentStatusResponse getPaymentStatus(String requestId) {
        UUID eventUuid = parseEventUuid(requestId);
        FinancialEvent event = eventRepository.findByEventUuid(eventUuid)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment request not found: " + requestId));
        return mapToStatusResponse(event);
    }

    /**
     * Triggers OCR processing for a document stored in the vault.
     *
     * @param documentId the vault document ID
     * @return OCR processing result with extracted data
     */
    public OcrProcessingResponse processDocumentOcr(Long documentId) {
        VaultDocument document = documentVaultService.getDocument(documentId);
        // In production, this would trigger an AI/ML pipeline (AWS Textract, Google Document AI)
        // to extract structured data from the document stored at document.getStorageKey().
        return new OcrProcessingResponse(
                String.valueOf(documentId),
                "COMPLETED",
                buildOcrExtractedData(document),
                "OCR processing completed for document: " + document.getFileName()
        );
    }

    // --- Private helpers ---

    private String serializePayload(PharmacyPaymentRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize pharmacy payment request: " + e.getMessage());
        }
    }

    private String storeInvoiceDocument(PharmacyPaymentRequest request) {
        PharmacyPaymentRequest.DocumentInfo docInfo = request.documentInfo();
        if (docInfo == null || docInfo.fileName() == null || docInfo.checksum() == null) {
            return null;
        }

        String uploadedBy = request.metadata() != null ? request.metadata().createdBy() : null;

        DocumentUploadRequest uploadRequest = new DocumentUploadRequest(
                request.tenantId(),
                docInfo.fileName(),
                docInfo.contentType() != null ? docInfo.contentType() : "application/octet-stream",
                docInfo.fileSize() != null ? docInfo.fileSize() : 0L,
                docInfo.checksum(),
                null,
                uploadedBy
        );

        VaultDocument doc = documentVaultService.storeDocumentWithOriginalPath(
                uploadRequest,
                docInfo.invoiceFilePath()
        );
        return doc.getId() != null ? doc.getId().toString() : null;
    }

    private PharmacyPaymentRequest enrichWithTenantId(PharmacyPaymentRequest req, String tenantId) {
        if (req.tenantId() != null && !req.tenantId().isBlank()) {
            return req;
        }
        return new PharmacyPaymentRequest(
                tenantId,
                req.applicationName(),
                req.paymentData(),
                req.documentInfo(),
                req.metadata()
        );
    }

    private PharmacyPaymentResponse buildPaymentResponse(FinancialEvent event, String documentId) {
        String eventId = event.getEventUuid().toString();
        String status = event.getStatus().name();
        String message = event.getErrorMessage() != null
                ? event.getErrorMessage()
                : "Payment request received successfully";
        return new PharmacyPaymentResponse(eventId, status, message, eventId, documentId);
    }

    private UUID parseEventUuid(String requestId) {
        try {
            return UUID.fromString(requestId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid payment request ID format: " + requestId);
        }
    }

    private PaymentStatusResponse mapToStatusResponse(FinancialEvent event) {
        EventStatus eventStatus = event.getStatus();
        String externalStatus = mapEventStatusToExternal(eventStatus);
        String workflowStage = mapEventStatusToWorkflowStage(eventStatus);

        return new PaymentStatusResponse(
                event.getEventUuid().toString(),
                externalStatus,
                workflowStage,
                null,
                null,
                null
        );
    }

    private String mapEventStatusToExternal(EventStatus status) {
        return switch (status) {
            case RECEIVED -> "RECEIVED";
            case VALIDATED -> "VALIDATED";
            case MAPPED -> "PROCESSED";
            case POSTED -> "APPROVED";
            case FAILED -> "FAILED";
            case REJECTED -> "REJECTED";
        };
    }

    private String mapEventStatusToWorkflowStage(EventStatus status) {
        return switch (status) {
            case RECEIVED, VALIDATED, MAPPED -> "PENDING";
            case POSTED -> "PENDING_PAYMENT";
            case FAILED, REJECTED -> "REJECTED";
        };
    }

    private String buildOcrExtractedData(VaultDocument document) {
        return String.format(
                "{\"fileName\":\"%s\",\"contentType\":\"%s\",\"fileSize\":%d,\"storageKey\":\"%s\"}",
                document.getFileName(),
                document.getContentType(),
                document.getFileSize(),
                document.getStorageKey()
        );
    }
}
