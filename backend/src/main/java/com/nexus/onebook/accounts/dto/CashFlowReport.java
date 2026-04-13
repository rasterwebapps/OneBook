package com.nexus.onebook.accounts.dto;

import java.math.BigDecimal;
import java.util.List;

public record CashFlowReport(
    String tenantId,
    List<CashFlowLine> operatingActivities,
    List<CashFlowLine> investingActivities,
    List<CashFlowLine> financingActivities,
    BigDecimal netCashFromOperating,
    BigDecimal netCashFromInvesting,
    BigDecimal netCashFromFinancing,
    BigDecimal netCashChange
) {}
