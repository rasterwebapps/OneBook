package com.nexus.onebook.payment.dto;

import com.nexus.onebook.payment.model.PaymentBatch;
import com.nexus.onebook.payment.model.PaymentBatchItem;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public record PaymentBatchResponse(
    Long id,
    String tenantId,
    String batchNumber,
    Long vendorAccountId,
    String vendorName,
    BigDecimal totalPurchases,
    BigDecimal totalReturns,
    BigDecimal totalCreditNotes,
    BigDecimal netPayable,
    Long bankAccountId,
    String paymentMode,
    String status,
    String createdBy,
    String approvedBy,
    Instant approvedAt,
    String rejectedBy,
    Instant rejectedAt,
    String rejectionReason,
    Long paymentJournalId,
    boolean paymentFileGenerated,
    Instant createdAt,
    Instant updatedAt,
    List<PaymentRegisterEntryResponse> items
) {
    public static PaymentBatchResponse from(PaymentBatch batch, List<PaymentBatchItem> batchItems) {
        List<PaymentRegisterEntryResponse> itemResponses = batchItems.stream()
            .map(item -> PaymentRegisterEntryResponse.from(item.getRegisterEntry()))
            .collect(Collectors.toList());
        return new PaymentBatchResponse(
            batch.getId(), batch.getTenantId(), batch.getBatchNumber(),
            batch.getVendorAccountId(), batch.getVendorName(),
            batch.getTotalPurchases(), batch.getTotalReturns(), batch.getTotalCreditNotes(),
            batch.getNetPayable(), batch.getBankAccountId(), batch.getPaymentMode(),
            batch.getStatus().name(), batch.getCreatedBy(), batch.getApprovedBy(),
            batch.getApprovedAt(), batch.getRejectedBy(), batch.getRejectedAt(),
            batch.getRejectionReason(), batch.getPaymentJournalId(), batch.isPaymentFileGenerated(),
            batch.getCreatedAt(), batch.getUpdatedAt(), itemResponses
        );
    }
}
