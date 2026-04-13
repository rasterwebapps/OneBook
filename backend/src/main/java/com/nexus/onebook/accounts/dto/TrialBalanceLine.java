package com.nexus.onebook.accounts.dto;

import java.math.BigDecimal;

/**
 * Response DTO representing a single line in a trial balance report.
 * Each line shows an account's total debits and credits.
 */
public record TrialBalanceLine(
        Long accountId,
        String accountCode,
        String accountName,
        String accountType,
        BigDecimal totalDebits,
        BigDecimal totalCredits
) {}
