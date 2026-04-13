package com.nexus.onebook.accounts.service;

import com.nexus.onebook.cache.WarmCacheService;
import com.nexus.onebook.accounts.dto.LedgerAccountRequest;
import com.nexus.onebook.accounts.model.AccountType;
import com.nexus.onebook.accounts.model.CostCenter;
import com.nexus.onebook.accounts.model.LedgerAccount;
import com.nexus.onebook.accounts.repository.CostCenterRepository;
import com.nexus.onebook.accounts.repository.LedgerAccountRepository;
import com.nexus.onebook.security.AuditLogService;
import com.nexus.onebook.security.BlindIndexService;
import com.nexus.onebook.security.FieldEncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerAccountServiceTest {

    @Mock
    private LedgerAccountRepository accountRepository;

    @Mock
    private CostCenterRepository costCenterRepository;

    @Mock
    private FieldEncryptionService encryptionService;

    @Mock
    private BlindIndexService blindIndexService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private WarmCacheService warmCacheService;

    @InjectMocks
    private LedgerAccountService accountService;

    private CostCenter costCenter;

    @BeforeEach
    void setUp() {
        costCenter = new CostCenter();
        costCenter.setId(1L);
        costCenter.setTenantId("tenant-1");
        costCenter.setCode("CC-001");
        costCenter.setName("General");

        // Encryption stubs — passthrough for unit tests
        lenient().when(encryptionService.encrypt(anyString())).thenAnswer(inv -> "ENC:" + inv.getArgument(0));
        lenient().when(blindIndexService.generateBlindIndex(anyString())).thenReturn("blind-index-hash");

        // Cache stubs — simulate cold cache (cache misses) for unit tests
        lenient().when(warmCacheService.getAccountsByTenant(anyString())).thenReturn(null);
        lenient().when(warmCacheService.getAccountById(anyLong())).thenReturn(null);
    }

    @Test
    void createAccount_validRequest_succeeds() {
        when(costCenterRepository.findById(1L)).thenReturn(Optional.of(costCenter));
        when(accountRepository.findByTenantIdAndCostCenterIdAndAccountCode("tenant-1", 1L, "1000"))
                .thenReturn(Optional.empty());
        when(accountRepository.save(any(LedgerAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LedgerAccountRequest request = new LedgerAccountRequest(
                "tenant-1", 1L, "1000", "Cash", "ASSET", null, null);

        LedgerAccount result = accountService.createAccount(request);

        assertNotNull(result);
        assertEquals("1000", result.getAccountCode());
        assertEquals("Cash", result.getAccountName());
        assertEquals(AccountType.ASSET, result.getAccountType());
        assertEquals(costCenter, result.getCostCenter());
        verify(accountRepository).save(any(LedgerAccount.class));
    }

    @Test
    void createAccount_withParent_setsParentAccount() {
        LedgerAccount parentAccount = new LedgerAccount();
        parentAccount.setId(10L);
        parentAccount.setAccountCode("1000");

        when(costCenterRepository.findById(1L)).thenReturn(Optional.of(costCenter));
        when(accountRepository.findByTenantIdAndCostCenterIdAndAccountCode("tenant-1", 1L, "1010"))
                .thenReturn(Optional.empty());
        when(accountRepository.findById(10L)).thenReturn(Optional.of(parentAccount));
        when(accountRepository.save(any(LedgerAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LedgerAccountRequest request = new LedgerAccountRequest(
                "tenant-1", 1L, "1010", "Petty Cash", "ASSET", 10L, null);

        LedgerAccount result = accountService.createAccount(request);

        assertNotNull(result.getParentAccount());
        assertEquals(10L, result.getParentAccount().getId());
    }

    @Test
    void createAccount_withMetadata_setsMetadata() {
        when(costCenterRepository.findById(1L)).thenReturn(Optional.of(costCenter));
        when(accountRepository.findByTenantIdAndCostCenterIdAndAccountCode("tenant-1", 1L, "1000"))
                .thenReturn(Optional.empty());
        when(accountRepository.save(any(LedgerAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String metadata = "{\"department\": \"pharmacy\"}";
        LedgerAccountRequest request = new LedgerAccountRequest(
                "tenant-1", 1L, "1000", "Cash", "ASSET", null, metadata);

        LedgerAccount result = accountService.createAccount(request);

        assertEquals(metadata, result.getMetadata());
    }

    @Test
    void createAccount_duplicateCode_throws() {
        LedgerAccount existing = new LedgerAccount();
        existing.setAccountCode("1000");

        when(costCenterRepository.findById(1L)).thenReturn(Optional.of(costCenter));
        when(accountRepository.findByTenantIdAndCostCenterIdAndAccountCode("tenant-1", 1L, "1000"))
                .thenReturn(Optional.of(existing));

        LedgerAccountRequest request = new LedgerAccountRequest(
                "tenant-1", 1L, "1000", "Cash", "ASSET", null, null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.createAccount(request)
        );
        assertTrue(ex.getMessage().contains("already exists"));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void createAccount_invalidCostCenter_throws() {
        when(costCenterRepository.findById(999L)).thenReturn(Optional.empty());

        LedgerAccountRequest request = new LedgerAccountRequest(
                "tenant-1", 999L, "1000", "Cash", "ASSET", null, null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.createAccount(request)
        );
        assertTrue(ex.getMessage().contains("Cost center not found"));
    }

    @Test
    void createAccount_invalidParentAccount_throws() {
        when(costCenterRepository.findById(1L)).thenReturn(Optional.of(costCenter));
        when(accountRepository.findByTenantIdAndCostCenterIdAndAccountCode("tenant-1", 1L, "1010"))
                .thenReturn(Optional.empty());
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        LedgerAccountRequest request = new LedgerAccountRequest(
                "tenant-1", 1L, "1010", "Petty Cash", "ASSET", 999L, null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.createAccount(request)
        );
        assertTrue(ex.getMessage().contains("Parent account not found"));
    }

    @Test
    void createAccount_invalidAccountType_throws() {
        when(costCenterRepository.findById(1L)).thenReturn(Optional.of(costCenter));
        when(accountRepository.findByTenantIdAndCostCenterIdAndAccountCode("tenant-1", 1L, "1000"))
                .thenReturn(Optional.empty());

        LedgerAccountRequest request = new LedgerAccountRequest(
                "tenant-1", 1L, "1000", "Cash", "INVALID", null, null);

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.createAccount(request)
        );
    }

    @Test
    void getAccountsByTenant_returnsList() {
        LedgerAccount account = new LedgerAccount();
        account.setAccountCode("1000");
        when(accountRepository.findByTenantId("tenant-1")).thenReturn(List.of(account));

        List<LedgerAccount> result = accountService.getAccountsByTenant("tenant-1");

        assertEquals(1, result.size());
        assertEquals("1000", result.get(0).getAccountCode());
    }

    @Test
    void getAccount_found_returns() {
        LedgerAccount account = new LedgerAccount();
        account.setId(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        LedgerAccount result = accountService.getAccount(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getAccount_notFound_throws() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.getAccount(999L)
        );
    }
}
