package com.nexus.onebook.ingestion.automation;

import com.nexus.onebook.ingestion.dto.OcrInvoiceRequest;
import com.nexus.onebook.ingestion.model.MatchStatus;
import com.nexus.onebook.ingestion.model.VendorInvoice;
import com.nexus.onebook.ingestion.repository.VendorInvoiceRepository;
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
class OcrInvoiceServiceTest {

    @Mock
    private VendorInvoiceRepository invoiceRepository;

    @InjectMocks
    private OcrInvoiceService ocrInvoiceService;

    @Test
    void processOcrInvoice_validRequest_createsInvoice() {
        when(invoiceRepository.findByTenantIdAndInvoiceNumber("tenant-1", "INV-001"))
                .thenReturn(Optional.empty());
        when(invoiceRepository.save(any(VendorInvoice.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        OcrInvoiceRequest request = new OcrInvoiceRequest(
                "tenant-1", "INV-001", "Supplier Inc", "PO-100",
                new BigDecimal("5000.00"), "USD",
                LocalDate.of(2026, 3, 10), "[{\"item\":\"Widget\",\"qty\":100}]",
                "OCR raw text"
        );

        VendorInvoice result = ocrInvoiceService.processOcrInvoice(request);

        assertNotNull(result);
        assertEquals("INV-001", result.getInvoiceNumber());
        assertEquals("Supplier Inc", result.getVendorName());
        assertTrue(result.isOcrExtracted());
        assertEquals(MatchStatus.PENDING, result.getMatchStatus());
    }

    @Test
    void processOcrInvoice_duplicateInvoice_throws() {
        when(invoiceRepository.findByTenantIdAndInvoiceNumber("tenant-1", "INV-001"))
                .thenReturn(Optional.of(new VendorInvoice()));

        OcrInvoiceRequest request = new OcrInvoiceRequest(
                "tenant-1", "INV-001", "Supplier Inc", "PO-100",
                new BigDecimal("5000.00"), "USD",
                LocalDate.of(2026, 3, 10), null, null
        );

        assertThrows(IllegalArgumentException.class,
                () -> ocrInvoiceService.processOcrInvoice(request));
    }

    @Test
    void processOcrInvoice_nullAmount_throws() {
        OcrInvoiceRequest request = new OcrInvoiceRequest(
                "tenant-1", "INV-002", "Supplier Inc", "PO-100",
                null, "USD", LocalDate.of(2026, 3, 10), null, null
        );

        assertThrows(IllegalArgumentException.class,
                () -> ocrInvoiceService.processOcrInvoice(request));
    }
}
