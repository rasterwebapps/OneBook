package com.nexus.onebook.ingestion.dto;

import com.nexus.onebook.payment.model.PaymentRegisterStatus;
import java.util.UUID;

/**
 * Response DTO returned after a financial event is ingested.
 */
public record FinancialEventResponse(
        UUID eventUuid,
        PaymentRegisterStatus status,
        String message
) {}
