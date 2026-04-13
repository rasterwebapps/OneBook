package com.nexus.onebook.payment.service;

import com.nexus.onebook.payment.model.PaymentBatch;
import com.nexus.onebook.payment.model.PaymentBatchItem;
import com.nexus.onebook.payment.model.PaymentBatchStatus;
import com.nexus.onebook.payment.model.PaymentRegisterEntry;
import com.nexus.onebook.payment.model.PaymentRegisterStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaymentFileGeneratorServiceTest {

    private PaymentFileGeneratorService fileGeneratorService;

    private static final String TENANT = "tenant-1";

    @BeforeEach
    void setUp() {
        fileGeneratorService = new PaymentFileGeneratorService();
    }

    private PaymentRegisterEntry buildEntry(Long id, String vendorName, BigDecimal amount) {
        PaymentRegisterEntry entry = new PaymentRegisterEntry();
        entry.setId(id);
        entry.setTenantId(TENANT);
        entry.setVendorAccountId(10L);
        entry.setVendorName(vendorName);
        entry.setTransactionType("PURCHASE");
        entry.setAmount(amount);
        entry.setStatus(PaymentRegisterStatus.APPROVED);
        entry.setDueDate(LocalDate.now().plusDays(30));
        entry.setBankAccountNumber("1234567890");
        entry.setBankIfscCode("HDFC0001234");
        entry.setBankName("HDFC Bank");
        return entry;
    }

    private PaymentBatch buildBatch(Long id, String batchNumber, String paymentMode) {
        PaymentBatch batch = new PaymentBatch();
        batch.setId(id);
        batch.setTenantId(TENANT);
        batch.setBatchNumber(batchNumber);
        batch.setVendorAccountId(10L);
        batch.setVendorName("Vendor A");
        batch.setTotalPurchases(new BigDecimal("1000.00"));
        batch.setTotalReturns(BigDecimal.ZERO);
        batch.setTotalCreditNotes(BigDecimal.ZERO);
        batch.setNetPayable(new BigDecimal("1000.00"));
        batch.setBankAccountId(20L);
        batch.setPaymentMode(paymentMode);
        batch.setStatus(PaymentBatchStatus.APPROVED);
        batch.setCreatedBy("user1");
        return batch;
    }

    private PaymentBatchItem buildBatchItem(Long id, PaymentBatch batch, PaymentRegisterEntry entry) {
        PaymentBatchItem item = new PaymentBatchItem();
        item.setId(id);
        item.setTenantId(TENANT);
        item.setBatch(batch);
        item.setRegisterEntry(entry);
        item.setTransactionType(entry.getTransactionType());
        item.setAmount(entry.getAmount());
        return item;
    }

    @Test
    void generateCsv_singleItem_returnsValidCsv() {
        PaymentBatch batch = buildBatch(1L, "PB-2026-01-001", "NEFT");
        PaymentRegisterEntry entry = buildEntry(1L, "Vendor A", new BigDecimal("1000.00"));
        PaymentBatchItem item = buildBatchItem(1L, batch, entry);

        byte[] csvBytes = fileGeneratorService.generateCsv(batch, List.of(item));
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertNotNull(csvBytes);
        assertTrue(csv.contains("Sr No,Vendor Name,Bank Account,IFSC Code,Bank Name,Payment Amount,Payment Reference,Payment Mode"));
        assertTrue(csv.contains("1,Vendor A,1234567890,HDFC0001234,HDFC Bank,1000.00,PB-2026-01-001,NEFT"));
    }

    @Test
    void generateCsv_multipleItems_returnsAllRows() {
        PaymentBatch batch = buildBatch(1L, "PB-2026-01-002", "RTGS");
        PaymentRegisterEntry entry1 = buildEntry(1L, "Vendor A", new BigDecimal("500.00"));
        PaymentRegisterEntry entry2 = buildEntry(2L, "Vendor A", new BigDecimal("300.00"));
        PaymentRegisterEntry entry3 = buildEntry(3L, "Vendor A", new BigDecimal("200.00"));

        PaymentBatchItem item1 = buildBatchItem(1L, batch, entry1);
        PaymentBatchItem item2 = buildBatchItem(2L, batch, entry2);
        PaymentBatchItem item3 = buildBatchItem(3L, batch, entry3);

        byte[] csvBytes = fileGeneratorService.generateCsv(batch, List.of(item1, item2, item3));
        String csv = new String(csvBytes, StandardCharsets.UTF_8);
        String[] lines = csv.split("\n");

        assertEquals(4, lines.length); // 1 header + 3 data rows
        assertTrue(lines[1].startsWith("1,"));
        assertTrue(lines[2].startsWith("2,"));
        assertTrue(lines[3].startsWith("3,"));
    }

    @Test
    void generateCsv_vendorNameWithComma_escapesCorrectly() {
        PaymentBatch batch = buildBatch(1L, "PB-2026-01-003", "IMPS");
        PaymentRegisterEntry entry = buildEntry(1L, "Vendor, Inc.", new BigDecimal("750.00"));
        PaymentBatchItem item = buildBatchItem(1L, batch, entry);

        byte[] csvBytes = fileGeneratorService.generateCsv(batch, List.of(item));
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csv.contains("\"Vendor, Inc.\""));
    }

    @Test
    void generateCsv_vendorNameWithQuotes_escapesCorrectly() {
        PaymentBatch batch = buildBatch(1L, "PB-2026-01-004", "NEFT");
        PaymentRegisterEntry entry = buildEntry(1L, "Vendor \"Best\" Ltd", new BigDecimal("500.00"));
        PaymentBatchItem item = buildBatchItem(1L, batch, entry);

        byte[] csvBytes = fileGeneratorService.generateCsv(batch, List.of(item));
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csv.contains("\"Vendor \"\"Best\"\" Ltd\""));
    }

    @Test
    void generateCsv_nullBankDetails_handlesGracefully() {
        PaymentBatch batch = buildBatch(1L, "PB-2026-01-005", "NEFT");
        PaymentRegisterEntry entry = buildEntry(1L, "Vendor B", new BigDecimal("250.00"));
        entry.setBankAccountNumber(null);
        entry.setBankIfscCode(null);
        entry.setBankName(null);
        PaymentBatchItem item = buildBatchItem(1L, batch, entry);

        byte[] csvBytes = fileGeneratorService.generateCsv(batch, List.of(item));
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertNotNull(csvBytes);
        assertTrue(csv.contains("Vendor B"));
        // Should have empty values for bank details
        assertTrue(csv.contains(",,,"));
    }

    @Test
    void generateCsv_emptyItemList_returnsHeaderOnly() {
        PaymentBatch batch = buildBatch(1L, "PB-2026-01-006", "NEFT");

        byte[] csvBytes = fileGeneratorService.generateCsv(batch, List.of());
        String csv = new String(csvBytes, StandardCharsets.UTF_8);
        String[] lines = csv.split("\n");

        assertEquals(1, lines.length); // Header only
        assertTrue(csv.contains("Sr No,Vendor Name,Bank Account"));
    }

    @Test
    void generateCsv_differentPaymentModes_includesCorrectMode() {
        PaymentBatch neftBatch = buildBatch(1L, "PB-NEFT", "NEFT");
        PaymentBatch rtgsBatch = buildBatch(2L, "PB-RTGS", "RTGS");
        PaymentBatch impsBatch = buildBatch(3L, "PB-IMPS", "IMPS");

        PaymentRegisterEntry entry = buildEntry(1L, "Vendor C", new BigDecimal("100.00"));
        PaymentBatchItem item = buildBatchItem(1L, neftBatch, entry);

        String neftCsv = new String(fileGeneratorService.generateCsv(neftBatch, List.of(item)), StandardCharsets.UTF_8);
        item.setBatch(rtgsBatch);
        String rtgsCsv = new String(fileGeneratorService.generateCsv(rtgsBatch, List.of(item)), StandardCharsets.UTF_8);
        item.setBatch(impsBatch);
        String impsCsv = new String(fileGeneratorService.generateCsv(impsBatch, List.of(item)), StandardCharsets.UTF_8);

        assertTrue(neftCsv.contains(",NEFT"));
        assertTrue(rtgsCsv.contains(",RTGS"));
        assertTrue(impsCsv.contains(",IMPS"));
    }

    @Test
    void generateCsv_largeAmount_handlesDecimalPrecision() {
        PaymentBatch batch = buildBatch(1L, "PB-2026-01-007", "RTGS");
        PaymentRegisterEntry entry = buildEntry(1L, "Vendor D", new BigDecimal("9999999.9999"));
        PaymentBatchItem item = buildBatchItem(1L, batch, entry);

        byte[] csvBytes = fileGeneratorService.generateCsv(batch, List.of(item));
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csv.contains("9999999.9999"));
    }

    @Test
    void generateCsv_vendorNameWithNewline_escapesCorrectly() {
        PaymentBatch batch = buildBatch(1L, "PB-2026-01-008", "NEFT");
        PaymentRegisterEntry entry = buildEntry(1L, "Vendor\nMultiline", new BigDecimal("100.00"));
        PaymentBatchItem item = buildBatchItem(1L, batch, entry);

        byte[] csvBytes = fileGeneratorService.generateCsv(batch, List.of(item));
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csv.contains("\"Vendor\nMultiline\""));
    }
}
