package com.nexus.onebook.payment.service;

import com.nexus.onebook.payment.dto.VendorGroupResponse;
import com.nexus.onebook.payment.model.PaymentRegisterEntry;
import com.nexus.onebook.payment.model.PaymentRegisterStatus;
import com.nexus.onebook.payment.repository.PaymentRegisterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentRegisterServiceTest {

    @Mock
    private PaymentRegisterRepository registerRepository;

    @InjectMocks
    private PaymentRegisterService registerService;

    private static final String TENANT = "tenant-1";

    private PaymentRegisterEntry buildEntry(Long id, Long vendorId, String vendorName, String type, BigDecimal amount, LocalDate dueDate) {
        PaymentRegisterEntry entry = new PaymentRegisterEntry();
        entry.setId(id);
        entry.setTenantId(TENANT);
        entry.setVendorAccountId(vendorId);
        entry.setVendorName(vendorName);
        entry.setTransactionType(type);
        entry.setAmount(amount);
        entry.setStatus(PaymentRegisterStatus.AVAILABLE_FOR_PROCESSING);
        entry.setDueDate(dueDate);
        return entry;
    }

    @Test
    void getRegisterGroupedByVendor_groupsCorrectly() {
        var entry1 = buildEntry(1L, 10L, "Vendor A", "PURCHASE", new BigDecimal("500.00"), LocalDate.now().plusDays(10));
        var entry2 = buildEntry(2L, 10L, "Vendor A", "PURCHASE_RETURN", new BigDecimal("50.00"), LocalDate.now().plusDays(15));
        var entry3 = buildEntry(3L, 20L, "Vendor B", "PURCHASE", new BigDecimal("300.00"), LocalDate.now().plusDays(5));

        when(registerRepository.findByTenantIdAndStatusOrderByDueDateAsc(TENANT, PaymentRegisterStatus.AVAILABLE_FOR_PROCESSING))
            .thenReturn(List.of(entry1, entry2, entry3));

        List<VendorGroupResponse> groups = registerService.getRegisterGroupedByVendor(TENANT);

        assertEquals(2, groups.size());

        VendorGroupResponse vendorA = groups.stream().filter(g -> g.vendorAccountId().equals(10L)).findFirst().orElseThrow();
        assertEquals(2, vendorA.entries().size());
        assertEquals(new BigDecimal("500.00"), vendorA.totalPurchases());
        assertEquals(new BigDecimal("50.00"), vendorA.totalReturns());
        assertEquals(new BigDecimal("450.00"), vendorA.netOutstanding());

        VendorGroupResponse vendorB = groups.stream().filter(g -> g.vendorAccountId().equals(20L)).findFirst().orElseThrow();
        assertEquals(1, vendorB.entries().size());
        assertEquals(new BigDecimal("300.00"), vendorB.totalPurchases());
    }

    @Test
    void getRegisterGroupedByVendor_sortsByDueDate() {
        var entry1 = buildEntry(1L, 10L, "Vendor A", "PURCHASE", new BigDecimal("100.00"), LocalDate.now().plusDays(20));
        var entry2 = buildEntry(2L, 10L, "Vendor A", "PURCHASE", new BigDecimal("200.00"), LocalDate.now().plusDays(5));
        var entry3 = buildEntry(3L, 10L, "Vendor A", "PURCHASE", new BigDecimal("150.00"), LocalDate.now().plusDays(10));

        when(registerRepository.findByTenantIdAndStatusOrderByDueDateAsc(TENANT, PaymentRegisterStatus.AVAILABLE_FOR_PROCESSING))
            .thenReturn(List.of(entry2, entry3, entry1));

        List<VendorGroupResponse> groups = registerService.getRegisterGroupedByVendor(TENANT);
        assertEquals(1, groups.size());

        List<BigDecimal> amounts = groups.get(0).entries().stream()
            .map(e -> e.amount())
            .toList();
        assertEquals(new BigDecimal("200.00"), amounts.get(0));
        assertEquals(new BigDecimal("150.00"), amounts.get(1));
        assertEquals(new BigDecimal("100.00"), amounts.get(2));
    }
}
