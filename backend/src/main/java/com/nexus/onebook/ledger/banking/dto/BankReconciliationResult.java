package com.nexus.onebook.ledger.banking.dto;

import java.math.BigDecimal;

public record BankReconciliationResult(
    String tenantId,
    long totalFeedTransactions,
    long matchedTransactions,
    long unmatchedTransactions,
    BigDecimal totalFeedAmount,
    BigDecimal matchedAmount,
    BigDecimal unmatchedAmount
) {}
