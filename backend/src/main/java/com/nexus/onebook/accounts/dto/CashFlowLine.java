package com.nexus.onebook.accounts.dto;

import java.math.BigDecimal;

public record CashFlowLine(
    String description,
    BigDecimal amount
) {}
