# Active Context — OneBook (Nexus Universal)

> **Current project state, recent changes, and what to focus on next.**  
> This file is updated at the end of every work session.

---

## Current Status

**Date:** 2026-04-10  
**Phase:** SDLC Agent System Implementation  
**Milestones:** ✅ M1–M10 Complete | 🔄 M11 In Progress | 📝 M12 Draft

---

## Recent Changes (Latest Session)

### Modern Fintech-Style Dashboard Redesign (2026-04-10)
- **REDESIGNED: Dashboard** with modern fintech UI patterns inspired by Mercury/Stripe:
  - **4 KPI Cards** (top row): Cash, Bank, Pending Validations, Failed PANs
    - Each card has `shadow-sm` (subtle shadow), `border-slate-100` (extremely faint borders)
    - Small trend badges showing percentage changes (↑/↓) with colored backgrounds
    - `tabular-nums` for all numeric values, `font-mono` for amounts
  - **2-Column Grid** (middle section):
    - **Left**: Cashflow chart placeholder with `bg-slate-50` background, SVG area chart visualization, metrics (Inflows/Outflows/Net)
    - **Right**: Compact Recent Activity list showing last 5 vouchers with right-aligned amounts using `tabular-nums`
  - **Secondary Section**: Quick Actions (4 action buttons) + Integrations status
  - Clean white background throughout, Tailwind-inspired color palette
- **FILES CHANGED**:
  - `dashboard.component.html` — Complete template rewrite with new fintech layout
  - `dashboard.component.ts` — Added signals: `bankBalance()`, `pendingValidations()`, `failedPANs()`, `recentVouchers()`
  - `dashboard.component.scss` — Trimmed from 21KB to ~11KB, removed legacy styles, added new fintech styles
- **BUILD:** Passes cleanly | **TESTS:** 217 pass, 15 pre-existing failures — no regressions

### Previous Session: UI/UX Shared Component Migration — All Screens (2026-04-10)
- **MIGRATED: 11 feature screens** to use the shared UI/UX component library. Previously only 3 screens (Dashboard, Voucher Explorer, Reports) used shared components — now **all 14 routed screens** use them:
  - **Ledger** → `<nx-page-header>`, `<nx-search-input>`, `<nx-loading-spinner>`, `<nx-empty-state>`
  - **Banking** → `<nx-page-header>`, `<nx-status-badge>` (replaced inline recon-status CSS)
  - **Inventory** → `<nx-page-header>`, `<nx-stat-card>`, `<nx-search-input>`, `<nx-status-badge>` (replaced hardcoded stat cards and stock status CSS)
  - **GST Dashboard** → `<nx-page-header>`, `<nx-status-badge>` (replaced inline badge CSS)
  - **Masters** → `<nx-page-header>`, `<nx-search-input>`, `<nx-empty-state>`
  - **Client Accounts** → `<nx-page-header>`, `<nx-search-input>`, `<nx-empty-state>`
  - **Payment Register** → `<nx-page-header>`, `<nx-search-input>`, `<nx-status-badge>`, `<nx-empty-state>`
  - **AI Dashboard** → `<nx-page-header>`, `<nx-loading-spinner>`
  - **Market Valuation** → `<nx-page-header>`, `<nx-loading-spinner>`
  - **Auditor Dashboard** → `<nx-page-header>`, `<nx-loading-spinner>`
  - **Accounts Receivable** → `<nx-page-header>`, `<nx-search-input>`, `<nx-status-badge>`
- **REMOVED: Duplicate CSS** — Removed inline status badge styles, hardcoded stat card CSS, stock-status CSS that duplicated shared component functionality
- **BUILD:** Passes cleanly | **TESTS:** 217 pass, 15 pre-existing failures (7 AppComponent + 8 StartComponent) — no regressions

**Summary:** All 14 application screens now consistently use the shared `nx-*` component library. Every screen has a standardized `<nx-page-header>` for its title. Search inputs use `<nx-search-input>` with debounce. Loading states use `<nx-loading-spinner>` with accessible aria labels. Empty states use `<nx-empty-state>`. Status badges use `<nx-status-badge>` with automatic status-to-color mapping.

### Previous Session: Shared Components & Theme Compliance (2026-04-10)
- **CREATED: 6 new shared components** — All 6 previously-identified gaps are now implemented, tested, and documented:
  - `NxPageHeaderComponent` (`<nx-page-header>`) — Consistent page title + subtitle + projected action buttons
  - `NxSearchInputComponent` (`<nx-search-input>`) — Debounced search field with SVG icon, clear button, focus ring
  - `NxLoadingSpinnerComponent` (`<nx-loading-spinner>`) — Accessible spinner with `role="status"` and `.sr-only` fallback
  - `NxConfirmDialogComponent` (`<nx-confirm-dialog>`) — Service-driven confirmation dialog with promise API
  - `NxStatusBadgeComponent` (`<nx-status-badge>`) — Auto-maps 16+ accounting statuses to color variants
  - `NxToastComponent` (`<nx-toast>`) — Service-driven slide-in toast notifications with auto-dismiss
- **ADDED: Global CSS utilities** — `.form-grid`, `.form-group`, `.form-control`, `.sr-only`, `.page-container`, `.nx-btn--danger`, `.nx-btn--ghost`, `.nx-btn--outline`, `prefers-reduced-motion` media query
- **FIXED: Font inlining for CI** — Disabled Google Fonts optimization in `angular.json` to prevent build failures in sandboxed environments
- **UPDATED: Dashboard** — Now uses `<nx-page-header>`, `<nx-stat-card>`, `<nx-card>` shared components instead of raw CSS classes
- **UPDATED: Voucher Explorer** — Header uses `<nx-page-header>`, status column uses `<nx-status-badge>`
- **UPDATED: Reports** — Uses `<nx-page-header>`, `<nx-loading-spinner>`, `<nx-empty-state>`, and standardized button classes
- **UPDATED: App shell** — `<nx-toast>` and `<nx-confirm-dialog>` placed in `app.component.html` for global availability
- **ADDED: 50 unit tests** — All 6 new components have comprehensive spec files (50 tests, all passing)
- **UPDATED: `docs/technical/ui-ux-guidelines.md`** — Marked all 6 components as implemented, added usage examples, updated CSS utility table

**Summary:** The shared component library is now complete with 13 components (7 existing + 6 new). Feature pages are migrated to use the design system. Global CSS utilities support forms, accessibility, and reduced-motion preferences. All changes build cleanly with no warnings.

### Previous Session: UI/UX Agent Guidelines & Skill Enhancement (2026-04-09)
- **CREATED: `docs/technical/ui-ux-guidelines.md`** — Comprehensive, single-source-of-truth UI/UX guidelines document with 14 sections covering: design system overview, component library reference, page structure patterns, accounting-specific UI patterns, keyboard-first UX, navigation & layout, responsive breakpoints, dark mode, animation guidelines, i18n, print styles, CSS custom properties reference, accessibility, and do's/don'ts. This consolidates previously fragmented knowledge from `styles.scss`, shared components, keyboard services, and tribal knowledge.
- **UPDATED: `.github/skills/create-angular-component/SKILL.md`** — Enhanced the @frontend Copilot Skill with: UI/UX guidelines reference, page template patterns (list, dashboard, form), shared component usage table, accounting-specific patterns section, dark mode checklist, accessibility checklist, breadcrumb registration, and expanded i18n translation structure.
- **UPDATED: `.github/copilot-instructions.md`** — Added UI/UX guidelines reference to the global References section so all agents can discover the document.
- **Identified 6 missing shared components**: `nx-page-header`, `nx-search-input`, `nx-loading-spinner`, `nx-confirm-dialog`, `nx-status-badge`, `nx-toast` — documented in guidelines for future implementation.

**Summary:** OneBook now has a unified UI/UX guidelines document that exceeds the RasterOneLab equivalent. It covers 14 sections (vs 10), documents 60+ CSS tokens (vs 14), includes keyboard-first UX, dark mode, and i18n that RasterOneLab lacks, and identifies 6 component gaps for future work.

### Previous Session: Copilot Skills & Environment Setup (2026-04-09)
- **CREATED: `.github/copilot-setup-steps.yml`** — Configures Copilot cloud agent environment with PostgreSQL 17, Redis 7, Java 21, and Node.js so agents can build, test, and run the full stack.
- **CREATED: `.github/skills/` (8 skills)** — Added Copilot Skills for all 8 agents:
  - `analyze-requirement` — @partner: classify, plan, track requirements through SDLC
  - `add-rest-endpoint` — @backend: scaffold DTO → Repository → Service → Controller → Test
  - `create-angular-component` — @frontend: standalone component with Signals, i18n, routing
  - `create-flyway-migration` — @database: migration with RLS, tenant isolation, proper types
  - `security-review` — @security: RLS, encryption, secrets, OWASP compliance review
  - `setup-dev-environment` — @infra: Docker Compose dev environment management
  - `update-documentation` — @docs: Selective Documentation Update Protocol
  - `run-quality-gates` — @quality: full quality validation pipeline (8 gates)
- **UPDATED: All 8 `.agent.md` files** — Added `skills:` frontmatter and Skills reference section
- **UPDATED: `.github/copilot-instructions.md`** — Added Copilot Skills table to Agent System section
- **UPDATED: `.github/agents/INDEX.md`** — Added skills table with locations
- **UPDATED: `.github/agents/README.md`** — Added Copilot Skills section and Environment Setup
- **UPDATED: `CLAUDE.md`** — Added Copilot Skills table, Environment Setup, and skills/ to Key File Map

**Summary:** Agents define WHO does the work (persona + rules + scope). Skills define HOW to do specific tasks (step-by-step procedures + templates). Together they form the complete Copilot automation layer. The copilot-setup-steps.yml ensures PostgreSQL and Redis are available for dev builds.

### Previous Session: Selective Documentation Update Strategy (2026-04-09)
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
- **Shared Components:** 13 (`nx-card`, `nx-stat-card`, `nx-badge`, `nx-amount`, `nx-skeleton`, `nx-empty-state`, `nx-data-table`, `nx-page-header`, `nx-search-input`, `nx-loading-spinner`, `nx-confirm-dialog`, `nx-status-badge`, `nx-toast`)
- **Tests:** 217+ passing (15 pre-existing failures: 7 AppComponent + 8 StartComponent OAuthService dependency)
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
