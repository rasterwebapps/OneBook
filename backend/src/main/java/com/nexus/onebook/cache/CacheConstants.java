package com.nexus.onebook.cache;

/**
 * Constants for Redis cache key prefixes and default TTLs.
 * Key schema: {@code onebook:cache:<tenant>:<domain>:<qualifier>}
 */
public final class CacheConstants {

    private CacheConstants() {
        // utility class
    }

    /** Root namespace for all OneBook cache keys. */
    public static final String PREFIX = "onebook:cache:";

    /** Cached list of all accounts for a tenant. */
    public static final String ACCOUNTS_BY_TENANT = PREFIX + "accounts:tenant:";

    /** Cached individual account by id. */
    public static final String ACCOUNT_BY_ID = PREFIX + "account:id:";

    /** Cached trial balance report for a tenant. */
    public static final String TRIAL_BALANCE = PREFIX + "trial-balance:tenant:";

    /** Marker indicating a tenant's working set has been warmed. */
    public static final String WARM_MARKER = PREFIX + "warm:tenant:";

    /** Default TTL in minutes for cached data. */
    public static final long DEFAULT_TTL_MINUTES = 30;

    /** Default TTL in minutes for trial balance cache (shorter due to volatility). */
    public static final long TRIAL_BALANCE_TTL_MINUTES = 10;
}
