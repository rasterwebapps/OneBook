package com.nexus.onebook.inventory.dto;

import java.math.BigDecimal;
import java.util.List;

public record ReorderAlert(
    Long stockItemId,
    String itemCode,
    String itemName,
    BigDecimal currentBalance,
    BigDecimal reorderLevel,
    BigDecimal reorderQuantity,
    String godownName
) {
    public static record ReorderAlertList(List<ReorderAlert> alerts, int totalCount) {}
}
