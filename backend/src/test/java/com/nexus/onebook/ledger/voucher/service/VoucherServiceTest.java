package com.nexus.onebook.ledger.voucher.service;

import com.nexus.onebook.ledger.model.*;
import com.nexus.onebook.ledger.repository.*;
import com.nexus.onebook.ledger.voucher.dto.*;
import com.nexus.onebook.ledger.voucher.model.*;
import com.nexus.onebook.ledger.voucher.repository.VoucherItemRepository;
import com.nexus.onebook.ledger.voucher.repository.VoucherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoucherServiceTest {

    @Mock private VoucherRepository voucherRepository;
    @Mock private VoucherItemRepository voucherItemRepository;
    @Mock private VoucherTypeRepository voucherTypeRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private SubDepartmentRepository subDepartmentRepository;
    @Mock private PayerRepository payerRepository;
    @Mock private PayerBankAccountRepository payerBankAccountRepository;
    @Mock private PayeeRepository payeeRepository;
    @Mock private PayeeBankAccountRepository payeeBankAccountRepository;
    @Mock private LedgerAccountRepository ledgerAccountRepository;
    @Mock private CostCenterRepository costCenterRepository;

    @InjectMocks
    private VoucherService voucherService;

    private Voucher sampleVoucher;

    @BeforeEach
    void setUp() {
        sampleVoucher = new Voucher("tenant1", "VCH-001",
                new BigDecimal("10000.0000"), new BigDecimal("9500.0000"), "admin");
        sampleVoucher.setId(1L);
        sampleVoucher.setTdsAmount(new BigDecimal("500.0000"));
    }

    @Test
    void createVoucher_shouldPersistAndReturnResponse() {
        CreateVoucherRequest request = new CreateVoucherRequest(
                "tenant1", "VCH-001", null, null, null, null, null,
                Instant.now(), new BigDecimal("10000.0000"), new BigDecimal("500.0000"),
                new BigDecimal("9500.0000"), null, "Test voucher", null, "admin");

        when(voucherRepository.save(any(Voucher.class))).thenAnswer(invocation -> {
            Voucher v = invocation.getArgument(0);
            v.setId(1L);
            return v;
        });

        VoucherResponse response = voucherService.createVoucher(request);

        assertNotNull(response);
        assertEquals("VCH-001", response.voucherNumber());
        assertEquals("tenant1", response.tenantId());
        assertEquals("CREATED", response.status());
        assertEquals(new BigDecimal("10000.0000"), response.totalAmount());
        assertEquals(new BigDecimal("9500.0000"), response.netAmount());
        verify(voucherRepository).save(any(Voucher.class));
    }

    @Test
    void getVouchersByTenant_shouldReturnList() {
        when(voucherRepository.findByTenantId("tenant1")).thenReturn(List.of(sampleVoucher));

        List<VoucherResponse> results = voucherService.getVouchersByTenant("tenant1");

        assertEquals(1, results.size());
        assertEquals("VCH-001", results.get(0).voucherNumber());
    }

    @Test
    void getVoucherById_shouldReturnVoucher() {
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(sampleVoucher));

        VoucherResponse response = voucherService.getVoucherById(1L);

        assertNotNull(response);
        assertEquals("VCH-001", response.voucherNumber());
    }

    @Test
    void getVoucherById_shouldThrowWhenNotFound() {
        when(voucherRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> voucherService.getVoucherById(999L));
    }

    @Test
    void approveVoucher_shouldUpdateStatusAndApprover() {
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(sampleVoucher));
        when(voucherRepository.save(any(Voucher.class))).thenAnswer(i -> i.getArgument(0));

        VoucherResponse response = voucherService.approveVoucher(1L, "manager");

        assertEquals("APPROVED", response.status());
        assertEquals("manager", response.approvedBy());
        assertNotNull(response.approvedAt());
        assertEquals(new BigDecimal("10000.0000"), response.approvedAmount());
    }

    @Test
    void approveVoucher_shouldRejectIfNotCreatedStatus() {
        sampleVoucher.setStatus(VoucherStatus.APPROVED);
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(sampleVoucher));

        assertThrows(IllegalStateException.class,
                () -> voucherService.approveVoucher(1L, "manager"));
    }

    @Test
    void cancelVoucher_shouldSetCancelledFields() {
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(sampleVoucher));
        when(voucherRepository.save(any(Voucher.class))).thenAnswer(i -> i.getArgument(0));

        VoucherResponse response = voucherService.cancelVoucher(1L, "admin", "Duplicate entry");

        assertTrue(response.cancelled());
    }

    @Test
    void createVoucherItem_shouldPersistItem() {
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(sampleVoucher));
        when(voucherItemRepository.save(any(VoucherItem.class))).thenAnswer(invocation -> {
            VoucherItem item = invocation.getArgument(0);
            item.setId(10L);
            return item;
        });

        CreateVoucherItemRequest request = new CreateVoucherItemRequest(
                "tenant1", 1L, 1, null, null, null, null,
                "Office supplies", new BigDecimal("5000.0000"),
                false, null, null, new BigDecimal("5000.0000"), null);

        VoucherItemResponse response = voucherService.createVoucherItem(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("5000.0000"), response.amount());
        assertEquals("CREATED", response.status());
        verify(voucherItemRepository).save(any(VoucherItem.class));
    }

    @Test
    void getVoucherItems_shouldReturnItemsList() {
        VoucherItem item = new VoucherItem("tenant1", sampleVoucher,
                new BigDecimal("5000.0000"), new BigDecimal("5000.0000"));
        item.setId(10L);
        when(voucherItemRepository.findByTenantIdAndVoucherId("tenant1", 1L))
                .thenReturn(List.of(item));

        List<VoucherItemResponse> items = voucherService.getVoucherItems("tenant1", 1L);

        assertEquals(1, items.size());
    }
}
