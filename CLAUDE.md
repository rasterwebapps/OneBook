# CLAUDE.md — OneBook (Nexus Universal) AI Memory Bank

> **This file is the primary entry point for AI agents working on this repository.**  
> It provides persistent context, architectural intelligence, and project memory accumulated across sessions.  
> Read this file at the start of every session. Update the memory bank at the end of every task.

---

## How to Use This Memory Bank

```
memory-bank/
├── projectbrief.md     ← Vision, goals, original requirements
├── techcontext.md      ← Stack, setup, build & test commands
├── systempatterns.md   ← Architecture decisions, design patterns, critical rules
├── activecontext.md    ← Current focus, recent changes, next steps
├── progress.md         ← Milestone status, what is done, what is planned
└── troubleshooting.md  ← Known issues, past bugs, applied fixes
```

**At the start of a session:** Read `activecontext.md` + `progress.md` to understand current state.  
**During a session:** Consult `systempatterns.md` + `techcontext.md` for conventions.  
**At the end of a session:** Update `activecontext.md` with what changed; update `progress.md` if milestones moved; add new learnings to `systempatterns.md` or `troubleshooting.md`.

---

## Project Identity

| Field | Value |
|-------|-------|
| **Project** | OneBook — Nexus Universal |
| **Type** | Sector-agnostic, Zero-Trust, High-Performance Accounting OS |
| **Vision** | Tally-speed keyboard UX + 2026-grade security + AI intelligence |
| **Status** | Milestones 1–10 complete (see `memory-bank/progress.md`) |

---

## Tech Stack (Quick Reference)

| Layer | Technology |
|-------|-----------|
| Backend | Java 21+ / Spring Boot 3.4+ / Virtual Threads (Loom) |
| Frontend | Angular 21+ / Signals-based state / Standalone components |
| Database | PostgreSQL 17+ (RLS, JSONB, Flyway migrations) |
| Cache | Redis 7+ (Warm Cache, Cache-Aside) |
| Security | AES-256-GCM field encryption, HMAC-SHA256 blind indexes, RLS |
| i18n | @jsverse/transloco |

---

## Build & Test Commands

```bash
# Backend
cd backend && ./gradlew build
cd backend && ./gradlew test

# Frontend
cd frontend && npm install && npx ng build
cd frontend && npx ng test --watch=false --browsers=ChromeHeadless

# Infrastructure only (for local development)
docker compose up -d postgres redis openldap keycloak

# Full stack (all services)
docker compose up -d

# Validate agent ownership
./.github/scripts/validate-agent-ownership.sh

# Validate quality gates (RLS, DTO, BigDecimal, test coverage, memory bank freshness)
./.github/scripts/validate-quality-gates.sh

# Check memory bank freshness (detect stale values)
./.github/scripts/sync-memory-bank.sh --check

# Auto-fix memory bank staleness
./.github/scripts/sync-memory-bank.sh --fix
```

---

## Agent System — SDLC with Agile Feedback Loops

### Tier 1: Invocable Copilot Agents (`.agent.md`)

Users invoke ONLY `@partner`. It orchestrates all other agents.

| Agent | File | SDLC Role |
|-------|------|-----------|
| 🤝 **@partner** | `partner.agent.md` | Head Orchestrator (BA + PM + Team Lead) |
| 📒 @backend | `backend.agent.md` | Backend Dev Team |
| 🎹 @frontend | `frontend.agent.md` | Frontend Dev Team |
| 🗄️ @database | `database.agent.md` | DB Design Team |
| 🔐 @security | `security.agent.md` | Security Review Team |
| 🏗️ @infra | `infra.agent.md` | DevOps Team |
| 📝 @docs | `docs.agent.md` | BA Documentation Phase |
| ✅ @quality | `quality.agent.md` | Testing Team |

**Global rules:** `.github/copilot-instructions.md` (injected into ALL Copilot interactions)

### Tier 2: Knowledge Base (Legacy `.md`)

11 detailed domain knowledge files referenced by Tier 1 agents. Not directly invocable.

### Workflow
```
User → @partner → @database → @backend → @frontend → @security → @quality → @docs → done
                  ↑___ feedback loops: @partner validates each phase, routes issues back ___↓
```

→ See `.github/agents/README.md` for full SDLC workflow details.
→ See `.github/agents/INDEX.md` for design requirements by category.

---

## Critical Rules (Never Violate)

1. **Double-entry invariant**: Every journal entry MUST have debits == credits (BigDecimal, 3-level validation).
2. **Encryption**: AES-256-GCM with unique random IV per operation. Never reuse IVs.
3. **Multi-tenancy**: Always include `tenantId` in every query. Enable RLS on tenant-scoped tables.
4. **DTOs only**: Never expose JPA entities in REST responses — always use DTO records.
5. **Amount types**: Always use `BigDecimal` for monetary values. Never `double`/`float`.
6. **Secrets**: Never commit secrets. Never store keys in `application.yml` — use environment variables.
7. **Agent ownership**: When adding new modules/services/controllers, update the relevant `.github/agents/*.md` file and run the validation script.

---

## Key File Map

```
OneBook/
├── CLAUDE.md                          ← YOU ARE HERE (AI memory bank entry point)
├── memory-bank/                       ← Persistent AI memory
│   ├── projectbrief.md
│   ├── techcontext.md
│   ├── systempatterns.md
│   ├── activecontext.md
│   ├── progress.md
│   └── troubleshooting.md
├── README.md                          ← Human-facing project overview
├── CONTRIBUTING.md                    ← Branching, PR, coding standards
├── docker-compose.yml                 ← Full-stack orchestration (7 services)
├── .github/
│   ├── copilot-instructions.md        ← Global rules for ALL Copilot interactions
│   ├── agents/                        ← SDLC agent system
│   │   ├── partner.agent.md           ← 🤝 @partner — Head orchestrator (ONLY user-facing agent)
│   │   ├── backend.agent.md           ← 📒 @backend — Backend dev (Java/Spring Boot)
│   │   ├── frontend.agent.md          ← 🎹 @frontend — Frontend dev (Angular)
│   │   ├── database.agent.md          ← 🗄️ @database — DB design (PostgreSQL/Flyway)
│   │   ├── security.agent.md          ← 🔐 @security — Security (encryption, RLS)
│   │   ├── infra.agent.md             ← 🏗️ @infra — Infrastructure (Docker, CI/CD)
│   │   ├── docs.agent.md              ← 📝 @docs — Documentation & memory bank
│   │   ├── quality.agent.md           ← ✅ @quality — Testing & quality gates
│   │   ├── INDEX.md                   ← Design requirements quick index
│   │   ├── MAINTENANCE.md             ← Agent ownership maintenance guide
│   │   ├── README.md                  ← Agent system architecture & SDLC workflow
│   │   └── *.md                       ← Legacy knowledge base files (11 domain experts)
│   ├── scripts/
│   │   ├── validate-agent-ownership.sh    ← Agent ownership validation
│   │   ├── validate-quality-gates.sh      ← RLS, DTO, BigDecimal, test coverage, memory bank freshness
│   │   └── sync-memory-bank.sh            ← Auto-sync memory bank from actual repo state
│   └── workflows/ci.yml
├── infrastructure/                    ← Infrastructure service configs
│   ├── README.md                      ← Infrastructure documentation
│   ├── postgres/init/                 ← PostgreSQL init scripts
│   ├── redis/redis.conf               ← Redis 7 configuration
│   ├── keycloak/                      ← Keycloak 24 realm & themes
│   └── ldap/bootstrap/                ← OpenLDAP LDIF bootstrap
├── docs/
│   ├── architecture.md                ← High-level Mermaid diagram
│   ├── milestones.md                  ← Full milestone specifications
│   ├── sub-agents.md                  ← Sub-agent architecture & delegation rules
│   ├── business/
│   │   ├── tally-features.md          ← Tally feature parity reference
│   │   ├── BRD.md, FRD.md, TRD.md    ← Business requirement docs
│   │   └── glossary.md, user-stories.md
│   ├── technical/
│   │   ├── architecture-diagram.md    ← Detailed Mermaid diagrams
│   │   ├── api-documentation.md       ← REST API reference
│   │   ├── sql-schema.md              ← Database schema docs
│   │   ├── developer-guide.md         ← Onboarding guide
│   │   ├── operational-runbook.md     ← Deployment & monitoring
│   │   ├── key-binding-registry.md    ← Keyboard navigation design
│   │   ├── api-contracts.md, data-dictionary.md, workflow-diagrams.md
│   │   └── ...
│   ├── user/                          ← User-facing documentation
│   ├── requirements/                  ← Requirements tracking (RTM, active reqs)
│   └── automation/                    ← Doc generation scripts
├── backend/                           ← Backend Service (Spring Boot 3.4+)
│   ├── Dockerfile                     ← Multi-stage JRE 21 image
│   └── src/
└── frontend/                          ← Frontend Service (Angular 21+)
    ├── Dockerfile                     ← Multi-stage Nginx image
    ├── nginx.conf                     ← Production Nginx config
    └── src/
```

---

## Memory Bank Update Protocol

At the end of each task, the active AI agent MUST:

1. **Update `memory-bank/activecontext.md`** — Record what was done, what changed, what to focus on next.
2. **Update `memory-bank/progress.md`** — If any milestone items were completed or new tasks added.
3. **Update `memory-bank/systempatterns.md`** — If new patterns, conventions, or architectural decisions were made.
4. **Update `memory-bank/troubleshooting.md`** — If bugs were found and fixed, or workarounds discovered.
5. **Update agent files** (`.github/agents/*.md`) — If new modules, services, or controllers were added.
6. **Run validation** — `./.github/scripts/validate-agent-ownership.sh` to ensure agent ownership is complete.
7. **Run quality gates** — `./.github/scripts/validate-quality-gates.sh` to catch skipped steps (RLS, DTO, tests, BigDecimal).
8. **Run memory bank sync** — `./.github/scripts/sync-memory-bank.sh --check` to detect stale context. Use `--fix` to auto-correct.

This keeps the memory bank accurate and reduces token usage in future sessions by providing compressed, high-signal context.

---

## Automated Quality Gates (CI-Enforced)

These checks run automatically on every PR and push to main. They **cannot be skipped**:

| Gate | Script | What It Catches |
|------|--------|-----------------|
| Memory Bank Freshness | `sync-memory-bank.sh` | Stale test counts, missing modules, outdated migration lists |
| Quality Gates | `validate-quality-gates.sh` | RLS missing on tenant tables, JPA entities in REST responses, double/float for money, missing test files, migration conventions |
| Agent Ownership | `validate-agent-ownership.sh` | New services/controllers not documented in agent files |
| Backend Build & Test | `./gradlew build` | Compilation errors, test failures |
| Frontend Build & Test | `ng build && ng test` | Build errors, component test failures |

**On push to main:** Memory bank is auto-synced (stale values auto-corrected and committed).
