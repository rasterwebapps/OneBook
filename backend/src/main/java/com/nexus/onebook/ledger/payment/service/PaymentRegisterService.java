package com.nexus.onebook.ledger.payment.service;

import com.nexus.onebook.ledger.payment.dto.PaymentRegisterEntryResponse;
import com.nexus.onebook.ledger.payment.dto.VendorGroupResponse;
import com.nexus.onebook.ledger.payment.model.PaymentRegisterEntry;
import com.nexus.onebook.ledger.payment.model.PaymentRegisterStatus;
import com.nexus.onebook.ledger.payment.repository.PaymentRegisterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PaymentRegisterService {

    private final PaymentRegisterRepository registerRepository;

    public PaymentRegisterService(PaymentRegisterRepository registerRepository) {
        this.registerRepository = registerRepository;
    }

    @Transactional(readOnly = true)
    public List<VendorGroupResponse> getRegisterGroupedByVendor(String tenantId) {
        List<PaymentRegisterEntry> entries = registerRepository
            .findByTenantIdAndStatusOrderByDueDateAsc(tenantId, PaymentRegisterStatus.AVAILABLE_FOR_PROCESSING);

        Map<Long, List<PaymentRegisterEntry>> grouped = entries.stream()
            .collect(Collectors.groupingBy(PaymentRegisterEntry::getVendorAccountId,
                LinkedHashMap::new, Collectors.toList()));

        return grouped.entrySet().stream()
            .map(e -> buildVendorGroupResponse(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VendorGroupResponse getRegisterForVendor(String tenantId, Long vendorAccountId) {
        List<PaymentRegisterEntry> entries = registerRepository
            .findByTenantIdAndVendorAccountIdAndStatus(tenantId, vendorAccountId, PaymentRegisterStatus.AVAILABLE_FOR_PROCESSING);
        entries.sort(Comparator.comparing(PaymentRegisterEntry::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())));
        return buildVendorGroupResponse(vendorAccountId, entries);
    }

    private VendorGroupResponse buildVendorGroupResponse(Long vendorAccountId, List<PaymentRegisterEntry> entries) {
        String vendorName = entries.isEmpty() ? "" : entries.get(0).getVendorName();

        BigDecimal totalPurchases = entries.stream()
            .filter(e -> "PURCHASE".equals(e.getTransactionType()))
            .map(PaymentRegisterEntry::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalReturns = entries.stream()
            .filter(e -> "PURCHASE_RETURN".equals(e.getTransactionType()))
            .map(PaymentRegisterEntry::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCreditNotes = entries.stream()
            .filter(e -> "CREDIT_NOTE".equals(e.getTransactionType()))
            .map(PaymentRegisterEntry::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netOutstanding = totalPurchases.subtract(totalReturns).subtract(totalCreditNotes);

        List<PaymentRegisterEntryResponse> entryResponses = entries.stream()
            .map(PaymentRegisterEntryResponse::from)
            .collect(Collectors.toList());

        return new VendorGroupResponse(vendorAccountId, vendorName, entryResponses,
            totalPurchases, totalReturns, totalCreditNotes, netOutstanding);
    }
}
