package com.nexus.onebook.ledger.payment.model;

public enum PaymentRegisterStatus {
    RECEIVED,
    VALIDATED,
    FAILED,
    REJECTED,
    AVAILABLE_FOR_PROCESSING,
    IN_BATCH,
    APPROVED,
    POSTED,
    PAYMENT_GENERATED,
    PAID
}
