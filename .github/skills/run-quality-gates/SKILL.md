---
name: run-quality-gates
description: >-
  Execute all OneBook quality gates: backend build and tests, frontend build,
  quality gate validation (8 gates), agent ownership validation, and memory bank
  freshness check. Produces a comprehensive test report.
---

# Run Quality Gates

Execute the complete OneBook quality validation pipeline and produce a test report.

## When to Use

- After implementing any code changes
- Before creating a pull request
- When @partner delegates testing to @quality
- After fixing issues reported in a previous quality run

## Steps

### 1. Backend Compilation

```bash
cd backend && ./gradlew compileJava
```

**If fails:** Stop immediately. Report compilation errors.

### 2. Backend Tests

```bash
cd backend && ./gradlew test
```

**Record:** Total test count, new tests added, any failures.
**Baseline:** 514+ tests expected.

### 3. Frontend Build

```bash
cd frontend && npx ng build
```

**If fails:** Report build errors.

### 4. Quality Gates Validation (8 Gates)

```bash
./.github/scripts/validate-quality-gates.sh
```

This validates:
1. **Memory bank freshness** — test counts, modules, migrations not stale
2. **RLS policy coverage** — all tenant tables have RLS (regression detection)
3. **DTO enforcement** — no JPA entities in controller responses
4. **Backend test coverage** — every Service has a Test file
5. **Flyway migration conventions** — naming, TIMESTAMPTZ, NUMERIC(19,4)
6. **Frontend test coverage** — every component has `.spec.ts`
7. **i18n enforcement** — no hardcoded strings in templates
8. **BigDecimal enforcement** — no `double`/`float` for monetary values

**Baselines tracked in:** `.github/scripts/quality-gate-baselines.conf`

### 5. Agent Ownership Validation

```bash
./.github/scripts/validate-agent-ownership.sh
```

Verifies all services, controllers, and modules are documented in agent files.

### 6. Memory Bank Freshness

```bash
./.github/scripts/sync-memory-bank.sh --check
```

Detects stale values in the memory bank (test counts, module lists, migration numbers).

### 7. Generate Test Report

Compile results into the standard report format:

```markdown
## @quality — Test Report

### Backend
- **Compilation**: ✅ PASS / ❌ FAIL
- **Tests**: {count} total ({delta} new) — ✅ ALL PASSING / ❌ {N} FAILURES

### Frontend
- **Build**: ✅ PASS / ❌ FAIL

### Quality Gates
- Gate 1 (Memory Bank): ✅ / ❌
- Gate 2 (RLS): ✅ / ❌
- Gate 3 (DTO): ✅ / ❌
- Gate 4 (Backend Tests): ✅ / ❌
- Gate 5 (Migrations): ✅ / ❌
- Gate 6 (Frontend Tests): ✅ / ❌
- Gate 7 (i18n): ✅ / ❌
- Gate 8 (BigDecimal): ✅ / ❌

### Agent Ownership: ✅ PASS / ❌ FAIL
### Memory Bank: ✅ CURRENT / ❌ STALE

### Overall: ✅ ALL PASSED / ❌ ISSUES FOUND
```

## References

- Quality agent: `.github/agents/quality.agent.md`
- Baselines: `.github/scripts/quality-gate-baselines.conf`
- Scripts: `.github/scripts/`
