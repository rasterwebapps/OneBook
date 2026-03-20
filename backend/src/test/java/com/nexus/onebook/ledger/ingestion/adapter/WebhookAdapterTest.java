package com.nexus.onebook.ledger.ingestion.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.onebook.ledger.ingestion.model.AdapterType;
import com.nexus.onebook.ledger.payment.model.PaymentRegisterEntry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class WebhookAdapterTest {

    private final WebhookAdapter adapter = new WebhookAdapter(new ObjectMapper());

    private static final String VALID_PAYLOAD = """
            {
              "eventType": "INVOICE_PAID",
              "amount": 1250.00,
              "currency": "USD",
              "date": "2026-03-10",
              "description": "SaaS subscription payment",
              "sourceReference": "INV-001",
              "debitAccountCode": "1000",
              "creditAccountCode": "4000",
              "industryTags": { "saasProvider": "Acme Corp" }
            }
            """;

    @Test
    void getAdapterType_returnsRestWebhook() {
        assertEquals(AdapterType.REST_WEBHOOK, adapter.getAdapterType());
    }

    @Test
    void parse_validPayload_returnsEvent() {
        PaymentRegisterEntry event = adapter.parse("tenant-1", VALID_PAYLOAD);

        assertEquals("tenant-1", event.getTenantId());
        assertEquals(AdapterType.REST_WEBHOOK, event.getAdapterType());
        assertEquals("INVOICE_PAID", event.getEventType());
        assertEquals(0, new BigDecimal("1250.00").compareTo(event.getAmount()));
        assertEquals("USD", event.getCurrency());
        assertEquals(LocalDate.of(2026, 3, 10), event.getEventDate());
        assertEquals("SaaS subscription payment", event.getDescription());
        assertEquals("INV-001", event.getSourceReference());
        assertEquals("1000", event.getDebitAccountCode());
        assertEquals("4000", event.getCreditAccountCode());
        assertTrue(event.getIndustryTags().contains("Acme Corp"));
    }

    @Test
    void parse_nullPayload_throws() {
        assertThrows(IllegalArgumentException.class, () -> adapter.parse("tenant-1", null));
    }

    @Test
    void parse_invalidJson_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> adapter.parse("tenant-1", "not-json"));
    }

    @Test
    void parse_missingEventType_throws() {
        String payload = """
                { "amount": 100, "date": "2026-03-10" }
                """;
        assertThrows(IllegalArgumentException.class, () -> adapter.parse("tenant-1", payload));
    }

    @Test
    void parse_minimalPayload_usesDefaults() {
        String payload = """
                {
                  "eventType": "GENERIC",
                  "amount": 50,
                  "date": "2026-03-10"
                }
                """;

        PaymentRegisterEntry event = adapter.parse("tenant-1", payload);

        assertEquals("GENERIC", event.getEventType());
        assertEquals("USD", event.getCurrency());
        assertEquals("", event.getDescription());
    }
}
