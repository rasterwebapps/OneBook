# OneBook — Global Copilot Instructions

> These rules are injected into EVERY Copilot interaction in this repository.
> They represent non-negotiable project conventions that all agents must follow.

---

## Project Identity

**OneBook — Nexus Universal** is a sector-agnostic, zero-trust, high-performance accounting OS.
Stack: Java 21+ / Spring Boot 3.4+ / Angular 21+ / PostgreSQL 17+ / Redis 7+.

---

## Critical Rules (Never Violate)

1. **Double-entry invariant**: Every journal entry MUST have debits == credits (`BigDecimal`, 3-level validation).
2. **Encryption**: AES-256-GCM with unique random IV per operation. Never reuse IVs.
3. **Multi-tenancy**: Always include `tenantId` in every query. Enable RLS on tenant-scoped tables.
4. **DTOs only**: Never expose JPA entities in REST responses — always use DTO records.
5. **Amount types**: Always use `BigDecimal` for monetary values. Never `double`/`float`.
6. **Secrets**: Never commit secrets. Never store keys in `application.yml` — use environment variables.
7. **Standalone components**: Angular components must be standalone (no NgModules). Use Signals for state.
8. **Memory bank**: Read `CLAUDE.md` at session start. Update `memory-bank/activecontext.md` at session end.

---

## Build & Test Commands

```bash
# Backend
cd backend && ./gradlew build        # Full build with tests
cd backend && ./gradlew test          # Tests only
cd backend && ./gradlew compileJava   # Compile only

# Frontend
cd frontend && npm install && npx ng build
cd frontend && npx ng test --watch=false --browsers=ChromeHeadless

# Quality Gates
./.github/scripts/validate-quality-gates.sh
./.github/scripts/validate-agent-ownership.sh
./.github/scripts/sync-memory-bank.sh --check
```

---

## Naming Conventions

| Layer | Convention | Example |
|-------|-----------|---------|
| Java Classes | PascalCase | `JournalService` |
| Java Methods | camelCase | `createTransaction()` |
| Database Tables | snake_case | `ledger_accounts` |
| Database Columns | snake_case | `account_code` |
| TypeScript Files | kebab-case | `voucher-entry.component.ts` |
| Angular Components | PascalCase + Suffix | `VoucherEntryComponent` |
| Angular Selectors | app- prefix + kebab-case | `app-voucher-entry` |
| Cache Keys | colon-separated | `onebook:cache:accounts:tenant:123` |
| Flyway Migrations | `V{N}__{description}.sql` | `V15__employee_advances.sql` |

---

## Agent System

This repository uses a hierarchical agent system for SDLC automation:

- **`@partner`** — Head orchestrator. The ONLY agent users invoke directly. Analyzes requirements, delegates to specialists, tracks progress.
- **`@backend`** — Backend development (Java/Spring Boot services, controllers, DTOs, tests)
- **`@frontend`** — Frontend development (Angular components, services, modules, tests)
- **`@database`** — Database design (Flyway migrations, RLS policies, schema changes)
- **`@security`** — Security (encryption, authentication, RLS, audit trails)
- **`@infra`** — Infrastructure (Docker, CI/CD, Redis, deployment)
- **`@docs`** — Documentation (API docs, memory bank, architecture diagrams)
- **`@quality`** — Quality assurance (testing, quality gates, validation)

**Workflow**: User → `@partner` → domain agents → `@partner` validates → `@quality` tests → `@docs` documents → done.

### Copilot Skills

Each agent has an associated **Copilot Skill** in `.github/skills/` that provides reusable, step-by-step task procedures:

| Agent | Skill | Purpose |
|-------|-------|---------|
| @partner | `analyze-requirement` | Classify, plan, and track new requirements |
| @backend | `add-rest-endpoint` | Scaffold DTO → Repository → Service → Controller → Test |
| @frontend | `create-angular-component` | Create standalone component with Signals, i18n, routing |
| @database | `create-flyway-migration` | Create migration with RLS, tenant isolation, proper types |
| @security | `security-review` | Review for RLS, encryption, secrets, OWASP compliance |
| @infra | `setup-dev-environment` | Set up/manage Docker Compose dev environment |
| @docs | `update-documentation` | Update docs using Selective Documentation Update Protocol |
| @quality | `run-quality-gates` | Execute full quality validation pipeline (8 gates) |

**Agents** define WHO does the work and WHAT rules to follow.
**Skills** define HOW to do specific tasks step-by-step.

### Selective Documentation Update Rule

**⚡ @docs updates ONLY the documentation files affected by the current change — NOT all docs for every input.**

| Always Updated | Conditionally Updated | Never Touched (if unrelated) |
|---|---|---|
| `memory-bank/activecontext.md` | Docs in the change's scope (see CLAUDE.md Impact Matrix) | Docs for unrelated features/modules |

Refer to the **Documentation Impact Matrix** in `CLAUDE.md` → "Selective Documentation Update Protocol" for the full mapping of change types to affected docs.

---

## Memory Bank

All agents share context through the memory bank at `memory-bank/`:

| File | Purpose |
|------|---------|
| `CLAUDE.md` | Entry point — read first |
| `memory-bank/activecontext.md` | Current state, recent changes |
| `memory-bank/progress.md` | Milestone completion status |
| `memory-bank/systempatterns.md` | Architecture decisions |
| `memory-bank/techcontext.md` | Stack and conventions |
| `memory-bank/troubleshooting.md` | Known issues and fixes |

---

## Error Response Format

```json
{
  "timestamp": "2026-04-09T08:47:19.123Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Descriptive error message"
}
```

---

## Testing Conventions

| Framework | Pattern | Example |
|-----------|---------|---------|
| JUnit 5 | `methodName_condition_result` | `createAccount_duplicateCode_throws` |
| Mockito | Constructor-based mocks | `@InjectMocks` with `@Mock` |
| Jasmine | `describe` + `it` blocks | `describe('Service', () => it('should...'))` |

---

## References

- [CLAUDE.md](../CLAUDE.md) — AI memory bank entry point
- [Memory Bank](../memory-bank/) — Persistent project intelligence
- [Agent Directory](.github/agents/) — All specialist agents
- [UI/UX Guidelines](../docs/technical/ui-ux-guidelines.md) — Comprehensive UI/UX agent guidelines (design system, components, patterns, accessibility, do's/don'ts)
