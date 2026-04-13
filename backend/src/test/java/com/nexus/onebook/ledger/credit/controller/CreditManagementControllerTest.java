package com.nexus.onebook.ledger.credit.controller;

import com.nexus.onebook.ledger.exception.GlobalExceptionHandler;
import com.nexus.onebook.ledger.credit.model.CreditLimit;
import com.nexus.onebook.ledger.credit.service.CreditManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CreditManagementController.class)
@Import(GlobalExceptionHandler.class)
class CreditManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreditManagementService creditManagementService;

    @Test
    void setCreditLimit_validRequest_returns201() throws Exception {
        CreditLimit cl = new CreditLimit();
        cl.setId(1L);
        cl.setTenantId("t1");
        cl.setCreditLimit(new BigDecimal("50000"));
        cl.setCreditPeriodDays(30);

        when(creditManagementService.setCreditLimit(any())).thenReturn(cl);

        mockMvc.perform(post("/api/credit-management")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "t1",
                                    "accountId": 1,
                                    "creditLimit": 50000,
                                    "creditPeriodDays": 30
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.creditLimit").value(50000))
                .andExpect(jsonPath("$.creditPeriodDays").value(30));
    }

    @Test
    void getCreditLimit_returnsOk() throws Exception {
        CreditLimit cl = new CreditLimit();
        cl.setId(1L);
        cl.setTenantId("t1");
        cl.setCreditLimit(new BigDecimal("50000"));

        when(creditManagementService.getCreditLimit(1L)).thenReturn(cl);

        mockMvc.perform(get("/api/credit-management/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creditLimit").value(50000));
    }

    @Test
    void checkCredit_returnsTrue() throws Exception {
        when(creditManagementService.checkCreditAvailability("t1", 1L, new BigDecimal("10000")))
                .thenReturn(true);

        mockMvc.perform(get("/api/credit-management/check")
                        .param("tenantId", "t1")
                        .param("accountId", "1")
                        .param("amount", "10000"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void getBlockedAccounts_returnsList() throws Exception {
        CreditLimit cl = new CreditLimit();
        cl.setId(1L);
        cl.setTenantId("t1");
        cl.setBlocked(true);

        when(creditManagementService.getBlockedAccounts("t1")).thenReturn(List.of(cl));

        mockMvc.perform(get("/api/credit-management/blocked")
                        .param("tenantId", "t1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].blocked").value(true));
    }
}
