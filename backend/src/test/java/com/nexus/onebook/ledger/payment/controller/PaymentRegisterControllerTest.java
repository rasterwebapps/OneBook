package com.nexus.onebook.ledger.payment.controller;

import com.nexus.onebook.ledger.payment.dto.PaymentRegisterEntryResponse;
import com.nexus.onebook.ledger.payment.dto.VendorGroupResponse;
import com.nexus.onebook.ledger.payment.service.PaymentRegisterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PaymentRegisterControllerTest {

    @Mock
    private PaymentRegisterService registerService;

    @InjectMocks
    private PaymentRegisterController registerController;

    private MockMvc mockMvc;

    private static final String TENANT = "tenant-1";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(registerController).build();
    }

    private PaymentRegisterEntryResponse buildEntryResponse(Long id, Long vendorId, String vendorName,
            String type, BigDecimal amount) {
        return new PaymentRegisterEntryResponse(
            id, TENANT, vendorId, vendorName,
            "JOURNAL", "JRN-001", type,
            "INV-001", LocalDate.now(), LocalDate.now().plusDays(30),
            amount, "INR", "NEFT",
            "1234567890", "HDFC0001234", "HDFC Bank",
            "AVAILABLE_FOR_PROCESSING", null
        );
    }

    private VendorGroupResponse buildVendorGroupResponse(Long vendorId, String vendorName,
            List<PaymentRegisterEntryResponse> entries) {
        BigDecimal totalPurchases = entries.stream()
            .filter(e -> "PURCHASE".equals(e.transactionType()))
            .map(PaymentRegisterEntryResponse::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalReturns = entries.stream()
            .filter(e -> "PURCHASE_RETURN".equals(e.transactionType()))
            .map(PaymentRegisterEntryResponse::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCreditNotes = entries.stream()
            .filter(e -> "CREDIT_NOTE".equals(e.transactionType()))
            .map(PaymentRegisterEntryResponse::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netOutstanding = totalPurchases.subtract(totalReturns).subtract(totalCreditNotes);

        return new VendorGroupResponse(vendorId, vendorName, entries,
            totalPurchases, totalReturns, totalCreditNotes, netOutstanding);
    }

    @Test
    void getRegister_returnsGroupedByVendor() throws Exception {
        var entry1 = buildEntryResponse(1L, 10L, "Vendor A", "PURCHASE", new BigDecimal("500.00"));
        var entry2 = buildEntryResponse(2L, 10L, "Vendor A", "PURCHASE_RETURN", new BigDecimal("50.00"));
        var entry3 = buildEntryResponse(3L, 20L, "Vendor B", "PURCHASE", new BigDecimal("300.00"));

        var vendorAGroup = buildVendorGroupResponse(10L, "Vendor A", List.of(entry1, entry2));
        var vendorBGroup = buildVendorGroupResponse(20L, "Vendor B", List.of(entry3));

        when(registerService.getRegisterGroupedByVendor(TENANT))
            .thenReturn(List.of(vendorAGroup, vendorBGroup));

        mockMvc.perform(get("/api/payment-register")
                .param("tenantId", TENANT))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].vendorAccountId").value(10))
            .andExpect(jsonPath("$[0].vendorName").value("Vendor A"))
            .andExpect(jsonPath("$[0].netOutstanding").value(450.00))
            .andExpect(jsonPath("$[1].vendorAccountId").value(20))
            .andExpect(jsonPath("$[1].vendorName").value("Vendor B"))
            .andExpect(jsonPath("$[1].netOutstanding").value(300.00));
    }

    @Test
    void getRegister_emptyResult_returnsEmptyArray() throws Exception {
        when(registerService.getRegisterGroupedByVendor(TENANT))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/payment-register")
                .param("tenantId", TENANT))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getRegisterForVendor_returnsVendorEntries() throws Exception {
        var entry1 = buildEntryResponse(1L, 10L, "Vendor A", "PURCHASE", new BigDecimal("1000.00"));
        var entry2 = buildEntryResponse(2L, 10L, "Vendor A", "CREDIT_NOTE", new BigDecimal("100.00"));
        var vendorGroup = buildVendorGroupResponse(10L, "Vendor A", List.of(entry1, entry2));

        when(registerService.getRegisterForVendor(TENANT, 10L))
            .thenReturn(vendorGroup);

        mockMvc.perform(get("/api/payment-register/vendor/10")
                .param("tenantId", TENANT))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.vendorAccountId").value(10))
            .andExpect(jsonPath("$.vendorName").value("Vendor A"))
            .andExpect(jsonPath("$.totalPurchases").value(1000.00))
            .andExpect(jsonPath("$.totalCreditNotes").value(100.00))
            .andExpect(jsonPath("$.netOutstanding").value(900.00))
            .andExpect(jsonPath("$.entries.length()").value(2));
    }

    @Test
    void getRegisterForVendor_noEntries_returnsEmptyGroup() throws Exception {
        var vendorGroup = new VendorGroupResponse(10L, "", List.of(),
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        when(registerService.getRegisterForVendor(TENANT, 10L))
            .thenReturn(vendorGroup);

        mockMvc.perform(get("/api/payment-register/vendor/10")
                .param("tenantId", TENANT))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.vendorAccountId").value(10))
            .andExpect(jsonPath("$.entries.length()").value(0))
            .andExpect(jsonPath("$.netOutstanding").value(0));
    }
}

