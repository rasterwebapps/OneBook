package com.nexus.onebook.ledger.payment.service;

import com.nexus.onebook.ledger.payment.model.PaymentBatch;
import com.nexus.onebook.ledger.payment.model.PaymentBatchItem;
import com.nexus.onebook.ledger.payment.model.PaymentBatchStatus;
import com.nexus.onebook.ledger.payment.model.PaymentRegisterEntry;
import com.nexus.onebook.ledger.payment.model.PaymentRegisterStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaymentFileGeneratorServiceTest {

    private PaymentFileGeneratorService generatorService;
    private static final String TENANT = "tenant-1";

    @BeforeEach
    void setUp() {
        generatorService = new PaymentFileGeneratorService();
    }

    private PaymentRegisterEntry buildEntry(Long id, String vendorName, String bankAccount,
            String ifsc, String bankName, BigDecimal amount) {
        PaymentRegisterEntry entry = new PaymentRegisterEntry();
        entry.setId(id);
        entry.setTenantId(TENANT);
        entry.setVendorAccountId(10L);
        entry.setVendorName(vendorName);
        entry.setTransactionType("PURCHASE");
        entry.setAmount(amount);
        entry.setStatus(PaymentRegisterStatus.IN_BATCH);
        entry.setDueDate(LocalDate.now().plusDays(30));
        entry.setBankAccountNumber(bankAccount);
        entry.setBankIfscCode(ifsc);
        entry.setBankName(bankName);
        return entry;
    }

    private PaymentBatch buildBatch() {
        PaymentBatch batch = new PaymentBatch();
        batch.setId(1L);
        batch.setTenantId(TENANT);
        batch.setBatchNumber("PB-2026-04-001");
        batch.setVendorAccountId(10L);
        batch.setVendorName("Vendor A");
        batch.setTotalPurchases(new BigDecimal("2500.00"));
        batch.setTotalReturns(BigDecimal.ZERO);
        batch.setTotalCreditNotes(BigDecimal.ZERO);
        batch.setNetPayable(new BigDecimal("2500.00"));
        batch.setBankAccountId(20L);
        batch.setPaymentMode("NEFT");
        batch.setStatus(PaymentBatchStatus.APPROVED);
        batch.setCreatedBy("user1");
        return batch;
    }

    private PaymentBatchItem buildItem(PaymentBatch batch, PaymentRegisterEntry entry) {
        PaymentBatchItem item = new PaymentBatchItem();
        item.setId(entry.getId());
        item.setTenantId(TENANT);
        item.setBatch(batch);
        item.setRegisterEntry(entry);
        item.setTransactionType(entry.getTransactionType());
        item.setAmount(entry.getAmount());
        return item;
    }

    @Test
    void generateCsv_singleItem_returnsValidCsv() {
        PaymentBatch batch = buildBatch();
        PaymentRegisterEntry entry = buildEntry(1L, "Vendor A", "1234567890", "HDFC0001234", "HDFC Bank", new BigDecimal("1000.00"));
        PaymentBatchItem item = buildItem(batch, entry);

        byte[] csvBytes = generatorService.generateCsv(batch, List.of(item));
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csv.contains("Sr No,Vendor Name,Bank Account,IFSC Code,Bank Name,Payment Amount,Payment Reference,Payment Mode"));
        assertTrue(csv.contains("1,Vendor A,1234567890,HDFC0001234,HDFC Bank,1000.00,PB-2026-04-001,NEFT"));
    }

    @Test
    void generateCsv_multipleItems_returnsCorrectSequence() {
        PaymentBatch batch = buildBatch();
        PaymentRegisterEntry entry1 = buildEntry(1L, "Vendor A", "1111111111", "ICIC0001111", "ICICI Bank", new BigDecimal("1000.00"));
        PaymentRegisterEntry entry2 = buildEntry(2L, "Vendor A", "2222222222", "SBIN0002222", "SBI", new BigDecimal("1500.00"));

        PaymentBatchItem item1 = buildItem(batch, entry1);
        PaymentBatchItem item2 = buildItem(batch, entry2);

        byte[] csvBytes = generatorService.generateCsv(batch, List.of(item1, item2));
        String csv = new String(csvBytes, StandardCharsets.UTF_8);
        String[] lines = csv.split("\n");

        assertEquals(3, lines.length); // header + 2 data lines
        assertTrue(lines[1].startsWith("1,"));
        assertTrue(lines[2].startsWith("2,"));
    }

    @Test
    void generateCsv_emptyItems_returnsHeaderOnly() {
        PaymentBatch batch = buildBatch();

        byte[] csvBytes = generatorService.generateCsv(batch, List.of());
        String csv = new String(csvBytes, StandardCharsets.UTF_8);
        String[] lines = csv.trim().split("\n");

        assertEquals(1, lines.length);
        assertTrue(lines[0].contains("Sr No"));
    }

    @Test
    void generateCsv_specialCharacters_escapesCorrectly() {
        PaymentBatch batch = buildBatch();
        PaymentRegisterEntry entry = buildEntry(1L, "Vendor, \"Special\" & Co.", "9999999999", "UTIB0009999", "Axis Bank", new BigDecimal("500.00"));
        PaymentBatchItem item = buildItem(batch, entry);

        byte[] csvBytes = generatorService.generateCsv(batch, List.of(item));
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        // CSV should escape the vendor name with quotes and double the internal quotes
        assertTrue(csv.contains("\"Vendor, \"\"Special\"\" & Co.\""));
    }

    @Test
    void generateCsv_nullBankDetails_handlesGracefully() {
        PaymentBatch batch = buildBatch();
        PaymentRegisterEntry entry = buildEntry(1L, "Vendor A", null, null, null, new BigDecimal("750.00"));
        PaymentBatchItem item = buildItem(batch, entry);

        byte[] csvBytes = generatorService.generateCsv(batch, List.of(item));
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        // Should not throw, and should have empty values for null fields
        assertTrue(csv.contains("1,Vendor A,,,"));
    }

    @Test
    void generateCsv_paymentMode_includesCorrectMode() {
        PaymentBatch batch = buildBatch();
        batch.setPaymentMode("RTGS");

        PaymentRegisterEntry entry = buildEntry(1L, "Vendor A", "1234567890", "HDFC0001234", "HDFC Bank", new BigDecimal("1000000.00"));
        PaymentBatchItem item = buildItem(batch, entry);

        byte[] csvBytes = generatorService.generateCsv(batch, List.of(item));
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csv.contains("RTGS"));
    }

    @Test
    void generateCsv_amountPrecision_preservesBigDecimal() {
        PaymentBatch batch = buildBatch();
        PaymentRegisterEntry entry = buildEntry(1L, "Vendor A", "1234567890", "HDFC0001234", "HDFC Bank", new BigDecimal("12345.67"));
        PaymentBatchItem item = buildItem(batch, entry);

        byte[] csvBytes = generatorService.generateCsv(batch, List.of(item));
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csv.contains("12345.67"));
    }
}

