# Active Context — OneBook (Nexus Universal)

> **Current project state, recent changes, and what to focus on next.**  
> This file is updated at the end of every work session.

---

## Current Status

**Date:** 2026-04-04  
**Phase:** Requirements & Milestones Restructuring  
**Milestones:** ✅ M1–M10 Complete | 🔄 M11 In Progress | 📝 M12 Draft

---

## Recent Changes (Latest Session)

### Requirements & Milestones Restructuring (2026-04-04)
- **milestones.md:** Added M11 (Payment Processing Pipeline) and M12 (Employee Advances & Settlement) milestone definitions with goals, deliverables, exit criteria, and dependency chain. Updated summary timeline and specialist roles table.
- **memory-bank/progress.md:** Updated overall status to reflect M11/M12. Added detailed M11 and M12 progress tracking sections. Updated documentation inventory to list REQ-011–014 separately. Fixed status header.
- **memory-bank/activecontext.md:** Fixed "Current Project State" — backend tests updated from 405+ to 489, migrations updated from V1–V10 to V1–V14.
- **memory-bank/techcontext.md:** Added `ledger/payment/` and `ledger/voucher/` packages to repo structure. Updated migration list to V1–V14 with descriptions.
- **memory-bank/troubleshooting.md:** Documented V12 migration gap (intentionally skipped).
- **sub-agents.md:** Expanded @LedgerExpert scope to M11/M12 with payment, voucher, and advance module ownership.
- **Auto-generated docs regenerated:** BRD, FRD, TRD, RTM, requirements-index — all now include 14 requirements (REQ-001 through REQ-014). Validation: 14/14 files pass, 0 errors, 0 warnings.

### Previous Session: Voucher-Receipt-Advance Settlement System (2026-04-04)
- Foundation Entities, Voucher Enums, Core Entities, DTOs, Services, Controllers
- Flyway V14 migration, 24 new unit tests, 489 backend tests passing

---

## Current Project State

### Backend (Spring Boot 3.4+ / Java 21)
- **Package:** `com.nexus.onebook.ledger`
- **Migrations:** V1–V14 (all applied; V12 intentionally skipped)
- **Tests:** 489 passing
- **API:** All endpoints functional at `/api/*`

### Frontend (Angular 19+)
- **Modules:** 15 feature modules (all lazy-loaded standalone components)
- **Tests:** 105+ passing (2 pre-existing AppComponent failures known)
- **State:** Signals-based (no RxJS for simple state)

### Infrastructure
- PostgreSQL 17 + Redis 7 via Docker Compose
- GitHub Actions CI validates build, test, and agent ownership on every PR

---

## Active Focus Areas

Authentication module is now complete. All 10 milestones remain complete.

**New authentication capability:**
- `/start` is the public landing page with OIDC login redirect
- All other routes are protected by `authGuard`
- Keycloak + OpenLDAP can be started with `docker compose up keycloak openldap`
- Demo users: `demo-admin`/`admin123!` and `demo-accountant`/`account123!`

**For backend integration (next steps):**
- Add `spring-boot-starter-oauth2-resource-server` to backend
- Configure JWT validation against Keycloak JWKS endpoint
- Extract `tenant_id` from JWT and set RLS context

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
