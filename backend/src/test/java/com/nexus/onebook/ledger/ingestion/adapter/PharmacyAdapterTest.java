package com.nexus.onebook.ledger.ingestion.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.onebook.ledger.ingestion.model.AdapterType;
import com.nexus.onebook.ledger.ingestion.model.FinancialEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PharmacyAdapterTest {

    private final PharmacyAdapter adapter = new PharmacyAdapter(new ObjectMapper());

    private static final String VALID_PAYLOAD = """
            {
              "tenantId": "pharmacy-branch-001",
              "applicationName": "PHARMACY",
              "paymentData": {
                "invoiceNumber": "PH-INV-2026-001",
                "invoiceDate": "2026-03-16",
                "payerName": "ABC Hospital Pharmacy",
                "payeeType": "VENDOR",
                "payeeName": "XYZ Medical Supplies",
                "bankDetails": {
                  "accountNumber": "1234567890",
                  "accountName": "XYZ Medical Supplies",
                  "bankName": "State Bank of India",
                  "branchName": "Medical District Branch",
                  "ifscCode": "SBIN0001234"
                },
                "amounts": {
                  "grossAmount": 15000.00,
                  "netBillAmount": 14250.00,
                  "tdsAmount": 750.00,
                  "deductions": 0.00,
                  "payableAmount": 14250.00
                },
                "paymentMode": "NEFT",
                "transactionType": "PURCHASE_PAYMENT",
                "dueDate": "2026-03-20"
              },
              "metadata": {
                "branchId": "PH-BR-001",
                "organizationId": "ABC-HOSPITAL",
                "createdBy": "pharmacy-system",
                "sourceSystem": "PHARMACY_ERP",
                "batchNumber": "BATCH-2026-03-001"
              }
            }
            """;

    private static final String PAYLOAD_WITH_DATETIME = """
            {
              "tenantId": "t1",
              "applicationName": "PHARMACY",
              "paymentData": {
                "invoiceDate": "2026-03-16T00:00:00Z",
                "amounts": { "payableAmount": 5000.00 }
              }
            }
            """;

    private static final String PAYLOAD_MISSING_AMOUNTS = """
            {
              "tenantId": "t1",
              "applicationName": "PHARMACY",
              "paymentData": {
                "invoiceNumber": "INV-001",
                "transactionType": "PURCHASE_PAYMENT"
              }
            }
            """;

    @Test
    void getAdapterType_returnsPharmacy() {
        assertEquals(AdapterType.PHARMACY, adapter.getAdapterType());
    }

    @Test
    void parse_validPayload_returnsNormalisedEvent() {
        FinancialEvent event = adapter.parse("pharmacy-branch-001", VALID_PAYLOAD);

        assertEquals("pharmacy-branch-001", event.getTenantId());
        assertEquals(AdapterType.PHARMACY, event.getAdapterType());
        assertEquals("PURCHASE_PAYMENT", event.getEventType());
        assertEquals(0, new BigDecimal("14250.00").compareTo(event.getAmount()));
        assertEquals("INR", event.getCurrency());
        assertEquals(LocalDate.of(2026, 3, 16), event.getEventDate());
        assertEquals("PH-INV-2026-001", event.getSourceReference());
        assertEquals(PharmacyAdapter.DEFAULT_DEBIT_ACCOUNT_CODE, event.getDebitAccountCode());
        assertEquals(PharmacyAdapter.DEFAULT_CREDIT_ACCOUNT_CODE, event.getCreditAccountCode());
    }

    @Test
    void parse_validPayload_industryTagsContainBankDetails() {
        FinancialEvent event = adapter.parse("pharmacy-branch-001", VALID_PAYLOAD);

        String tags = event.getIndustryTags();
        assertNotNull(tags);
        assertTrue(tags.contains("SBIN0001234"), "IFSC code should be in industry tags");
        assertTrue(tags.contains("NEFT"), "Payment mode should be in industry tags");
        assertTrue(tags.contains("XYZ Medical Supplies"), "Payee name should be in industry tags");
        assertTrue(tags.contains("PHARMACY_ERP"), "Source system should be in industry tags");
    }

    @Test
    void parse_invoiceDateAsDateTime_parsesDatePart() {
        FinancialEvent event = adapter.parse("t1", PAYLOAD_WITH_DATETIME);

        assertEquals(LocalDate.of(2026, 3, 16), event.getEventDate());
    }

    @Test
    void parse_missingAmounts_returnsZeroAmount() {
        FinancialEvent event = adapter.parse("t1", PAYLOAD_MISSING_AMOUNTS);

        assertEquals(0, BigDecimal.ZERO.compareTo(event.getAmount()));
        assertEquals("INV-001", event.getSourceReference());
        assertEquals("PURCHASE_PAYMENT", event.getEventType());
    }

    @Test
    void parse_missingTransactionType_defaultsToPurchasePayment() {
        String payload = """
                {
                  "tenantId": "t1",
                  "paymentData": {
                    "amounts": { "payableAmount": 1000.00 }
                  }
                }
                """;

        FinancialEvent event = adapter.parse("t1", payload);
        assertEquals("PURCHASE_PAYMENT", event.getEventType());
    }

    @Test
    void parse_rawPayloadIsPreserved() {
        FinancialEvent event = adapter.parse("pharmacy-branch-001", VALID_PAYLOAD);

        assertNotNull(event.getRawPayload());
        assertTrue(event.getRawPayload().contains("PH-INV-2026-001"));
    }

    @Test
    void parse_grossAmountUsedWhenPayableAmountMissing() {
        String payload = """
                {
                  "tenantId": "t1",
                  "paymentData": {
                    "amounts": { "grossAmount": 9999.00 }
                  }
                }
                """;

        FinancialEvent event = adapter.parse("t1", payload);
        assertEquals(0, new BigDecimal("9999.00").compareTo(event.getAmount()));
    }

    @Test
    void parse_nullPayload_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> adapter.parse("t1", null));
    }

    @Test
    void parse_emptyPayload_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> adapter.parse("t1", "   "));
    }

    @Test
    void parse_invalidJson_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> adapter.parse("t1", "not-json"));
    }

    @Test
    void parse_missingPaymentData_throws() {
        String payload = """
                { "tenantId": "t1", "applicationName": "PHARMACY" }
                """;

        assertThrows(IllegalArgumentException.class,
                () -> adapter.parse("t1", payload));
    }

    @Test
    void parse_invoiceFilePathStoredInIndustryTags() {
        String payload = """
                {
                  "tenantId": "t1",
                  "paymentData": { "amounts": { "payableAmount": 100.00 } },
                  "documentInfo": {
                    "invoiceFilePath": "/minio-bucket/pharmacy/invoices/2026/inv.pdf",
                    "fileName": "inv.pdf"
                  }
                }
                """;

        FinancialEvent event = adapter.parse("t1", payload);
        assertTrue(event.getIndustryTags().contains("/minio-bucket/pharmacy/invoices/2026/inv.pdf"));
    }
}
