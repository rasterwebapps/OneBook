package com.nexus.onebook.ledger.payment.dto;

import java.math.BigDecimal;
import java.util.List;

public record VendorGroupResponse(
    Long vendorAccountId,
    String vendorName,
    List<PaymentRegisterEntryResponse> entries,
    BigDecimal totalPurchases,
    BigDecimal totalReturns,
    BigDecimal totalCreditNotes,
    BigDecimal netOutstanding
) {}
