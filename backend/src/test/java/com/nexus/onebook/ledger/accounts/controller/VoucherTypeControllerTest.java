package com.nexus.onebook.ledger.accounts.controller;

import com.nexus.onebook.ledger.exception.GlobalExceptionHandler;
import com.nexus.onebook.ledger.accounts.model.VoucherCategory;
import com.nexus.onebook.ledger.accounts.model.VoucherType;
import com.nexus.onebook.ledger.accounts.service.VoucherTypeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VoucherTypeController.class)
@Import(GlobalExceptionHandler.class)
class VoucherTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VoucherTypeService voucherTypeService;

    @Test
    void create_validRequest_returns201() throws Exception {
        VoucherType vt = new VoucherType("t1", "SV", "Sales Voucher", VoucherCategory.SALES);
        vt.setId(1L);

        when(voucherTypeService.createVoucherType("t1", "SV", "Sales Voucher", "SALES"))
                .thenReturn(vt);

        mockMvc.perform(post("/api/voucher-types")
                        .param("tenantId", "t1")
                        .param("voucherCode", "SV")
                        .param("voucherName", "Sales Voucher")
                        .param("category", "SALES"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.voucherCode").value("SV"))
                .andExpect(jsonPath("$.voucherName").value("Sales Voucher"))
                .andExpect(jsonPath("$.category").value("SALES"));
    }

    @Test
    void create_duplicateCode_returns400() throws Exception {
        when(voucherTypeService.createVoucherType(any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Voucher type 'SV' already exists"));

        mockMvc.perform(post("/api/voucher-types")
                        .param("tenantId", "t1")
                        .param("voucherCode", "SV")
                        .param("voucherName", "Sales Voucher")
                        .param("category", "SALES"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Voucher type 'SV' already exists"));
    }

    @Test
    void getAll_returnsList() throws Exception {
        VoucherType vt = new VoucherType("t1", "SV", "Sales Voucher", VoucherCategory.SALES);
        vt.setId(1L);

        when(voucherTypeService.getVoucherTypes("t1")).thenReturn(List.of(vt));

        mockMvc.perform(get("/api/voucher-types")
                        .param("tenantId", "t1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].voucherCode").value("SV"));
    }

    @Test
    void getByCategory_returnsList() throws Exception {
        VoucherType vt = new VoucherType("t1", "SV", "Sales Voucher", VoucherCategory.SALES);
        vt.setId(1L);

        when(voucherTypeService.getVoucherTypesByCategory("t1", VoucherCategory.SALES))
                .thenReturn(List.of(vt));

        mockMvc.perform(get("/api/voucher-types/by-category")
                        .param("tenantId", "t1")
                        .param("category", "SALES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].voucherCode").value("SV"))
                .andExpect(jsonPath("$[0].category").value("SALES"));
    }
}
