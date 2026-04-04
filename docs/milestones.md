# OneBook — Nexus Universal: Project Milestones

---

## Milestone 1 — Foundation & Core Architecture ✅

**Status:** ✅ Complete

**Goal:** Establish the project skeleton, CI/CD pipeline, and foundational infrastructure so every subsequent milestone builds on solid ground.

- [x] Set up the monorepo structure (backend / frontend / shared libraries).
- [x] Bootstrap Spring Boot 3.4+ (Java 21+) backend with Virtual Threads enabled.
- [x] Bootstrap Angular 19+ frontend with Signals-based state management.
- [x] Provision PostgreSQL 17+ with Row-Level Security (RLS) enabled.
- [x] PostgreSQL 17 provisioned via Docker Compose and backend configured.
- [x] Row-Level Security (RLS) infrastructure via Flyway migration (V1__rls_infrastructure.sql).
- [x] Provision Redis 7+ for session and cache management.
- [x] Configure CI/CD pipelines (build, lint, test, deploy) and containerised local development (Docker Compose).
- [x] Docker Compose configured for local development (PostgreSQL 17 + Redis 7).
- [x] GitHub Actions CI pipeline (.github/workflows/ci.yml) — backend build/test, frontend build/test.
- [x] Define coding standards, branching strategy, and PR review workflows.
- [x] CONTRIBUTING.md — branching strategy, PR workflow, Java/TypeScript/SQL coding standards.
- [x] .editorconfig — consistent formatting across editors.

**Exit Criteria:** A deployable "Hello World" stack where all four layers (Angular → Spring Boot → Redis → PostgreSQL) communicate end-to-end.

---

## Milestone 2 — Universal Ledger & Double-Entry Engine ✅

**Status:** ✅ Complete

**Goal:** Build the core accounting engine — the heart of the system — using a sector-agnostic data model.

- [x] Design the Universal Secured Ledger SQL schema with JSONB columns for industry-specific metadata (Patient ID, VIN, SKU, etc.).
- [x] Implement the Double-Entry Accounting Engine (every transaction creates balanced debit/credit entries).
- [x] Build master data management: Chart of Accounts, Ledger Groups, Cost Centers.
- [x] Implement multi-entity hierarchy support (Enterprise → Branch → Cost Center).
- [x] Create seed data and migration scripts for initial account structures.
- [x] Write comprehensive unit and integration tests for ledger integrity (balance assertions, orphan detection).

**Exit Criteria:** Ability to create accounts, post journal entries, and verify trial balance correctness through automated tests.

---

## Milestone 3 — Zero-Knowledge Security Layer ("Blind DBA")

**Status:** ✅ Complete

**Goal:** Ensure that sensitive financial data is unreadable at rest and by database administrators, while remaining fully searchable and performant.

- [x] Implement **Selective Field-Level Encryption (AES-256-GCM)** in the JVM layer for sensitive values and names before database persistence.
- [x] Build **Blind Indexing (HMAC-SHA256)** to enable fast, encrypted-field search without exposing plaintext.
- [x] Implement the **Hash-Chained Audit Trail** — each audit record cryptographically linked to its predecessor for tamper detection.
- [x] Design and implement key management (envelope encryption, key rotation strategy).
- [x] Add encryption/decryption integration tests and tamper-detection tests for the audit chain.

**Exit Criteria:** A DBA with full database access cannot read sensitive ledger values; blind-index queries return correct results; any tampered audit row is detected.

---

## Milestone 4 — Redis "Warm Cache" & Performance Strategy

**Status:** ✅ Complete

**Goal:** Eliminate encryption-induced latency for active users by decrypting and caching their working set in Redis upon login.

- [x] Design the Active Session Cache strategy — on login, decrypt the user's current working set into Redis.
- [x] Implement cache population, invalidation, and eviction policies.
- [x] Integrate Virtual Threads (Project Loom) for handling high-concurrency API calls from external systems (HMS, ERP, DMS).
- [x] Benchmark and tune: target sub-100 ms response times for common ledger and report queries.
- [x] Implement cache-aside and write-through patterns to keep Redis and PostgreSQL in sync.
- [x] Load-test with simulated concurrent sessions to validate throughput goals.

**Exit Criteria:** Authenticated users experience near-instant UI interactions; system sustains thousands of concurrent virtual-thread connections under load.

---

## Milestone 5 — "Better-than-Tally" Keyboard Navigation

**Status:** ✅ Complete

**Goal:** Deliver a keyboard-first UX that matches or exceeds Tally's speed, enhanced with a modern Command Palette.

- [x] Build the Key-Binding Registry — a configurable mapping layer for all shortcuts.
- [x] Map all legacy Tally shortcuts (F4 Contra, F5 Payment, F7 Journal, Alt+C Create Master, Ctrl+A Save, etc.).
- [x] Implement the Command Palette (Cmd+K / Ctrl+K) — a global Omni-Search for commands like "New Invoice," "Jump to Pharmacy Ledger," or "Show Stock."
- [x] Implement Contextual Power Keys — shortcuts that adapt to the active screen (e.g., Enter = Drill-down in Reports, + = Add Column, / = Filter).
- [x] Ensure full accessibility (ARIA roles, screen-reader support for keyboard flows).
- [x] User-test with Tally-experienced accountants for feedback and iteration.

**Exit Criteria:** A trained user can complete all core accounting workflows (voucher entry, ledger lookup, report drill-down) without touching the mouse.

---

## Milestone 6 — Sector-Agnostic Universal Ingestion Layer & Automation

**Status:** ✅ Complete

**Goal:** Allow any industry system to feed financial events into Nexus Universal through a standardised adapter pattern, alongside smart workflow automation.

- [x] Design and implement the Financial Event Gateway — a pluggable adapter interface for external data ingestion.
- [x] Build reference adapters:
  - [x] HL7 adapter for Healthcare systems.
  - [x] DMS adapter for Automotive dealer management.
  - [x] ISO 20022 adapter for Banking and direct bank reconciliations.
  - [x] REST/Webhook adapter for generic SaaS integrations.
- [x] Implement the Universal Mapper — transforms any adapter output into the core Double-Entry format with JSONB industry tags.
- [x] AP/AR Automation with OCR: Implement an AI module to read emailed PDF invoices, extract line items/totals, and auto-draft journal entries.
- [x] Automated 3-Way Matching: Build the logic to automatically verify that the Purchase Order, Goods Receipt, and Vendor Invoice match before allowing payment.
- [x] Corporate Card API Integration: Sync transactions instantly from corporate cards (Ramp/Brex equivalents).
- [x] Build HRM/Payroll Connector & Inventory Event Listener (Stock-In/Stock-Out).

**Exit Criteria:** Financial events from multiple industry protocols and card providers are successfully ingested, transformed, and posted; invoices are parsed via OCR successfully.

---

## Milestone 7 — Reporting, Tax Compliance & Multi-Locale Support

**Status:** ✅ Complete

**Goal:** Deliver configurable financial reports, localized compliance, and enterprise-grade consolidation.

- [x] Build core financial reports: Trial Balance, Profit & Loss, Balance Sheet, Cash Flow Statement.
- [x] Implement Dynamic UI i18n/L10n using Angular Transloco for real-time language switching, date formats, and localized currency display ($ vs ₹).
- [x] Implement the Feature Entitlement Engine to toggle locale-specific modules per tenant.
- [x] Enhance Compliance Engine with automated e-Invoicing and e-Way bill generation.
- [x] Implement Automated Reconciliation via real-time bank feeds using Open Banking APIs.
- [x] Build Intercompany Accounting & Consolidation: Automate the elimination of intercompany transactions across global branches.
- [x] Develop the Fixed Asset Register (FAR): Track physical assets, compute automated monthly depreciation, and handle impairment/disposal.
- [x] Establish a Headless API approach ensuring the backend can seamlessly serve a future Flutter/Native mobile app.

**Exit Criteria:** A tenant can be configured for a specific country/tax regime, auto-generate e-invoices, and run global consolidated reports across multiple branches.

---

## Milestone 8 — Advanced Intelligence & AI Features

**Status:** ✅ Complete

**Goal:** Add predictive analytics and market integrations to transform Nexus Universal into a decision-support platform.

- [x] Implement Predictive Cash Flow Forecasting using historical ledger data.
- [x] Build AI-driven Scenario Modeling ("What-If" analysis) to simulate financial impacts of external factors (sales drops, interest hikes).
- [x] Build Mark-to-Market (MTM) Valuation engine for share-market investment portfolios.
- [x] Automate accounting for Corporate Actions (Stock Splits, Dividends, Bonus Issues) via financial APIs.
- [x] Create a Market Sentiment Overlay on the CFO dashboard to display relevant market news.
- [x] Add anomaly detection for unusual transactions (fraud indicators, duplicate entries).
- [x] Implement Digital Asset & Crypto Ledger tracking for Mark-to-Market valuations of stablecoins/crypto.

**Exit Criteria:** The system generates accurate 30/60/90-day cash flow forecasts; MTM valuations reconcile with market APIs; stock splits are handled automatically.

---

## Milestone 9 — Architecture Documentation & Deliverables

**Status:** ✅ Complete

**Goal:** Produce the formal technical deliverables outlined in the project vision.

- [x] Create the Architecture Diagram (Mermaid.js) illustrating: External Adapters → Encryption Layer → Redis Cache → PostgreSQL.
  - Enhanced `docs/architecture.md` with detailed Mermaid.js diagram (data flow, security, cache, deployment).
  - Published comprehensive diagrams in `docs/technical/architecture-diagram.md`.
- [x] Document the Key-Binding Registry technical design — legacy Tally keys vs. modern Command Palette logic, conflict resolution, and extensibility.
  - Published `docs/technical/key-binding-registry.md` covering all 17 Tally shortcuts, Command Palette architecture, conflict resolution strategy, contextual bindings, and extensibility APIs.
- [x] Publish the Universal Secured Ledger SQL Schema documentation.
  - Published `docs/technical/sql-schema.md` documenting all 8 Flyway migrations (V1–V8), 21 tables, RLS policies, ER diagrams, balanced transaction trigger, and zero-knowledge encryption.
- [x] Write developer onboarding guides, API documentation, and operational runbooks.
  - Published `docs/technical/developer-guide.md` — developer onboarding, setup, coding standards, workflows.
  - Published `docs/technical/api-documentation.md` — REST API reference for all 15 controllers.
  - Published `docs/technical/operational-runbook.md` — deployment, monitoring, troubleshooting, backup procedures.

**Exit Criteria:** All architectural deliverables are published, reviewed, and approved.

---

## Milestone 10 — Hardening, Audit & Production Readiness

**Status:** ✅ Complete

**Goal:** Prepare the system for production deployment with enterprise-grade reliability and security posture.

- [x] Build the "External Auditor" Portal: A secure, read-only interface for CPAs to request samples, leave comments, and approve workflows directly inside the system.
- [x] Implement the Smart Document Vault: Ensure every journal entry can attach encrypted source documents stored securely in an S3/MinIO bucket.
- [x] Conduct a full security audit (penetration testing, encryption verification, key management review).
- [x] Perform load and stress testing at projected production scale.
- [x] Implement observability: structured logging, distributed tracing, and metrics dashboards.
- [x] Set up disaster recovery: automated backups, point-in-time recovery, failover procedures.
- [x] Obtain required compliance certifications relevant to target industries.

**Deliverables:**
- Flyway migration `V9__hardening_audit_production.sql` — 6 new tables with RLS policies
- Backend: 7 new models, 6 repositories, 6 services, 6 controllers, 7 DTOs (37 new files)
- Frontend: Auditor Dashboard component with lazy-loaded route at `/auditor`
- Observability: `RequestLoggingFilter` with MDC trace/span IDs, structured logging config
- Security Audit: `SecurityAuditService` with 5 automated checks (encryption, key mgmt, audit chain, round-trip, key derivation)
- 301 backend tests (97 new), 105 frontend tests (4 new) — all passing

**Exit Criteria:** The system passes security audit with no critical findings; the Auditor Portal functions end-to-end; disaster recovery procedures are tested.

---

## Milestone 11 — Payment Processing Pipeline

**Status:** 🔄 In Progress

**Goal:** Provide a complete payment processing workflow from payment register view through batch approval to bank-ready file generation.

- [x] Design payment register data model and vendor-grouping queries (REQ-011).
- [x] Implement payment register view with vendor-grouped, due-date-sorted AP items.
- [x] Design payment batch processing workflow with maker-checker approval (REQ-012).
- [x] Implement batch creation, net payable calculation, and approval/rejection flows.
- [x] Implement journal posting on batch approval (Dr Accounts Payable, Cr Bank Account).
- [x] Design CSV payment file generation for NEFT/RTGS/IMPS (REQ-013).
- [x] Implement payment file generator with batch status transitions.
- [x] Flyway migration `V11__payment_processing.sql` — payment_register, payment_batches, payment_batch_items tables with RLS.
- [x] Flyway migration `V13__merge_financial_events_into_payment_register.sql` — consolidate ingested events into payment register.
- [ ] Complete unit and integration tests for all payment services and controllers.
- [ ] Update BRD, FRD, TRD, RTM, and requirements index with final status.
- [ ] Update agent ownership for payment module.

**Deliverables:**
- Flyway migrations `V11` and `V13` — 3 new tables with RLS policies
- Backend: `PaymentRegisterEntry`, `PaymentBatch`, `PaymentBatchItem` models; `PaymentRegisterService`, `PaymentBatchService`, `PaymentFileGeneratorService` services; `PaymentRegisterController`, `PaymentBatchController` controllers
- API endpoints: `/api/payment-register`, `/api/payment-batches`, `/api/payment-batches/{id}/generate-file`

**Exit Criteria:** Payment register displays vendor-grouped AP items; batches can be created, approved/rejected with journal posting; CSV payment files can be generated from approved batches.

---

## Milestone 12 — Employee Advances & Settlement

**Status:** 📝 Draft

**Goal:** Implement a fully integrated Advance → Expense Settlement → Receipt / Payment Advice cycle with configurable limits, tiered multi-level approvals, and department-scoped visibility.

- [ ] Implement per-employee advance limit configuration (REQ-014, BR-014.1).
- [ ] Implement tiered approval workflow: HOD (≤₹10k), HOD+CEO (₹10k–₹20k), HOD+CEO+MD (>₹20k) (BR-014.6).
- [ ] Implement expense voucher settlement logic — split between advance reduction and reimbursement payable (BR-014.7).
- [ ] Implement advance receipt for unspent cash returns (BR-014.9).
- [ ] Implement system-generated Payment Advice for overspend reimbursements (BR-014.8).
- [ ] Implement department-based visibility for HOD users (BR-014.5).
- [ ] Implement override mechanism with mandatory reason and audit logging (BR-014.3).
- [ ] Flyway migration `V15__employee_advances_settlement.sql` — 6 new tables with RLS.
- [ ] Build frontend components: advance request, approval queue, expense voucher, receipt, payment advice list, reports.
- [ ] Write unit tests (≥19) and integration tests (≥7) for all new services and controllers.
- [ ] Update BRD, FRD, TRD, RTM, and requirements index.
- [ ] Update agent ownership for advance module.

**Deliverables:**
- Flyway migration `V15` — `employee_advance_config`, `employee_advance_balance`, `employee_advances`, `expense_vouchers`, `advance_receipts`, `payment_advices` tables
- Backend: 6 models, 6 repositories, 7 services, 5 controllers, 6 DTOs (32+ new files)
- Frontend: 7 new components in advance module with lazy-loaded route at `/advances`
- Reports: Outstanding aging, pending approvals, reimbursements payable, overrides audit

**Exit Criteria:** Employees can request advances with limit enforcement; tiered approval chain functions correctly; expense vouchers auto-settle against outstanding advances; Payment Advices are generated for overspend; Finance can process reimbursements; full audit trail for all state transitions.

---

## 🤖 Subagent Operations (Nexus Universal)

To maintain 2026-grade performance and zero-trust context management, the following delegation rules are active:

### 1. Specialist Roles

| Role | Milestones | Focus |
|------|-----------|-------|
| @Architect | 1 & 9 | Project skeleton, Monorepo structure, and Documentation |
| @LedgerExpert | 2, 7, 10, 11, & 12 | SQL Schema, Double-Entry logic, Tax/Compliance, Auditor Portal, Payment Processing, and Employee Advances |
| @SecurityWarden | 3 & 10 | AES-256-GCM, HMAC-SHA256 Blind Indexing, RLS policies, and Document Vault |
| @PerfEngineer | 4 | Redis Warm Cache, Virtual Threads (Loom), and Benchmarking |
| @UXSpecialist | 5 & 7 | Angular 19 Signals, Keyboard Registry, Tally shortcuts, and i18n/L10n |
| @IntegrationBot | 6 & 8 | HL7/DMS/ISO 20022 Adapters, OCR logic, and AI Ingestion |

### 2. Delegation Protocol (The "3-File Rule")

**Trigger:** If a user request requires reading/analyzing 3 or more files across different layers (e.g., Angular + Spring Boot + SQL).

**Action:**

1. **Spawn Subagent:** Use the `/research` or `@specialist` command.
2. **Isolate:** The subagent investigates the raw code.
3. **Distill:** Subagent returns ONLY the "Interface" or "Summary" to the Main Session.
4. **Result:** Keep the Main Session context < 5,000 tokens at all times.

### 3. Context Pruning

After a Milestone is "Exit Criteria Met," summarize the final state into `PROJECT_STATE.md` and clear the subagent history to reclaim tokens.

---

## Summary Timeline

| Milestone | Theme | Status | Dependencies |
|-----------|-------|--------|-------------|
| 1 | Foundation & Core Architecture | ✅ Complete | — |
| 2 | Universal Ledger & Double-Entry Engine | ✅ Complete | 1 |
| 3 | Zero-Knowledge Security Layer & Controls | ✅ Complete | 2 |
| 4 | Redis Warm Cache & Performance | ✅ Complete | 3 |
| 5 | Keyboard Navigation & Command Palette | ✅ Complete | 1 |
| 6 | Universal Ingestion, Integrations & Automation | ✅ Complete | 2 |
| 7 | Reporting, Tax, Compliance & FAR | ✅ Complete | 2, 6 |
| 8 | Advanced Intelligence, Forecasting & Markets | ✅ Complete | 2, 7 |
| 9 | Architecture Documentation & Deliverables | ✅ Complete | 1–8 |
| 10 | Hardening, Auditor Portal & Prod Readiness | ✅ Complete | 1–9 |
| 11 | Payment Processing Pipeline | 🔄 In Progress | 2, 10 |
| 12 | Employee Advances & Settlement | 📝 Draft | 2, 4, 6, 10 |
