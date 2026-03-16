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
| Frontend    | Angular 19+ (Signals-based state) |
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

| Document | Description |
|----------|-------------|
| [Architecture Diagram](docs/architecture-diagram.md) | Mermaid.js system, data flow, security, and deployment diagrams |
| [Key-Binding Registry](docs/key-binding-registry.md) | Keyboard navigation design (Tally shortcuts, Command Palette, extensibility) |
| [SQL Schema](docs/sql-schema.md) | Universal Secured Ledger schema documentation |
| [API Documentation](docs/api-documentation.md) | REST API reference for all endpoints |
| [Developer Guide](docs/developer-guide.md) | Onboarding, setup, coding standards |
| [Operational Runbook](docs/operational-runbook.md) | Deployment, monitoring, troubleshooting |
| [Tally Features](tally_features.md) | Tally feature parity reference and breakdown |
| [Sub-Agent Instructions](.github/agents/README.md) | Design patterns and conventions for 10 specialist agents |
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
