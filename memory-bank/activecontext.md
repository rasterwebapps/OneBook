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

### SDLC Agent System with @partner Orchestrator (2026-04-09)
- **NEW: `.github/copilot-instructions.md`** — Global cross-cutting rules injected into ALL Copilot interactions (BigDecimal, tenantId, DTOs, naming conventions, build commands)
- **NEW: `.github/agents/partner.agent.md`** — Head orchestrator agent (`@partner`). The ONLY agent users invoke. Combines BA + PM + Team Lead roles. Implements full SDLC workflow with Agile feedback loops.
- **NEW: `.github/agents/backend.agent.md`** — Backend dev agent (`@backend`). Consolidates @LedgerExpert, @IntegrationBot, @AIEngineer, @ComplianceAgent, @PerfEngineer knowledge. Sub-task decomposition: Model → Repository → Service → Controller → Tests.
- **NEW: `.github/agents/frontend.agent.md`** — Frontend dev agent (`@frontend`). Consolidates @UXSpecialist knowledge. Standalone Angular components, Signals, OnPush, i18n, keyboard navigation.
- **NEW: `.github/agents/database.agent.md`** — DB design agent (`@database`). Schema design, RLS policies, Flyway migrations, NUMERIC(19,4), TIMESTAMPTZ conventions.
- **NEW: `.github/agents/security.agent.md`** — Security agent (`@security`). AES-256-GCM, blind indexes, RLS, audit trails, Blind DBA model.
- **NEW: `.github/agents/infra.agent.md`** — Infrastructure agent (`@infra`). Docker, CI/CD, Redis, Spring config.
- **NEW: `.github/agents/docs.agent.md`** — Documentation agent (`@docs`). API docs, schema docs, memory bank, diagrams.
- **NEW: `.github/agents/quality.agent.md`** — QA agent (`@quality`). 7-check testing protocol, regression detection, quality gates.
- **UPDATED: `.github/agents/README.md`** — Rewritten with Tier 1 (invocable agents) and Tier 2 (knowledge base) architecture, SDLC mapping, feedback loop protocol.
- **UPDATED: `.github/agents/INDEX.md`** — Added SDLC Agent System section with all 8 invocable agents.
- **UPDATED: `CLAUDE.md`** — Agent System section updated with new Tier 1/Tier 2 architecture, file map updated with all new files.
- **Existing `.md` files preserved** — All 11 legacy agent docs remain as knowledge base, referenced by Tier 1 agents.

### SDLC Workflow Implemented
The agent system maps to a real-world SDLC with Agile feedback loops:
1. User → @partner (requirement intake)
2. @partner analyzes, classifies, plans phases
3. @partner delegates to @database → @backend → @frontend → @security
4. @partner validates each phase output (feedback loop)
5. @quality runs full test suite (feedback loop on failures)
6. @docs updates all documentation and memory bank
7. @partner delivers final status report

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
