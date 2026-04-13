package com.nexus.onebook.ledger.advance.model;

/**
 * Approval status for employee advance vouchers.
 * Supports tiered approval workflow: HOD → CEO → MD based on amount.
 */
public enum AdvanceStatus {
    DRAFT,
    PENDING_HOD_APPROVAL,
    PENDING_CEO_APPROVAL,
    PENDING_MD_APPROVAL,
    APPROVED,
    POSTED,
    REJECTED
}
