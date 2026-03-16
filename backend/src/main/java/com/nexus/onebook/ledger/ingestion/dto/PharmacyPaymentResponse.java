package com.nexus.onebook.ledger.ingestion.dto;

/**
 * Response DTO returned after a pharmacy payment request is ingested.
 */
public record PharmacyPaymentResponse(
        String eventId,
        String status,
        String message,
        String workflowId,
        String documentId
) {}
