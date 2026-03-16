# Active Context — OneBook (Nexus Universal)

> **Current project state, recent changes, and what to focus on next.**  
> This file is updated at the end of every work session.

---

## Current Status

**Date:** 2026-03-16  
**Phase:** Memory Bank Initialization  
**All Milestones:** ✅ Complete (M1–M10)

---

## Recent Changes (Latest Session)

### Memory Bank System Initialized (2026-03-16)
- **Created** `CLAUDE.md` at project root — AI memory bank entry point
- **Created** `memory-bank/` directory with 6 structured memory files:
  - `projectbrief.md` — Vision, goals, original architect prompt
  - `techcontext.md` — Stack, setup, build commands, conventions
  - `systempatterns.md` — Architecture decisions, critical rules
  - `activecontext.md` — This file (current state)
  - `progress.md` — Milestone tracker
  - `troubleshooting.md` — Known issues and fixes
- **Removed** unwanted PR-specific files that cluttered the repository:
  - `.github/BEFORE_AFTER.md` (PR summary)
  - `.github/COMPLETE_GUIDE.md` (duplicate guide)
  - `.github/SOLUTION_SUMMARY.md` (PR summary)
  - `.github/agents/IMPLEMENTATION_SUMMARY.md` (implementation summary)
  - `overview` (stray architect prompt file without .md extension)
- **Updated** `README.md` to reference CLAUDE.md and the memory bank
- **Updated** `.github/agents/README.md` to mention memory bank protocol
- **Updated** `sub-agents.md` with memory bank update protocol

---

## Current Project State

### Backend (Spring Boot 3.4+ / Java 21)
- **Package:** `com.nexus.onebook.ledger`
- **Migrations:** V1–V10 (all applied)
- **Tests:** 405+ passing
- **API:** All endpoints functional at `/api/*`

### Frontend (Angular 19+)
- **Modules:** 11 feature modules (all lazy-loaded standalone components)
- **Tests:** 105+ passing (2 pre-existing AppComponent failures known)
- **State:** Signals-based (no RxJS for simple state)

### Infrastructure
- PostgreSQL 17 + Redis 7 via Docker Compose
- GitHub Actions CI validates build, test, and agent ownership on every PR

---

## Active Focus Areas

Currently no active development tasks — all 10 milestones are complete.

**Ready for:**
- Additional AI/ML features
- New sector adapters (e.g., EduTech, Legal)
- Performance optimization (benchmarking, profiling)
- UI/UX refinements
- Additional compliance modules (VAT, IFRS reporting)

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
