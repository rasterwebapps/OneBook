package com.nexus.onebook.accounts.service;

import com.nexus.onebook.accounts.model.VoucherCategory;
import com.nexus.onebook.accounts.model.VoucherType;
import com.nexus.onebook.accounts.repository.VoucherTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoucherTypeServiceTest {

    @Mock
    private VoucherTypeRepository voucherTypeRepository;

    @InjectMocks
    private VoucherTypeService voucherTypeService;

    @Test
    void createVoucherType_newCode_createsSuccessfully() {
        when(voucherTypeRepository.findByTenantIdAndVoucherCode("tenant-1", "SLS"))
                .thenReturn(Optional.empty());
        when(voucherTypeRepository.save(any(VoucherType.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        VoucherType result = voucherTypeService.createVoucherType(
                "tenant-1", "SLS", "Sales Voucher", "SALES");

        assertNotNull(result);
        assertEquals("SLS", result.getVoucherCode());
        assertEquals("Sales Voucher", result.getVoucherName());
        assertEquals(VoucherCategory.SALES, result.getCategory());
    }

    @Test
    void createVoucherType_duplicateCode_throwsException() {
        VoucherType existing = new VoucherType("tenant-1", "SLS", "Sales", VoucherCategory.SALES);
        when(voucherTypeRepository.findByTenantIdAndVoucherCode("tenant-1", "SLS"))
                .thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () ->
                voucherTypeService.createVoucherType("tenant-1", "SLS", "Sales Voucher", "SALES"));
    }

    @Test
    void getVoucherTypes_returnsList() {
        VoucherType v1 = new VoucherType("tenant-1", "SLS", "Sales", VoucherCategory.SALES);
        VoucherType v2 = new VoucherType("tenant-1", "PUR", "Purchase", VoucherCategory.PURCHASE);

        when(voucherTypeRepository.findByTenantId("tenant-1"))
                .thenReturn(List.of(v1, v2));

        List<VoucherType> result = voucherTypeService.getVoucherTypes("tenant-1");

        assertEquals(2, result.size());
    }

    @Test
    void getVoucherTypesByCategory_returnsFilteredList() {
        VoucherType v1 = new VoucherType("tenant-1", "SLS", "Sales", VoucherCategory.SALES);

        when(voucherTypeRepository.findByTenantIdAndCategory("tenant-1", VoucherCategory.SALES))
                .thenReturn(List.of(v1));

        List<VoucherType> result = voucherTypeService.getVoucherTypesByCategory(
                "tenant-1", VoucherCategory.SALES);

        assertEquals(1, result.size());
        assertEquals(VoucherCategory.SALES, result.get(0).getCategory());
    }
}
