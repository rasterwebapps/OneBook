# Active Context — OneBook (Nexus Universal)

> **Current project state, recent changes, and what to focus on next.**  
> This file is updated at the end of every work session.

---

## Current Status

**Date:** 2026-04-09  
**Phase:** SDLC Agent System Implementation  
**Milestones:** ✅ M1–M10 Complete | 🔄 M11 In Progress | 📝 M12 Draft

---

## Recent Changes (Latest Session)

### Selective Documentation Update Strategy Implementation (2026-04-09)
- **UPDATED: `CLAUDE.md`** — Added full "Selective Documentation Update Protocol" section with:
  - Documentation Impact Matrix (11 change types → which docs to update/skip)
  - Decision flow for @docs agent (how to determine what needs updating)
  - Auto-generated docs guidance (run specific generators, not `generate-all`)
  - Three concrete examples (bug fix, new feature, keyboard shortcut)
- **UPDATED: `.github/copilot-instructions.md`** — Added "Selective Documentation Update Rule" to global Agent System section. All agents now see the rule that @docs updates ONLY affected docs.
- **UPDATED: `memory-bank/systempatterns.md`** — Added "Selective Documentation Update Protocol" pattern with decision flow, key rules, and rationale.
- **UPDATED: `memory-bank/activecontext.md`** — This file, recording the session changes.

**Strategy summary:** When @partner delegates to @docs in Phase 6, the @docs agent consults the Documentation Impact Matrix to determine which files to update. `activecontext.md` is always updated. All other docs are only touched if the current change directly affects their content. This prevents unnecessary doc regeneration, reduces errors, and speeds up the SDLC workflow.

### Previous Session: Automated Quality Gate Enforcement (2026-04-04)
- Added validate-quality-gates.sh (8 gates), sync-memory-bank.sh, quality-gate-baselines.conf
- CI updated with 5 jobs: validate-ownership, validate-quality-gates, sync-memory-bank, backend, frontend

---

## Current Project State

### Backend (Spring Boot 3.4+ / Java 21)
- **Package:** `com.nexus.onebook.ledger`
- **Migrations:** V1–V14 (all applied; V12 intentionally skipped)
- **Tests:** 514 passing
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
