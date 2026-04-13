package com.nexus.onebook.ledger.accounts.controller;

import com.nexus.onebook.ledger.accounts.dto.TrialBalanceLine;
import com.nexus.onebook.ledger.accounts.dto.TrialBalanceReport;
import com.nexus.onebook.ledger.exception.GlobalExceptionHandler;
import com.nexus.onebook.ledger.accounts.model.AccountType;
import com.nexus.onebook.ledger.accounts.model.LedgerAccount;
import com.nexus.onebook.ledger.accounts.service.LedgerAccountService;
import com.nexus.onebook.ledger.accounts.service.TrialBalanceService;
import com.nexus.onebook.ledger.accounts.repository.CostCenterRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LedgerController.class)
@Import(GlobalExceptionHandler.class)
class LedgerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LedgerAccountService accountService;

    @MockitoBean
    private TrialBalanceService trialBalanceService;

    @MockitoBean
    private CostCenterRepository costCenterRepository;

    @Test
    void createAccount_validRequest_returns201() throws Exception {
        LedgerAccount account = new LedgerAccount();
        account.setId(1L);
        account.setAccountCode("1000");
        account.setAccountName("Cash");
        account.setAccountType(AccountType.ASSET);
        account.setTenantId("tenant-1");

        when(accountService.createAccount(any())).thenReturn(account);

        mockMvc.perform(post("/api/ledger/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "tenant-1",
                                    "costCenterId": 1,
                                    "accountCode": "1000",
                                    "accountName": "Cash",
                                    "accountType": "ASSET"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountCode").value("1000"))
                .andExpect(jsonPath("$.accountName").value("Cash"));
    }

    @Test
    void createAccount_missingFields_returns400() throws Exception {
        mockMvc.perform(post("/api/ledger/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "",
                                    "accountCode": "",
                                    "accountName": "",
                                    "accountType": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAccount_duplicateCode_returns400() throws Exception {
        when(accountService.createAccount(any()))
                .thenThrow(new IllegalArgumentException("Account code already exists: 1000"));

        mockMvc.perform(post("/api/ledger/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "tenant-1",
                                    "costCenterId": 1,
                                    "accountCode": "1000",
                                    "accountName": "Cash",
                                    "accountType": "ASSET"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account code already exists: 1000"));
    }

    @Test
    void getAccounts_returnsList() throws Exception {
        LedgerAccount account = new LedgerAccount();
        account.setId(1L);
        account.setAccountCode("1000");
        account.setAccountName("Cash");
        account.setAccountType(AccountType.ASSET);
        account.setTenantId("tenant-1");

        when(accountService.getAccountsByTenant("tenant-1")).thenReturn(List.of(account));

        mockMvc.perform(get("/api/ledger/accounts")
                        .param("tenantId", "tenant-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountCode").value("1000"));
    }

    @Test
    void getTrialBalance_returnsReport() throws Exception {
        TrialBalanceReport report = new TrialBalanceReport(
                "tenant-1",
                List.of(
                        new TrialBalanceLine(1L, "1000", "Cash", "ASSET",
                                new BigDecimal("500.0000"), BigDecimal.ZERO),
                        new TrialBalanceLine(2L, "4000", "Revenue", "REVENUE",
                                BigDecimal.ZERO, new BigDecimal("500.0000"))
                ),
                new BigDecimal("500.0000"),
                new BigDecimal("500.0000"),
                true
        );

        when(trialBalanceService.generateTrialBalance("tenant-1", null, null)).thenReturn(report);

        mockMvc.perform(get("/api/ledger/trial-balance")
                        .param("tenantId", "tenant-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("tenant-1"))
                .andExpect(jsonPath("$.balanced").value(true))
                .andExpect(jsonPath("$.lines").isArray())
                .andExpect(jsonPath("$.lines.length()").value(2));
    }
}
