package com.nexus.onebook.ledger.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record BatchApprovalRequest(
    @NotBlank String action,
    String rejectionReason
) {}
