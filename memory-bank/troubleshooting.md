# Troubleshooting — OneBook (Nexus Universal)

> **Known issues, past bugs encountered, and applied fixes.**  
> Add new entries when you fix bugs or discover workarounds. This reduces repeated debugging effort.

---

## Known Pre-existing Issues

### Frontend AppComponent Test Failures
**Status:** Known, not fixed (pre-existing)  
**Symptom:** 2 AppComponent test failures in frontend test suite  
**Cause:** Pre-existing configuration issue in test setup  
**Impact:** None — all functional tests pass; only these 2 scaffolding tests fail  
**Action:** Do not fix unless specifically tasked — changing them risks breaking functional tests

---

## Past Bugs Encountered & Fixed

### RLS Policy Grant Missing
**When:** M3 Security implementation  
**Symptom:** Queries failing with "permission denied" for tenant-scoped tables  
**Root Cause:** RLS policies were created but `GRANT SELECT` to application role was missing  
**Fix:** Added `GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE <name> TO onebook_app;` to migration  
**Files:** `V5__blind_dba_infrastructure.sql`  
**Lesson:** Always add GRANT statements alongside RLS policy creation in Flyway migrations

### AES-GCM IV Reuse Risk
**When:** M3 Security review  
**Symptom:** N/A (detected during code review, not at runtime)  
**Root Cause:** Naive implementation might cache the SecureRandom instance incorrectly  
**Fix:** Confirmed `SecureRandom.generateSeed(12)` is called fresh per encryption operation  
**Files:** `FieldEncryptionService.java`  
**Lesson:** Always generate a new random IV for every AES-GCM encrypt call — reuse is catastrophic

### Cache Key Collision Risk
**When:** M4 Redis implementation  
**Symptom:** Different tenants seeing each other's cached data (hypothetical, caught in design)  
**Root Cause:** Cache key format was `onebook:cache:<domain>:<id>` (missing tenant)  
**Fix:** Changed format to `onebook:cache:<domain>:<tenantId>:<id>`  
**Files:** `CacheConstants.java`  
**Lesson:** Cache keys must always include tenantId for multi-tenant isolation

### Lazy Load Exception in REST Response
**When:** M2 Ledger Engine — early implementation  
**Symptom:** `LazyInitializationException` when serializing JPA entities to JSON  
**Root Cause:** JPA entity returned directly from controller, triggering lazy-loaded collection access outside transaction  
**Fix:** Introduced DTO records for all REST responses  
**Files:** All `*Controller.java` and module `dto/` package  
**Lesson:** Never expose JPA entities in REST responses — always use DTOs

### Validation Script False Positives
**When:** M10 Agent ownership validation setup  
**Symptom:** Script reporting missing services that actually exist in agent files  
**Root Cause:** Script was doing substring match — service names with shared prefixes confused it  
**Fix:** Validated script logic for exact matching in context of agent file sections  
**Files:** `.github/scripts/validate-agent-ownership.sh`  
**Lesson:** Run the validation script after every major agent file edit to confirm no regressions

### Flyway V12 Migration Number Skipped
**When:** Post-M10 payment processing development  
**Symptom:** V12 does not exist in `db/migration/`; numbering goes V11 → V13 → V14  
**Root Cause:** V12 was intentionally skipped during development. V13 was authored as a follow-on to V11 (merging `financial_events` into `payment_register`), and the V12 slot was simply never used.  
**Impact:** None — Flyway tolerates gaps in version numbers. The migration sequence V11 → V13 → V14 is valid.  
**Action:** No fix needed. Documented here to prevent future confusion. Future migrations should continue from V15.  
**Lesson:** If a migration number is skipped, note it in documentation to avoid confusion when auditing the migration history.

---

## Common Development Issues

### Docker Compose Not Starting
**Symptom:** Backend fails with "connection refused" to PostgreSQL  
**Check:**
```bash
docker compose ps  # Are both services Up?
docker compose logs postgres  # Any startup errors?
```
**Fix:** `docker compose down -v && docker compose up -d` (full reset)

### Flyway Migration Failed
**Symptom:** Application fails to start with "Migration checksum mismatch"  
**Cause:** Migration file was edited after being applied  
**Fix:** Never edit an applied migration. Create a new one instead.

### Redis Connection Refused
**Symptom:** `Could not connect to Redis` in backend logs  
**Check:** `docker compose ps redis`  
**Fix:** `docker compose restart redis`

### Angular Build Fails with Signal Errors
**Symptom:** Compilation error about calling signal value  
**Cause:** Signals must be called as functions: `mySignal()` not `mySignal`  
**Fix:** Add `()` when reading signal values

### Agent Ownership Validation Fails
**Symptom:** `.github/scripts/validate-agent-ownership.sh` reports missing components  
**Fix:**
1. Identify which agent owns the new component (see `.github/agents/MAINTENANCE.md`)
2. Add the component to the appropriate agent's `.md` file
3. Re-run the script until it passes

---

## Environment-Specific Notes

### Local Development
- Application starts at `http://localhost:4200` (frontend) and `http://localhost:8080` (backend)
- Frontend proxies `/api` calls to backend automatically (configured in `proxy.conf.json`)
- Database resets on `docker compose down -v` — Flyway re-runs all migrations on next start

### Testing
- Backend tests use `@ActiveProfiles("test")` — H2 in-memory DB (no Docker needed for tests)
- Frontend tests require ChromeHeadless: `npx ng test --watch=false --browsers=ChromeHeadless`
- No test data setup needed — each test manages its own data via Mockito or `@Sql` scripts

---

## Security Notes

### Encryption Key Rotation
- KEK rotation: Generate new KEK, re-encrypt DEKs, update environment variable
- Data re-encryption NOT needed (DEKs are wrapped, not the data itself)
- Document rotation in operational runbook

### RLS Testing
- Always test with multiple tenant IDs to confirm isolation
- SQL injection test: ensure tenant filter cannot be bypassed via crafted input
- Service account should never have `BYPASSRLS` privilege
