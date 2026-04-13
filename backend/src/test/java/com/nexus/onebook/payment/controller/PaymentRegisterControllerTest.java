package com.nexus.onebook.payment.controller;

import com.nexus.onebook.payment.dto.PaymentRegisterEntryResponse;
import com.nexus.onebook.payment.dto.VendorGroupResponse;
import com.nexus.onebook.payment.service.PaymentRegisterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentRegisterControllerTest {

    @Mock
    private PaymentRegisterService registerService;

    @InjectMocks
    private PaymentRegisterController controller;

    private static final String TENANT = "tenant-1";

    private VendorGroupResponse buildVendorGroup(Long vendorId, String vendorName, BigDecimal netOutstanding) {
        PaymentRegisterEntryResponse entry = new PaymentRegisterEntryResponse(
                1L, TENANT, vendorId, vendorName,
                "PURCHASE_INVOICE", "SRC-001", "PURCHASE", "INV-001",
                LocalDate.now(), LocalDate.now().plusDays(30), new BigDecimal("1000.00"),
                "INR", "NEFT", "1234567890", "HDFC0001234", "HDFC Bank",
                "AVAILABLE_FOR_PROCESSING", null
        );
        return new VendorGroupResponse(
                vendorId, vendorName, List.of(entry),
                new BigDecimal("1000.00"), BigDecimal.ZERO, BigDecimal.ZERO, netOutstanding
        );
    }

    @Test
    void getRegister_returnsVendorGroups() {
        VendorGroupResponse group1 = buildVendorGroup(10L, "Vendor A", new BigDecimal("1000.00"));
        VendorGroupResponse group2 = buildVendorGroup(20L, "Vendor B", new BigDecimal("500.00"));

        when(registerService.getRegisterGroupedByVendor(TENANT)).thenReturn(List.of(group1, group2));

        ResponseEntity<List<VendorGroupResponse>> response = controller.getRegister(TENANT);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(registerService).getRegisterGroupedByVendor(TENANT);
    }

    @Test
    void getRegister_emptyList_returnsOk() {
        when(registerService.getRegisterGroupedByVendor(TENANT)).thenReturn(List.of());

        ResponseEntity<List<VendorGroupResponse>> response = controller.getRegister(TENANT);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getRegisterForVendor_returnsVendorGroup() {
        VendorGroupResponse group = buildVendorGroup(10L, "Vendor A", new BigDecimal("1500.00"));

        when(registerService.getRegisterForVendor(TENANT, 10L)).thenReturn(group);

        ResponseEntity<VendorGroupResponse> response = controller.getRegisterForVendor(10L, TENANT);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().vendorAccountId());
        assertEquals("Vendor A", response.getBody().vendorName());
        assertEquals(new BigDecimal("1500.00"), response.getBody().netOutstanding());
    }

    @Test
    void getRegisterForVendor_callsServiceWithCorrectParams() {
        VendorGroupResponse group = buildVendorGroup(25L, "Vendor C", BigDecimal.ZERO);
        when(registerService.getRegisterForVendor("tenant-xyz", 25L)).thenReturn(group);

        controller.getRegisterForVendor(25L, "tenant-xyz");

        verify(registerService).getRegisterForVendor("tenant-xyz", 25L);
    }
}
