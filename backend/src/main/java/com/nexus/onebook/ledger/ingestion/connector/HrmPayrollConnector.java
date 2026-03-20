package com.nexus.onebook.ledger.ingestion.connector;

import com.nexus.onebook.ledger.ingestion.gateway.FinancialEventGateway;
import com.nexus.onebook.ledger.ingestion.model.AdapterType;
import com.nexus.onebook.ledger.payment.model.PaymentRegisterEntry;
import org.springframework.stereotype.Service;

/**
 * HRM/Payroll Connector.
 * Listens for payroll events and feeds them through the Financial Event Gateway
 * for automatic journal entry creation (salary expenses, tax withholdings, etc.).
 */
@Service
public class HrmPayrollConnector {

    private final FinancialEventGateway gateway;

    public HrmPayrollConnector(FinancialEventGateway gateway) {
        this.gateway = gateway;
    }

    /**
     * Processes a payroll event payload and ingests it through the gateway.
     *
     * @param tenantId   the tenant context
     * @param rawPayload the payroll event payload (JSON format expected by REST_WEBHOOK adapter)
     * @return the processed FinancialEvent
     */
    public PaymentRegisterEntry processPayrollEvent(String tenantId, String rawPayload) {
        return gateway.ingest(tenantId, AdapterType.REST_WEBHOOK, rawPayload);
    }
}
