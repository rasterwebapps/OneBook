package com.nexus.onebook.ledger.ingestion.connector;

import com.nexus.onebook.ledger.ingestion.gateway.FinancialEventGateway;
import com.nexus.onebook.ledger.ingestion.model.AdapterType;
import com.nexus.onebook.ledger.payment.model.PaymentRegisterStatus;
import com.nexus.onebook.ledger.payment.model.PaymentRegisterEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HrmPayrollConnectorTest {

    @Mock
    private FinancialEventGateway gateway;

    @InjectMocks
    private HrmPayrollConnector connector;

    @Test
    void processPayrollEvent_delegatesToGateway() {
        String payload = "{\"eventType\":\"SALARY\",\"amount\":5000,\"date\":\"2026-03-10\"}";
        PaymentRegisterEntry expected = new PaymentRegisterEntry("tenant-1", AdapterType.REST_WEBHOOK, "SALARY");
        expected.setStatus(PaymentRegisterStatus.POSTED);

        when(gateway.ingest("tenant-1", AdapterType.REST_WEBHOOK, payload)).thenReturn(expected);

        PaymentRegisterEntry result = connector.processPayrollEvent("tenant-1", payload);

        assertEquals(PaymentRegisterStatus.POSTED, result.getStatus());
        verify(gateway).ingest("tenant-1", AdapterType.REST_WEBHOOK, payload);
    }
}
