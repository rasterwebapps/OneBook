package com.nexus.onebook.ledger.compliance.service;

import com.nexus.onebook.ledger.compliance.dto.EInvoiceRequest;
import com.nexus.onebook.ledger.compliance.dto.EWayBillRequest;
import com.nexus.onebook.ledger.compliance.model.EInvoice;
import com.nexus.onebook.ledger.compliance.model.EInvoiceStatus;
import com.nexus.onebook.ledger.compliance.repository.EInvoiceRepository;
import com.nexus.onebook.ledger.accounts.repository.JournalTransactionRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplianceServiceTest {

    @Mock
    private EInvoiceRepository eInvoiceRepository;
    @Mock
    private JournalTransactionRepository journalTransactionRepository;

    @InjectMocks
    private ComplianceService complianceService;

    @Test
    void createEInvoice_validRequest_succeeds() {
        EInvoiceRequest request = new EInvoiceRequest(
                "tenant-1", "INV-001", LocalDate.of(2024, 3, 1),
                "29ABCDE1234F1Z5", "29XYZAB5678C1D3",
                new BigDecimal("50000.0000"), new BigDecimal("9000.0000"), null);

        when(eInvoiceRepository.findByTenantIdAndInvoiceNumber("tenant-1", "INV-001"))
                .thenReturn(Optional.empty());
        when(eInvoiceRepository.save(any(EInvoice.class))).thenAnswer(inv -> inv.getArgument(0));

        EInvoice result = complianceService.createEInvoice(request);

        assertNotNull(result);
        assertEquals("INV-001", result.getInvoiceNumber());
        assertEquals(EInvoiceStatus.DRAFT, result.getStatus());
    }

    @Test
    void createEInvoice_duplicateNumber_throws() {
        EInvoiceRequest request = new EInvoiceRequest(
                "tenant-1", "INV-001", LocalDate.of(2024, 3, 1),
                null, null, new BigDecimal("50000.0000"), null, null);

        when(eInvoiceRepository.findByTenantIdAndInvoiceNumber("tenant-1", "INV-001"))
                .thenReturn(Optional.of(new EInvoice()));

        assertThrows(IllegalArgumentException.class, () -> complianceService.createEInvoice(request));
    }

    @Test
    void generateEInvoice_draftInvoice_generatesIrn() {
        EInvoice invoice = new EInvoice("tenant-1", "INV-001",
                LocalDate.of(2024, 3, 1), new BigDecimal("50000.0000"));
        invoice.setId(1L);

        when(eInvoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(eInvoiceRepository.save(any(EInvoice.class))).thenAnswer(inv -> inv.getArgument(0));

        EInvoice result = complianceService.generateEInvoice(1L);

        assertEquals(EInvoiceStatus.GENERATED, result.getStatus());
        assertNotNull(result.getIrn());
        assertTrue(result.getIrn().startsWith("IRN-"));
    }

    @Test
    void generateEInvoice_nonDraft_throws() {
        EInvoice invoice = new EInvoice("tenant-1", "INV-001",
                LocalDate.of(2024, 3, 1), new BigDecimal("50000.0000"));
        invoice.setId(1L);
        invoice.setStatus(EInvoiceStatus.GENERATED);

        when(eInvoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        assertThrows(IllegalArgumentException.class, () -> complianceService.generateEInvoice(1L));
    }

    @Test
    void generateEWayBill_generatedInvoice_succeeds() {
        EInvoice invoice = new EInvoice("tenant-1", "INV-001",
                LocalDate.of(2024, 3, 1), new BigDecimal("50000.0000"));
        invoice.setId(1L);
        invoice.setStatus(EInvoiceStatus.GENERATED);
        invoice.setIrn("IRN-TEST123");

        EWayBillRequest request = new EWayBillRequest(
                "tenant-1", 1L, "29TRNSP4567G1H8", "KA01AB1234", new BigDecimal("100"));

        when(eInvoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(eInvoiceRepository.save(any(EInvoice.class))).thenAnswer(inv -> inv.getArgument(0));

        EInvoice result = complianceService.generateEWayBill(request);

        assertNotNull(result.getEWayBillNumber());
        assertTrue(result.getEWayBillNumber().startsWith("EWB"));
    }

    @Test
    void cancelEInvoice_succeeds() {
        EInvoice invoice = new EInvoice("tenant-1", "INV-001",
                LocalDate.of(2024, 3, 1), new BigDecimal("50000.0000"));
        invoice.setId(1L);

        when(eInvoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(eInvoiceRepository.save(any(EInvoice.class))).thenAnswer(inv -> inv.getArgument(0));

        EInvoice result = complianceService.cancelEInvoice(1L);

        assertEquals(EInvoiceStatus.CANCELLED, result.getStatus());
    }
}
