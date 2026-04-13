package com.nexus.onebook.ingestion.gateway;

import com.nexus.onebook.ingestion.model.AdapterType;
import com.nexus.onebook.payment.model.PaymentRegisterEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdapterRegistryTest {

    @Test
    void getAdapter_registeredType_returnsAdapter() {
        FinancialEventAdapter mockAdapter = new FinancialEventAdapter() {
            @Override
            public AdapterType getAdapterType() { return AdapterType.HL7; }

            @Override
            public PaymentRegisterEntry parse(String tenantId, String rawPayload) {
                return null;
            }
        };

        AdapterRegistry registry = new AdapterRegistry(List.of(mockAdapter));

        assertSame(mockAdapter, registry.getAdapter(AdapterType.HL7));
    }

    @Test
    void getAdapter_unregisteredType_throws() {
        AdapterRegistry registry = new AdapterRegistry(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> registry.getAdapter(AdapterType.HL7));
    }

    @Test
    void getRegisteredTypes_returnsAllTypes() {
        FinancialEventAdapter hl7 = new FinancialEventAdapter() {
            @Override
            public AdapterType getAdapterType() { return AdapterType.HL7; }

            @Override
            public PaymentRegisterEntry parse(String tenantId, String rawPayload) {
                return null;
            }
        };

        FinancialEventAdapter dms = new FinancialEventAdapter() {
            @Override
            public AdapterType getAdapterType() { return AdapterType.DMS; }

            @Override
            public PaymentRegisterEntry parse(String tenantId, String rawPayload) {
                return null;
            }
        };

        AdapterRegistry registry = new AdapterRegistry(List.of(hl7, dms));

        List<AdapterType> types = registry.getRegisteredTypes();
        assertEquals(2, types.size());
        assertTrue(types.contains(AdapterType.HL7));
        assertTrue(types.contains(AdapterType.DMS));
    }
}
