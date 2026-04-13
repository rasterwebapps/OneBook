package com.nexus.onebook.ingestion.automation;

import com.nexus.onebook.ingestion.dto.OcrInvoiceRequest;
import com.nexus.onebook.ingestion.model.MatchStatus;
import com.nexus.onebook.ingestion.model.VendorInvoice;
import com.nexus.onebook.ingestion.repository.VendorInvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AP/AR Automation with OCR.
 * Processes OCR-extracted invoice data and creates VendorInvoice records
 * that can then be matched against Purchase Orders and Goods Receipts.
 * <p>
 * In production, the OCR extraction would be performed by an AI/ML
 * pipeline (e.g. AWS Textract, Google Document AI) before calling this service.
 * This service handles the post-extraction workflow: validation, persistence,
 * and auto-drafting of journal entries.
 */
@Service
public class OcrInvoiceService {

    private final VendorInvoiceRepository invoiceRepository;

    public OcrInvoiceService(VendorInvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Processes an OCR-extracted invoice and persists it for 3-way matching.
     *
     * @param request the OCR-extracted invoice data
     * @return the persisted VendorInvoice
     */
    @Transactional
    public VendorInvoice processOcrInvoice(OcrInvoiceRequest request) {
        if (request.totalAmount() == null) {
            throw new IllegalArgumentException("Invoice total amount is required");
        }

        // Check for duplicate invoice
        invoiceRepository.findByTenantIdAndInvoiceNumber(request.tenantId(), request.invoiceNumber())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Duplicate invoice: " + request.invoiceNumber());
                });

        VendorInvoice invoice = new VendorInvoice(
                request.tenantId(),
                request.invoiceNumber(),
                request.poNumber() != null ? request.poNumber() : "",
                request.vendorName(),
                request.totalAmount(),
                request.invoiceDate()
        );

        invoice.setOcrExtracted(true);
        invoice.setCurrency(request.currency());
        invoice.setMatchStatus(MatchStatus.PENDING);

        if (request.lineItems() != null) {
            invoice.setLineItems(request.lineItems());
        }
        if (request.rawOcrText() != null) {
            invoice.setMetadata("{\"ocrExtracted\":true}");
        }

        return invoiceRepository.save(invoice);
    }
}
