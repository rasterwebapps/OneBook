package com.nexus.onebook.ingestion.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.onebook.ingestion.gateway.FinancialEventAdapter;
import com.nexus.onebook.ingestion.model.AdapterType;
import com.nexus.onebook.payment.model.PaymentRegisterEntry;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Generic REST/Webhook adapter for SaaS integrations.
 * Accepts a JSON payload with a standardised set of fields.
 * <p>
 * Expected JSON:
 * <pre>{@code
 * {
 *   "eventType": "INVOICE_PAID",
 *   "amount": 1250.00,
 *   "currency": "USD",
 *   "date": "2026-03-10",
 *   "description": "SaaS subscription payment",
 *   "sourceReference": "INV-001",
 *   "debitAccountCode": "1000",
 *   "creditAccountCode": "4000",
 *   "industryTags": { "saasProvider": "Acme Corp" }
 * }
 * }</pre>
 */
@Component
public class WebhookAdapter implements FinancialEventAdapter {

    private final ObjectMapper objectMapper;

    public WebhookAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public AdapterType getAdapterType() {
        return AdapterType.REST_WEBHOOK;
    }

    @Override
    public PaymentRegisterEntry parse(String tenantId, String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            throw new IllegalArgumentException("Webhook payload must not be empty");
        }

        try {
            JsonNode root = objectMapper.readTree(rawPayload);

            String eventType = requiredText(root, "eventType");
            BigDecimal amount = new BigDecimal(requiredText(root, "amount"));
            String dateStr = requiredText(root, "date");

            PaymentRegisterEntry event = new PaymentRegisterEntry(tenantId, AdapterType.REST_WEBHOOK, eventType);
            event.setAmount(amount);
            event.setCurrency(optionalText(root, "currency", "USD"));
            event.setEventDate(LocalDate.parse(dateStr));
            event.setDescription(optionalText(root, "description", ""));
            event.setSourceReference(optionalText(root, "sourceReference", ""));
            event.setDebitAccountCode(optionalText(root, "debitAccountCode", ""));
            event.setCreditAccountCode(optionalText(root, "creditAccountCode", ""));
            event.setRawPayload(rawPayload);

            JsonNode tags = root.get("industryTags");
            if (tags != null) {
                event.setIndustryTags(objectMapper.writeValueAsString(tags));
            }

            return event;

        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON in webhook payload: " + e.getMessage());
        }
    }

    private String requiredText(JsonNode root, String field) {
        JsonNode node = root.get(field);
        if (node == null || node.isNull()) {
            throw new IllegalArgumentException("Webhook payload missing required field: " + field);
        }
        return node.asText();
    }

    private String optionalText(JsonNode root, String field, String defaultValue) {
        JsonNode node = root.get(field);
        return (node != null && !node.isNull()) ? node.asText() : defaultValue;
    }
}
