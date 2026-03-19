# Technical Requirements Document (TRD)
## OneBook — Nexus Universal Accounting OS

> **Auto-generated from REQ-*.md files. Version: Living Document.**  
> Last Updated: 2026-03-18 | Owner: @Architect | Status: APPROVED

---

## Table of Contents

1. [System Architecture Overview](#1-system-architecture-overview)
2. [TR-001: Multi-Tenant RLS](#2-tr-001-multi-tenant-rls)
3. [TR-002: Field-Level Encryption (AES-256-GCM)](#3-tr-002-field-level-encryption-aes-256-gcm)
4. [TR-003: Cache-Aside Pattern (Redis)](#4-tr-003-cache-aside-pattern-redis)
5. [TR-004: Virtual Threads (Project Loom)](#5-tr-004-virtual-threads-project-loom)
6. [TR-005: Double-Entry Validation](#6-tr-005-double-entry-validation)
7. [TR-006: Pluggable Adapter Pattern](#7-tr-006-pluggable-adapter-pattern)
8. [TR-007: Hash-Chained Audit Trail](#8-tr-007-hash-chained-audit-trail)
9. [TR-008: Angular Signals Architecture](#9-tr-008-angular-signals-architecture)
10. [Performance Requirements](#10-performance-requirements)
11. [Security Requirements](#11-security-requirements)
12. [Deployment Architecture](#12-deployment-architecture)

---

## 1. System Architecture Overview

### Technology Stack

| Layer | Technology | Version | Purpose |
|-------|-----------|---------|---------|
| Backend API | Spring Boot | 3.4+ | REST API, business logic |
| Runtime | Java (OpenJDK) | 21+ | Virtual Threads (Project Loom) |
| Frontend SPA | Angular | 19+ | Signals-based reactive UI |
| Database | PostgreSQL | 17+ | Primary data store with RLS |
| Cache | Redis | 7+ | Warm cache, session data |
| ORM | Spring Data JPA / Hibernate | 6.x | Repository pattern |
| Migrations | Flyway | 10.x | Schema versioning (V1–V10) |
| i18n | @jsverse/transloco | 7+ | Internationalization |
| Build | Gradle (backend), npm/Angular CLI (frontend) | — | Build toolchain |
| CI/CD | GitHub Actions | — | Build, test, agent ownership validation |
| Container | Docker Compose | — | Local dev + staging |

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                          Angular 19+ SPA                         │
│   Signals State │ Command Palette │ Lazy-Loaded Feature Modules  │
└─────────────────────────┬───────────────────────────────────────┘
                          │ HTTPS (JWT Bearer)
┌─────────────────────────▼───────────────────────────────────────┐
│                    Spring Boot 3.4+ API                          │
│  Virtual Threads │ DTO-only REST │ Tenant Context Filter         │
│  ┌─────────────┐  ┌──────────────┐  ┌───────────────────────┐   │
│  │  Controller  │  │   Service    │  │  Security (Encrypt)   │   │
│  │    Layer     │  │    Layer     │  │  AuditLog │ BlindIdx  │   │
│  └──────┬──────┘  └──────┬───────┘  └───────────────────────┘   │
│         │                │                                        │
│  ┌──────▼──────────────▼────────────────────────────────────┐   │
│  │              Repository Layer (Spring Data JPA)           │   │
│  └─────────────────────────┬─────────────────────────────────┘  │
└────────────────────────────┼────────────────────────────────────┘
               ┌─────────────┼─────────────────┐
               ▼             ▼                  ▼
     ┌─────────────┐  ┌────────────┐   ┌──────────────┐
     │ PostgreSQL  │  │  Redis 7+  │   │  External    │
     │     17+     │  │  (Cache)   │   │  Adapters    │
     │  RLS + JSONB│  │  30min TTL │   │ (HL7/ISO20022│
     └─────────────┘  └────────────┘   │  DMS/Webhook)│
                                        └──────────────┘
```

---

## 2. TR-001: Multi-Tenant RLS

**Description:** All tenant-scoped tables enforce Row-Level Security (RLS) at the PostgreSQL layer, providing an infrastructure-level guarantee of tenant data isolation.

**Implementation Details:**

```sql
-- Every tenant-scoped table follows this pattern
ALTER TABLE ledger_accounts ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON ledger_accounts
    USING (tenant_id = current_setting('app.current_tenant_id')::uuid);
```

**Application-Side Pattern:**
```java
// TenantContextFilter sets tenant before each request
entityManager.createNativeQuery(
    "SET app.current_tenant_id = :tenantId"
).setParameter("tenantId", tenantId).executeUpdate();
```

**Tables with RLS:**
- `ledger_accounts`
- `journal_transactions`
- `journal_entries`
- `audit_logs`
- `financial_events`
- `fixed_assets`
- `bank_feed_transactions`
- All 40+ tenant-scoped tables in V1–V10 migrations

**Migration File:** `V1__rls_infrastructure.sql`

**Guarantee:** Even a SQL injection attack cannot cross tenant boundaries because PostgreSQL enforces the policy at the storage engine level, below the application.

---

## 3. TR-002: Field-Level Encryption (AES-256-GCM)

**Description:** Sensitive fields are encrypted at the JVM layer using AES-256-GCM before the data reaches the database. Database administrators see only ciphertext.

**Wire Format:**
```
[version byte (1)] + [random IV (12 bytes)] + [GCM ciphertext + auth tag (16 bytes)]
→ Base64 encoded string stored in VARCHAR/TEXT column
```

**Encryption Flow:**
```
Plaintext → KeyManagementService.getCurrentKey()
         → SecureRandom.generateIV(12 bytes)
         → AES/GCM/NoPadding encrypt
         → Base64(version + IV + ciphertext)
         → Store in database
```

**Decryption Flow:**
```
Base64 string → Decode
             → Extract version byte → resolve key
             → Extract IV (bytes 1–12)
             → AES/GCM/NoPadding decrypt with auth tag verification
             → Plaintext returned
```

**Blind Index for Search:**
```
SearchTerm → HMAC-SHA256(key=blindIndexKey, data=normalize(searchTerm))
           → Base64 → Store in separate column (e.g., party_name_idx)
           → WHERE party_name_idx = ? (equality search only)
```

**Key Management:**
- Keys stored in environment variables (`ONEBOOK_ENCRYPTION_KEY`, `ONEBOOK_BLIND_INDEX_KEY`)
- Key version byte enables key rotation without re-encryption of all data
- Never in `application.yml` or source code

**Implementation Files:**
- `FieldEncryptionService.java` — AES-256-GCM encrypt/decrypt
- `BlindIndexService.java` — HMAC-SHA256 blind index computation
- `KeyManagementService.java` — Key loading, versioning, rotation
- `EncryptedStringConverter.java` — JPA `@Convert` annotation integration
- `V5__blind_dba_infrastructure.sql` — Blind index columns

---

## 4. TR-003: Cache-Aside Pattern (Redis)

**Description:** Frequently accessed data (account balances, report results, decrypted tenant config) is cached in Redis to eliminate repeated AES-GCM decryption overhead.

**Read Path (Cache-Aside):**
```
Request arrives
  → Check Redis: GET onebook:cache:<domain>:<qualifier>:<id>
  → Hit: Return cached value (no DB hit, no decryption)
  → Miss: Query PostgreSQL → Decrypt → Store in Redis → Return
```

**Write Path (Write-Through Invalidation):**
```
Write to PostgreSQL
  → DEL onebook:cache:<domain>:<qualifier>:<id>
  → Next read will repopulate cache
```

**Key Format:** `onebook:cache:<domain>:<qualifier>:<id>`
- Example: `onebook:cache:ledger:account:550e8400-e29b-41d4-a716-446655440000`

**TTL Strategy:**
| Data Type | TTL |
|-----------|-----|
| Account balances | 30 minutes |
| Report results | 30 minutes |
| Tenant configuration | 120 minutes |
| Session/volatile data | 10 minutes |

**Failure-Safe Behaviour:**
```java
try {
    return redisTemplate.opsForValue().get(cacheKey);
} catch (RedisException e) {
    log.warn("Redis unavailable, falling back to DB: {}", e.getMessage());
    return fetchFromDatabase(); // no circuit breaker — DB always available
}
```

**Cache Constants:** `CacheConstants.java`  
**Implementation:** `WarmCacheService.java`  
**Warm-up Trigger:** Login event — pre-populate frequently accessed accounts

---

## 5. TR-004: Virtual Threads (Project Loom)

**Description:** Spring Boot is configured to use Java 21 Virtual Threads for all HTTP request handling, enabling high concurrency without the overhead of platform thread pools.

**Configuration:**
```yaml
# application.yml
spring:
  threads:
    virtual:
      enabled: true
```

**Impact:**
- Each HTTP request runs on a Virtual Thread (lightweight, heap-allocated)
- Thread-per-request model without blocking platform thread pool
- Supports 10,000+ concurrent connections on a single JVM instance
- JDBC blocking I/O is transparently unmounted while waiting

**Why Virtual Threads (not WebFlux):**
- Spring MVC with Virtual Threads provides near-reactive throughput with imperative (readable) code
- Reactive WebFlux would require rewriting all repository and service code in reactive style
- Decision documented in `memory-bank/systempatterns.md`

---

## 6. TR-005: Double-Entry Validation

**Description:** Triple-layer validation ensures journal entries can never be posted with imbalanced debits and credits.

**Layer 1 — Service Layer (Java):**
```java
BigDecimal totalDebits = entries.stream()
    .filter(e -> e.getEntryType() == EntryType.DEBIT)
    .map(JournalEntry::getAmount)
    .reduce(BigDecimal.ZERO, BigDecimal::add);

BigDecimal totalCredits = entries.stream()
    .filter(e -> e.getEntryType() == EntryType.CREDIT)
    .map(JournalEntry::getAmount)
    .reduce(BigDecimal.ZERO, BigDecimal::add);

if (totalDebits.compareTo(totalCredits) != 0) {
    throw new UnbalancedTransactionException(totalDebits, totalCredits);
}
```

**Layer 2 — Database Trigger:**
```sql
CREATE OR REPLACE FUNCTION check_balanced_transaction()
RETURNS TRIGGER AS $$
BEGIN
    IF (SELECT SUM(CASE WHEN entry_type = 'DEBIT' THEN amount ELSE -amount END)
        FROM journal_entries WHERE transaction_id = NEW.id) != 0 THEN
        RAISE EXCEPTION 'Journal entries are not balanced';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

**Layer 3 — Exception Surfacing:**  
`UnbalancedTransactionException` returns HTTP 422 with debit/credit totals.

**Why Three Layers:** Defense-in-depth. A single layer can be bypassed by direct DB access or a service-layer bug. All three must fail simultaneously for corrupt data to enter the system.

---

## 7. TR-006: Pluggable Adapter Pattern

**Description:** The ingestion pipeline uses the Strategy + Registry pattern to make external application adapters pluggable without modifying core code.

**Interface:**
```java
public interface ExternalAppAdapter {
    String getAdapterType();
    boolean canHandle(String adapterType);
    FinancialEvent parse(String payload);
    List<JournalEntryRequest> map(FinancialEvent event);
}
```

**Registry:**
```java
@Component
public class AdapterRegistry {
    private final List<ExternalAppAdapter> adapters;
    
    public ExternalAppAdapter resolve(String type) {
        return adapters.stream()
            .filter(a -> a.canHandle(type))
            .findFirst()
            .orElseThrow(() -> new AdapterNotFoundException(type));
    }
}
```

**Pipeline Stages:**
```
Receive (HTTP POST) → Parse (adapter-specific) → Validate (schema check)
→ Map (to JournalEntryRequest) → Post (JournalService) → Update status
```

**To Add a New Adapter:**
1. Implement `ExternalAppAdapter` interface
2. Annotate with `@Component`
3. Spring DI auto-registers it in `AdapterRegistry`
4. Zero changes to core ingestion logic

---

## 8. TR-007: Hash-Chained Audit Trail

**Description:** Each audit log entry includes a cryptographic hash that chains to the previous entry, making any record modification detectable.

**Hash Computation:**
```java
String hash = SHA256(
    previousHash +
    entityType +
    entityId +
    action +
    actorId +
    timestamp.toString() +
    payload.toJson()
);
```

**Chain Verification:**
```
For each entry in sequence:
  computedHash = SHA256(entry[n-1].hash + entry[n].data)
  if computedHash != entry[n].hash → TAMPER DETECTED
```

**Database Schema:**
```sql
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    actor_id VARCHAR(255),
    payload JSONB,
    previous_hash VARCHAR(64),
    hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
```

**Implementation:** `AuditLogService.java`  
**Migration:** `V9__hardening_audit_production.sql`

---

## 9. TR-008: Angular Signals Architecture

**Description:** The Angular 19+ frontend uses Signals for all component-local state management, replacing RxJS for simple state.

**Patterns:**
```typescript
// Mutable state
accountBalance = signal<BigDecimal>(0);

// Derived state (auto-recomputes when dependencies change)
formattedBalance = computed(() => 
  this.accountBalance().toLocaleString('en-IN', { style: 'currency', currency: 'INR' })
);

// Effect (side-effect when signal changes)
effect(() => {
  this.updateTitle(this.selectedAccount().name);
});
```

**Component Configuration:**
- All components: `standalone: true` (no NgModules)
- Change detection: `ChangeDetectionStrategy.OnPush` on every component
- Lazy loading: All feature modules lazy-loaded via `app.routes.ts`

**State Boundaries:**
- Component-local state: `signal()` / `computed()`
- Cross-component state: Angular Signals-based service with `signal()` fields
- Async/stream operations: RxJS (HTTP calls, event streams)

---

## 10. Performance Requirements

| Metric | Requirement | Measurement Method |
|--------|-------------|-------------------|
| API P95 response time | < 300ms | Load test (1000 concurrent users) |
| Report generation (Trial Balance, 1M entries) | < 5 seconds | Performance test |
| Cache hit ratio | > 80% after warm-up | Redis INFO stats |
| Journal entry posting | < 200ms P95 | Load test |
| Concurrent tenants (no degradation) | 100+ tenants | Chaos/load test |
| Ingestion event processing | < 500ms per event | Performance test |
| Frontend initial load (LCP) | < 2 seconds | Lighthouse |
| Frontend route navigation | < 100ms | Browser performance API |

---

## 11. Security Requirements

| Requirement | Implementation |
|-------------|----------------|
| All sensitive fields encrypted | AES-256-GCM via `FieldEncryptionService` |
| No plaintext secrets in source code | Environment variables; validated in CI |
| Tenant isolation | RLS policies on all tenant-scoped tables |
| Tamper-evident logs | SHA-256 hash chain in `audit_logs` |
| Search on encrypted data | HMAC-SHA256 blind indexes |
| JWT token validation | Spring Security (every endpoint) |
| TLS in transit | HTTPS required; HTTP redirected |
| Key rotation support | Key version byte in ciphertext wire format |
| SQL injection prevention | Parameterized queries; Spring Data JPA |
| CORS policy | Configured for specific allowed origins only |

---

## 12. Deployment Architecture

### Docker Compose (Local / Staging)
```yaml
services:
  postgres:
    image: postgres:17
    environment:
      POSTGRES_DB: onebook
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    
  redis:
    image: redis:7-alpine
    
  backend:
    build: ./backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/onebook
      SPRING_REDIS_HOST: redis
      ONEBOOK_ENCRYPTION_KEY: ${ONEBOOK_ENCRYPTION_KEY}
      ONEBOOK_BLIND_INDEX_KEY: ${ONEBOOK_BLIND_INDEX_KEY}
    depends_on: [postgres, redis]
    
  frontend:
    build: ./frontend
    ports: ["80:80"]
    depends_on: [backend]
```

### Production Topology (Recommended)
```
Load Balancer (HTTPS termination)
  ├── Backend Instance 1 (Virtual Threads, 4 vCPU, 8GB RAM)
  ├── Backend Instance 2 (Virtual Threads, 4 vCPU, 8GB RAM)
  └── Backend Instance N (scale horizontally)

PostgreSQL 17+ (Primary-Replica)
  ├── Primary (writes)
  └── Read Replica (reports/analytics)

Redis 7+ Cluster
  ├── Primary
  └── Replica (failover)

CDN → Angular SPA (static assets)
```

### CI/CD Pipeline
```yaml
# .github/workflows/ci.yml
1. Backend: ./gradlew build test  (Java 21, Gradle)
2. Frontend: npm ci && npx ng build && npx ng test
3. Agent Ownership: .github/scripts/validate-agent-ownership.sh
4. Docker build (main branch only)
5. Deploy to staging (main branch only)
```

---

*This document is auto-generated from REQ-*.md files by `docs/automation/generate-trd.js`. Do not edit manually.*
