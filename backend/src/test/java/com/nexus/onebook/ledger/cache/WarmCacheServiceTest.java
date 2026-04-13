package com.nexus.onebook.ledger.cache;

import com.nexus.onebook.ledger.accounts.dto.TrialBalanceLine;
import com.nexus.onebook.ledger.accounts.dto.TrialBalanceReport;
import com.nexus.onebook.ledger.accounts.model.AccountType;
import com.nexus.onebook.ledger.accounts.model.LedgerAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarmCacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private WarmCacheService warmCacheService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        warmCacheService = new WarmCacheService(redisTemplate);
    }

    // ── Cache-Aside read tests ──

    @Test
    void getAccountsByTenant_cacheHit_returnsList() {
        LedgerAccount account = new LedgerAccount();
        account.setId(1L);
        account.setAccountCode("1000");
        List<LedgerAccount> accounts = List.of(account);

        when(valueOperations.get(CacheConstants.ACCOUNTS_BY_TENANT + "tenant-1"))
                .thenReturn(accounts);

        List<LedgerAccount> result = warmCacheService.getAccountsByTenant("tenant-1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1000", result.get(0).getAccountCode());
    }

    @Test
    void getAccountsByTenant_cacheMiss_returnsNull() {
        when(valueOperations.get(CacheConstants.ACCOUNTS_BY_TENANT + "tenant-1"))
                .thenReturn(null);

        assertNull(warmCacheService.getAccountsByTenant("tenant-1"));
    }

    @Test
    void getAccountById_cacheHit_returnsAccount() {
        LedgerAccount account = new LedgerAccount();
        account.setId(1L);
        account.setAccountCode("1000");

        when(valueOperations.get(CacheConstants.ACCOUNT_BY_ID + 1L))
                .thenReturn(account);

        LedgerAccount result = warmCacheService.getAccountById(1L);

        assertNotNull(result);
        assertEquals("1000", result.getAccountCode());
    }

    @Test
    void getAccountById_cacheMiss_returnsNull() {
        when(valueOperations.get(CacheConstants.ACCOUNT_BY_ID + 999L))
                .thenReturn(null);

        assertNull(warmCacheService.getAccountById(999L));
    }

    @Test
    void getTrialBalance_cacheHit_returnsReport() {
        TrialBalanceReport report = new TrialBalanceReport(
                "tenant-1",
                List.of(new TrialBalanceLine(1L, "1000", "Cash", "ASSET",
                        new BigDecimal("500"), BigDecimal.ZERO)),
                new BigDecimal("500"),
                BigDecimal.ZERO,
                false
        );

        when(valueOperations.get(CacheConstants.TRIAL_BALANCE + "tenant-1"))
                .thenReturn(report);

        TrialBalanceReport result = warmCacheService.getTrialBalance("tenant-1");

        assertNotNull(result);
        assertEquals("tenant-1", result.tenantId());
    }

    @Test
    void getTrialBalance_cacheMiss_returnsNull() {
        when(valueOperations.get(CacheConstants.TRIAL_BALANCE + "tenant-1"))
                .thenReturn(null);

        assertNull(warmCacheService.getTrialBalance("tenant-1"));
    }

    // ── Write-through put tests ──

    @Test
    void putAccountsByTenant_cachesWithTTL() {
        LedgerAccount account = new LedgerAccount();
        account.setId(1L);
        List<LedgerAccount> accounts = List.of(account);

        warmCacheService.putAccountsByTenant("tenant-1", accounts);

        verify(valueOperations).set(
                eq(CacheConstants.ACCOUNTS_BY_TENANT + "tenant-1"),
                eq(accounts),
                eq(CacheConstants.DEFAULT_TTL_MINUTES),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    void putAccount_cachesWithTTL() {
        LedgerAccount account = new LedgerAccount();
        account.setId(42L);

        warmCacheService.putAccount(account);

        verify(valueOperations).set(
                eq(CacheConstants.ACCOUNT_BY_ID + 42L),
                eq(account),
                eq(CacheConstants.DEFAULT_TTL_MINUTES),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    void putTrialBalance_cachesWithShorterTTL() {
        TrialBalanceReport report = new TrialBalanceReport(
                "tenant-1", List.of(), BigDecimal.ZERO, BigDecimal.ZERO, true);

        warmCacheService.putTrialBalance("tenant-1", report);

        verify(valueOperations).set(
                eq(CacheConstants.TRIAL_BALANCE + "tenant-1"),
                eq(report),
                eq(CacheConstants.TRIAL_BALANCE_TTL_MINUTES),
                eq(TimeUnit.MINUTES)
        );
    }

    // ── Eviction tests ──

    @Test
    void evictAccountsByTenant_deletesKey() {
        warmCacheService.evictAccountsByTenant("tenant-1");
        verify(redisTemplate).delete(CacheConstants.ACCOUNTS_BY_TENANT + "tenant-1");
    }

    @Test
    void evictAccount_deletesKey() {
        warmCacheService.evictAccount(42L);
        verify(redisTemplate).delete(CacheConstants.ACCOUNT_BY_ID + 42L);
    }

    @Test
    void evictTrialBalance_deletesKey() {
        warmCacheService.evictTrialBalance("tenant-1");
        verify(redisTemplate).delete(CacheConstants.TRIAL_BALANCE + "tenant-1");
    }

    @Test
    void evictAll_deletesAllTenantKeys() {
        warmCacheService.evictAll("tenant-1");

        verify(redisTemplate).delete(CacheConstants.ACCOUNTS_BY_TENANT + "tenant-1");
        verify(redisTemplate).delete(CacheConstants.TRIAL_BALANCE + "tenant-1");
        verify(redisTemplate).delete(CacheConstants.WARM_MARKER + "tenant-1");
    }

    // ── Warm marker tests ──

    @Test
    void markWarm_setsMarkerWithTTL() {
        warmCacheService.markWarm("tenant-1");

        verify(valueOperations).set(
                eq(CacheConstants.WARM_MARKER + "tenant-1"),
                eq(Boolean.TRUE),
                eq(CacheConstants.DEFAULT_TTL_MINUTES),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    void isCacheWarm_markerExists_returnsTrue() {
        when(redisTemplate.hasKey(CacheConstants.WARM_MARKER + "tenant-1"))
                .thenReturn(true);

        assertTrue(warmCacheService.isCacheWarm("tenant-1"));
    }

    @Test
    void isCacheWarm_markerAbsent_returnsFalse() {
        when(redisTemplate.hasKey(CacheConstants.WARM_MARKER + "tenant-1"))
                .thenReturn(false);

        assertFalse(warmCacheService.isCacheWarm("tenant-1"));
    }

    // ── Failure-safety tests ──

    @Test
    void getAccountsByTenant_redisDown_returnsNull() {
        when(valueOperations.get(anyString()))
                .thenThrow(new RuntimeException("Redis connection refused"));

        assertNull(warmCacheService.getAccountsByTenant("tenant-1"));
    }

    @Test
    void putAccountsByTenant_redisDown_doesNotThrow() {
        doThrow(new RuntimeException("Redis connection refused"))
                .when(valueOperations).set(anyString(), any(), anyLong(), any(TimeUnit.class));

        assertDoesNotThrow(() ->
                warmCacheService.putAccountsByTenant("tenant-1", List.of()));
    }

    @Test
    void evictTrialBalance_redisDown_doesNotThrow() {
        doThrow(new RuntimeException("Redis connection refused"))
                .when(redisTemplate).delete(anyString());

        assertDoesNotThrow(() -> warmCacheService.evictTrialBalance("tenant-1"));
    }

    @Test
    void isCacheWarm_redisDown_returnsFalse() {
        when(redisTemplate.hasKey(anyString()))
                .thenThrow(new RuntimeException("Redis connection refused"));

        assertFalse(warmCacheService.isCacheWarm("tenant-1"));
    }
}
