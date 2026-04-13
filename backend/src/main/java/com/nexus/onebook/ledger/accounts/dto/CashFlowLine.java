package com.nexus.onebook.ledger.accounts.dto;

import java.math.BigDecimal;

public record CashFlowLine(
    String description,
    BigDecimal amount
) {}
