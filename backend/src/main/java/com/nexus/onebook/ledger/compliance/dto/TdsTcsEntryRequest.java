package com.nexus.onebook.ledger.compliance.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record TdsTcsEntryRequest(
    @NotBlank String tenantId,
    @NotBlank String entryType,
    @NotBlank String sectionCode,
    @NotBlank String partyName,
    String partyPan,
    @NotNull java.time.LocalDate transactionDate,
    @NotNull @DecimalMin("0.01") BigDecimal taxableAmount,
    @NotNull @DecimalMin("0.01") BigDecimal taxRate,
    Long journalTransactionId
) {}
