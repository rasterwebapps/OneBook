package com.nexus.onebook.cache;

import com.nexus.onebook.accounts.dto.TrialBalanceReport;
import com.nexus.onebook.accounts.model.LedgerAccount;
import com.nexus.onebook.accounts.service.LedgerAccountService;
import com.nexus.onebook.accounts.service.TrialBalanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for the Warm Cache lifecycle.
 * <p>
 * Typical flow:
 * <ol>
 *   <li>Client authenticates → calls {@code POST /api/cache/warm/{tenantId}}</li>
 *   <li>Server decrypts the tenant's working set into Redis.</li>
 *   <li>All subsequent API calls hit the warm cache (sub-100 ms).</li>
 *   <li>On logout → calls {@code DELETE /api/cache/evict/{tenantId}}</li>
 * </ol>
 */
@RestController
@RequestMapping("/api/cache")
public class WarmCacheController {

    private static final Logger log = LoggerFactory.getLogger(WarmCacheController.class);

    private final WarmCacheService warmCacheService;
    private final LedgerAccountService accountService;
    private final TrialBalanceService trialBalanceService;

    public WarmCacheController(WarmCacheService warmCacheService,
                               LedgerAccountService accountService,
                               TrialBalanceService trialBalanceService) {
        this.warmCacheService = warmCacheService;
        this.accountService = accountService;
        this.trialBalanceService = trialBalanceService;
    }

    /**
     * Warms the Redis cache for a tenant on login.
     * Decrypts the active working set and populates the cache.
     */
    @PostMapping("/warm/{tenantId}")
    public ResponseEntity<Map<String, Object>> warmCache(@PathVariable String tenantId) {
        log.info("Warming cache for tenant {}", tenantId);

        // 1. Populate accounts (triggers cache-aside in service)
        List<LedgerAccount> accounts = accountService.getAccountsByTenant(tenantId);

        // 2. Cache individual accounts
        for (LedgerAccount account : accounts) {
            warmCacheService.putAccount(account);
        }

        // 3. Populate trial balance (triggers cache-aside in service)
        trialBalanceService.generateTrialBalance(tenantId);

        // 4. Mark as warmed
        warmCacheService.markWarm(tenantId);

        log.info("Cache warmed for tenant {} — {} accounts cached", tenantId, accounts.size());

        return ResponseEntity.ok(Map.of(
                "tenantId", tenantId,
                "status", "warmed",
                "accountsCached", accounts.size()
        ));
    }

    /**
     * Reports the warm-cache status for a tenant.
     */
    @GetMapping("/status/{tenantId}")
    public ResponseEntity<Map<String, Object>> cacheStatus(@PathVariable String tenantId) {
        boolean warm = warmCacheService.isCacheWarm(tenantId);
        return ResponseEntity.ok(Map.of(
                "tenantId", tenantId,
                "warm", warm
        ));
    }

    /**
     * Evicts all cached data for a tenant on logout / session expiry.
     */
    @DeleteMapping("/evict/{tenantId}")
    public ResponseEntity<Map<String, Object>> evictCache(@PathVariable String tenantId) {
        warmCacheService.evictAll(tenantId);
        return ResponseEntity.ok(Map.of(
                "tenantId", tenantId,
                "status", "evicted"
        ));
    }
}
