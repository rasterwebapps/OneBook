package com.nexus.onebook.education.model;

/**
 * Lifecycle status of a student fee enquiry.
 * OPEN: enquiry created, awaiting finalization.
 * FINALIZED: fee structure has been finalized and discount applied.
 * CANCELLED: enquiry was cancelled and will not be processed.
 */
public enum EnquiryStatus {
    OPEN,
    FINALIZED,
    CANCELLED
}
