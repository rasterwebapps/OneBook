package com.nexus.onebook.ledger.banking.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ChequeEntryRequest(
    @NotBlank String tenantId,
    @NotBlank String chequeNumber,
    @NotNull Long bankAccountId,
    @NotBlank String partyName,
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    @NotNull LocalDate chequeDate,
    @NotBlank String chequeType
) {}
