package com.nexus.onebook.payment.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateBatchRequest(
    @NotNull Long vendorAccountId,
    @NotEmpty List<Long> registerEntryIds,
    Long bankAccountId,
    String paymentMode
) {}
