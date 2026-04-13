package com.nexus.onebook.ledger.cache;

import com.nexus.onebook.ledger.exception.GlobalExceptionHandler;
import com.nexus.onebook.ledger.accounts.model.AccountType;
import com.nexus.onebook.ledger.accounts.model.LedgerAccount;
import com.nexus.onebook.ledger.accounts.service.LedgerAccountService;
import com.nexus.onebook.ledger.accounts.service.TrialBalanceService;
import com.nexus.onebook.ledger.accounts.dto.TrialBalanceReport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WarmCacheController.class)
@Import(GlobalExceptionHandler.class)
class WarmCacheControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WarmCacheService warmCacheService;

    @MockitoBean
    private LedgerAccountService accountService;

    @MockitoBean
    private TrialBalanceService trialBalanceService;

    @Test
    void warmCache_populatesTenantCache_returns200() throws Exception {
        LedgerAccount account = new LedgerAccount();
        account.setId(1L);
        account.setAccountCode("1000");
        account.setAccountName("Cash");
        account.setAccountType(AccountType.ASSET);

        when(accountService.getAccountsByTenant("tenant-1"))
                .thenReturn(List.of(account));
        when(trialBalanceService.generateTrialBalance("tenant-1"))
                .thenReturn(new TrialBalanceReport(
                        "tenant-1", List.of(), BigDecimal.ZERO, BigDecimal.ZERO, true));

        mockMvc.perform(post("/api/cache/warm/tenant-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("tenant-1"))
                .andExpect(jsonPath("$.status").value("warmed"))
                .andExpect(jsonPath("$.accountsCached").value(1));

        verify(warmCacheService).putAccount(account);
        verify(warmCacheService).markWarm("tenant-1");
    }

    @Test
    void cacheStatus_warm_returnsTrue() throws Exception {
        when(warmCacheService.isCacheWarm("tenant-1")).thenReturn(true);

        mockMvc.perform(get("/api/cache/status/tenant-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("tenant-1"))
                .andExpect(jsonPath("$.warm").value(true));
    }

    @Test
    void cacheStatus_cold_returnsFalse() throws Exception {
        when(warmCacheService.isCacheWarm("tenant-1")).thenReturn(false);

        mockMvc.perform(get("/api/cache/status/tenant-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.warm").value(false));
    }

    @Test
    void evictCache_clearsAllTenantData_returns200() throws Exception {
        mockMvc.perform(delete("/api/cache/evict/tenant-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("tenant-1"))
                .andExpect(jsonPath("$.status").value("evicted"));

        verify(warmCacheService).evictAll("tenant-1");
    }
}
