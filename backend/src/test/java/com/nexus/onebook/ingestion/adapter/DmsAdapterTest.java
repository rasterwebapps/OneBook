package com.nexus.onebook.ingestion.adapter;

import com.nexus.onebook.ingestion.model.AdapterType;
import com.nexus.onebook.payment.model.PaymentRegisterEntry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DmsAdapterTest {

    private final DmsAdapter adapter = new DmsAdapter();

    @Test
    void getAdapterType_returnsDms() {
        assertEquals(AdapterType.DMS, adapter.getAdapterType());
    }

    @Test
    void parse_validPayload_returnsEvent() {
        String payload = "DMS|VEHICLE_SALE|1HGBH41JXMN109186|35000.0000|USD|2026-03-10|Honda Civic Sale|1200|4000";

        PaymentRegisterEntry event = adapter.parse("tenant-1", payload);

        assertEquals("tenant-1", event.getTenantId());
        assertEquals(AdapterType.DMS, event.getAdapterType());
        assertEquals("VEHICLE_SALE", event.getEventType());
        assertEquals("Honda Civic Sale", event.getDescription());
        assertEquals(new BigDecimal("35000.0000"), event.getAmount());
        assertEquals("USD", event.getCurrency());
        assertEquals(LocalDate.of(2026, 3, 10), event.getEventDate());
        assertEquals("1HGBH41JXMN109186", event.getSourceReference());
        assertEquals("1200", event.getDebitAccountCode());
        assertEquals("4000", event.getCreditAccountCode());
        assertTrue(event.getIndustryTags().contains("1HGBH41JXMN109186"));
    }

    @Test
    void parse_nullPayload_throws() {
        assertThrows(IllegalArgumentException.class, () -> adapter.parse("tenant-1", null));
    }

    @Test
    void parse_emptyPayload_throws() {
        assertThrows(IllegalArgumentException.class, () -> adapter.parse("tenant-1", "  "));
    }

    @Test
    void parse_wrongPrefix_throws() {
        String payload = "HL7|VEHICLE_SALE|VIN123|35000|USD|2026-03-10|Sale|1200|4000";
        assertThrows(IllegalArgumentException.class, () -> adapter.parse("tenant-1", payload));
    }
}
