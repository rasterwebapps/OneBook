package com.nexus.onebook.ledger.compliance.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record EWayBillRequest(
    @NotBlank String tenantId,
    @NotNull Long eInvoiceId,
    @NotBlank String transporterGstin,
    @NotBlank String vehicleNumber,
    @NotNull @DecimalMin("0.01") BigDecimal distance
) {}
