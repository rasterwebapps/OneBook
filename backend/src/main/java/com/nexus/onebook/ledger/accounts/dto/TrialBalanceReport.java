package com.nexus.onebook.ledger.accounts.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for a complete trial balance report.
 * Contains line items per account and aggregate totals.
 */
public record TrialBalanceReport(
        String tenantId,
        List<TrialBalanceLine> lines,
        BigDecimal totalDebits,
        BigDecimal totalCredits,
        boolean balanced
) {}
