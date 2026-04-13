package com.nexus.onebook.ingestion.dto;

import com.nexus.onebook.ingestion.model.MatchStatus;
import java.math.BigDecimal;
import java.util.List;

/**
 * Result of a 3-way matching attempt between PO, Goods Receipt, and Invoice.
 */
public record ThreeWayMatchResult(
        MatchStatus status,
        String poNumber,
        BigDecimal poAmount,
        BigDecimal grAmount,
        BigDecimal invoiceAmount,
        boolean amountsMatch,
        List<String> discrepancies,
        String message
) {}
