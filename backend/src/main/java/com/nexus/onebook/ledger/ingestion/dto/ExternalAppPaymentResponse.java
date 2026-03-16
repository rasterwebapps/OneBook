package com.nexus.onebook.ledger.ingestion.dto;

/**
 * Response DTO returned after an external application payment request is ingested.
 * Common across all source applications (Pharmacy, Lab, Stores, HIS, etc.).
 */
public record ExternalAppPaymentResponse(
        String eventId,
        String status,
        String message,
        String workflowId,
        String documentId
) {}
