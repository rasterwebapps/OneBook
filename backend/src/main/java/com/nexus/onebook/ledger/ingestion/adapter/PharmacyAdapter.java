package com.nexus.onebook.ledger.ingestion.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.onebook.ledger.ingestion.gateway.FinancialEventAdapter;
import com.nexus.onebook.ledger.ingestion.model.AdapterType;
import com.nexus.onebook.ledger.ingestion.model.FinancialEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Adapter for Pharmacy and other integrated external applications.
 * Parses the pharmacy payment request format and maps it to a normalised
 * {@link FinancialEvent} that drives the downstream journal entry workflow.
 * <p>
 * Expected JSON payload (PharmacyPaymentRequest serialized):
 * <pre>{@code
 * {
 *   "tenantId": "pharmacy-branch-001",
 *   "applicationName": "PHARMACY",
 *   "paymentData": {
 *     "invoiceNumber": "PH-INV-2026-001",
 *     "invoiceDate": "2026-03-16",
 *     "payerName": "ABC Hospital Pharmacy",
 *     "payeeType": "VENDOR",
 *     "payeeName": "XYZ Medical Supplies",
 *     "bankDetails": { "accountNumber": "...", "ifscCode": "..." },
 *     "amounts": { "grossAmount": 15000.00, "payableAmount": 14250.00, ... },
 *     "paymentMode": "NEFT",
 *     "transactionType": "PURCHASE_PAYMENT",
 *     "dueDate": "2026-03-20"
 *   },
 *   "documentInfo": { ... },
 *   "metadata": { ... }
 * }
 * }</pre>
 */
@Component
public class PharmacyAdapter implements FinancialEventAdapter {

    static final String DEFAULT_DEBIT_ACCOUNT_CODE = "5000";
    static final String DEFAULT_CREDIT_ACCOUNT_CODE = "2000";

    private final ObjectMapper objectMapper;

    public PharmacyAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public AdapterType getAdapterType() {
        return AdapterType.PHARMACY;
    }

    @Override
    public FinancialEvent parse(String tenantId, String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            throw new IllegalArgumentException("Pharmacy payload must not be empty");
        }

        try {
            JsonNode root = objectMapper.readTree(rawPayload);
            JsonNode paymentData = root.path("paymentData");

            if (paymentData.isMissingNode() || paymentData.isNull()) {
                throw new IllegalArgumentException("Pharmacy payload missing required field: paymentData");
            }

            String transactionType = optionalText(paymentData, "transactionType", "PURCHASE_PAYMENT");
            String invoiceNumber = optionalText(paymentData, "invoiceNumber", "");
            String currency = "INR";

            BigDecimal payableAmount = extractPayableAmount(paymentData);
            LocalDate eventDate = extractEventDate(paymentData);

            FinancialEvent event = new FinancialEvent(tenantId, AdapterType.PHARMACY, transactionType);
            event.setAmount(payableAmount);
            event.setCurrency(currency);
            event.setEventDate(eventDate);
            event.setSourceReference(invoiceNumber);
            event.setDescription(buildDescription(paymentData));
            event.setDebitAccountCode(DEFAULT_DEBIT_ACCOUNT_CODE);
            event.setCreditAccountCode(DEFAULT_CREDIT_ACCOUNT_CODE);
            event.setRawPayload(rawPayload);
            event.setIndustryTags(buildIndustryTags(root, paymentData));

            return event;

        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON in pharmacy payload: " + e.getMessage());
        }
    }

    private BigDecimal extractPayableAmount(JsonNode paymentData) {
        JsonNode amounts = paymentData.path("amounts");
        if (!amounts.isMissingNode() && !amounts.isNull()) {
            JsonNode payable = amounts.get("payableAmount");
            if (payable != null && !payable.isNull()) {
                return payable.decimalValue();
            }
            JsonNode gross = amounts.get("grossAmount");
            if (gross != null && !gross.isNull()) {
                return gross.decimalValue();
            }
        }
        return BigDecimal.ZERO;
    }

    private LocalDate extractEventDate(JsonNode paymentData) {
        String dateStr = optionalText(paymentData, "invoiceDate", null);
        if (dateStr != null) {
            try {
                // Handle both ISO date-time and plain date formats
                String plainDate = dateStr.contains("T") ? dateStr.substring(0, 10) : dateStr;
                return LocalDate.parse(plainDate);
            } catch (DateTimeParseException ignored) {
                // Fall through to today
            }
        }
        return LocalDate.now();
    }

    private String buildDescription(JsonNode paymentData) {
        String payeeName = optionalText(paymentData, "payeeName", "");
        String invoiceNumber = optionalText(paymentData, "invoiceNumber", "");
        if (!payeeName.isBlank() && !invoiceNumber.isBlank()) {
            return "Payment to " + payeeName + " for invoice " + invoiceNumber;
        }
        if (!payeeName.isBlank()) {
            return "Payment to " + payeeName;
        }
        return "Pharmacy purchase payment";
    }

    private String buildIndustryTags(JsonNode root, JsonNode paymentData) throws JsonProcessingException {
        Map<String, Object> tags = new HashMap<>();

        // Payment information
        tags.put("applicationName", optionalText(root, "applicationName", "PHARMACY"));
        tags.put("payerName", optionalText(paymentData, "payerName", ""));
        tags.put("payeeName", optionalText(paymentData, "payeeName", ""));
        tags.put("payeeType", optionalText(paymentData, "payeeType", ""));
        tags.put("paymentMode", optionalText(paymentData, "paymentMode", ""));
        tags.put("dueDate", optionalText(paymentData, "dueDate", ""));

        // Bank details
        JsonNode bankDetails = paymentData.path("bankDetails");
        if (!bankDetails.isMissingNode() && !bankDetails.isNull()) {
            tags.put("bankName", optionalText(bankDetails, "bankName", ""));
            tags.put("branchName", optionalText(bankDetails, "branchName", ""));
            tags.put("ifscCode", optionalText(bankDetails, "ifscCode", ""));
            tags.put("beneficiaryAccount", optionalText(bankDetails, "accountNumber", ""));
            tags.put("beneficiaryName", optionalText(bankDetails, "accountName", ""));
        }

        // Amount breakdown
        JsonNode amounts = paymentData.path("amounts");
        if (!amounts.isMissingNode() && !amounts.isNull()) {
            tags.put("grossAmount", amounts.path("grossAmount").asText(""));
            tags.put("tdsAmount", amounts.path("tdsAmount").asText(""));
            tags.put("deductions", amounts.path("deductions").asText(""));
        }

        // Source metadata
        JsonNode metadata = root.path("metadata");
        if (!metadata.isMissingNode() && !metadata.isNull()) {
            tags.put("branchId", optionalText(metadata, "branchId", ""));
            tags.put("organizationId", optionalText(metadata, "organizationId", ""));
            tags.put("sourceSystem", optionalText(metadata, "sourceSystem", ""));
            tags.put("batchNumber", optionalText(metadata, "batchNumber", ""));
        }

        // Document info (MinIO path stored securely)
        JsonNode documentInfo = root.path("documentInfo");
        if (!documentInfo.isMissingNode() && !documentInfo.isNull()) {
            String invoiceFilePath = optionalText(documentInfo, "invoiceFilePath", "");
            if (!invoiceFilePath.isBlank()) {
                tags.put("invoiceFilePath", invoiceFilePath);
            }
        }

        return objectMapper.writeValueAsString(tags);
    }

    private String optionalText(JsonNode node, String field, String defaultValue) {
        JsonNode child = node.get(field);
        return (child != null && !child.isNull()) ? child.asText() : defaultValue;
    }
}
