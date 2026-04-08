# Active Context — OneBook (Nexus Universal)

> **Current project state, recent changes, and what to focus on next.**  
> This file is updated at the end of every work session.

---

## Current Status

**Date:** 2026-04-07  
**Phase:** M11 Complete  
**Milestones:** ✅ M1–M11 Complete | 📝 M12 Draft

---

## Recent Changes (Latest Session)

### M11 Payment Processing Pipeline COMPLETED (2026-04-07)
- **NEW: `PaymentFileGeneratorServiceTest.java`** — 7 unit tests covering CSV generation, special character escaping, null handling, precision preservation
- **NEW: `PaymentRegisterControllerTest.java`** — 4 unit tests covering grouped vendor response, empty results, vendor-specific queries
- **Payment module now has 22 tests total** (5 service + 7 service + 2 service + 4 controller + 4 controller)
- **Requirements updated:** REQ-011, REQ-012, REQ-013 status changed to COMPLETED
- **All quality gate checklists complete** in all three requirement files
- **BRD/FRD/TRD/RTM regenerated** with COMPLETED status (13/14 requirements complete, 93% coverage)

### Previous Session: Automated Quality Gate Enforcement (2026-04-04)
- **NEW: `.github/scripts/validate-quality-gates.sh`** — 8-gate CI validation that catches ALL commonly skipped steps:
  1. Memory bank freshness (stale test counts, missing modules, migration lists)
  2. RLS policy coverage (regression detection against baseline)
  3. DTO enforcement (no JPA entities in controller returns — regression detection)
  4. Backend test coverage (every new Service must have a Test file)
  5. Flyway migration conventions (naming, TIMESTAMPTZ)
  6. Frontend test coverage (every new component must have a .spec.ts)
  7. i18n enforcement (no new hardcoded strings in templates)
  8. BigDecimal enforcement (no double/float for monetary fields)
- **NEW: `.github/scripts/sync-memory-bank.sh`** — Auto-detects and fixes stale values in memory bank files. `--check` mode for validation, `--fix` mode for auto-correction.
- **NEW: `.github/scripts/quality-gate-baselines.conf`** — Tracks pre-existing violation counts (125 DTO, 5 RLS, 3 missing tests, 8 missing specs, 1 i18n). Only NEW violations fail CI.
- **CI updated:** `ci.yml` now has 5 jobs: validate-ownership, validate-quality-gates, sync-memory-bank (auto-commit on main), backend build, frontend build.
- **Memory bank fixed:** techcontext.md test count 405→489, module count 11→15, added auth/master/payable/reports modules, systempatterns.md baseline updated.
- **CLAUDE.md updated:** Added automation references, quality gate table, updated file map.

### Previous Session: Requirements & Milestones Restructuring (2026-04-04)
- Added M11/M12 milestone definitions to docs/milestones.md
- Auto-generated docs regenerated (14 requirements, 0 errors)

---

## Current Project State

### Backend (Spring Boot 3.4+ / Java 21)
- **Package:** `com.nexus.onebook.ledger`
- **Migrations:** V1–V14 (all applied; V12 intentionally skipped)
- **Tests:** 525 passing
- **API:** All endpoints functional at `/api/*`

### Frontend (Angular 19+)
- **Modules:** 17 feature modules (all lazy-loaded standalone components)
- **Tests:** 105+ passing (2 pre-existing AppComponent failures known)
- **State:** Signals-based (no RxJS for simple state)

### Infrastructure
- PostgreSQL 17 + Redis 7 via Docker Compose
- GitHub Actions CI validates build, test, and agent ownership on every PR

---

## Active Focus Areas

Quality gate automation is now enforced in CI. All 8 gates pass. Memory bank auto-sync runs on every push to main.

**Automated enforcement (cannot be skipped):**
- Memory bank freshness validated on every PR (test counts, modules, migrations)
- RLS, DTO, BigDecimal, test coverage validated with regression detection
- Memory bank auto-synced on push to main (stale values auto-corrected)
- Agent ownership validated on every PR

**For any new implementation:**
1. Add the feature code
2. Run `validate-quality-gates.sh` to check all 8 gates
3. Run `sync-memory-bank.sh --fix` to auto-update memory bank
4. Run `validate-agent-ownership.sh` to ensure ownership
5. CI will enforce all of the above automatically

---

## Next Steps (When New Tasks Arrive)

1. Read `memory-bank/progress.md` to understand what's complete
2. Identify which sub-agent(s) own the affected code
3. Consult the relevant `.github/agents/*.md` file for patterns
4. Implement using conventions in `memory-bank/systempatterns.md`
5. Update this file (`activecontext.md`) before ending the session
6. Run `.github/scripts/validate-agent-ownership.sh` to validate ownership

---

## Open Questions / Decisions Pending

_(None currently — all architectural decisions are documented in `systempatterns.md`)_

---

## How to Update This File

At the end of each session, replace the "Recent Changes" section with a summary of what was done. Add a new date header if multiple sessions occur on different dates. Keep this file concise — 1-2 paragraphs per session maximum.
