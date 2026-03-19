package com.nexus.onebook.ledger.payment.dto;

import com.nexus.onebook.ledger.payment.model.PaymentRegisterEntry;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentRegisterEntryResponse(
    Long id, String tenantId, Long vendorAccountId, String vendorName,
    String sourceType, String sourceReferenceId, String transactionType,
    String invoiceNumber, LocalDate invoiceDate, LocalDate dueDate,
    BigDecimal amount, String currency, String paymentMode,
    String bankAccountNumber, String bankIfscCode, String bankName,
    String status, Long batchId
) {
    public static PaymentRegisterEntryResponse from(PaymentRegisterEntry e) {
        return new PaymentRegisterEntryResponse(
            e.getId(), e.getTenantId(), e.getVendorAccountId(), e.getVendorName(),
            e.getSourceType(), e.getSourceReferenceId(), e.getTransactionType(),
            e.getInvoiceNumber(), e.getInvoiceDate(), e.getDueDate(),
            e.getAmount(), e.getCurrency(), e.getPaymentMode(),
            e.getBankAccountNumber(), e.getBankIfscCode(), e.getBankName(),
            e.getStatus().name(), e.getBatchId()
        );
    }
}
