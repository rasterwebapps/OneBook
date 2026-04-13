package com.nexus.onebook.ingestion.connector;

import com.nexus.onebook.ingestion.gateway.FinancialEventGateway;
import com.nexus.onebook.ingestion.model.AdapterType;
import com.nexus.onebook.payment.model.PaymentRegisterStatus;
import com.nexus.onebook.payment.model.PaymentRegisterEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryEventListenerTest {

    @Mock
    private FinancialEventGateway gateway;

    @InjectMocks
    private InventoryEventListener listener;

    @Test
    void processInventoryEvent_delegatesToGateway() {
        String payload = "{\"eventType\":\"STOCK_IN\",\"amount\":2500,\"date\":\"2026-03-10\"}";
        PaymentRegisterEntry expected = new PaymentRegisterEntry("tenant-1", AdapterType.REST_WEBHOOK, "STOCK_IN");
        expected.setStatus(PaymentRegisterStatus.POSTED);

        when(gateway.ingest("tenant-1", AdapterType.REST_WEBHOOK, payload)).thenReturn(expected);

        PaymentRegisterEntry result = listener.processInventoryEvent("tenant-1", payload);

        assertEquals(PaymentRegisterStatus.POSTED, result.getStatus());
        verify(gateway).ingest("tenant-1", AdapterType.REST_WEBHOOK, payload);
    }
}
