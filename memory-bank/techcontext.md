# Tech Context — OneBook (Nexus Universal)

> **Technology stack, setup instructions, build commands, and coding conventions.**  
> Keep this file updated when dependencies change or new tools are introduced.

---

## Tech Stack

| Layer | Technology | Version | Purpose |
|-------|-----------|---------|---------|
| Backend | Java | 21+ | Virtual Threads (Project Loom), records, sealed classes |
| Backend | Spring Boot | 3.4+ | REST API, DI, Flyway, JPA |
| Backend | Gradle | 8+ | Build tool |
| Backend | PostgreSQL | 17+ | Primary database (RLS, JSONB, triggers) |
| Backend | Redis | 7+ | Warm cache, session data |
| Frontend | Angular | 21+ | SPA with Signals-based state |
| Frontend | Node.js | 24+ | Build toolchain |
| Frontend | TypeScript | 5.9+ | Type safety (strict mode) |
| Frontend | @jsverse/transloco | — | i18n / L10n |
| Security | AES-256-GCM | — | Field-level encryption |
| Security | HMAC-SHA256 | — | Blind indexes |
| Security | Flyway | — | Database migrations |
| Infrastructure | Docker Compose | — | Local dev (PostgreSQL + Redis) |
| CI/CD | GitHub Actions | — | Build, test, ownership validation |

---

## Repository Structure

```
OneBook/
├── CLAUDE.md                     ← AI memory bank entry point
├── memory-bank/                  ← Persistent AI memory files
├── README.md                     ← Human-facing overview
├── milestones.md                 ← Milestone specifications
├── sub-agents.md                 ← Sub-agent architecture
├── architecture.md               ← High-level system diagram
├── tally_features.md             ← Tally feature parity reference
├── CONTRIBUTING.md               ← Branching, PR, coding standards
├── docker-compose.yml            ← PostgreSQL 17 + Redis 7
├── .github/
│   ├── agents/                   ← 10 agent instruction files
│   ├── scripts/                  ← validate-agent-ownership.sh
│   └── workflows/ci.yml          ← GitHub Actions CI
├── docs/
│   ├── architecture-diagram.md   ← Detailed Mermaid diagrams
│   ├── api-documentation.md      ← REST API reference
│   ├── sql-schema.md             ← DB schema documentation
│   ├── developer-guide.md        ← Onboarding guide
│   ├── operational-runbook.md    ← Deployment & monitoring
│   └── key-binding-registry.md   ← Keyboard nav design
├── backend/
│   └── src/main/java/com/nexus/onebook/
│       ├── OneBookApplication.java
│       ├── HealthController.java
│       ├── config/               ← HeadlessApiConfig, RedisConfig
│       └── ledger/
│           ├── cache/            ← WarmCacheService, CacheConstants
│           ├── controller/       ← REST controllers
│           ├── dto/              ← Request/Response records
│           ├── exception/        ← GlobalExceptionHandler
│           ├── ingestion/        ← Gateway, Adapters, Mapper
│           ├── model/            ← JPA entities
│           ├── repository/       ← Spring Data JPA repositories
│           ├── security/         ← Encryption, BlindIndex, Audit
│           └── service/          ← Business logic services
└── frontend/src/app/
    ├── accounting/               ← Ledger view, voucher entry
    ├── ai/                       ← AI dashboard
    ├── auditor/                  ← Auditor portal
    ├── banking/                  ← Bank reconciliation
    ├── dashboard/                ← Main dashboard
    ├── gst/                      ← GST compliance
    ├── i18n/                     ← Transloco config
    ├── inventory/                ← Stock management
    ├── keyboard/                 ← Key-binding registry, command palette
    ├── market/                   ← Mark-to-Market valuation
    └── receivable/               ← Accounts receivable
```

---

## Build & Test Commands

### Local Development Setup

```bash
# 1. Start infrastructure (PostgreSQL 17 + Redis 7)
docker compose up -d

# 2. Run backend
cd backend && ./gradlew bootRun
# API: http://localhost:8080
# Health: http://localhost:8080/api/health

# 3. Run frontend
cd frontend && npm install && npm start
# App: http://localhost:4200 (proxies /api to backend)
```

### Backend

```bash
cd backend

# Full build (compile + test)
./gradlew build

# Compile only
./gradlew compileJava

# Run tests (405+ tests)
./gradlew test

# Run specific test class
./gradlew test --tests "com.nexus.onebook.ledger.service.JournalServiceTest"
```

### Frontend

```bash
cd frontend

# Install dependencies
npm install

# Production build
npx ng build

# Run tests (105+ tests, headless)
npx ng test --watch=false --browsers=ChromeHeadless

# Development server
npm start
```

### Validation

```bash
# Validate agent ownership (must pass before every commit)
./.github/scripts/validate-agent-ownership.sh
```

---

## Backend Conventions

### Package Structure
All code lives under `com.nexus.onebook` (base package).  
Business logic lives under `com.nexus.onebook.ledger`.

### Naming Conventions
- Classes: `PascalCase`
- Methods/variables: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- REST paths: `/api/<resource>` (plural, kebab-case)

### REST API Patterns
- `GET /api/<resource>` → list all
- `GET /api/<resource>/{id}` → get one
- `POST /api/<resource>` → create (returns 201)
- `PUT /api/<resource>/{id}` → full update (returns 200)
- `DELETE /api/<resource>/{id}` → delete (returns 204)

### Error Response Format
```json
{
  "timestamp": "2026-03-16T06:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Debits and credits must balance"
}
```
Handled by `GlobalExceptionHandler` (`@RestControllerAdvice`).

### Database Migrations
- Flyway naming: `V<version>__<description>.sql`
- Never use `ddl-auto: create` or `update` in production
- Current migrations: V1 through V10

### Testing
- Controller tests: `@WebMvcTest`
- Service tests: `@ExtendWith(MockitoExtension.class)`
- Integration tests: `@SpringBootTest` with `@ActiveProfiles("test")`

---

## Frontend Conventions

### Component Pattern
- All components are **standalone** (no NgModules)
- `ChangeDetectionStrategy.OnPush` for performance
- `signal()` for mutable state, `computed()` for derived state
- Call signals as functions to read: `mySignal()` not `mySignal`
- Use `set()` / `update()` to modify signal values

### Routing
- All routes are lazy-loaded
- Routes defined in `app.routes.ts`
- Key routes: `/accounting`, `/banking`, `/gst`, `/inventory`, `/keyboard`, `/ai`, `/market`, `/receivable`, `/auditor`

### Design System
- CSS custom properties (`--nx-*`) in `styles.scss`
- Dark sidebar (#263238), white navbar, light content (#eef0f2)
- Teal primary (#26a69a)
- Font: 'Mukta Malar', 'Noto Sans', sans-serif
- Dark mode: `dark-mode` class on `<html>` element

### i18n
- `@jsverse/transloco` for translations
- Config in `frontend/src/app/i18n/transloco-config.ts`

---

## Security Context

### Encryption Wire Format
```
[version byte (1)] [IV (12 bytes)] [ciphertext + GCM tag] → Base64-encoded
```

### Key Services
- `FieldEncryptionService` — AES-256-GCM encrypt/decrypt
- `BlindIndexService` — HMAC-SHA256 blind index generation
- `EncryptedStringConverter` — JPA converter for transparent field encryption

### RLS Policy Pattern
Every tenant-scoped table has RLS enabled. All queries automatically filtered by `app.current_tenant_id` session variable.

---

## Environment Variables

| Variable | Purpose |
|----------|---------|
| `ENCRYPTION_KEY` | AES-256-GCM master key (Base64) |
| `BLIND_INDEX_KEY` | HMAC-SHA256 blind index key (Base64) |
| `SPRING_DATASOURCE_URL` | PostgreSQL connection |
| `SPRING_REDIS_HOST` | Redis host |

**Never commit these to source control.**
