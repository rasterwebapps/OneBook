package com.nexus.onebook.ledger.compliance.service;

import com.nexus.onebook.ledger.compliance.dto.EInvoiceRequest;
import com.nexus.onebook.ledger.compliance.dto.EWayBillRequest;
import com.nexus.onebook.ledger.compliance.model.EInvoice;
import com.nexus.onebook.ledger.compliance.model.EInvoiceStatus;
import com.nexus.onebook.ledger.accounts.model.JournalTransaction;
import com.nexus.onebook.ledger.compliance.repository.EInvoiceRepository;
import com.nexus.onebook.ledger.accounts.repository.JournalTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ComplianceService {

    private final EInvoiceRepository eInvoiceRepository;
    private final JournalTransactionRepository journalTransactionRepository;

    public ComplianceService(EInvoiceRepository eInvoiceRepository,
                              JournalTransactionRepository journalTransactionRepository) {
        this.eInvoiceRepository = eInvoiceRepository;
        this.journalTransactionRepository = journalTransactionRepository;
    }

    @Transactional
    public EInvoice createEInvoice(EInvoiceRequest request) {
        eInvoiceRepository.findByTenantIdAndInvoiceNumber(request.tenantId(), request.invoiceNumber())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Invoice number '" + request.invoiceNumber() + "' already exists");
                });

        EInvoice invoice = new EInvoice(
                request.tenantId(), request.invoiceNumber(),
                request.invoiceDate(), request.totalAmount());

        if (request.buyerGstin() != null) invoice.setBuyerGstin(request.buyerGstin());
        if (request.sellerGstin() != null) invoice.setSellerGstin(request.sellerGstin());
        if (request.taxAmount() != null) invoice.setTaxAmount(request.taxAmount());

        if (request.journalTransactionId() != null) {
            JournalTransaction txn = journalTransactionRepository.findById(request.journalTransactionId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Journal transaction not found: " + request.journalTransactionId()));
            invoice.setJournalTransaction(txn);
        }

        return eInvoiceRepository.save(invoice);
    }

    @Transactional
    public EInvoice generateEInvoice(Long invoiceId) {
        EInvoice invoice = eInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("E-Invoice not found: " + invoiceId));

        if (invoice.getStatus() != EInvoiceStatus.DRAFT) {
            throw new IllegalArgumentException("Can only generate from DRAFT status");
        }

        // Generate IRN (Invoice Reference Number) — simulated
        String irn = "IRN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        invoice.setIrn(irn);
        invoice.setStatus(EInvoiceStatus.GENERATED);

        return eInvoiceRepository.save(invoice);
    }

    @Transactional
    public EInvoice generateEWayBill(EWayBillRequest request) {
        EInvoice invoice = eInvoiceRepository.findById(request.eInvoiceId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "E-Invoice not found: " + request.eInvoiceId()));

        if (invoice.getStatus() != EInvoiceStatus.GENERATED) {
            throw new IllegalArgumentException("E-Way bill requires a generated e-Invoice");
        }

        // Generate e-Way bill number — simulated
        String ewayBillNumber = "EWB" + System.currentTimeMillis();
        invoice.setEWayBillNumber(ewayBillNumber);

        return eInvoiceRepository.save(invoice);
    }

    @Transactional
    public EInvoice cancelEInvoice(Long invoiceId) {
        EInvoice invoice = eInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("E-Invoice not found: " + invoiceId));

        invoice.setStatus(EInvoiceStatus.CANCELLED);
        return eInvoiceRepository.save(invoice);
    }

    @Transactional(readOnly = true)
    public List<EInvoice> getInvoicesByTenant(String tenantId) {
        return eInvoiceRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public EInvoice getInvoice(Long id) {
        return eInvoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("E-Invoice not found: " + id));
    }
}
