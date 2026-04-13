package com.nexus.onebook.ingestion.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.onebook.ingestion.model.AdapterType;
import com.nexus.onebook.payment.model.PaymentRegisterEntry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.nexus.onebook.accounts.model.Branch;
import com.nexus.onebook.foundation.model.Payee;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ExternalAppAdapter} — the common adapter for Pharmacy, Lab, Stores, HIS, etc.
 * Verifies that any external application using the ExternalAppPaymentRequest JSON format
 * is correctly parsed into a normalised PaymentRegisterEntry regardless of applicationName.
 */
class ExternalAppAdapterTest {

    private final ExternalAppAdapter adapter = new ExternalAppAdapter(new ObjectMapper());

    private static final String PHARMACY_PAYLOAD = """
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

    private static final String LAB_PAYLOAD = """
            {
              "tenantId": "lab-branch-001",
              "applicationName": "LAB",
              "paymentData": {
                "invoiceNumber": "LAB-INV-2026-001",
                "invoiceDate": "2026-03-16",
                "payeeName": "Lab Reagents Pvt Ltd",
                "amounts": {
                  "grossAmount": 8500.00,
                  "payableAmount": 8500.00
                },
                "paymentMode": "RTGS",
                "transactionType": "PURCHASE_PAYMENT"
              }
            }
            """;

    private static final String STORE_PAYLOAD = """
            {
              "tenantId": "store-branch-001",
              "applicationName": "STORE",
              "paymentData": {
                "invoiceNumber": "ST-INV-2026-001",
                "invoiceDate": "2026-03-16",
                "payeeName": "General Stores Supplier",
                "amounts": { "payableAmount": 3200.00 },
                "paymentMode": "NEFT",
                "transactionType": "PURCHASE_PAYMENT"
              }
            }
            """;

    private static final String PAYLOAD_WITH_DATETIME = """
            {
              "tenantId": "t1",
              "applicationName": "HIS",
              "paymentData": {
                "invoiceDate": "2026-03-16T00:00:00Z",
                "amounts": { "payableAmount": 5000.00 }
              }
            }
            """;

    @Test
    void getAdapterType_returnsExternalApp() {
        assertEquals(AdapterType.EXTERNAL_APP, adapter.getAdapterType());
    }

    @Test
    void parse_pharmacyPayload_returnsNormalisedEvent() {
        PaymentRegisterEntry event = adapter.parse("pharmacy-branch-001", PHARMACY_PAYLOAD);

        assertEquals("pharmacy-branch-001", event.getTenantId());
        assertEquals(AdapterType.EXTERNAL_APP, event.getAdapterType());
        assertEquals("PURCHASE_PAYMENT", event.getEventType());
        assertEquals(0, new BigDecimal("14250.00").compareTo(event.getAmount()));
        assertEquals("INR", event.getCurrency());
        assertEquals(LocalDate.of(2026, 3, 16), event.getEventDate());
        assertEquals("PH-INV-2026-001", event.getSourceReference());
        assertEquals(ExternalAppAdapter.DEFAULT_DEBIT_ACCOUNT_CODE, event.getDebitAccountCode());
        assertEquals(ExternalAppAdapter.DEFAULT_CREDIT_ACCOUNT_CODE, event.getCreditAccountCode());
    }

    @Test
    void parse_pharmacyPayload_industryTagsContainApplicationName() {
        PaymentRegisterEntry event = adapter.parse("pharmacy-branch-001", PHARMACY_PAYLOAD);

        String tags = event.getIndustryTags();
        assertNotNull(tags);
        assertTrue(tags.contains("PHARMACY"), "applicationName must be preserved in industry tags");
        assertTrue(tags.contains("SBIN0001234"), "IFSC code should be in industry tags");
        assertTrue(tags.contains("NEFT"), "Payment mode should be in industry tags");
        assertTrue(tags.contains("XYZ Medical Supplies"), "Payee name should be in industry tags");
        assertTrue(tags.contains("PHARMACY_ERP"), "Source system should be in industry tags");
    }

    @Test
    void parse_labPayload_applicationNameIsLab() {
        PaymentRegisterEntry event = adapter.parse("lab-branch-001", LAB_PAYLOAD);

        assertEquals("PURCHASE_PAYMENT", event.getEventType());
        assertEquals(0, new BigDecimal("8500.00").compareTo(event.getAmount()));
        assertEquals("LAB-INV-2026-001", event.getSourceReference());
        assertTrue(event.getIndustryTags().contains("LAB"),
                "applicationName LAB must be preserved in industry tags");
    }

    @Test
    void parse_storePayload_applicationNameIsStore() {
        PaymentRegisterEntry event = adapter.parse("store-branch-001", STORE_PAYLOAD);

        assertEquals(0, new BigDecimal("3200.00").compareTo(event.getAmount()));
        assertTrue(event.getIndustryTags().contains("STORE"),
                "applicationName STORE must be preserved in industry tags");
    }

    @Test
    void parse_invoiceDateAsDateTime_parsesDatePart() {
        PaymentRegisterEntry event = adapter.parse("t1", PAYLOAD_WITH_DATETIME);

        assertEquals(LocalDate.of(2026, 3, 16), event.getEventDate());
        assertTrue(event.getIndustryTags().contains("HIS"),
                "applicationName HIS must be preserved in industry tags");
    }

    @Test
    void parse_missingAmounts_returnsZeroAmount() {
        String payload = """
                {
                  "tenantId": "t1",
                  "applicationName": "PHARMACY",
                  "paymentData": {
                    "invoiceNumber": "INV-001",
                    "transactionType": "PURCHASE_PAYMENT"
                  }
                }
                """;

        PaymentRegisterEntry event = adapter.parse("t1", payload);
        assertEquals(0, BigDecimal.ZERO.compareTo(event.getAmount()));
    }

    @Test
    void parse_missingTransactionType_defaultsToPurchasePayment() {
        String payload = """
                {
                  "tenantId": "t1",
                  "applicationName": "LAB",
                  "paymentData": {
                    "amounts": { "payableAmount": 1000.00 }
                  }
                }
                """;

        PaymentRegisterEntry event = adapter.parse("t1", payload);
        assertEquals("PURCHASE_PAYMENT", event.getEventType());
    }

    @Test
    void parse_rawPayloadIsPreserved() {
        PaymentRegisterEntry event = adapter.parse("pharmacy-branch-001", PHARMACY_PAYLOAD);

        assertNotNull(event.getRawPayload());
        assertTrue(event.getRawPayload().contains("PH-INV-2026-001"));
    }

    @Test
    void parse_grossAmountUsedWhenPayableAmountMissing() {
        String payload = """
                {
                  "tenantId": "t1",
                  "applicationName": "STORE",
                  "paymentData": {
                    "amounts": { "grossAmount": 9999.00 }
                  }
                }
                """;

        PaymentRegisterEntry event = adapter.parse("t1", payload);
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
                { "tenantId": "t1", "applicationName": "HIS" }
                """;

        assertThrows(IllegalArgumentException.class,
                () -> adapter.parse("t1", payload));
    }

    @Test
    void parse_invoiceFilePathStoredInIndustryTags() {
        String payload = """
                {
                  "tenantId": "t1",
                  "applicationName": "PHARMACY",
                  "paymentData": { "amounts": { "payableAmount": 100.00 } },
                  "documentInfo": {
                    "invoiceFilePath": "/minio-bucket/pharmacy/invoices/2026/inv.pdf",
                    "fileName": "inv.pdf"
                  }
                }
                """;

        PaymentRegisterEntry event = adapter.parse("t1", payload);
        assertTrue(event.getIndustryTags().contains("/minio-bucket/pharmacy/invoices/2026/inv.pdf"));
    }

    @Test
    void parse_descriptionIncludesApplicationNameWhenPayeeNameMissing() {
        String payload = """
                {
                  "tenantId": "t1",
                  "applicationName": "LAB",
                  "paymentData": {
                    "amounts": { "payableAmount": 500.00 }
                  }
                }
                """;

        PaymentRegisterEntry event = adapter.parse("t1", payload);
        assertTrue(event.getDescription().contains("LAB"),
                "Description should reference the application name when payee name is absent");
    }
}
