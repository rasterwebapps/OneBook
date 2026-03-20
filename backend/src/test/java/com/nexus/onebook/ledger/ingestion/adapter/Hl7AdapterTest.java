package com.nexus.onebook.ledger.ingestion.adapter;

import com.nexus.onebook.ledger.ingestion.model.AdapterType;
import com.nexus.onebook.ledger.payment.model.PaymentRegisterEntry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class Hl7AdapterTest {

    private final Hl7Adapter adapter = new Hl7Adapter();

    @Test
    void getAdapterType_returnsHl7() {
        assertEquals(AdapterType.HL7, adapter.getAdapterType());
    }

    @Test
    void parse_validPayload_returnsEvent() {
        String payload = "DFT|CHARGE|P-12345|1500.0000|USD|2026-03-10|Lab Test Fee|4100|2100";

        PaymentRegisterEntry event = adapter.parse("tenant-1", payload);

        assertEquals("tenant-1", event.getTenantId());
        assertEquals(AdapterType.HL7, event.getAdapterType());
        assertEquals("CHARGE", event.getEventType());
        assertEquals("Lab Test Fee", event.getDescription());
        assertEquals(new BigDecimal("1500.0000"), event.getAmount());
        assertEquals("USD", event.getCurrency());
        assertEquals(LocalDate.of(2026, 3, 10), event.getEventDate());
        assertEquals("P-12345", event.getSourceReference());
        assertEquals("4100", event.getDebitAccountCode());
        assertEquals("2100", event.getCreditAccountCode());
        assertTrue(event.getIndustryTags().contains("P-12345"));
    }

    @Test
    void parse_nullPayload_throws() {
        assertThrows(IllegalArgumentException.class, () -> adapter.parse("tenant-1", null));
    }

    @Test
    void parse_emptyPayload_throws() {
        assertThrows(IllegalArgumentException.class, () -> adapter.parse("tenant-1", ""));
    }

    @Test
    void parse_tooFewSegments_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> adapter.parse("tenant-1", "DFT|CHARGE|P-12345"));
    }

    @Test
    void parse_wrongPrefix_throws() {
        String payload = "ADT|CHARGE|P-12345|1500.0000|USD|2026-03-10|Lab Test|4100|2100";
        assertThrows(IllegalArgumentException.class, () -> adapter.parse("tenant-1", payload));
    }
}
