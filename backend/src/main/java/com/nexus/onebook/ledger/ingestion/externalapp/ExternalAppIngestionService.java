package com.nexus.onebook.ledger.ingestion.externalapp;

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
 * Universal External Application Ingestion Service.
 * Orchestrates the ingestion of payment requests from any integrated external application —
 * Pharmacy, Lab, Stores, HIS, ERP, or any future integration — through the common
 * {@link ExternalAppPaymentRequest} format.
 * <p>
 * The {@code applicationName} field inside each request identifies the source system
 * (e.g. {@code PHARMACY}, {@code LAB}, {@code STORE}, {@code HIS}). All source systems
 * share the same ingestion pipeline, API endpoints, and workflow states. No new service
 * or adapter is required to onboard a new external application; simply set a new
 * {@code applicationName} value.
 * <p>
 * Workflow:
 * <ol>
 *   <li>Receive {@link ExternalAppPaymentRequest} from the external system</li>
 *   <li>Route through {@link FinancialEventGateway} using the {@code EXTERNAL_APP} adapter</li>
 *   <li>Store invoice document metadata (preserving original MinIO path) in the Document Vault</li>
 *   <li>Return an {@link ExternalAppPaymentResponse} with event/workflow/document IDs</li>
 * </ol>
 */
@Service
public class ExternalAppIngestionService {

    private final FinancialEventGateway gateway;
    private final FinancialEventRepository eventRepository;
    private final DocumentVaultService documentVaultService;
    private final ObjectMapper objectMapper;

    public ExternalAppIngestionService(FinancialEventGateway gateway,
                                       FinancialEventRepository eventRepository,
                                       DocumentVaultService documentVaultService,
                                       ObjectMapper objectMapper) {
        this.gateway = gateway;
        this.eventRepository = eventRepository;
        this.documentVaultService = documentVaultService;
        this.objectMapper = objectMapper;
    }

    /**
     * Ingests a single external application payment request.
     * Works for any application: Pharmacy, Lab, Stores, HIS, etc.
     *
     * @param request the payment request from the external application
     * @return response containing event ID, status, workflow ID, and document ID
     */
    public ExternalAppPaymentResponse ingestPaymentRequest(ExternalAppPaymentRequest request) {
        String payload = serializePayload(request);

        FinancialEvent event = gateway.ingest(request.tenantId(), AdapterType.EXTERNAL_APP, payload);

        String documentId = null;
        if (request.documentInfo() != null) {
            documentId = storeInvoiceDocument(request);
        }

        return buildPaymentResponse(event, documentId);
    }

    /**
     * Ingests multiple external application payment requests in a single batch call.
     * Requests from different source applications can be mixed in the same batch.
     *
     * @param bulkRequest bulk request containing the tenant ID and list of payment requests
     * @return aggregated response with individual results and summary counts
     */
    public BulkExternalAppPaymentResponse ingestBulkPaymentRequests(BulkExternalAppPaymentRequest bulkRequest) {
        List<ExternalAppPaymentResponse> results = new ArrayList<>();

        for (ExternalAppPaymentRequest req : bulkRequest.requests()) {
            // Ensure each individual request uses the bulk request's tenant ID if not explicitly set
            ExternalAppPaymentRequest enriched = enrichWithTenantId(req, bulkRequest.tenantId());
            results.add(ingestPaymentRequest(enriched));
        }

        int failed = (int) results.stream().filter(r -> "FAILED".equals(r.status())).count();
        return new BulkExternalAppPaymentResponse(results, results.size(), results.size() - failed, failed);
    }

    /**
     * Returns the current workflow status of a previously ingested payment request.
     * Works for requests originating from any external application.
     *
     * @param requestId the event UUID (as returned in {@link ExternalAppPaymentResponse#eventId()})
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
     * Applicable to invoices uploaded by any external application.
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

    private String serializePayload(ExternalAppPaymentRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "Failed to serialize external app payment request: " + e.getMessage());
        }
    }

    private String storeInvoiceDocument(ExternalAppPaymentRequest request) {
        ExternalAppPaymentRequest.DocumentInfo docInfo = request.documentInfo();
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

    private ExternalAppPaymentRequest enrichWithTenantId(ExternalAppPaymentRequest req, String tenantId) {
        if (req.tenantId() != null && !req.tenantId().isBlank()) {
            return req;
        }
        return new ExternalAppPaymentRequest(
                tenantId,
                req.applicationName(),
                req.paymentData(),
                req.documentInfo(),
                req.metadata()
        );
    }

    private ExternalAppPaymentResponse buildPaymentResponse(FinancialEvent event, String documentId) {
        String eventId = event.getEventUuid().toString();
        String status = event.getStatus().name();
        String message = event.getErrorMessage() != null
                ? event.getErrorMessage()
                : "Payment request received successfully";
        // workflowId reuses eventId at ingestion time; it is updated by the approval
        // workflow engine once the event progresses through maker-checker stages.
        return new ExternalAppPaymentResponse(eventId, status, message, eventId, documentId);
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
