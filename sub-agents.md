# OneBook — Nexus Universal: Sub-Agent Architecture & Analysis

---

## Overview

This document analyses all project requirements across Milestones 1–10 and proposes a comprehensive set of **specialist sub-agents** designed to manage context, enforce domain boundaries, and maintain 2026-grade engineering quality across the full Nexus Universal platform.

Each sub-agent is a domain-expert role with a clear scope, a defined set of files/modules it owns, the milestones it serves, and the specific skills it brings. The goal is to ensure that no single context window needs to hold the entire codebase — instead, each agent operates within its bounded context and communicates via well-defined interfaces.

**✅ Agent Instructions Available:** Detailed instruction files for all 10 sub-agents are available in [`.github/agents/`](.github/agents/). Each file contains scope, responsibilities, design patterns, best practices, and collaboration guidelines. See [Agent Instructions README](.github/agents/README.md) and [Design Requirements Index](.github/agents/INDEX.md) for quick reference.

**🧠 Memory Bank Available:** This project uses a persistent AI memory bank at [`CLAUDE.md`](CLAUDE.md) and [`memory-bank/`](memory-bank/). All agents MUST read `CLAUDE.md` at the start of each session and update `memory-bank/activecontext.md` at the end.

---

## Why Sub-Agents?

Nexus Universal spans **7 backend packages**, **11 frontend modules**, **9 Flyway migrations**, **6 documentation sets**, and **500+ tests** (405+ backend, 105+ frontend). A single agent context cannot hold all of this simultaneously without degrading quality. Sub-agents solve this by:

1. **Bounded Context:** Each agent knows its domain deeply — it reads only the files it owns.
2. **Parallel Execution:** Independent agents can work simultaneously on different layers.
3. **Quality Enforcement:** Each agent enforces its domain's specific conventions and standards.
4. **Context Budget:** The main orchestrator stays under ~5,000 tokens by delegating raw code inspection to specialists.
5. **Design Requirements:** Each agent has detailed instruction files in `.github/agents/` with patterns, conventions, and examples.
6. **Persistent Memory:** The memory bank in `memory-bank/` ensures agents continue from accumulated intelligence, not from zero.

---

## Memory Bank Protocol

Every agent MUST follow this protocol for persistent context:

### Session Start
1. Read `CLAUDE.md` — understand project state, tech stack, critical rules
2. Read `memory-bank/activecontext.md` — understand what changed recently
3. Read `memory-bank/progress.md` — understand milestone and feature status
4. Consult relevant `.github/agents/*.md` file for domain-specific patterns

### During Session
- Consult `memory-bank/systempatterns.md` for architecture decisions
- Consult `memory-bank/troubleshooting.md` for known issues
- Follow patterns from `memory-bank/techcontext.md`

### Session End (REQUIRED)
1. Update `memory-bank/activecontext.md` — record what was done, what changed
2. Update `memory-bank/progress.md` — if any milestone items were completed
3. Update `memory-bank/systempatterns.md` — if new patterns or decisions were made
4. Update `memory-bank/troubleshooting.md` — if bugs were found and fixed
5. Update `.github/agents/*.md` — if new services/controllers/modules were added
6. Run `.github/scripts/validate-agent-ownership.sh` — confirm ownership is complete

---

## Quick Reference

For detailed design patterns and implementation guidelines, see:
- [CLAUDE.md](CLAUDE.md) - **AI memory bank entry point** (read first)
- [Memory Bank](memory-bank/) - Persistent project intelligence files
- [Agent Instructions Directory](.github/agents/README.md) - Overview of all 10 agents
- [Design Requirements Index](.github/agents/INDEX.md) - Quick reference by category
- Individual agent files in `.github/agents/` - Comprehensive patterns and conventions

---

## Proposed Sub-Agents

### 1. 🏗️ @Architect — Foundation & Structure Agent

**Milestones Served:** M1 (Foundation), M9 (Documentation)

**What It Owns:**
- Root configuration files: `docker-compose.yml`, `.editorconfig`, `CONTRIBUTING.md`, `README.md`
- CI/CD pipeline: `.github/workflows/ci.yml`
- Backend entry point: `OneBookApplication.java`, `HealthController.java`
- Spring configuration: `com.nexus.onebook.config/` (all `*Config.java` files)
- Architecture docs: `architecture.md`, `docs/architecture-diagram.md`, `docs/developer-guide.md`
- Flyway migration structure and naming conventions

**What It Does:**
- Ensures the monorepo structure remains clean (backend / frontend / docs separation).
- Validates that CI/CD pipelines cover build, lint, and test for both layers.
- Maintains Docker Compose service definitions (PostgreSQL 17, Redis 7).
- Reviews Spring Boot configuration (Virtual Threads, CORS, JSON negotiation via `HeadlessApiConfig`).
- Generates and updates Mermaid.js architecture diagrams.
- Enforces the branching strategy and PR workflow defined in `CONTRIBUTING.md`.

**Why It's Needed:**
The Architect agent prevents "foundation rot" — small misconfigurations in Docker, CI, or Spring Boot that silently break downstream milestones. It's the only agent that sees the system holistically, but it never touches business logic.

---

### 2. 📒 @LedgerExpert — Accounting Engine Agent

**Milestones Served:** M2 (Universal Ledger), M7 (Reporting & FAR), M10 (Auditor Portal), M11 (Payment Processing), M12 (Employee Advances)

**What It Owns:**
- **Backend Models:** `ledger/model/` — `LedgerAccount`, `JournalEntry`, `JournalLine`, `VoucherType`, `CostCenter`, `LedgerGroup`, `FixedAsset`, `DepreciationSchedule`
- **Payment Module:** `ledger/payment/` — `PaymentRegisterEntry`, `PaymentBatch`, `PaymentBatchItem`, `PaymentRegisterService`, `PaymentBatchService`, `PaymentFileGeneratorService`, `PaymentRegisterController`, `PaymentBatchController`
- **Voucher Settlement Module:** `ledger/voucher/` — `Voucher`, `VoucherItem`, `Receipt`, `PaymentAdvice`, `VoucherService`, `ReceiptService`, `PaymentAdviceService`, `UploadedFileService`, `VoucherController`, `ReceiptController`, `PaymentAdviceController`, `UploadedFileController`
- **Advance Module (planned):** `ledger/advance/` — `EmployeeAdvance`, `ExpenseVoucher`, `AdvanceReceipt`, `AdvanceLimitCheckService`, `ApprovalTierResolver`, `AdvanceSettlementService`, `EmployeeAdvanceService`, `ExpenseVoucherService`, `AdvanceReceiptService`, `PaymentAdviceService`, `AdvanceReportService`, and corresponding controllers
- **Backend Repositories:** `ledger/repository/` — all JPA repositories
- **Backend Services:** `ledger/service/` — `JournalService`, `LedgerAccountService`, `TrialBalanceService`, `VoucherTypeService`, `CostCenterService`, `LedgerGroupService`, `FixedAssetService`, `ProfitAndLossService`, `BalanceSheetService`, `CashFlowService`
- **Backend Controllers:** `ledger/controller/` — `LedgerController`, `JournalController`, `VoucherTypeController`, `CostCenterController`, `LedgerGroupController`, `ReportController`, `FixedAssetController`
- **Backend DTOs:** `ledger/dto/` — all request/response records
- **Backend Exceptions:** `ledger/exception/` — `GlobalExceptionHandler`, custom exceptions
- **Flyway Migrations:** SQL schema design (V1–V11, V13–V15) including JSONB columns, balanced-entry triggers, payment processing, voucher settlement, and employee advances
- **Documentation:** `docs/sql-schema.md`, `docs/api-documentation.md`
- **Frontend:** `accounting/` module (ledger view, voucher entry), `reports/` module

**What It Does:**
- Enforces double-entry invariants: every journal entry must have balanced debit/credit lines.
- Validates the Universal Secured Ledger schema — JSONB metadata columns for industry-specific tags.
- Reviews Trial Balance correctness, P&L/Balance Sheet/Cash Flow report logic.
- Manages the Fixed Asset Register (FAR): depreciation schedules, impairment, disposal.
- Ensures Auditor Portal read-only access and sample-request workflows.
- Validates multi-entity hierarchy: Enterprise → Branch → Cost Center.
- Manages the Payment Processing Pipeline (M11): payment register, batch processing, payment file generation.
- Manages Employee Advance & Settlement workflows (M12): advance limits, tiered approvals, expense settlement, advance receipts, reimbursement payment advices.

**Why It's Needed:**
The accounting engine is the **heart of the system**. Every other module (ingestion, AI, compliance) ultimately posts to the ledger. A domain expert ensures double-entry integrity is never violated, reports are mathematically correct, and the schema supports sector-agnostic metadata without corruption.

---

### 3. 🔐 @SecurityWarden — Zero-Knowledge Security Agent

**Milestones Served:** M3 (Zero-Knowledge Security), M10 (Hardening & Production Readiness)

**What It Owns:**
- **Backend Security Package:** `ledger/security/` — `FieldEncryptionService`, `BlindIndexService`, `KeyManagementService`, `AuditLogService`, `EncryptedStringConverter`
- **Security Models:** `ledger/security/model/` — encrypted field entities
- **Security Repositories:** `ledger/security/repository/`
- **Security Config:** `application.yml` keys under `onebook.security.encryption.*`
- **Hardening Services:** `SecurityAuditService` (5 automated checks)
- **Document Vault:** Smart Document Vault (S3/MinIO encrypted attachments)
- **Flyway:** RLS policies in all migrations, `V1__rls_infrastructure.sql`
- **Observability:** `RequestLoggingFilter` with MDC trace/span IDs

**What It Does:**
- Enforces the "Blind DBA" principle — a security model where even database administrators with full DB access cannot read sensitive financial data, because all sensitive values are encrypted in the application layer before persistence.
- Validates AES-256-GCM field-level encryption: ensures sensitive values (amounts, names, account numbers) are encrypted before DB persistence.
- Reviews Blind Index (HMAC-SHA256) correctness: encrypted fields must remain searchable without exposing plaintext.
- Enforces hash-chained audit trail integrity: each audit record is cryptographically linked to its predecessor.
- Manages envelope encryption and key rotation strategy.
- Validates Row-Level Security (RLS) policies: ensures tenants cannot access each other's data at the database level.
- Reviews penetration testing results and compliance certifications.
- Ensures the Document Vault encrypts source documents before S3 storage.

**Why It's Needed:**
Security is non-negotiable in a financial system. The "Blind DBA" concept (where even database administrators cannot read sensitive data) requires deep cryptographic expertise. This agent ensures encryption is applied consistently, keys are rotated properly, and the audit chain is truly tamper-proof. A single missed field could expose financial data.

---

### 4. ⚡ @PerfEngineer — Performance & Caching Agent

**Milestones Served:** M4 (Redis Warm Cache & Performance)

**What It Owns:**
- **Backend Cache Package:** `ledger/cache/` — `WarmCacheService`, `WarmCacheController`, `CacheConstants`
- **Redis Config:** `com.nexus.onebook.config.RedisConfig` (Jackson + JSR310 serialization)
- **Virtual Threads:** Spring Boot Virtual Thread configuration
- **Cache Patterns:** Cache-aside and write-through implementations in service layer
- **Docker Compose:** Redis 7+ service definition
- **Performance Tests:** Load test configurations and benchmark results

**What It Does:**
- Validates the "Warm Cache" strategy: on user login, the working set is decrypted and cached in Redis.
- Reviews cache invalidation and eviction policies — ensures Redis and PostgreSQL stay in sync.
- Monitors cache-aside pattern implementation: service methods must check Redis before hitting PostgreSQL.
- Ensures Virtual Threads (Project Loom) are properly configured for high-concurrency API handling.
- Benchmarks response times: target < 100ms for common ledger and report queries.
- Reviews `CacheConstants` for appropriate TTLs and key prefix conventions.
- Validates that cache failures are graceful (fail-safe, fall back to DB).

**Why It's Needed:**
The encryption layer (M3) adds latency to every database read. Without an expert focused on caching, encrypted field reads could make the UI feel sluggish. The PerfEngineer ensures that the Redis warm cache compensates for encryption overhead, maintaining sub-100ms response times. It also ensures Virtual Threads don't introduce concurrency bugs.

---

### 5. 🎹 @UXSpecialist — Frontend & Keyboard Navigation Agent

**Milestones Served:** M5 (Keyboard Navigation), M7 (i18n/L10n)

**What It Owns:**
- **Frontend Keyboard Module:** `keyboard/` — `KeyBindingRegistryService`, `CommandRegistryService`, `KeyboardNavigationService`, `CommandPaletteComponent`, `KeyboardContextDirective`
- **Frontend Accounting Module:** `accounting/` — `LedgerComponent`, `VoucherEntryComponent`
- **Frontend Banking Module:** `banking/`
- **Frontend Dashboard Module:** `dashboard/`
- **Frontend GST Module:** `gst/`
- **Frontend Inventory Module:** `inventory/`
- **Frontend Master Module:** `master/`
- **Frontend Reports Module:** `reports/`
- **Frontend i18n Module:** `i18n/` — `TranslocoConfig`, `LanguageSwitcherComponent`
- **Angular Config:** `app.config.ts`, `app.routes.ts`, `app.component.*`
- **Documentation:** `docs/key-binding-registry.md`

**What It Does:**
- Ensures all 17 Tally shortcuts are correctly mapped (F4 Contra, F5 Payment, F7 Journal, Alt+C Create, Ctrl+A Save, etc.).
- Validates the Command Palette (Ctrl+K / Cmd+K): fuzzy search, command registration, keyboard-driven selection.
- Reviews Contextual Power Keys: shortcuts that adapt based on the active screen.
- Enforces ARIA roles and screen-reader support for all keyboard flows.
- Manages Angular Signals-based state management patterns across all frontend modules.
- Validates i18n/L10n via Transloco: real-time language switching, localized date formats, currency display ($ vs ₹).
- Ensures lazy-loaded module routing is correctly configured.
- Reviews SCSS styling consistency and responsive design.

**Why It's Needed:**
The "Better-than-Tally" UX is a core differentiator. Tally-experienced accountants expect specific keyboard shortcuts to work identically. The UXSpecialist ensures no shortcut regressions, the Command Palette remains responsive, and the frontend is accessible. It also owns the i18n layer, which is critical for multi-locale deployment.

---

### 6. 🔌 @IntegrationBot — Ingestion & Adapters Agent

**Milestones Served:** M6 (Universal Ingestion Layer & Automation)

**What It Owns:**
- **Ingestion Gateway:** `ledger/ingestion/gateway/` — `FinancialEventAdapter` (interface), `AdapterRegistry`, `FinancialEventGateway`
- **Adapters:** `ledger/ingestion/adapter/` — `Hl7Adapter` (Healthcare), `DmsAdapter` (Automotive), `Iso20022Adapter` (Banking), `WebhookAdapter` (SaaS)
- **Universal Mapper:** `ledger/ingestion/mapper/` — `UniversalMapper` (account code → ID resolution)
- **Automation:** `ledger/ingestion/automation/` — `OcrInvoiceService`, `ThreeWayMatchingService`
- **Connectors:** `ledger/ingestion/connector/` — `CorporateCardService`, `HrmPayrollConnector`, `InventoryEventListener`
- **Ingestion DTOs/Models/Controllers/Repositories:** All files under `ledger/ingestion/`

**What It Does:**
- Validates the pluggable adapter pattern: new industry adapters must implement `FinancialEventAdapter` and auto-register via Spring DI.
- Reviews the parse → validate → map → post pipeline in `FinancialEventGateway`.
- Ensures `UniversalMapper` correctly resolves account codes to ledger account IDs.
- Validates HL7 message parsing for healthcare financial events.
- Reviews DMS adapter for automotive dealer management integration.
- Validates ISO 20022 XML parsing for banking/reconciliation feeds.
- Ensures OCR invoice processing correctly extracts line items and auto-drafts journal entries.
- Reviews 3-Way Matching logic: PO ↔ Goods Receipt ↔ Vendor Invoice verification.
- Validates corporate card sync, HRM/payroll connector, and inventory event listener.

**Why It's Needed:**
The ingestion layer is the primary integration surface — it's where external systems (hospitals, dealerships, banks, ERPs) connect to Nexus Universal. Each adapter has its own data format (HL7 messages, ISO 20022 XML, REST webhooks). An expert ensures new adapters follow the established pattern, the Universal Mapper doesn't lose data during transformation, and the automation services (OCR, 3-Way Matching) maintain accuracy.

---

### 7. 🧠 @AIEngineer — Intelligence & Forecasting Agent

**Milestones Served:** M8 (Advanced Intelligence & AI Features)

**What It Owns:**
- **Backend AI Services:** Predictive Cash Flow Forecasting, Scenario Modeling ("What-If" analysis), Mark-to-Market (MTM) Valuation engine, Corporate Actions automation, Market Sentiment Overlay, Anomaly Detection, Digital Asset/Crypto Ledger
- **Frontend AI Module:** `ai/` — components, models, services (forecasting, MTM, anomaly detection)

**What It Does:**
- Validates predictive cash flow forecasting using historical ledger data (30/60/90-day projections).
- Reviews "What-If" scenario modeling: simulates financial impacts of external factors (sales drops, interest rate changes).
- Ensures Mark-to-Market valuation accuracy for share-market investment portfolios.
- Validates corporate action handling: stock splits, dividends, bonus issues via financial APIs.
- Reviews market sentiment overlay integration on the CFO dashboard.
- Ensures anomaly detection correctly flags unusual transactions (fraud indicators, duplicate entries).
- Validates Digital Asset & Crypto ledger tracking for stablecoin/crypto MTM valuations.

**Why It's Needed:**
AI features transform Nexus Universal from a bookkeeping tool into a decision-support platform. The AIEngineer ensures forecasting models are statistically sound, MTM valuations reconcile with market APIs, and anomaly detection doesn't produce excessive false positives. This is a highly specialized domain where errors could lead to incorrect financial decisions.

---

### 8. 📋 @ComplianceAgent — Tax, Regulatory & Compliance Agent

**Milestones Served:** M7 (Tax Compliance), M10 (Certifications)

**What It Owns:**
- **Backend Controllers/Services:** `ComplianceController`, Compliance services (e-Invoice, e-Way Bill generation)
- **Feature Entitlement Engine:** `FeatureEntitlementService` — toggles locale-specific modules per tenant
- **Tenant Locale Service:** `TenantLocaleService` — country/tax regime configuration
- **Reconciliation:** `ReconciliationController`, `BankReconciliationService` — real-time bank feeds via Open Banking APIs
- **Consolidation:** `ConsolidationController`, `IntercompanyService` — intercompany elimination
- **GST Frontend Module:** `gst/` — GST compliance components

**What It Does:**
- Validates automated e-Invoicing generation per country-specific formats.
- Reviews e-Way Bill generation logic for goods transport compliance.
- Ensures the Feature Entitlement Engine correctly toggles locale-specific modules (GST for India, VAT for EU, Sales Tax for US).
- Validates bank reconciliation via Open Banking APIs: automated matching of bank feeds to ledger entries.
- Reviews intercompany accounting: automatic elimination of intercompany transactions during consolidation.
- Ensures compliance certifications are maintained for target industries.
- Validates localized tax calculations (CGST/SGST/IGST for India, etc.).

**Why It's Needed:**
Tax compliance is legally critical — errors lead to penalties and audits. Each country has different invoicing formats, tax regimes, and reporting requirements. The ComplianceAgent ensures that e-invoices conform to government schemas, tax calculations are accurate to the penny, and bank reconciliation logic correctly matches transactions. It also owns the entitlement engine that controls which compliance modules are active per tenant.

---

### 9. 🛡️ @AuditAgent — Auditor Portal & Production Readiness Agent

**Milestones Served:** M10 (Hardening, Audit & Production Readiness)

**What It Owns:**
- **Backend Auditor Services:** Auditor Portal services (sample requests, comments, approval workflows)
- **Backend Models:** Auditor-related entities (6 new tables from V9 migration)
- **Frontend Auditor Module:** `auditor/` — `AuditorDashboardComponent` (lazy-loaded at `/auditor`)
- **Observability:** Structured logging configuration, distributed tracing, metrics dashboards
- **Disaster Recovery:** Automated backup procedures, point-in-time recovery, failover
- **Load Testing:** Stress test configurations at production scale

**What It Does:**
- Validates the Auditor Portal: secure, read-only interface for CPAs.
- Reviews sample request workflows: auditors can request specific journal entries or document samples.
- Ensures comment and approval workflows function end-to-end.
- Validates observability stack: structured logging with MDC trace/span IDs, metrics dashboards.
- Reviews disaster recovery procedures: automated backups, recovery testing.
- Ensures load/stress testing covers projected production scale.
- Validates that the Auditor route (`/auditor`) is properly lazy-loaded and access-controlled.

**Why It's Needed:**
Production readiness requires a dedicated focus area. The AuditAgent ensures the system can survive real-world conditions: external auditor access, high load, infrastructure failures, and regulatory inspections. Without a dedicated agent, production-readiness tasks often get deprioritized in favor of feature work.

---

### 10. 📝 @DocAgent — Documentation & Knowledge Management Agent

**Milestones Served:** M9 (Architecture Documentation & Deliverables), Cross-cutting

**What It Owns:**
- **All Documentation:** `docs/` directory — `api-documentation.md`, `architecture-diagram.md`, `developer-guide.md`, `key-binding-registry.md`, `operational-runbook.md`, `sql-schema.md`
- **Root Docs:** `README.md`, `CONTRIBUTING.md`, `architecture.md`, `milestones.md`, `tally_features.md`
- **Project State:** `PROJECT_STATE.md` (to be created after milestones complete)
- **Code Comments:** Inline documentation standards across backend and frontend

**What It Does:**
- Maintains API documentation in sync with actual controller endpoints (15+ controllers).
- Updates architecture diagrams (Mermaid.js) when system topology changes.
- Ensures the developer onboarding guide reflects current setup steps.
- Keeps the operational runbook current with deployment, monitoring, and troubleshooting procedures.
- Updates the SQL schema documentation when new Flyway migrations are added.
- Maintains the Key-Binding Registry documentation when shortcuts are added/modified.
- Creates and maintains `PROJECT_STATE.md` for context pruning after milestone completion.

**Why It's Needed:**
Documentation drift is a common problem in fast-moving projects. The DocAgent ensures that when a new controller is added, the API docs are updated; when a new migration runs, the schema docs reflect it; when a new shortcut is mapped, the key-binding docs include it. It also owns the context-pruning protocol — summarizing completed milestones into `PROJECT_STATE.md` to free tokens.

---

## Sub-Agent Interaction Matrix

This matrix shows which agents need to collaborate on cross-cutting concerns:

| Concern | Primary Agent | Collaborating Agents |
|---------|--------------|---------------------|
| New Journal Entry Flow | @LedgerExpert | @SecurityWarden (encryption), @PerfEngineer (cache), @UXSpecialist (UI) |
| New Industry Adapter | @IntegrationBot | @LedgerExpert (account mapping), @ComplianceAgent (tax rules) |
| Adding Encrypted Field | @SecurityWarden | @LedgerExpert (schema), @PerfEngineer (cache invalidation) |
| New Financial Report | @LedgerExpert | @UXSpecialist (frontend component), @ComplianceAgent (tax formatting) |
| New Keyboard Shortcut | @UXSpecialist | @DocAgent (documentation update) |
| AI Feature Addition | @AIEngineer | @LedgerExpert (data source), @UXSpecialist (dashboard display) |
| Flyway Migration | @LedgerExpert | @SecurityWarden (RLS policies), @Architect (naming conventions) |
| API Endpoint Change | @LedgerExpert or @IntegrationBot | @DocAgent (API docs), @UXSpecialist (frontend integration) |
| Performance Issue | @PerfEngineer | @SecurityWarden (encryption overhead), @LedgerExpert (query optimization) |
| Compliance Update | @ComplianceAgent | @LedgerExpert (tax accounts), @IntegrationBot (government APIs) |
| Production Incident | @AuditAgent | @PerfEngineer (load), @SecurityWarden (security breach) |
| Onboarding Guide Update | @DocAgent | @Architect (setup steps), all agents (their domain docs) |

---

## Delegation Protocol

### The "3-File Rule"

**Trigger:** If a task requires reading or modifying **3 or more files across different layers** (e.g., Angular component + Spring Boot service + SQL migration), a sub-agent must be spawned.

**Workflow:**
1. **Identify Scope:** The orchestrator determines which files are affected and which sub-agent(s) own them.
2. **Spawn Agent:** The appropriate sub-agent is invoked with isolated context — only its owned files.
3. **Execute:** The sub-agent investigates, modifies, and tests within its bounded context.
4. **Report:** The sub-agent returns only the **interface summary** (what changed, what was tested, what contracts are affected) — never raw code dumps.
5. **Integrate:** The orchestrator merges the results, ensuring no cross-agent conflicts.

### Context Budget

- **Main Orchestrator:** < 5,000 tokens at all times — holds only interfaces, summaries, and the current task.
- **Sub-Agent Context:** Each sub-agent can use up to its full context window, but must distill results to < 500 tokens when reporting back.
- **Context Pruning:** After a milestone reaches "Exit Criteria Met," the @DocAgent summarizes the final state into `PROJECT_STATE.md` and all sub-agent histories are cleared.

### Parallel Execution Rules

- **Safe to Parallelize:** Agents working on different layers (e.g., @UXSpecialist on frontend + @LedgerExpert on backend).
- **Must Serialize:** Agents modifying the same file or dependent data (e.g., @SecurityWarden adding encryption + @PerfEngineer updating cache for the same field).
- **Conflict Resolution:** If two agents propose conflicting changes, the **primary agent** (per the Interaction Matrix) has priority.

---

## Summary Table

| # | Sub-Agent | Icon | Milestones | Backend Packages | Frontend Modules | Key Skill |
|---|-----------|------|-----------|-----------------|-----------------|-----------|
| 1 | @Architect | 🏗️ | M1, M9 | `config/` | — | Infrastructure & CI/CD |
| 2 | @LedgerExpert | 📒 | M2, M7, M10 | `model/`, `repository/`, `service/`, `controller/`, `dto/`, `exception/` | `accounting/`, `reports/` | Double-entry accounting |
| 3 | @SecurityWarden | 🔐 | M3, M10 | `security/` | — | Cryptography & RLS |
| 4 | @PerfEngineer | ⚡ | M4 | `cache/` | — | Redis caching & Virtual Threads |
| 5 | @UXSpecialist | 🎹 | M5, M7 | — | `keyboard/`, `accounting/`, `banking/`, `dashboard/`, `gst/`, `inventory/`, `master/`, `reports/`, `i18n/` | Angular Signals & accessibility |
| 6 | @IntegrationBot | 🔌 | M6 | `ingestion/` (all sub-packages) | — | Protocol adapters & data mapping |
| 7 | @AIEngineer | 🧠 | M8 | AI/forecasting services | `ai/` | ML models & financial APIs |
| 8 | @ComplianceAgent | 📋 | M7, M10 | Compliance, reconciliation, consolidation, entitlement services | `gst/` | Tax law & regulatory formats |
| 9 | @AuditAgent | 🛡️ | M10 | Auditor services, observability | `auditor/` | Production ops & audit workflows |
| 10 | @DocAgent | 📝 | M9, Cross-cutting | — | — | Technical writing & diagrams |

---

## How This Maps to the Existing Codebase

### Backend File Ownership (405+ tests)

```
com.nexus.onebook/
├── config/                          → @Architect
└── ledger/
    ├── cache/                       → @PerfEngineer
    ├── controller/                  → @LedgerExpert (core), @ComplianceAgent (compliance endpoints)
    ├── dto/                         → @LedgerExpert
    ├── exception/                   → @LedgerExpert
    ├── ingestion/
    │   ├── adapter/                 → @IntegrationBot
    │   ├── automation/              → @IntegrationBot
    │   ├── connector/               → @IntegrationBot
    │   ├── controller/              → @IntegrationBot
    │   ├── dto/                     → @IntegrationBot
    │   ├── gateway/                 → @IntegrationBot
    │   ├── mapper/                  → @IntegrationBot
    │   ├── model/                   → @IntegrationBot
    │   └── repository/              → @IntegrationBot
    ├── model/                       → @LedgerExpert
    ├── repository/                  → @LedgerExpert
    ├── security/
    │   ├── model/                   → @SecurityWarden
    │   └── repository/              → @SecurityWarden
    └── service/                     → @LedgerExpert (core), @AIEngineer (AI services),
                                       @ComplianceAgent (compliance services)
```

### Frontend Module Ownership (105+ tests)

```
frontend/src/app/
├── accounting/                      → @UXSpecialist + @LedgerExpert (data contracts)
├── ai/                              → @UXSpecialist + @AIEngineer (data contracts)
├── auditor/                         → @UXSpecialist + @AuditAgent (workflows)
├── banking/                         → @UXSpecialist
├── dashboard/                       → @UXSpecialist
├── gst/                             → @UXSpecialist + @ComplianceAgent (tax rules)
├── i18n/                            → @UXSpecialist
├── inventory/                       → @UXSpecialist
├── keyboard/                        → @UXSpecialist
├── market/                          → @UXSpecialist + @AIEngineer (MTM valuation)
├── master/                          → @UXSpecialist
├── receivable/                      → @UXSpecialist + @LedgerExpert (AR data)
└── reports/                         → @UXSpecialist + @LedgerExpert (report logic)
```

### Documentation Ownership

```
docs/
├── api-documentation.md             → @DocAgent + @LedgerExpert (endpoint accuracy)
├── architecture-diagram.md          → @DocAgent + @Architect (system topology)
├── developer-guide.md               → @DocAgent + @Architect (setup steps)
├── key-binding-registry.md          → @DocAgent + @UXSpecialist (shortcut accuracy)
├── operational-runbook.md           → @DocAgent + @AuditAgent (ops procedures)
└── sql-schema.md                    → @DocAgent + @LedgerExpert (schema accuracy)
```

---

## Conclusion

These 10 sub-agents provide **complete coverage** of the Nexus Universal platform:

- **No orphaned code:** Every file in the repository has a clear owner.
- **No context overload:** Each agent focuses on its bounded context.
- **Clear escalation paths:** The Interaction Matrix defines who collaborates on cross-cutting concerns.
- **Parallel-safe:** Independent agents can work simultaneously.
- **Self-documenting:** The @DocAgent ensures documentation stays in sync with code changes.

This sub-agent architecture ensures that as the project evolves beyond M10, new features can be assigned to the appropriate specialist without context fragmentation or quality degradation.
