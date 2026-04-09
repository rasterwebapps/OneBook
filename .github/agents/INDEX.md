# OneBook Design Requirements Index

This index provides quick access to design requirements, patterns, and conventions across all agent instruction files.

---

## SDLC Agent System (Copilot Agents)

**Head Orchestrator:** `@partner` (`partner.agent.md`)

Users invoke ONLY `@partner`. It orchestrates the full SDLC lifecycle with Agile feedback loops.

| Invocable Agent | File | SDLC Role |
|----------------|------|-----------|
| 🤝 @partner | `partner.agent.md` | BA + PM + Team Lead (orchestrator) |
| 📒 @backend | `backend.agent.md` | Backend Dev Team |
| 🎹 @frontend | `frontend.agent.md` | Frontend Dev Team |
| 🗄️ @database | `database.agent.md` | DB Design Team |
| 🔐 @security | `security.agent.md` | Security Review Team |
| 🏗️ @infra | `infra.agent.md` | DevOps Team |
| 📝 @docs | `docs.agent.md` | BA Documentation Phase |
| ✅ @quality | `quality.agent.md` | Testing Team |

**Global Rules:** `.github/copilot-instructions.md` (injected into ALL Copilot interactions)

**Workflow:** User → @partner → domain agents → @partner validates → @quality tests → @docs documents → done.

**Feedback Loops:**
- After every agent phase: @partner reviews → issues route back → fix → re-review
- After all phases: @quality tests → failures route back → fix → re-test
- Cross-agent validation: API contracts, schema alignment, security coverage

---

## Requirement Orchestration

**Agent:** @partner (`partner.agent.md`)

- Domain Classification Matrix (7 domains mapped to specialist agents)
- Complexity Assessment Framework (LOW / MEDIUM / HIGH / CRITICAL)
- Orchestration workflow patterns (Sequential, Parallel, Iterative)
- Quality gate checkpoints (6 gates from classification through deployment)
- Requirement lifecycle tracking (INTAKE → CLOSED)

**Critical Rules:**
- ALWAYS classify a requirement before assigning agents
- ALWAYS define measurable acceptance criteria before implementation begins
- ALWAYS include @docs for every HIGH/CRITICAL requirement
- ALWAYS include @security for final sign-off on HIGH/CRITICAL requirements
- NEVER skip quality gates to meet speed targets
- NEVER approve CRITICAL requirements without mandatory human review
- NEVER allow implementation before Gate 1 (Classification Approved) is complete

**Templates:** `.github/templates/requirement-analysis-template.md`

**References:**
- [`partner.agent.md`](partner.agent.md) (invocable orchestrator)
- [`.github/templates/requirement-analysis-template.md`](../../.github/templates/requirement-analysis-template.md)

---

## Architecture & Infrastructure

**Agent:** @infra (`infra.agent.md`)

- Docker Compose service definitions (PostgreSQL 17, Redis 7)
- Spring Boot configuration patterns
- CI/CD pipeline structure
- Virtual Threads configuration
- CORS and API gateway setup
- Health check patterns
- Failure-safe Redis pattern (NOT circuit breaker)
- Cache strategy by data type (TTL table)
- Structured logging with MDC

**References:**
- [`docs/architecture.md`](../../docs/architecture.md)
- [`docs/technical/architecture-diagram.md`](../../docs/technical/architecture-diagram.md)
- [`docs/technical/developer-guide.md`](../../docs/technical/developer-guide.md)

---

## Accounting & Ledger

**Agent:** @backend (`backend.agent.md`)

- Double-entry validation (3-level validation: service → trigger → exception)
- Layered architecture (Controller → Service → Repository → Database)
- DTO pattern with Java records and validation annotations
- Repository query patterns (derived methods, @Query)
- Financial reporting formulas (Trial Balance, P&L, Balance Sheet, Cash Flow)
- Fixed Asset Register and depreciation calculations
- Global exception handler pattern
- Payment Processing Pipeline (3-stage pre-ledger workflow)
- Ingestion adapter pattern (FinancialEventAdapter interface)

**Critical Rules:**
- ALWAYS validate transaction balance before posting
- ALWAYS use `BigDecimal` for amounts (never `double`/`float`)
- ALWAYS include `tenantId` in queries
- NEVER post unbalanced transactions
- NEVER expose JPA entities in REST responses (use DTOs)

**References:**
- [`docs/sql-schema.md`](../../docs/sql-schema.md)
- [`docs/api-documentation.md`](../../docs/api-documentation.md)

---

## Security & Encryption

**Agent:** @security (`security.agent.md`)

- AES-256-GCM field-level encryption pattern
- Blind index (HMAC-SHA256) for searchable encryption
- Envelope encryption and key rotation
- Hash-chained audit log pattern
- Row-Level Security (RLS) policy templates
- JPA converter pattern for transparent encryption
- "Blind DBA" security model

**Critical Rules:**
- ALWAYS generate unique random IV per encryption
- ALWAYS use HMAC-SHA256 for blind indexes (not plain SHA-256)
- ALWAYS enable RLS on tenant-scoped tables
- NEVER reuse IVs (catastrophic for AES-GCM)
- NEVER log encryption keys or plaintext sensitive data
- NEVER store keys in application.yml (use environment variables)

**Wire Format:** `[version byte][IV (12 bytes)][ciphertext+tag]` → Base64

**References:**
- NIST SP 800-38D (AES-GCM)
- NIST SP 800-57 (Key Management)

---

## Performance & Caching

**Agent:** @infra (`infra.agent.md`) + @backend (`backend.agent.md`)

- Cache-aside read pattern (check cache → fallback to DB)
- Write-through pattern (write DB → invalidate cache)
- Failure-safe pattern (NOT circuit breaker)
- Cache key naming convention: `onebook:cache:<domain>:<qualifier>:<id>`
- TTL strategy (30 min default, 10 min volatile, 120 min static)
- Virtual Threads for high concurrency
- Redis configuration with Jackson serialization

**Critical Rules:**
- ALWAYS implement failure-safe for Redis (fall back to DB)
- ALWAYS use appropriate TTLs (prevent memory bloat)
- ALWAYS namespace cache keys with `onebook:cache:` prefix
- NEVER fail requests when cache is unavailable
- NEVER use indefinite TTLs
- NEVER use circuit breaker for optional services

**Performance Targets:**
- < 100ms response time for cached queries
- < 500ms for uncached queries
- > 80% cache hit rate

**References:**
- Redis Best Practices
- Spring Boot Virtual Threads documentation

---

## Frontend & UX

**Agent:** @frontend (`frontend.agent.md`)

- Angular Signals for reactive state (not RxJS Subjects)
- Standalone components (no NgModules)
- Lazy-loaded routes with `loadComponent` / `loadChildren`
- Command Palette pattern (Ctrl+K / Cmd+K)
- Tally keyboard shortcuts (17 legacy keys)
- Transloco i18n with real-time language switching
- Nexus Universal design system (CSS custom properties)
- `ChangeDetectionStrategy.OnPush` for performance
- OIDC authentication via `angular-oauth2-oidc` (Keycloak)

**Critical Rules:**
- ALWAYS use Signals for component state
- ALWAYS use `computed()` for derived state
- ALWAYS make components standalone
- ALWAYS preserve Tally shortcut compatibility
- ALWAYS support keyboard navigation (zero-mouse workflows)
- ALWAYS protect routes with `authGuard` or role-based guards
- NEVER use NgModules (use standalone components)
- NEVER mutate signals directly (use `set()` / `update()`)
- NEVER hardcode colors (use CSS custom properties)
- NEVER store tokens in localStorage (memory-only for security)

**Design Tokens:** `--nx-emerald`, `--nx-purple`, `--nx-amber`, `--nx-font-primary`

**References:**
- [`docs/key-binding-registry.md`](../../docs/key-binding-registry.md)
- [`frontend/src/styles.scss`](../../frontend/src/styles.scss)
- Angular Signals Documentation

---

## Ingestion & Adapters

**Agent:** @backend (`backend.agent.md`)

- Pluggable adapter pattern (`FinancialEventAdapter` interface)
- Adapter auto-discovery via Spring DI
- Financial Event Gateway pipeline: Parse → Validate → Map → Post
- Universal Mapper (account code → ID resolution)
- Event status tracking (RECEIVED → VALIDATED → MAPPED → POSTED → FAILED)
- Protocol support (HL7, ISO 20022, DMS, Webhooks)
- OCR invoice processing
- 3-Way Matching (PO ↔ GR ↔ Invoice)

**Critical Rules:**
- ALWAYS implement `FinancialEventAdapter` for new adapters
- ALWAYS use `@Component` for auto-discovery
- ALWAYS resolve account codes to IDs (never hardcode IDs)
- ALWAYS preserve original payload for audit trail
- NEVER bypass the gateway pipeline
- NEVER post unbalanced journal entries from adapters

**References:**
- HL7 Specification
- ISO 20022 Specification

---

## AI & Intelligence

**Agent:** @backend (`backend.agent.md`)

- Predictive cash flow forecasting (30/60/90-day horizons)
- Scenario modeling ("What-If" analysis)
- Mark-to-Market (MTM) valuation for investments
- Corporate actions automation (splits, dividends, bonus issues)
- Anomaly detection (statistical + ML-based)
- Digital asset tracking and crypto accounting

**Critical Rules:**
- ALWAYS provide confidence intervals with forecasts
- ALWAYS handle market API failures gracefully
- ALWAYS flag anomalies for review (don't auto-reject)
- NEVER train on insufficient data (< 3 months history)
- NEVER block ledger operations on AI failures
- NEVER hardcode market data (use APIs with caching)

**Anomaly Types:** HIGH_AMOUNT, DUPLICATE, UNUSUAL_TIME, RAPID_SEQUENCE

**References:**
- Alpha Vantage API
- Time series forecasting libraries

---

## Compliance & Tax

**Agent:** @backend (`backend.agent.md`)

- e-Invoice generation (India GST, EU VAT, US Sales Tax)
- e-Way Bill generation
- Feature entitlement engine (locale-specific modules)
- Bank reconciliation via Open Banking APIs
- Intercompany elimination for consolidation
- Tax calculation patterns (CGST/SGST/IGST)

**Critical Rules:**
- ALWAYS support multiple tax jurisdictions
- ALWAYS calculate tax to 2 decimal precision
- ALWAYS generate compliant e-invoices per government schemas
- NEVER hardcode tax rates (use configuration)
- NEVER mix different tax regimes in same calculation
- NEVER auto-approve unmatched bank transactions

**Tax Formula (India GST):**
- Intra-state: CGST + SGST (each = rate / 2)
- Inter-state: IGST (= full rate)

**References:**
- India GST Portal
- ISO 20022 Banking Standards

---

## Production & Auditing

**Agent:** @security (`security.agent.md`) + @infra (`infra.agent.md`)

- Read-only auditor portal (CPAs cannot modify data)
- 5 automated security checks (RLS, encryption, audit chain, secrets, CORS)
- Structured logging with MDC trace/span IDs
- Health check patterns (database, Redis, dependencies)
- Disaster recovery (automated backups, point-in-time recovery)
- Load testing (1000+ concurrent users)

**Critical Rules:**
- ALWAYS provide read-only access for auditors
- ALWAYS log auditor access for compliance
- ALWAYS use structured logging (JSON format)
- ALWAYS test disaster recovery procedures
- NEVER allow auditors to modify data
- NEVER deploy without health checks
- NEVER skip load testing before production

**Performance Targets:**
- < 100ms response (p95) for cached queries
- < 500ms response (p95) for uncached queries
- < 1% error rate under load

**References:**
- [`docs/operational-runbook.md`](../../docs/operational-runbook.md)

---

## Documentation Management

**Agent:** @docs (`docs.agent.md`)

- API documentation patterns (endpoint, request, response, errors)
- Mermaid.js diagram conventions (flowchart, sequence, ER)
- Code example patterns (realistic, working, concise)
- Markdown formatting conventions
- Documentation maintenance workflow

**Critical Rules:**
- ALWAYS keep documentation in sync with code changes
- ALWAYS test commands before documenting
- ALWAYS use Mermaid.js for diagrams (not images)
- NEVER document features that don't exist
- NEVER use outdated version numbers
- NEVER break links to other documentation

**Update Triggers:**
- New endpoint → Update `docs/api-documentation.md`
- New migration → Update `docs/sql-schema.md`
- Architecture change → Update diagrams
- New shortcut → Update `docs/key-binding-registry.md`

---

## Cross-Cutting Concerns

### Naming Conventions

| Layer | Convention | Example |
|-------|-----------|----------|
| Java Classes | PascalCase | `JournalService` |
| Java Methods | camelCase | `createTransaction()` |
| Java Constants | UPPER_SNAKE_CASE | `DEFAULT_TTL_MINUTES` |
| Database Tables | snake_case | `ledger_accounts` |
| Database Columns | snake_case | `account_code` |
| TypeScript Files | kebab-case | `voucher-entry.component.ts` |
| Angular Components | PascalCase + Suffix | `VoucherEntryComponent` |
| Angular Selectors | app- prefix + kebab-case | `app-voucher-entry` |
| Cache Keys | colon-separated | `onebook:cache:accounts:tenant:123` |

### Testing Conventions

| Framework | Pattern | Example |
|-----------|---------|---------|
| JUnit 5 | `methodName_condition_result` | `createAccount_duplicateCode_throws` |
| Mockito | Constructor-based mocks | `@InjectMocks` with `@Mock` constructor args |
| Jasmine | `describe` + `it` blocks | `describe('Service', () => it('should...'))` |
| TestBed | `configureTestingModule` | Standalone component testing |

### HTTP Status Codes

| Code | Usage | Pattern |
|------|-------|---------|
| 200 OK | Successful GET/PUT | `ResponseEntity.ok(entity)` |
| 201 Created | Successful POST | `ResponseEntity.status(HttpStatus.CREATED).body(entity)` |
| 204 No Content | Successful DELETE | `ResponseEntity.noContent().build()` |
| 400 Bad Request | Validation error | Global exception handler |
| 404 Not Found | Resource not found | `ResponseEntity.notFound().build()` |
| 500 Internal Error | Unexpected error | Global exception handler |

### Error Response Format (Uniform)
```json
{
  "timestamp": "2026-03-13T08:47:19.123Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Descriptive error message"
}
```

---

## Quick Reference by Task Type

### Analyzing a New Requirement
1. **Agent**: @partner
2. **Steps**: Receive → Classify (domain + complexity) → Assign agents → Define phases → Track progress
3. **Template**: `.github/templates/requirement-analysis-template.md`

### Adding a New REST Endpoint
1. **Agent**: @backend
2. **Steps**: DTO → Service → Controller → Tests → API docs
3. **Pattern**: See `backend.agent.md` → Domain Knowledge Reference

### Adding a New Ingestion Adapter
1. **Agent**: @backend
2. **Steps**: Implement interface → `@Component` → Tests
3. **Pattern**: See `backend.agent.md` → Ingestion Layer

### Adding Encrypted Field
1. **Agents**: @security + @backend + @infra
2. **Steps**: Add encrypted column + blind index → JPA converter → Cache invalidation
3. **Pattern**: See `security.agent.md` → Encrypted Field Schema Pattern

### Adding Keyboard Shortcut
1. **Agents**: @frontend + @docs
2. **Steps**: Register in KeyBindingRegistry → Register command → Update docs
3. **Pattern**: See `frontend.agent.md` → Keyboard Shortcut Registration

### Adding Financial Report
1. **Agents**: @backend + @frontend
2. **Steps**: Service logic → Controller endpoint → Frontend component
3. **Pattern**: See `backend.agent.md` → Financial Formulas

### Adding Flyway Migration
1. **Agents**: @database + @security + @docs
2. **Steps**: Create V#__description.sql → Enable RLS → Update schema docs
3. **Pattern**: See `database.agent.md` → Standard Table Template

---

## Common Pitfalls & Solutions

| Pitfall | Agent | Solution Reference |
|---------|-------|-------------------|
| Unbalanced transaction | @backend | `backend.agent.md` → Double-Entry Validation |
| IV reuse in encryption | @security | `security.agent.md` → Critical Security Rules |
| Cache failure crashes app | @infra | `infra.agent.md` → Failure-Safe Pattern |
| N+1 query problem | @backend | `backend.agent.md` → Additional Conventions |
| Missing RLS policy | @security, @database | `database.agent.md` → RLS Policies |
| Keyboard shortcut conflict | @frontend | `frontend.agent.md` → Tally Keyboard Shortcuts |
| Hardcoded tax rates | @backend | `backend.agent.md` → Compliance Rules |
| Incomplete documentation | @docs | `docs.agent.md` → Documentation Review Checklist |

---

## Test Count Reference

| Layer | Count | Command |
|-------|-------|---------|
| Backend | 405+ tests | `cd backend && ./gradlew test` |
| Frontend | 105+ tests | `cd frontend && npx ng test --watch=false --browsers=ChromeHeadless` |

**Testing Standards:**
- Every service method has a unit test
- Every controller has a `@WebMvcTest`
- Integration tests use `@SpringBootTest`
- Frontend tests use `TestBed`

---

## Build Commands

### Backend
```bash
cd backend
./gradlew build           # Full build with tests
./gradlew compileJava     # Compile only
./gradlew test            # Tests only
./gradlew bootRun         # Run application
```

### Frontend
```bash
cd frontend
npm install
npx ng build --configuration=production
npx ng test --watch=false --browsers=ChromeHeadless
npm start                 # Development server
```

### Infrastructure
```bash
docker compose up -d      # Start PostgreSQL + Redis
docker compose ps         # Check status
docker compose logs       # View logs
docker compose down       # Stop services
```

---

## External Resources

### Documentation
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Angular Documentation](https://angular.dev/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/17/)
- [Redis Documentation](https://redis.io/docs/)

### Standards
- [NIST Cryptographic Standards](https://csrc.nist.gov/publications)
- [HL7 Specification](https://www.hl7.org/)
- [ISO 20022 Standard](https://www.iso20022.org/)
- [India GST Portal](https://www.gst.gov.in/)

### Code Quality
- [Java Coding Conventions](https://google.github.io/styleguide/javaguide.html)
- [Angular Style Guide](https://angular.dev/style-guide)
- [OWASP Security Practices](https://owasp.org/www-project-top-ten/)

---

## Agent Collaboration Matrix

Quick reference for which agents work together on common tasks:

| Task | Primary Agent | Collaborating Agents |
|------|--------------|---------------------|
| **New Requirement Analysis** | @partner | All relevant domain agents |
| New Journal Entry Flow | @backend | @security, @infra, @frontend |
| New Industry Adapter | @backend | @backend, @backend |
| Add Encrypted Field | @security | @backend, @infra |
| New Financial Report | @backend | @frontend, @backend |
| New Keyboard Shortcut | @frontend | @docs |
| AI Feature Addition | @backend | @backend, @frontend |
| Flyway Migration | @database | @security, @infra |
| API Endpoint Change | @backend | @docs, @frontend |
| Performance Issue | @infra | @security, @backend |
| Compliance Update | @backend | @backend, @backend |
| Production Incident | @security | @infra, @security |
| **Multi-domain Feature** | @partner | Assigned per classification |

---

## Quick Navigation

- **New requirement?** → Read [`partner.agent.md`](partner.agent.md)
- **Accounting logic / Backend?** → Read [`backend.agent.md`](backend.agent.md)
- **Security/encryption?** → Read [`security.agent.md`](security.agent.md)
- **Infrastructure/performance?** → Read [`infra.agent.md`](infra.agent.md)
- **Frontend/UI work?** → Read [`frontend.agent.md`](frontend.agent.md)
- **Database/migrations?** → Read [`database.agent.md`](database.agent.md)
- **Testing/quality?** → Read [`quality.agent.md`](quality.agent.md)
- **Documentation update?** → Read [`docs.agent.md`](docs.agent.md)

---

## Maintaining Agent Ownership

**⚠️ CRITICAL**: Agent ownership must be kept up-to-date when adding new code.

When you add new files, modules, services, controllers, or migrations:
1. **Identify the owner agent** using the Quick Navigation above
2. **Update the agent instruction file** - Add to the `Files Owned` section
3. **Run validation**: `./.github/scripts/validate-agent-ownership.sh`

See **[MAINTENANCE.md](MAINTENANCE.md)** for detailed guidance, ownership rules, examples, and troubleshooting.

---

**Last Updated:** 2026-04-09  
**Maintained By:** @docs
