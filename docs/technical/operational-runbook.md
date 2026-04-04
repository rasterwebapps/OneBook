# OneBook — Operational Runbook

> Procedures for deploying, operating, monitoring, and troubleshooting the Nexus Universal Accounting OS.

---

## Table of Contents

1. [Infrastructure Overview](#infrastructure-overview)
2. [Deployment](#deployment)
3. [Configuration](#configuration)
4. [Monitoring & Health Checks](#monitoring--health-checks)
5. [Database Operations](#database-operations)
6. [Cache Operations](#cache-operations)
7. [Security Operations](#security-operations)
8. [Troubleshooting](#troubleshooting)
9. [Backup & Recovery](#backup--recovery)
10. [Performance Tuning](#performance-tuning)

---

## Infrastructure Overview

### Components

| Component | Technology | Default Port | Purpose |
|-----------|-----------|-------------|---------|
| Backend API | Java 21 / Spring Boot 3.4+ | 8080 | REST API with Virtual Threads |
| Frontend | Angular 21+ / Nginx | 80 (Docker) / 4200 (dev) | Single Page Application |
| Database | PostgreSQL 17+ | 5433 → 5432 | Encrypted ledger with RLS |
| Cache | Redis 7+ | 6379 | Warm cache for active sessions |
| Identity | Keycloak 24 | 8180 | OIDC / OAuth 2.0 provider |
| Directory | OpenLDAP 1.5 | 389, 636 | User directory (Keycloak federation) |
| LDAP Admin | phpLDAPadmin | 8081 | LDAP management UI (dev only) |

### System Requirements

| Resource | Minimum | Recommended |
|----------|---------|-------------|
| CPU | 2 cores | 4+ cores |
| Memory | 4 GB | 8+ GB |
| Storage | 20 GB | 100+ GB (SSD) |
| Java | 21+ | 21+ (LTS) |
| Node.js | 20+ | 20+ (LTS) |

---

## Deployment

### Local Development

```bash
# 1. Start infrastructure only (for local development)
docker compose up -d postgres redis openldap keycloak

# Or start full stack (all services)
docker compose up -d

# 2. Verify infrastructure
docker compose ps
curl -s http://localhost:5432 || echo "PostgreSQL running"
redis-cli -p 6379 ping  # Should return PONG

# 3. Start backend
cd backend
./gradlew bootRun

# 4. Start frontend (separate terminal)
cd frontend
npm install
npm start
```

### Docker Compose Services

The `docker-compose.yml` at the repository root orchestrates all 7 services:

| Service | Image | Port | Purpose |
|---------|-------|------|---------|
| `postgres` | `postgres:17-alpine` | 5433 → 5432 | Primary data store with RLS |
| `redis` | `redis:7-alpine` | 6379 | Warm cache (Cache-Aside) |
| `openldap` | `osixia/openldap:1.5.0` | 389, 636 | User directory |
| `ldapadmin` | `osixia/phpldapadmin:0.9.0` | 8081 | LDAP UI (dev only) |
| `keycloak` | `quay.io/keycloak/keycloak:24.0` | 8180 | OIDC identity provider |
| `backend` | `backend/Dockerfile` | 8080 | Spring Boot API |
| `frontend` | `frontend/Dockerfile` | 80 | Angular SPA (Nginx) |

Service configs live in `infrastructure/` — see [`infrastructure/README.md`](../infrastructure/README.md).

### Production Deployment Checklist

- [ ] Set `ONEBOOK_SECURITY_ENCRYPTION_MASTER_KEY` environment variable (64-char hex)
- [ ] Update `spring.datasource.password` to a strong production password
- [ ] Configure Redis authentication (`requirepass`)
- [ ] Set `spring.jpa.hibernate.ddl-auto` to `validate` (default)
- [ ] Enable SSL/TLS for PostgreSQL connections
- [ ] Configure CORS allowed origins (restrict from `*`)
- [ ] Review and restrict Actuator endpoint exposure
- [ ] Set up log aggregation (structured JSON logging)
- [ ] Configure database connection pooling (HikariCP)
- [ ] Verify Flyway migrations complete successfully on startup

---

## Configuration

### Application Configuration

The primary configuration file is `backend/src/main/resources/application.yml`.

#### Key Settings

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 8080 | API server port |
| `spring.threads.virtual.enabled` | true | Enable Project Loom Virtual Threads |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/onebook` | Database URL |
| `spring.jpa.hibernate.ddl-auto` | validate | Schema validation mode |
| `spring.flyway.enabled` | true | Enable Flyway migrations |
| `spring.data.redis.host` | localhost | Redis host |
| `spring.data.redis.port` | 6379 | Redis port |
| `spring.data.redis.timeout` | 2000ms | Redis connection timeout |
| `onebook.security.encryption.master-key` | (env var) | AES-256-GCM master key |
| `onebook.security.encryption.key-version` | 1 | Current encryption key version |

#### Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `ONEBOOK_SECURITY_ENCRYPTION_MASTER_KEY` | Yes (prod) | 64-character hex string for AES-256 encryption |
| `SPRING_DATASOURCE_URL` | No | Override database URL |
| `SPRING_DATASOURCE_PASSWORD` | No | Override database password |
| `SPRING_DATA_REDIS_HOST` | No | Override Redis host |

---

## Monitoring & Health Checks

### Health Endpoints

| Endpoint | Purpose |
|----------|---------|
| `GET /api/health` | Application health |
| `GET /actuator/health` | Spring Actuator health (includes DB and Redis) |
| `GET /actuator/info` | Application info |
| `GET /actuator/metrics` | JVM and application metrics |

### Key Metrics to Monitor

| Metric | Threshold | Action |
|--------|-----------|--------|
| Response time (P95) | < 100ms | Investigate if > 200ms |
| Error rate (5xx) | < 0.1% | Alert if > 1% |
| Database connections | < 80% pool | Increase pool if consistently high |
| Redis hit ratio | > 90% | Investigate cache eviction if < 80% |
| JVM heap usage | < 80% | Increase heap or investigate leaks |
| Virtual Thread count | Informational | Monitor for thread exhaustion |

### Health Check Script

```bash
#!/bin/bash
# Quick health check for all components

echo "=== OneBook Health Check ==="

# Backend
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/health)
echo "Backend API: $HTTP_CODE"

# PostgreSQL
pg_isready -h localhost -p 5432 -U onebook && echo "PostgreSQL: OK" || echo "PostgreSQL: FAIL"

# Redis
redis-cli -p 6379 ping | grep -q PONG && echo "Redis: OK" || echo "Redis: FAIL"

# Actuator
curl -s http://localhost:8080/actuator/health | python3 -m json.tool
```

---

## Database Operations

### Flyway Migration Status

Flyway runs automatically on application startup. To check migration status:

```sql
SELECT version, description, installed_on, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

### Current Migrations

| Version | Description |
|---------|-------------|
| V1 | RLS infrastructure |
| V2 | Organizational hierarchy |
| V3 | Ledger and journal |
| V4 | Seed data |
| V5 | Blind DBA infrastructure |
| V6 | Ingestion layer |
| V7 | Reporting, compliance, FAR |
| V8 | AI intelligence features |

### RLS Verification

Verify tenant isolation is working correctly:

```sql
-- Set tenant context
SET app.current_tenant = 'tenant-A';

-- This should only return tenant-A data
SELECT * FROM ledger_accounts;

-- Verify RLS is enabled
SELECT tablename, rowsecurity
FROM pg_tables
WHERE schemaname = 'public' AND rowsecurity = true;
```

### Audit Log Chain Verification

```sql
-- Verify hash chain integrity
SELECT a1.id, a1.hash, a1.prev_hash,
       a2.hash AS expected_prev_hash,
       (a1.prev_hash = a2.hash) AS chain_valid
FROM audit_log a1
LEFT JOIN audit_log a2 ON a1.prev_hash = a2.hash
WHERE a1.tenant_id = 'default-tenant'
ORDER BY a1.id;
```

### Connection Monitoring

```sql
-- Active connections
SELECT count(*) FROM pg_stat_activity WHERE datname = 'onebook';

-- Connection details
SELECT pid, usename, application_name, state, query_start
FROM pg_stat_activity
WHERE datname = 'onebook'
ORDER BY query_start DESC;
```

---

## Cache Operations

### Cache Status

```bash
# Check if tenant cache is warm
curl http://localhost:8080/api/cache/status/{tenantId}
```

### Manual Cache Operations

```bash
# Warm cache for a tenant
curl -X POST http://localhost:8080/api/cache/warm/{tenantId}

# Evict cache for a tenant
curl -X DELETE http://localhost:8080/api/cache/evict/{tenantId}
```

### Redis Diagnostics

```bash
# Check Redis connectivity
redis-cli ping

# Check memory usage
redis-cli info memory

# List all OneBook cache keys
redis-cli keys "onebook:*"

# Check key TTL
redis-cli ttl "onebook:accounts:default-tenant"

# Monitor real-time operations
redis-cli monitor

# Flush all cache (emergency only)
redis-cli flushall
```

### Cache Key Patterns

| Key Pattern | TTL | Content |
|------------|-----|---------|
| `onebook:accounts:{tenantId}` | 30 min | Chart of Accounts (decrypted) |
| `onebook:account:{tenantId}:{accountId}` | 30 min | Individual account |
| `onebook:trial-balance:{tenantId}` | 30 min | Trial balance report |
| `onebook:warm:{tenantId}` | 30 min | Warm marker flag |

---

## Security Operations

### Encryption Key Management

The encryption master key is stored as an environment variable, never in source code.

```bash
# Generate a new 256-bit (64 hex char) master key
openssl rand -hex 32

# Set the environment variable
export ONEBOOK_SECURITY_ENCRYPTION_MASTER_KEY="<generated-key>"
```

### Key Rotation

The system supports key versioning for rotation without re-encryption:

1. The current `key-version` is set in `application.yml`
2. New data is encrypted with the current version
3. Old data can be decrypted using the version byte in the ciphertext wire format
4. To rotate: add the new key to `KeyManagementService`, increment `key-version`

### Encryption Wire Format

```
Base64( [1 byte version] [12 bytes IV] [N bytes ciphertext + 16 byte GCM auth tag] )
```

### Security Audit Checklist

- [ ] Verify encryption master key is set via environment variable (not in code)
- [ ] Verify RLS is enabled on all tenant-scoped tables
- [ ] Run audit log chain verification query
- [ ] Review CORS configuration (should not be `*` in production)
- [ ] Verify `ddl-auto` is set to `validate`
- [ ] Check that Actuator endpoints are restricted
- [ ] Verify blind indexes are working (search returns correct results)

---

## Troubleshooting

### Application Won't Start

| Symptom | Cause | Resolution |
|---------|-------|------------|
| `Connection refused: localhost:5432` | PostgreSQL not running | `docker compose up -d postgres` |
| `Connection refused: localhost:6379` | Redis not running | `docker compose up -d redis` |
| `Flyway migration failed` | Schema conflict | Check `flyway_schema_history` table |
| `No bean found for WarmCacheService` | Redis config issue | Verify Redis connection in `application.yml` |
| `Invalid encryption key` | Master key not set | Set `ONEBOOK_SECURITY_ENCRYPTION_MASTER_KEY` env var |

### Common Errors

#### Unbalanced Transaction

```
400 Bad Request: Transaction is not balanced: debits=5000.0000 credits=3000.0000
```

**Cause**: Journal entry POST with mismatched debit/credit amounts.
**Resolution**: Ensure sum of debit amounts equals sum of credit amounts.

#### Tenant Isolation Error

```
No data returned for tenant
```

**Cause**: `app.current_tenant` session variable not set or RLS blocking access.
**Resolution**: Verify tenant context is being set correctly in the request.

#### Cache Miss Fallback

```
WARN WarmCacheService: Cache miss for tenant default-tenant, falling back to DB
```

**Cause**: Redis cache expired or not warmed.
**Resolution**: Trigger cache warm-up: `POST /api/cache/warm/{tenantId}`

#### Redis Connection Failure

```
WARN WarmCacheService: Redis unavailable, degrading to DB-only mode
```

**Cause**: Redis is down or unreachable.
**Resolution**: The application continues to function (DB-only mode). Restart Redis when possible.

### Log Locations

| Component | Log Location |
|-----------|-------------|
| Backend | Console (stdout) — configure logback for file output |
| PostgreSQL | `docker logs onebook-postgres` |
| Redis | `docker logs onebook-redis` |
| Frontend | Browser console |

---

## Backup & Recovery

### PostgreSQL Backup

```bash
# Full backup
docker exec onebook-postgres pg_dump -U onebook onebook > backup_$(date +%Y%m%d_%H%M%S).sql

# Compressed backup
docker exec onebook-postgres pg_dump -U onebook -Fc onebook > backup_$(date +%Y%m%d).dump

# Restore from backup
docker exec -i onebook-postgres psql -U onebook onebook < backup.sql

# Restore from compressed
docker exec -i onebook-postgres pg_restore -U onebook -d onebook < backup.dump
```

### Redis Backup

```bash
# Trigger RDB snapshot
redis-cli bgsave

# Check backup status
redis-cli lastsave

# Backup file is at redis_data volume: /data/dump.rdb
```

### Recovery Procedures

1. **Database corruption**: Restore from latest backup, then restart application (Flyway validates schema)
2. **Cache loss**: Application automatically degrades to DB-only mode; trigger `POST /api/cache/warm/{tenantId}` after Redis recovery
3. **Encryption key loss**: **CRITICAL** — without the master key, encrypted data is irrecoverable. Always back up the encryption key separately and securely.

---

## Performance Tuning

### Virtual Threads (Project Loom)

Virtual Threads are enabled by default (`spring.threads.virtual.enabled: true`). This allows the application to handle thousands of concurrent connections without thread pool exhaustion.

**Monitoring**: Check the virtual thread count via Actuator metrics at `/actuator/metrics/jvm.threads.live`.

### Redis Cache Tuning

| Setting | Default | Recommendation |
|---------|---------|---------------|
| Cache TTL | 30 min | Increase for low-churn tenants |
| Max memory | Unlimited | Set `maxmemory` and `maxmemory-policy allkeys-lru` |
| Connection pool | Default | Configure pool size for high concurrency |

### PostgreSQL Tuning

| Setting | Recommendation |
|---------|---------------|
| `shared_buffers` | 25% of available RAM |
| `effective_cache_size` | 75% of available RAM |
| `work_mem` | 256MB for complex queries |
| `maintenance_work_mem` | 512MB |
| `max_connections` | Match HikariCP pool size + overhead |

### Database Indexes

All performance-critical indexes are defined in the Flyway migrations. Key indexes include:

| Index | Purpose |
|-------|---------|
| `idx_journal_entries_transaction` | Fast journal entry lookup by transaction |
| `idx_journal_entries_account` | Fast balance aggregation by account |
| `idx_financial_events_tenant_status` | Filter events by processing status |
| `idx_card_transactions_tenant_unposted` | Quick filter for unposted card transactions |
| `idx_bank_feed_txn_tenant_unmatched` | Quick filter for unmatched bank feeds |
| `idx_ledger_account_blind_index` | Fast searchable encryption queries |

---

## Related Documentation

- [Architecture Diagram](architecture-diagram.md)
- [Key-Binding Registry Design](key-binding-registry.md)
- [SQL Schema Documentation](sql-schema.md)
- [API Documentation](api-documentation.md)
- [Developer Onboarding Guide](developer-guide.md)
