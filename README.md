# OneBook — Nexus Universal

A sector-agnostic, Zero-Trust, High-Performance Accounting OS.

## 🤖 AI Memory Bank

This project uses a **CLAUDE.md memory bank** — a persistent AI context system that allows AI agents to continue from accumulated project intelligence rather than starting from zero each session.

| File | Purpose |
|------|---------|
| [`CLAUDE.md`](CLAUDE.md) | **Start here** — AI entry point, navigation, critical rules |
| [`memory-bank/projectbrief.md`](memory-bank/projectbrief.md) | Project vision, goals, original requirements |
| [`memory-bank/techcontext.md`](memory-bank/techcontext.md) | Stack, setup, build & test commands |
| [`memory-bank/systempatterns.md`](memory-bank/systempatterns.md) | Architecture decisions, design patterns |
| [`memory-bank/activecontext.md`](memory-bank/activecontext.md) | Current session state, recent changes |
| [`memory-bank/progress.md`](memory-bank/progress.md) | Milestone status, feature tracker |
| [`memory-bank/troubleshooting.md`](memory-bank/troubleshooting.md) | Known issues, past bugs, fixes |

> **For AI agents:** Read `CLAUDE.md` first. Update `memory-bank/activecontext.md` at the end of every task.

---

## Tech Stack

| Layer       | Technology                        |
|-------------|-----------------------------------|
| Backend     | Java 21+ / Spring Boot 3.4+      |
| Frontend    | Angular 21+ (Signals-based state) |
| Database    | PostgreSQL 17+ (RLS, JSONB)       |
| Cache       | Redis 7+                          |

## Monorepo Structure

```
OneBook/
├── backend/        # Spring Boot 3.4+ API (Gradle)
├── frontend/       # Angular 19+ SPA
├── docs/           # Architecture documentation
├── docker-compose.yml
└── milestones.md
```

## Documentation

### 📋 Business Documentation (Living Documents — auto-synced)

| Document | Description |
|----------|-------------|
| [BRD — Business Requirements](docs/business/BRD.md) | What the business needs and why |
| [FRD — Functional Requirements](docs/business/FRD.md) | How the system delivers business requirements |
| [TRD — Technical Requirements](docs/business/TRD.md) | How it's implemented technically |
| [User Stories](docs/business/user-stories.md) | Acceptance criteria and Gherkin scenarios |
| [Business Glossary](docs/business/glossary.md) | Definitions for all business and technical terms |

### 📁 Requirements Traceability

| Document | Description |
|----------|-------------|
| [Requirements Index](docs/requirements/requirements-index.md) | Master index of all 10 requirements |
| [RTM — Traceability Matrix](docs/requirements/RTM.md) | Requirements → Features → Code → Tests |
| [Requirement Template](docs/requirements/requirement-template.md) | Template for writing new requirements |
| [REQ-001: Multi-Tenant Ledger](docs/requirements/active/REQ-001-multi-tenant-ledger.md) | Multi-tenant architecture requirement |
| [REQ-002: Zero-Knowledge Encryption](docs/requirements/active/REQ-002-zero-knowledge-encryption.md) | Security & encryption requirement |
| [REQ-003: External App Ingestion](docs/requirements/active/REQ-003-external-app-ingestion.md) | Universal ingestion pipeline |
| [REQ-004: Voucher Posting](docs/requirements/active/REQ-004-voucher-posting.md) | Double-entry voucher posting |
| [REQ-005: Trial Balance Reports](docs/requirements/active/REQ-005-trial-balance-reports.md) | Financial reports |
| [REQ-006: Cost Center Management](docs/requirements/active/REQ-006-cost-center-management.md) | Cost centers and branches |
| [REQ-007: Fixed Asset Management](docs/requirements/active/REQ-007-fixed-asset-management.md) | FAR and depreciation |
| [REQ-008: TDS/TCS Compliance](docs/requirements/active/REQ-008-tds-tcs-compliance.md) | Tax deduction compliance |
| [REQ-009: Bank Reconciliation](docs/requirements/active/REQ-009-bank-reconciliation.md) | Bank statement reconciliation |
| [REQ-010: Maker-Checker Workflow](docs/requirements/active/REQ-010-maker-checker-workflow.md) | Approval workflow |

### 🔧 Technical Documentation

| Document | Description |
|----------|-------------|
| [Data Dictionary](docs/technical/data-dictionary.md) | All entities, fields, validations |
| [API Contracts](docs/technical/api-contracts.md) | Complete API specifications by module |
| [Workflow Diagrams](docs/technical/workflow-diagrams.md) | Mermaid process flow diagrams |
| [Architecture Diagram](docs/architecture-diagram.md) | Mermaid.js system, data flow, security diagrams |
| [SQL Schema](docs/sql-schema.md) | Universal Secured Ledger schema documentation |
| [API Documentation](docs/api-documentation.md) | REST API reference for all endpoints |

### 👤 User Documentation

| Document | Description |
|----------|-------------|
| [User Manual](docs/user/user-manual.md) | Complete user guide |
| [Feature Catalog](docs/user/feature-catalog.md) | All features with status and endpoints |
| [Keyboard Shortcuts](docs/user/keyboard-shortcuts.md) | Complete shortcut reference |
| [Key-Binding Registry](docs/key-binding-registry.md) | Keyboard navigation design spec |

### 🛠️ Developer Documentation

| Document | Description |
|----------|-------------|
| [Developer Guide](docs/developer-guide.md) | Onboarding, setup, coding standards |
| [Operational Runbook](docs/operational-runbook.md) | Deployment, monitoring, troubleshooting |
| [Tally Features](tally_features.md) | Tally feature parity reference and breakdown |
| [Sub-Agent Instructions](.github/agents/README.md) | Design patterns for 10 specialist agents |
| [Agent Ownership Maintenance](.github/agents/MAINTENANCE.md) | **⚠️ How to update agent ownership when adding new code** |

## Quick Start

### Prerequisites

- Java 21+
- Node.js 20+
- Docker & Docker Compose

### 1. Start Infrastructure

```bash
docker compose up -d
```

This provisions PostgreSQL 17 and Redis 7.

### 2. Run Backend

```bash
cd backend
./gradlew bootRun
```

The API starts at `http://localhost:8080` with Virtual Threads enabled.

### 3. Run Frontend

```bash
cd frontend
npm install
npm start
```

The Angular app starts at `http://localhost:4200` with API proxy to the backend.

### 4. Verify

- Frontend: http://localhost:4200
- Backend Health: http://localhost:8080/api/health
- Actuator: http://localhost:8080/actuator/health
