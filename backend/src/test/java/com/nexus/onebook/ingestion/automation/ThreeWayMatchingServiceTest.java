package com.nexus.onebook.ingestion.automation;

import com.nexus.onebook.ingestion.dto.ThreeWayMatchResult;
import com.nexus.onebook.ingestion.model.GoodsReceipt;
import com.nexus.onebook.ingestion.model.MatchStatus;
import com.nexus.onebook.ingestion.model.PurchaseOrder;
import com.nexus.onebook.ingestion.model.VendorInvoice;
import com.nexus.onebook.ingestion.repository.GoodsReceiptRepository;
import com.nexus.onebook.ingestion.repository.PurchaseOrderRepository;
import com.nexus.onebook.ingestion.repository.VendorInvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThreeWayMatchingServiceTest {

    @Mock
    private PurchaseOrderRepository poRepository;

    @Mock
    private GoodsReceiptRepository grRepository;

    @Mock
    private VendorInvoiceRepository invoiceRepository;

    @InjectMocks
    private ThreeWayMatchingService matchingService;

    private PurchaseOrder po;
    private GoodsReceipt gr;
    private VendorInvoice invoice;

    @BeforeEach
    void setUp() {
        po = new PurchaseOrder("tenant-1", "PO-100", "Supplier Inc",
                new BigDecimal("5000.0000"), LocalDate.of(2026, 3, 1));

        gr = new GoodsReceipt("tenant-1", "GR-100", "PO-100",
                new BigDecimal("100"), new BigDecimal("5000.0000"),
                LocalDate.of(2026, 3, 5));

        invoice = new VendorInvoice("tenant-1", "INV-100", "PO-100",
                "Supplier Inc", new BigDecimal("5000.0000"),
                LocalDate.of(2026, 3, 8));
    }

    @Test
    void match_allDocumentsMatch_returnsMatched() {
        when(poRepository.findByTenantIdAndPoNumber("tenant-1", "PO-100"))
                .thenReturn(Optional.of(po));
        when(grRepository.findByTenantIdAndPoNumber("tenant-1", "PO-100"))
                .thenReturn(Optional.of(gr));
        when(invoiceRepository.findByTenantIdAndPoNumber("tenant-1", "PO-100"))
                .thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(VendorInvoice.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ThreeWayMatchResult result = matchingService.match("tenant-1", "PO-100");

        assertEquals(MatchStatus.MATCHED, result.status());
        assertTrue(result.amountsMatch());
        assertTrue(result.discrepancies().isEmpty());
        assertTrue(result.message().contains("payment authorised"));
    }

    @Test
    void match_amountMismatch_returnsMismatched() {
        invoice.setTotalAmount(new BigDecimal("4500.0000")); // Different from PO

        when(poRepository.findByTenantIdAndPoNumber("tenant-1", "PO-100"))
                .thenReturn(Optional.of(po));
        when(grRepository.findByTenantIdAndPoNumber("tenant-1", "PO-100"))
                .thenReturn(Optional.of(gr));
        when(invoiceRepository.findByTenantIdAndPoNumber("tenant-1", "PO-100"))
                .thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(VendorInvoice.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ThreeWayMatchResult result = matchingService.match("tenant-1", "PO-100");

        assertEquals(MatchStatus.MISMATCHED, result.status());
        assertFalse(result.amountsMatch());
        assertFalse(result.discrepancies().isEmpty());
    }

    @Test
    void match_missingPO_returnsPartial() {
        when(poRepository.findByTenantIdAndPoNumber("tenant-1", "PO-100"))
                .thenReturn(Optional.empty());
        when(grRepository.findByTenantIdAndPoNumber("tenant-1", "PO-100"))
                .thenReturn(Optional.of(gr));
        when(invoiceRepository.findByTenantIdAndPoNumber("tenant-1", "PO-100"))
                .thenReturn(Optional.of(invoice));

        ThreeWayMatchResult result = matchingService.match("tenant-1", "PO-100");

        assertEquals(MatchStatus.PARTIAL, result.status());
        assertFalse(result.amountsMatch());
        assertTrue(result.discrepancies().stream()
                .anyMatch(d -> d.contains("Purchase Order not found")));
    }

    @Test
    void match_missingGR_returnsPartial() {
        when(poRepository.findByTenantIdAndPoNumber("tenant-1", "PO-100"))
                .thenReturn(Optional.of(po));
        when(grRepository.findByTenantIdAndPoNumber("tenant-1", "PO-100"))
                .thenReturn(Optional.empty());
        when(invoiceRepository.findByTenantIdAndPoNumber("tenant-1", "PO-100"))
                .thenReturn(Optional.of(invoice));

        ThreeWayMatchResult result = matchingService.match("tenant-1", "PO-100");

        assertEquals(MatchStatus.PARTIAL, result.status());
    }

    @Test
    void match_missingInvoice_returnsPartial() {
        when(poRepository.findByTenantIdAndPoNumber("tenant-1", "PO-100"))
                .thenReturn(Optional.of(po));
        when(grRepository.findByTenantIdAndPoNumber("tenant-1", "PO-100"))
                .thenReturn(Optional.of(gr));
        when(invoiceRepository.findByTenantIdAndPoNumber("tenant-1", "PO-100"))
                .thenReturn(Optional.empty());

        ThreeWayMatchResult result = matchingService.match("tenant-1", "PO-100");

        assertEquals(MatchStatus.PARTIAL, result.status());
    }

    @Test
    void match_withinTolerance_returnsMatched() {
        // PO = 5000.0000, GR = 5000.0050 (within 0.01 tolerance), Invoice = 5000.0000
        gr.setTotalAmount(new BigDecimal("5000.0050"));

        when(poRepository.findByTenantIdAndPoNumber("tenant-1", "PO-100"))
                .thenReturn(Optional.of(po));
        when(grRepository.findByTenantIdAndPoNumber("tenant-1", "PO-100"))
                .thenReturn(Optional.of(gr));
        when(invoiceRepository.findByTenantIdAndPoNumber("tenant-1", "PO-100"))
                .thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(VendorInvoice.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ThreeWayMatchResult result = matchingService.match("tenant-1", "PO-100");

        assertEquals(MatchStatus.MATCHED, result.status());
        assertTrue(result.amountsMatch());
    }
}
