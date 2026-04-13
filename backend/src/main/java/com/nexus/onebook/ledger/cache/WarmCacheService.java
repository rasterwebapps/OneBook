package com.nexus.onebook.ledger.cache;

import com.nexus.onebook.ledger.accounts.dto.TrialBalanceReport;
import com.nexus.onebook.ledger.accounts.model.LedgerAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis "Warm Cache" facade for Milestone 4.
 * <p>
 * Provides pure cache operations (get / put / evict) against Redis.
 * Business services inject this to implement the cache-aside (lazy read)
 * and write-through (eager invalidation) patterns.
 * <p>
 * All operations are failure-safe: if Redis is unavailable the application
 * degrades gracefully to DB-only mode with a warning log.
 */
@Service
public class WarmCacheService {

    private static final Logger log = LoggerFactory.getLogger(WarmCacheService.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public WarmCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ──────────────────────────────────────────────
    //  Warm marker
    // ──────────────────────────────────────────────

    /**
     * Marks a tenant's cache as warmed.
     */
    public void markWarm(String tenantId) {
        try {
            redisTemplate.opsForValue().set(
                    CacheConstants.WARM_MARKER + tenantId,
                    Boolean.TRUE,
                    CacheConstants.DEFAULT_TTL_MINUTES,
                    TimeUnit.MINUTES
            );
        } catch (Exception e) {
            log.warn("Redis unavailable — skipping warm marker for tenant {}: {}", tenantId, e.getMessage());
        }
    }

    /**
     * Returns {@code true} if the tenant's cache has been warmed
     * and has not yet expired.
     */
    public boolean isCacheWarm(String tenantId) {
        try {
            return Boolean.TRUE.equals(
                    redisTemplate.hasKey(CacheConstants.WARM_MARKER + tenantId));
        } catch (Exception e) {
            log.warn("Redis unavailable — cache warm check failed for tenant {}: {}", tenantId, e.getMessage());
            return false;
        }
    }

    // ──────────────────────────────────────────────
    //  Cache-Aside reads
    // ──────────────────────────────────────────────

    /**
     * Gets the cached account list for a tenant, or {@code null} on cache miss.
     */
    @SuppressWarnings("unchecked")
    public List<LedgerAccount> getAccountsByTenant(String tenantId) {
        try {
            Object cached = redisTemplate.opsForValue()
                    .get(CacheConstants.ACCOUNTS_BY_TENANT + tenantId);
            return cached != null ? (List<LedgerAccount>) cached : null;
        } catch (Exception e) {
            log.warn("Redis unavailable — cache miss for tenant accounts {}: {}", tenantId, e.getMessage());
            return null;
        }
    }

    /**
     * Gets a cached individual account by id, or {@code null} on cache miss.
     */
    public LedgerAccount getAccountById(Long accountId) {
        try {
            Object cached = redisTemplate.opsForValue()
                    .get(CacheConstants.ACCOUNT_BY_ID + accountId);
            return cached != null ? (LedgerAccount) cached : null;
        } catch (Exception e) {
            log.warn("Redis unavailable — cache miss for account {}: {}", accountId, e.getMessage());
            return null;
        }
    }

    /**
     * Gets the cached trial balance for a tenant, or {@code null} on cache miss.
     */
    public TrialBalanceReport getTrialBalance(String tenantId) {
        try {
            Object cached = redisTemplate.opsForValue()
                    .get(CacheConstants.TRIAL_BALANCE + tenantId);
            return cached != null ? (TrialBalanceReport) cached : null;
        } catch (Exception e) {
            log.warn("Redis unavailable — cache miss for trial balance {}: {}", tenantId, e.getMessage());
            return null;
        }
    }

    // ──────────────────────────────────────────────
    //  Write-through puts
    // ──────────────────────────────────────────────

    /**
     * Caches the full account list for a tenant (write-through).
     */
    public void putAccountsByTenant(String tenantId, List<LedgerAccount> accounts) {
        try {
            redisTemplate.opsForValue().set(
                    CacheConstants.ACCOUNTS_BY_TENANT + tenantId,
                    accounts,
                    CacheConstants.DEFAULT_TTL_MINUTES,
                    TimeUnit.MINUTES
            );
        } catch (Exception e) {
            log.warn("Redis unavailable — skipping cache put for tenant accounts {}: {}", tenantId, e.getMessage());
        }
    }

    /**
     * Caches an individual account (write-through).
     */
    public void putAccount(LedgerAccount account) {
        try {
            redisTemplate.opsForValue().set(
                    CacheConstants.ACCOUNT_BY_ID + account.getId(),
                    account,
                    CacheConstants.DEFAULT_TTL_MINUTES,
                    TimeUnit.MINUTES
            );
        } catch (Exception e) {
            log.warn("Redis unavailable — skipping cache put for account {}: {}", account.getId(), e.getMessage());
        }
    }

    /**
     * Caches a trial balance report (write-through).
     */
    public void putTrialBalance(String tenantId, TrialBalanceReport report) {
        try {
            redisTemplate.opsForValue().set(
                    CacheConstants.TRIAL_BALANCE + tenantId,
                    report,
                    CacheConstants.TRIAL_BALANCE_TTL_MINUTES,
                    TimeUnit.MINUTES
            );
        } catch (Exception e) {
            log.warn("Redis unavailable — skipping cache put for trial balance {}: {}", tenantId, e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    //  Invalidation
    // ──────────────────────────────────────────────

    /**
     * Invalidates the tenant's account list cache.
     * Called after account creation to force a fresh read.
     */
    public void evictAccountsByTenant(String tenantId) {
        try {
            redisTemplate.delete(CacheConstants.ACCOUNTS_BY_TENANT + tenantId);
        } catch (Exception e) {
            log.warn("Redis unavailable — skipping eviction for tenant accounts {}: {}", tenantId, e.getMessage());
        }
    }

    /**
     * Invalidates a cached individual account.
     */
    public void evictAccount(Long accountId) {
        try {
            redisTemplate.delete(CacheConstants.ACCOUNT_BY_ID + accountId);
        } catch (Exception e) {
            log.warn("Redis unavailable — skipping eviction for account {}: {}", accountId, e.getMessage());
        }
    }

    /**
     * Invalidates the tenant's trial balance cache.
     * Called after any journal transaction is posted.
     */
    public void evictTrialBalance(String tenantId) {
        try {
            redisTemplate.delete(CacheConstants.TRIAL_BALANCE + tenantId);
        } catch (Exception e) {
            log.warn("Redis unavailable — skipping eviction for trial balance {}: {}", tenantId, e.getMessage());
        }
    }

    /**
     * Evicts all cached data for a tenant.
     * Called at logout or session expiry.
     * <p>
     * Individual account entries (by id) are left to expire naturally via TTL
     * because their keys are not tenant-scoped and a {@code KEYS} scan would
     * block Redis and accidentally affect other tenants.
     *
     * @param tenantId the tenant whose cache should be cleared
     */
    public void evictAll(String tenantId) {
        log.info("Evicting all cache entries for tenant {}", tenantId);
        evictAccountsByTenant(tenantId);
        evictTrialBalance(tenantId);
        try {
            redisTemplate.delete(CacheConstants.WARM_MARKER + tenantId);
        } catch (Exception e) {
            log.warn("Redis unavailable — skipping warm marker eviction for tenant {}: {}", tenantId, e.getMessage());
        }
    }
}
