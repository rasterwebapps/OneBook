package com.nexus.onebook.ingestion.gateway;

import com.nexus.onebook.ingestion.model.AdapterType;
import com.nexus.onebook.payment.model.PaymentRegisterEntry;

/**
 * Pluggable adapter interface for the Financial Event Gateway.
 * Each adapter parses raw payloads from a specific industry protocol
 * and produces normalised {@link PaymentRegisterEntry} objects.
 */
public interface FinancialEventAdapter {

    /**
     * Returns the adapter type this implementation handles.
     */
    AdapterType getAdapterType();

    /**
     * Parses a raw payload string into a normalised PaymentRegisterEntry.
     *
     * @param tenantId   the tenant context
     * @param rawPayload the raw message from the external system
     * @return a normalised PaymentRegisterEntry ready for mapping
     * @throws IllegalArgumentException if the payload cannot be parsed
     */
    PaymentRegisterEntry parse(String tenantId, String rawPayload);
}
