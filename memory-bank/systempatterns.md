# System Patterns — OneBook (Nexus Universal)

> **Architecture decisions, design patterns, and critical conventions accumulated over all sessions.**  
> Every pattern here was decided intentionally. Understand the reason before changing it.

---

## Core Architecture Decisions

### 1. Double-Entry Validation (3 Layers)
Every journal entry is validated at three levels:
1. **Service layer** — `BigDecimal` sum check before posting
2. **Database trigger** — `check_balanced_transaction()` on POSTED status
3. **Exception** — `UnbalancedTransactionException` surfaced to the client

**Why:** Defense-in-depth for accounting integrity. A single-layer check can be bypassed.

### 2. Pluggable Adapter Pattern (Ingestion)
New data sources implement `FinancialEventAdapter` interface + `@Component`.  
`AdapterRegistry` auto-discovers via Spring DI.  
Pipeline: **Parse → Validate → Map → Post**  
Event status flow: `RECEIVED → VALIDATED → MAPPED → POSTED` (or `FAILED`)

**Why:** Sector-agnostic ingestion — any external system can be integrated without touching core logic.

### 3. Cache-Aside Read Pattern
```
Check Redis → null on miss → query DB → populate cache → return
```
**Write pattern:** Write DB → invalidate cache (write-through).  
**On Redis failure:** Log warning + fall back to DB (failure-safe, NOT circuit breaker).  
**Key format:** `onebook:cache:<domain>:<qualifier>:<id>`  
**TTL:** 30min default, 10min volatile, 120min static.

**Why:** Warm Cache eliminates AES-GCM decryption lag per request — users feel instant response after login.

### 4. Blind DBA Security Model
Sensitive fields encrypted in the JVM BEFORE reaching the database.  
DBA can see ciphertext but never plaintext.  
Blind indexes (HMAC-SHA256) stored alongside ciphertext for searchability.

**Wire format:** `[version byte][IV 12 bytes][ciphertext+tag]` → Base64

**Why:** Regulatory compliance + insider threat mitigation without losing search capability.

### 5. Row-Level Security (RLS)
Every tenant-scoped table has `ALTER TABLE ... ENABLE ROW LEVEL SECURITY`.  
Application sets `SET app.current_tenant_id = '...'` per session.  
PostgreSQL enforces tenant isolation at the database level.

**Why:** Even a SQL injection attack cannot cross tenant boundaries.

### 6. Hash-Chained Audit Trail
Each audit record contains: `hash = SHA-256(previous_hash + current_record_data)`.  
Any tampered record breaks the chain and is detectable.

**Why:** Regulatory requirement for tamper-evident financial audit trails.

### 7. DTO-Only REST Responses
JPA entities are never serialized directly into REST responses.  
Always use DTO records (`*Request`, `*Response`) in `ledger/dto/`.

**Why:** Prevents accidental exposure of encrypted fields, lazy-load exceptions, and circular references.

### 8. Angular Signals (Not RxJS for Simple State)
Use `signal()` for mutable state, `computed()` for derived state.  
All components standalone (no NgModules).  
`ChangeDetectionStrategy.OnPush` on every component.

**Why:** Signals are simpler, more performant, and easier to reason about for component-local state. RxJS is reserved for async/stream operations.

---

## Design Patterns in Use

### Repository Pattern
All data access through Spring Data JPA repositories.  
Custom queries use `@Query` annotation with JPQL.  
Always include `tenantId` in every query.

### Global Exception Handler
`GlobalExceptionHandler` (`@RestControllerAdvice`) catches all exceptions and returns uniform JSON:
```json
{"timestamp": "...", "status": 400, "error": "Bad Request", "message": "..."}
```

### Feature Entitlement Engine
`FeatureEntitlementService` toggles features (GST, VAT, IFRS, multi-currency) per tenant.  
Prevents code branching in business logic — features are configuration-driven.

### Envelope Encryption
Keys are encrypted with a KEK (Key Encryption Key) stored separately.  
Data encrypted with a DEK (Data Encryption Key).  
Supports key rotation without re-encrypting all data.

### Virtual Threads
All HTTP request handling uses Project Loom Virtual Threads.  
Configured in Spring Boot via `spring.threads.virtual.enabled=true`.  
Allows thousands of concurrent requests without thread pool exhaustion.

---

## Sub-Agent Context Budget Protocol

Per `sub-agents.md` — the "3-File Rule":
- **Trigger:** Task requires reading/analyzing 3+ files across different layers
- **Action:** Spawn subagent, isolate, distill to interface/summary only
- **Goal:** Keep Main Session context < 5,000 tokens at all times

After a milestone completes, summarize final state and clear subagent history.

---

## Agent Ownership Rules

When adding new code, update the corresponding agent file in `.github/agents/`:

| Component | Owner |
|-----------|-------|
| Core accounting service (Journal, Ledger, etc.) | @LedgerExpert |
| Financial reports (P&L, Balance Sheet, Cash Flow) | @LedgerExpert |
| Fixed Asset Register | @LedgerExpert |
| Encryption, Blind Index, Key Management | @SecurityWarden |
| Document Vault | @SecurityWarden |
| Warm Cache | @PerfEngineer |
| Ingestion adapters (HL7, ISO20022, DMS, Webhook) | @IntegrationBot |
| OCR, 3-way matching, corporate card | @IntegrationBot |
| Inventory (stock, batch, BOM) | @IntegrationBot |
| AI forecasting, MTM, anomaly detection | @AIEngineer |
| Compliance, GST, TDS/TCS, entitlements | @ComplianceAgent |
| Bank reconciliation, intercompany | @ComplianceAgent |
| Auditor portal, security audit, observability | @AuditAgent |
| Disaster recovery | @AuditAgent |
| All frontend modules (UI layer) | @UXSpecialist |
| Docker, CI/CD, Spring config | @Architect |
| Documentation | @DocAgent |

---

## Critical Rules (Never Violate)

### Accounting
- ALWAYS validate `SUM(debit) == SUM(credit)` before posting
- ALWAYS use `BigDecimal` for amounts (never `double`/`float`)
- ALWAYS include `tenantId` in all repository queries
- NEVER post unbalanced transactions
- NEVER expose JPA entities in REST responses

### Security
- ALWAYS generate unique random IV per AES-GCM encryption
- ALWAYS use HMAC-SHA256 for blind indexes (not plain SHA-256)
- ALWAYS enable RLS on all tenant-scoped tables
- NEVER reuse IVs (catastrophic for AES-GCM security)
- NEVER log encryption keys or plaintext sensitive data
- NEVER store secrets in `application.yml` — use environment variables

### Performance
- ALWAYS use `Cache-Aside` pattern (check Redis first)
- ALWAYS invalidate cache on write (write-through)
- NEVER introduce blocking I/O on Virtual Thread pools without explicit wrapping

### Ownership
- ALWAYS run `.github/scripts/validate-agent-ownership.sh` before committing new modules
- ALWAYS update relevant `.github/agents/*.md` when adding new services/controllers/modules

---

## Known Patterns to Avoid (Anti-Patterns)

| Anti-Pattern | Why | Correct Pattern |
|-------------|-----|-----------------|
| `double` for money | Floating point precision errors | Use `BigDecimal` |
| NgModules | Deprecated in Angular 19+ | Standalone components |
| JPA entity in REST response | Lazy-load issues, security leaks | Use DTO records |
| Direct DB write without cache invalidation | Stale cache | Write-through invalidation |
| Reusing AES-GCM IV | Catastrophic security break | Generate unique random IV per operation |
| Hardcoded tenant IDs | Multi-tenancy violation | Always from security context |
| `ddl-auto: create` in prod | Schema destruction | Flyway migrations only |
