# Active Context — OneBook (Nexus Universal)

> **Current project state, recent changes, and what to focus on next.**  
> This file is updated at the end of every work session.

---

## Current Status

**Date:** 2026-04-13  
**Phase:** Post-M12 — Backend Package Refactoring Complete  
**Milestones:** ✅ M1–M12 Complete

---

## Recent Changes (Latest Session)

### Package Flattening: Removed `ledger/` Intermediate Directory (2026-04-13)
- **REFACTORED: All 24 domain submodules moved** from `com.nexus.onebook.ledger.*` to `com.nexus.onebook.*` — the `ledger/` intermediate package has been eliminated.
- **Java package path**: All `package com.nexus.onebook.ledger.*` declarations and `import com.nexus.onebook.ledger.*` statements updated project-wide (492 Java files, main + test trees).
- **Filesystem**: All directories moved from `backend/src/main/java/com/nexus/onebook/ledger/<module>/` to `backend/src/main/java/com/nexus/onebook/<module>/`. Same for test tree.
- **`LedgerIntegrationTest.java`**: Moved from `ledger/` root to `com.nexus.onebook` root (package was already correct).
- **Agent/docs/scripts updated**: `validate-agent-ownership.sh`, `backend.agent.md`, `security.agent.md`, `MAINTENANCE.md`, developer-guide, memory-bank, glossary.

### Backend Domain Submodule Repackaging (2026-04-13)
- **REFACTORED: Backend Java package structure** — Eliminated flat shared directories under `ledger/` and replaced with 24 domain submodules, each owning its own controller/, service/, model/, repository/, and dto/ subdirectories.
- **OLD FLAT STRUCTURE removed**: `ledger/controller/` (26), `ledger/service/` (40), `ledger/model/` (80), `ledger/repository/` (44), `ledger/dto/` (42)
- **24 DOMAIN SUBMODULES directly under `com.nexus.onebook`**: `accounts`, `advance`, `auditor`, `banking`, `cache`, `clientaccount`, `compliance`, `credit`, `currency`, `dashboard`, `entitlement`, `exception`, `fixedasset`, `foundation`, `ingestion`, `intelligence`, `inventory`, `operations`, `payment`, `payroll`, `reporting`, `security`, `tenant`, `voucher`
- **BUG FIX**: Spring bean name conflicts resolved — `advance` vs `voucher` modules both had `PaymentAdviceController`/`PaymentAdviceService` — now qualified with unique bean names.
- **TESTS**: 562 backend tests pass, 0 failures. CodeQL 0 alerts.

### M12 Employee Advances — FULL STACK IMPLEMENTATION (2026-04-13)
- **COMPLETED: M12 Employee Advances & Settlement** — Full frontend implementation:
  - **Frontend Module**: `advances/` with 4 standalone components
    - `MyAdvancesComponent` (`/advances`) — Employee view of advances with create modal, settlement progress, approval chain display
    - `ApprovalQueueComponent` (`/advances/approvals`) — HOD/CEO/MD approval queue with urgent indicators, override support
    - `ExpenseVoucherComponent` (`/expense-vouchers`) — Expense submission with advance settlement preview
    - `PaymentAdviceListComponent` (`/payment-advices`) — Reimbursement payment tracking
  - **Models**: `advance.models.ts` — 10 TypeScript interfaces mirroring backend DTOs
  - **Services**: `AdvanceService` — HTTP client for all advance API endpoints
  - **Routes**: 4 new routes in `app.routes.ts`
  - **Tests**: 27 new frontend tests across 4 component spec files (all passing)
- **FILES CHANGED**:
  - `frontend/src/app/advances/components/my-advances/*`
  - `frontend/src/app/advances/components/approval-queue/*`
  - `frontend/src/app/advances/components/expense-voucher/*`
  - `frontend/src/app/advances/components/payment-advice-list/*`
  - `frontend/src/app/advances/models/advance.models.ts`
  - `frontend/src/app/advances/services/advance.service.ts`
  - `frontend/src/app/app.routes.ts`
  - `docs/requirements/active/REQ-014-employee-advances-and-settlement.md`
  - `memory-bank/progress.md`
- **BUILD:** Passes cleanly (no warnings)
- **TESTS:** 294 frontend tests (279 pass, 15 pre-existing failures)

### M12 Backend Implementation (2026-04-13, earlier session)
- **COMPLETED: M12 Backend Layer** — Complete employee advances module:
  - **V15 Migration**: `V15__employee_advances_settlement.sql` — 8 tables with RLS policies
  - **Models**: 12 model classes (EmployeeAdvance, ExpenseVoucher, AdvanceReceipt, EmployeePaymentAdvice, enums)
  - **DTOs**: 8 DTO records (request/response for all entities)
  - **Repositories**: 6 repositories with tenant-scoped queries
  - **Services**: 4 services implementing tiered approval workflow
  - **Controllers**: 4 REST controllers
  - **Tests**: 35 unit tests across 4 service test classes
- **BUILD:** Passes cleanly | **TESTS:** All 35 new advance tests pass

### Previous Session: Modern Fintech-Style Dashboard Redesign (2026-04-10)
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
- **Package:** `com.nexus.onebook`
- **Structure:** 24 submodules (8 pre-existing + 16 new domain submodules from refactoring)
- **Migrations:** V1–V15 (all applied; V12 intentionally skipped)
- **Tests:** 562 passing
- **API:** All endpoints functional at `/api/*`
- **Domain Submodules:** `accounts`, `advance`, `auditor`, `banking`, `cache`, `clientaccount`, `compliance`, `credit`, `currency`, `dashboard`, `entitlement`, `exception`, `fixedasset`, `foundation`, `ingestion`, `intelligence`, `inventory`, `operations`, `payment`, `payroll`, `reporting`, `security`, `tenant`, `voucher`

### Frontend (Angular 19+)
- **Modules:** 18 feature modules (all lazy-loaded standalone components)
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

## Next Steps (Post-M12)

All 12 milestones are now complete. Possible future enhancements:

1. **Enhanced Reporting:**
   - Advance aging report
   - Outstanding balance report by department
   - Override audit report

2. **Integration Improvements:**
   - Real backend API integration (replace mock data)
   - Keycloak authentication enablement
   - Real-time notifications for approval workflows

3. **UX Enhancements:**
   - Keyboard shortcuts for advance operations
   - Bulk approval functionality
   - File attachment support for expense vouchers

---

## Open Questions / Decisions Pending

_(None currently — all architectural decisions are documented in `systempatterns.md`)_

---

## How to Update This File

At the end of each session, replace the "Recent Changes" section with a summary of what was done. Add a new date header if multiple sessions occur on different dates. Keep this file concise — 1-2 paragraphs per session maximum.
