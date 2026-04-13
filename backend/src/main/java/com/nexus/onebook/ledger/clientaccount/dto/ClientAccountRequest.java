package com.nexus.onebook.ledger.clientaccount.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ClientAccountRequest(
    @NotBlank String tenantId,
    @NotNull Long ledgerAccountId,
    @NotBlank String clientType,
    @NotBlank String clientName,
    String contactPerson,
    @Email String email,
    String phone,
    String billingAddress,
    String shippingAddress,
    String gstin,
    String pan,
    @DecimalMin("0") BigDecimal creditLimit,
    @Min(0) Integer paymentTermsDays
) {}
