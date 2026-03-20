package com.nexus.onebook.ledger.ingestion.adapter;

import com.nexus.onebook.ledger.ingestion.model.AdapterType;
import com.nexus.onebook.ledger.payment.model.PaymentRegisterEntry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class Iso20022AdapterTest {

    private final Iso20022Adapter adapter = new Iso20022Adapter();

    private static final String VALID_PAYLOAD = """
            <Document>
              <CstmrCdtTrfInitn>
                <GrpHdr><MsgId>MSG-001</MsgId></GrpHdr>
                <PmtInf>
                  <CdtTrfTxInf>
                    <Amt><InstdAmt Ccy="EUR">5000.00</InstdAmt></Amt>
                    <ReqdExctnDt>2026-03-10</ReqdExctnDt>
                    <Dbtr><Nm>Acme Corp</Nm></Dbtr>
                    <DbtrAcct><Id><IBAN>DE89370400440532013000</IBAN></Id></DbtrAcct>
                    <Cdtr><Nm>Supplier Ltd</Nm></Cdtr>
                    <CdtrAcct><Id><IBAN>GB29NWBK60161331926819</IBAN></Id></CdtrAcct>
                  </CdtTrfTxInf>
                </PmtInf>
              </CstmrCdtTrfInitn>
            </Document>
            """;

    @Test
    void getAdapterType_returnsIso20022() {
        assertEquals(AdapterType.ISO_20022, adapter.getAdapterType());
    }

    @Test
    void parse_validPayload_returnsEvent() {
        PaymentRegisterEntry event = adapter.parse("tenant-1", VALID_PAYLOAD);

        assertEquals("tenant-1", event.getTenantId());
        assertEquals(AdapterType.ISO_20022, event.getAdapterType());
        assertEquals("PAYMENT", event.getEventType());
        assertEquals(new BigDecimal("5000.00"), event.getAmount());
        assertEquals("EUR", event.getCurrency());
        assertEquals(LocalDate.of(2026, 3, 10), event.getEventDate());
        assertEquals("MSG-001", event.getSourceReference());
        assertEquals("DE89370400440532013000", event.getDebitAccountCode());
        assertEquals("GB29NWBK60161331926819", event.getCreditAccountCode());
        assertTrue(event.getDescription().contains("Acme Corp"));
        assertTrue(event.getDescription().contains("Supplier Ltd"));
    }

    @Test
    void parse_nullPayload_throws() {
        assertThrows(IllegalArgumentException.class, () -> adapter.parse("tenant-1", null));
    }

    @Test
    void parse_missingMsgId_throws() {
        String payload = "<Document><Amt><InstdAmt Ccy=\"USD\">100</InstdAmt></Amt>"
                + "<ReqdExctnDt>2026-03-10</ReqdExctnDt></Document>";
        assertThrows(IllegalArgumentException.class, () -> adapter.parse("tenant-1", payload));
    }

    @Test
    void parse_missingAmount_throws() {
        String payload = "<Document><MsgId>MSG-002</MsgId>"
                + "<ReqdExctnDt>2026-03-10</ReqdExctnDt></Document>";
        assertThrows(IllegalArgumentException.class, () -> adapter.parse("tenant-1", payload));
    }
}
