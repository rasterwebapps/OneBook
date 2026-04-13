package com.nexus.onebook.ingestion.dto;

/**
 * Response DTO for querying the workflow status of an ingested payment request.
 * Maps the internal financial event state to the external workflow representation.
 */
public record PaymentStatusResponse(
        String requestId,
        String status,
        String workflowStage,
        String approvedBy,
        String approvedDate,
        PaymentDetails paymentDetails
) {
    public record PaymentDetails(
            String paymentMethod,
            String bankReference,
            String paymentDate
    ) {}
}
