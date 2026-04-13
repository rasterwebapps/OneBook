package com.nexus.onebook.advance.dto;

/**
 * Request DTO for approval actions on advances and expense vouchers.
 */
public record ApprovalRequest(
    String action,       // APPROVE or REJECT
    String actorId,
    String comment,
    String rejectionReason
) {}
