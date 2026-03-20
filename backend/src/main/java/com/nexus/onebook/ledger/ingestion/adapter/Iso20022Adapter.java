package com.nexus.onebook.ledger.ingestion.adapter;

import com.nexus.onebook.ledger.ingestion.gateway.FinancialEventAdapter;
import com.nexus.onebook.ledger.ingestion.model.AdapterType;
import com.nexus.onebook.ledger.payment.model.PaymentRegisterEntry;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ISO 20022 adapter for Banking and direct bank reconciliations.
 * Parses simplified ISO 20022-style XML payment messages (pain.001 / camt.053).
 * <p>
 * A production adapter would use a full XML parser; this reference implementation
 * extracts key fields from a minimal XML structure for demonstration purposes.
 */
@Component
public class Iso20022Adapter implements FinancialEventAdapter {

    private static final Pattern MSG_ID = Pattern.compile("<MsgId>([^<]+)</MsgId>");
    private static final Pattern AMOUNT = Pattern.compile("<InstdAmt[^>]*>([^<]+)</InstdAmt>");
    private static final Pattern CURRENCY = Pattern.compile("<InstdAmt Ccy=\"([^\"]+)\"");
    private static final Pattern DATE = Pattern.compile("<ReqdExctnDt>([^<]+)</ReqdExctnDt>");
    private static final Pattern DEBTOR = Pattern.compile("<Dbtr><Nm>([^<]+)</Nm></Dbtr>");
    private static final Pattern CREDITOR = Pattern.compile("<Cdtr><Nm>([^<]+)</Nm></Cdtr>");
    private static final Pattern DEBTOR_ACCT = Pattern.compile("<DbtrAcct><Id><IBAN>([^<]+)</IBAN></Id></DbtrAcct>");
    private static final Pattern CREDITOR_ACCT = Pattern.compile("<CdtrAcct><Id><IBAN>([^<]+)</IBAN></Id></CdtrAcct>");

    @Override
    public AdapterType getAdapterType() {
        return AdapterType.ISO_20022;
    }

    @Override
    public PaymentRegisterEntry parse(String tenantId, String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            throw new IllegalArgumentException("ISO 20022 payload must not be empty");
        }

        String msgId = extractRequired(MSG_ID, rawPayload, "MsgId");
        String amountStr = extractRequired(AMOUNT, rawPayload, "InstdAmt");
        String dateStr = extractRequired(DATE, rawPayload, "ReqdExctnDt");

        String currency = extractOptional(CURRENCY, rawPayload, "USD");
        String debtor = extractOptional(DEBTOR, rawPayload, "");
        String creditor = extractOptional(CREDITOR, rawPayload, "");
        String debtorAcct = extractOptional(DEBTOR_ACCT, rawPayload, "");
        String creditorAcct = extractOptional(CREDITOR_ACCT, rawPayload, "");

        PaymentRegisterEntry event = new PaymentRegisterEntry(tenantId, AdapterType.ISO_20022, "PAYMENT");
        event.setDescription("Payment from " + debtor + " to " + creditor);
        event.setAmount(new BigDecimal(amountStr));
        event.setCurrency(currency);
        event.setEventDate(LocalDate.parse(dateStr));
        event.setSourceReference(msgId);
        event.setDebitAccountCode(debtorAcct);
        event.setCreditAccountCode(creditorAcct);
        event.setRawPayload(rawPayload);
        event.setIndustryTags("{\"msgId\":\"" + msgId + "\",\"debtor\":\"" + debtor
                + "\",\"creditor\":\"" + creditor + "\"}");

        return event;
    }

    private String extractRequired(Pattern pattern, String payload, String fieldName) {
        Matcher m = pattern.matcher(payload);
        if (!m.find()) {
            throw new IllegalArgumentException("ISO 20022 payload missing required field: " + fieldName);
        }
        return m.group(1);
    }

    private String extractOptional(Pattern pattern, String payload, String defaultValue) {
        Matcher m = pattern.matcher(payload);
        return m.find() ? m.group(1) : defaultValue;
    }
}
