package com.nexus.onebook.ingestion.adapter;

import com.nexus.onebook.ingestion.gateway.FinancialEventAdapter;
import com.nexus.onebook.ingestion.model.AdapterType;
import com.nexus.onebook.payment.model.PaymentRegisterEntry;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * HL7 adapter for Healthcare systems.
 * Parses simplified HL7-style pipe-delimited financial messages (DFT/charge events).
 * <p>
 * Expected format: {@code DFT|eventType|patientId|amount|currency|date|description|debitAcct|creditAcct}
 * <p>
 * Example: {@code DFT|CHARGE|P-12345|1500.0000|USD|2026-03-10|Lab Test Fee|4100|2100}
 */
@Component
public class Hl7Adapter implements FinancialEventAdapter {

    private static final String DELIMITER = "\\|";
    private static final int MIN_SEGMENTS = 9;

    @Override
    public AdapterType getAdapterType() {
        return AdapterType.HL7;
    }

    @Override
    public PaymentRegisterEntry parse(String tenantId, String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            throw new IllegalArgumentException("HL7 payload must not be empty");
        }

        String[] segments = rawPayload.split(DELIMITER);
        if (segments.length < MIN_SEGMENTS) {
            throw new IllegalArgumentException(
                    "HL7 payload requires at least " + MIN_SEGMENTS + " pipe-delimited segments");
        }

        if (!"DFT".equals(segments[0])) {
            throw new IllegalArgumentException("HL7 adapter only supports DFT (financial) messages");
        }

        PaymentRegisterEntry event = new PaymentRegisterEntry(tenantId, AdapterType.HL7, segments[1]);
        event.setDescription(segments[6]);
        event.setAmount(new BigDecimal(segments[3]));
        event.setCurrency(segments[4]);
        event.setEventDate(LocalDate.parse(segments[5]));
        event.setSourceReference(segments[2]); // patientId
        event.setDebitAccountCode(segments[7]);
        event.setCreditAccountCode(segments[8]);
        event.setRawPayload(rawPayload);
        event.setIndustryTags("{\"patientId\":\"" + segments[2] + "\"}");

        return event;
    }
}
