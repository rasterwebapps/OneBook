package com.nexus.onebook.ledger.ingestion.dto;

import java.util.List;

/**
 * Response DTO returned after a bulk external application payment request ingestion.
 * Common across all source applications (Pharmacy, Lab, Stores, HIS, etc.).
 */
public record BulkExternalAppPaymentResponse(
        List<ExternalAppPaymentResponse> results,
        int totalReceived,
        int totalSucceeded,
        int totalFailed
) {}
