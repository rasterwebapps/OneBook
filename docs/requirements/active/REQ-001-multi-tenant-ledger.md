# REQ-001: Multi-Tenant Ledger

**Status:** COMPLETED  
**Priority:** CRITICAL  
**Owner:** @LedgerExpert  
**Milestone:** M1/M2  
**Created:** 2026-01-01  
**Last Updated:** 2026-03-18  
**Linked BRD:** [BR-001](../../business/BRD.md#br-001-multi-tenant-accounting)  
**Linked FRD:** [FR-001, FR-002](../../business/FRD.md#2-ledger-management)  
**Linked TRD:** [TR-001](../../business/TRD.md#2-tr-001-multi-tenant-rls)

---

## Quality Gate Checklist

- [x] Business Context documented
- [x] Functional Specification documented
- [x] Technical Specification documented
- [x] Acceptance Criteria (Gherkin) defined
- [x] Implementation complete
- [x] Unit tests written and passing
- [x] Integration tests written and passing
- [x] BRD updated
- [x] FRD updated
- [x] TRD updated
- [x] RTM updated
- [x] Agent ownership updated

---

## 1. Business Context

### 1.1 Problem Statement
Enterprise accounting systems historically required separate installations per organization, leading to high operational costs and fragmented data management. OneBook must serve multiple independent organizations (tenants) from a single deployment without any risk of data leakage between organizations.

Each tenant represents a completely independent business entity with its own chart of accounts, journal entries, users, and configuration. A transaction posted in Tenant A must never appear in any query executed in the context of Tenant B — even under adversarial conditions such as SQL injection.

### 1.2 Business Value
- **Cost reduction:** Single deployment serves N tenants, reducing infrastructure cost per tenant
- **Data isolation guarantee:** Regulatory and contractual requirement for SaaS financial systems
- **Scale:** New tenants can be onboarded without infrastructure changes
- **Compliance:** Required for multi-tenant financial data under GDPR, DPDP, and banking regulations

### 1.3 Stakeholders
| Role | Interest |
|------|---------|
| IT Administrators | Tenant provisioning, isolation verification |
| Finance Managers | Independent books per organization |
| Auditors | Verifiable tenant boundary enforcement |
| C-Suite | Multi-subsidiary management |

### 1.4 Business Rules
- BR-001.1: All tenant data is isolated at the database layer using Row-Level Security
- BR-001.2: Multi-entity hierarchy: Enterprise → Company → Branch → Cost Center
- BR-001.3: Tenant configuration is independently settable per tenant
- BR-001.4: API endpoints require validated tenant context before any database operation
- BR-001.5: Performance must not degrade as the number of tenants increases

---

## 2. Functional Specification

### 2.1 Feature Description
The multi-tenant ledger provides each tenant with a complete, isolated general ledger. Accounts, journal entries, voucher types, cost centers, and branches are all scoped to a single tenant via `tenant_id` on every table, with PostgreSQL RLS enforcing isolation at the database layer.

### 2.2 User Flows

**Flow 1: Accountant creates an account in their tenant**
```
Step 1: Accountant sends POST /api/ledger/accounts with JWT bearing tenant ID
Step 2: TenantContextFilter extracts tenant ID from JWT
Step 3: Filter sets: SET app.current_tenant_id = '{tenantId}'
Step 4: LedgerAccountService creates account with tenant_id set
Step 5: PostgreSQL RLS enforces that only this tenant's rows are visible
Step 6: Account returned in response
```

**Flow 2: Cross-tenant isolation test**
```
Step 1: Tenant A has 100 accounts
Step 2: Request arrives with Tenant B's JWT
Step 3: TenantContextFilter sets app.current_tenant_id to Tenant B's ID
Step 4: SELECT from ledger_accounts — RLS returns only Tenant B's rows
Step 5: Tenant A's 100 accounts are never returned
```

### 2.3 Inputs
| Input | Type | Required | Validation |
|-------|------|----------|-----------|
| accountCode | String | Yes | Unique per tenant, alphanumeric |
| accountName | String | Yes | 1–255 chars |
| accountType | Enum | Yes | ASSET, LIABILITY, INCOME, EXPENSE, EQUITY |
| parentId | UUID | No | Must exist in same tenant |
| openingBalance | BigDecimal | No | Default 0.00 |
| currencyCode | String | No | ISO 4217, default INR |

### 2.4 Outputs
| Output | Type | Description |
|--------|------|-------------|
| id | UUID | System-generated account identifier |
| tenantId | UUID | Owning tenant (set from JWT context) |
| accountCode | String | Unique code within tenant |
| accountType | Enum | Account classification |
| balance | BigDecimal | Current balance (aggregated) |

### 2.5 Validation Rules
- VR-001: Account code must be unique within the tenant
- VR-002: Circular parent references are prohibited
- VR-003: tenantId must exist in tenant_config table
- VR-004: Amount fields use BigDecimal (never double/float)
- VR-005: All queries include tenant_id predicate

### 2.6 API Endpoints
```
POST   /api/ledger/accounts          — Create account
GET    /api/ledger/accounts          — List accounts for current tenant
GET    /api/ledger/accounts/{id}     — Get account (tenant-scoped)
PUT    /api/ledger/accounts/{id}     — Update account
DELETE /api/ledger/accounts/{id}     — Deactivate account
GET    /api/ledger/accounts/tree     — Hierarchical account tree
GET    /api/ledger/accounts/search   — Search (blind index)
```

### 2.7 UI Screens
- Chart of Accounts (`/accounting`) — List, tree view, create/edit

---

## 3. Technical Specification

### 3.1 Architecture Decisions
- **Single-database multi-tenancy** (not schema-per-tenant) was chosen to simplify migrations (single Flyway run) and reduce operational overhead.
- **RLS over application-level filtering** was chosen as the primary isolation mechanism because it provides infrastructure-level guarantees that survive SQL injection attacks.
- **`SET app.current_tenant_id`** per connection is the PostgreSQL mechanism used to communicate tenant context to RLS policies.

### 3.2 Data Model
```sql
-- V1__rls_infrastructure.sql
CREATE TABLE tenant_config (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID UNIQUE NOT NULL,
    business_name VARCHAR(255) NOT NULL,
    locale VARCHAR(20) DEFAULT 'en-IN',
    currency_code VARCHAR(3) DEFAULT 'INR',
    fiscal_year_start INTEGER DEFAULT 4,
    encryption_key_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- V3__ledger_and_journal.sql
CREATE TABLE ledger_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    account_code VARCHAR(50) NOT NULL,
    account_name VARCHAR(255) NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    parent_id UUID REFERENCES ledger_accounts(id),
    opening_balance DECIMAL(19,4) DEFAULT 0,
    currency_code VARCHAR(3) DEFAULT 'INR',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, account_code)
);

ALTER TABLE ledger_accounts ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON ledger_accounts
    USING (tenant_id = current_setting('app.current_tenant_id')::uuid);
```

### 3.3 Key Algorithms / Logic
```java
// TenantContextFilter.java — sets per-request tenant context
@Component
public class TenantContextFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) {
        String tenantId = extractTenantFromJwt(req);
        entityManager.createNativeQuery(
            "SET app.current_tenant_id = :tenantId"
        ).setParameter("tenantId", tenantId).executeUpdate();
        chain.doFilter(req, res);
    }
}
```

### 3.4 Performance Considerations
- Expected data volume: ~10,000 accounts per tenant, 10–1000 tenants
- Target response time: < 100ms for account list
- Caching: Account balances cached in Redis (`onebook:cache:ledger:account:{id}`, 30min TTL)
- Index: `CREATE INDEX idx_ledger_accounts_tenant ON ledger_accounts(tenant_id, account_code)`

### 3.5 Security Considerations
- Fields requiring encryption: `account_name` (for sensitive party accounts)
- RLS: Yes — `ledger_accounts`, `journal_transactions`, `journal_entries`, all 40+ tenant tables
- Audit trail: CREATE, UPDATE, DEACTIVATE events

---

## 4. Acceptance Criteria

```gherkin
Feature: Multi-Tenant Ledger Isolation

  Scenario: Create account succeeds for valid tenant
    Given I am authenticated as tenant "Acme Corp" (UUID: acme-uuid)
    When I POST /api/ledger/accounts with accountCode "1001" and accountType "ASSET"
    Then the response status should be 201 Created
    And the returned account should have tenantId "acme-uuid"

  Scenario: Account not visible to another tenant
    Given account "1001" exists for tenant "Acme Corp"
    When I GET /api/ledger/accounts authenticated as tenant "Beta Ltd"
    Then account "1001" should NOT be in the response

  Scenario: Duplicate account code rejected within same tenant
    Given account code "1001" exists for tenant "Acme Corp"
    When I POST another account with code "1001" for "Acme Corp"
    Then the response status should be 409 Conflict

  Scenario: Different tenants can have same account code
    Given tenant "Acme Corp" has account code "1001"
    When tenant "Beta Ltd" creates account code "1001"
    Then the creation should succeed with status 201
    And each tenant's account "1001" is independent

  Scenario: RLS enforced at database level
    Given tenant context is set to "Acme Corp"
    When a raw SQL SELECT executes on ledger_accounts
    Then only rows where tenant_id = 'acme-uuid' are returned
    And no rows from "Beta Ltd" are visible
```

---

## 5. Implementation

### 5.1 New Files Created
| File | Package | Purpose |
|------|---------|---------|
| `LedgerAccount.java` | `com.nexus.onebook.ledger.entity` | JPA entity for ledger accounts |
| `LedgerAccountService.java` | `com.nexus.onebook.ledger.service` | Business logic for account management |
| `LedgerController.java` | `com.nexus.onebook.ledger.controller` | REST endpoints for ledger operations |
| `LedgerAccountRepository.java` | `com.nexus.onebook.ledger.repository` | Spring Data JPA repository |
| `TenantContextFilter.java` | `com.nexus.onebook.ledger.security` | Sets per-request tenant context |

### 5.2 Modified Files
| File | Change Description |
|------|-------------------|
| `V1__rls_infrastructure.sql` | Created tenant_config table and RLS infrastructure |
| `V2__organizational_hierarchy.sql` | Created companies, branches, cost_centers tables |
| `V3__ledger_and_journal.sql` | Created ledger_accounts with RLS |

### 5.3 Migration
- Migration files: `V1__rls_infrastructure.sql`, `V2__organizational_hierarchy.sql`, `V3__ledger_and_journal.sql`
- Tables: `tenant_config`, `companies`, `branches`, `cost_centers`, `ledger_accounts`
- RLS policies on all tenant-scoped tables

---

## 6. Testing

### 6.1 Unit Tests
| Test Class | Method | Scenario |
|------------|--------|---------|
| `LedgerAccountServiceTest` | `testCreateAccount_success` | Happy path account creation |
| `LedgerAccountServiceTest` | `testCreateAccount_duplicateCode` | Duplicate code rejection |
| `LedgerAccountServiceTest` | `testGetAccountHierarchy` | Tree structure correctness |

### 6.2 Integration Tests
| Test Class | Method | Scenario |
|------------|--------|---------|
| `LedgerControllerTest` | `testCreateAccount_returns201` | HTTP endpoint test |
| `RlsTenantIsolationTest` | `testTenantIsolation` | Cross-tenant isolation |

---

## 7. Traceability

| Artifact | Link |
|---------|------|
| BRD | [BR-001](../../business/BRD.md#br-001-multi-tenant-accounting) |
| FRD | [FR-001, FR-002](../../business/FRD.md#2-ledger-management) |
| TRD | [TR-001](../../business/TRD.md#2-tr-001-multi-tenant-rls) |
| RTM | [RTM Row REQ-001](../RTM.md) |
| User Stories | [US-001, US-003, US-004](../../business/user-stories.md) |
| Agent Owner | [@backend](../../../.github/agents/backend.agent.md) |
| Migration | `V1__rls_infrastructure.sql`, `V2__organizational_hierarchy.sql`, `V3__ledger_and_journal.sql` |

---

## 8. Change History

| Date | Author | Change |
|------|--------|--------|
| 2026-01-01 | @LedgerExpert | Initial implementation (M1/M2) |
| 2026-03-18 | @RequirementsAnalyzer | Documentation formalized |
