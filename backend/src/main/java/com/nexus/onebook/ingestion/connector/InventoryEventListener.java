package com.nexus.onebook.ingestion.connector;

import com.nexus.onebook.ingestion.gateway.FinancialEventGateway;
import com.nexus.onebook.ingestion.model.AdapterType;
import com.nexus.onebook.payment.model.PaymentRegisterEntry;
import org.springframework.stereotype.Service;

/**
 * Inventory Event Listener.
 * Processes Stock-In and Stock-Out events and feeds them through the
 * Financial Event Gateway for automatic journal entry creation.
 */
@Service
public class InventoryEventListener {

    private final FinancialEventGateway gateway;

    public InventoryEventListener(FinancialEventGateway gateway) {
        this.gateway = gateway;
    }

    /**
     * Processes a stock movement event and ingests it through the gateway.
     *
     * @param tenantId   the tenant context
     * @param rawPayload the inventory event payload (JSON format expected by REST_WEBHOOK adapter)
     * @return the processed FinancialEvent
     */
    public PaymentRegisterEntry processInventoryEvent(String tenantId, String rawPayload) {
        return gateway.ingest(tenantId, AdapterType.REST_WEBHOOK, rawPayload);
    }
}
