package com.nexus.onebook.advance.model;

/**
 * Approval status for expense vouchers.
 * Simple HOD-only approval workflow.
 */
public enum ExpenseVoucherStatus {
    DRAFT,
    PENDING_HOD_APPROVAL,
    POSTED,
    REJECTED
}
