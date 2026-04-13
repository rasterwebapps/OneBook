package com.nexus.onebook.ledger.compliance.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EInvoiceRequest(
    @NotBlank String tenantId,
    @NotBlank String invoiceNumber,
    @NotNull LocalDate invoiceDate,
    String buyerGstin,
    String sellerGstin,
    @NotNull @DecimalMin("0.01") BigDecimal totalAmount,
    BigDecimal taxAmount,
    Long journalTransactionId
) {}
