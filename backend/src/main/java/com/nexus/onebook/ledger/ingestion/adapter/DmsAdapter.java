package com.nexus.onebook.ledger.ingestion.adapter;

import com.nexus.onebook.ledger.ingestion.gateway.FinancialEventAdapter;
import com.nexus.onebook.ledger.ingestion.model.AdapterType;
import com.nexus.onebook.ledger.payment.model.PaymentRegisterEntry;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DMS adapter for Automotive Dealer Management systems.
 * Parses pipe-delimited messages for vehicle sales and service events.
 * <p>
 * Expected format: {@code DMS|eventType|vin|amount|currency|date|description|debitAcct|creditAcct}
 * <p>
 * Example: {@code DMS|VEHICLE_SALE|1HGBH41JXMN109186|35000.0000|USD|2026-03-10|Honda Civic Sale|1200|4000}
 */
@Component
public class DmsAdapter implements FinancialEventAdapter {

    private static final String DELIMITER = "\\|";
    private static final int MIN_SEGMENTS = 9;

    @Override
    public AdapterType getAdapterType() {
        return AdapterType.DMS;
    }

    @Override
    public PaymentRegisterEntry parse(String tenantId, String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            throw new IllegalArgumentException("DMS payload must not be empty");
        }

        String[] segments = rawPayload.split(DELIMITER);
        if (segments.length < MIN_SEGMENTS) {
            throw new IllegalArgumentException(
                    "DMS payload requires at least " + MIN_SEGMENTS + " pipe-delimited segments");
        }

        if (!"DMS".equals(segments[0])) {
            throw new IllegalArgumentException("DMS adapter only supports DMS-prefixed messages");
        }

        PaymentRegisterEntry event = new PaymentRegisterEntry(tenantId, AdapterType.DMS, segments[1]);
        event.setDescription(segments[6]);
        event.setAmount(new BigDecimal(segments[3]));
        event.setCurrency(segments[4]);
        event.setEventDate(LocalDate.parse(segments[5]));
        event.setSourceReference(segments[2]); // VIN
        event.setDebitAccountCode(segments[7]);
        event.setCreditAccountCode(segments[8]);
        event.setRawPayload(rawPayload);
        event.setIndustryTags("{\"vin\":\"" + segments[2] + "\"}");

        return event;
    }
}
