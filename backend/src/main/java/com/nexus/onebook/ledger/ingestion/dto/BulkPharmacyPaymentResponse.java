package com.nexus.onebook.ledger.ingestion.dto;

import java.util.List;

/**
 * Response DTO returned after a bulk pharmacy payment request ingestion.
 */
public record BulkPharmacyPaymentResponse(
        List<PharmacyPaymentResponse> results,
        int totalReceived,
        int totalSucceeded,
        int totalFailed
) {}
