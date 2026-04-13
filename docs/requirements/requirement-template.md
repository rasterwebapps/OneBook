# Requirement File Template
## OneBook — Nexus Universal Accounting OS

> **Copy this template for every new requirement file.**  
> File naming: `REQ-XXX-short-description.md`  
> Location: `docs/requirements/active/` (for active requirements)

---

# REQ-XXX: [Requirement Title]

**Status:** DRAFT | IN_PROGRESS | COMPLETED | REJECTED  
**Priority:** CRITICAL | HIGH | MEDIUM | LOW  
**Owner:** @AgentName  
**Milestone:** M[X]  
**Created:** YYYY-MM-DD  
**Last Updated:** YYYY-MM-DD  
**Linked BRD:** [BR-XXX](../../business/BRD.md#br-xxx)  
**Linked FRD:** [FR-XXX](../../business/FRD.md#fr-xxx)  
**Linked TRD:** [TR-XXX](../../business/TRD.md#tr-xxx)

---

## Quality Gate Checklist

- [ ] Business Context documented
- [ ] Functional Specification documented
- [ ] Technical Specification documented
- [ ] Acceptance Criteria (Gherkin) defined
- [ ] Implementation complete
- [ ] Unit tests written and passing
- [ ] Integration tests written and passing
- [ ] BRD updated
- [ ] FRD updated
- [ ] TRD updated
- [ ] RTM updated
- [ ] Agent ownership updated

---

## 1. Business Context

### 1.1 Problem Statement
_Describe the business problem this requirement solves. 1–3 paragraphs._

### 1.2 Business Value
_Quantify the value delivered: time saved, risk mitigated, compliance achieved, etc._

### 1.3 Stakeholders
| Role | Interest |
|------|---------|
| [Stakeholder 1] | [What they need from this requirement] |
| [Stakeholder 2] | [What they need from this requirement] |

### 1.4 Business Rules
- BR-XXX.1: [Business rule statement]
- BR-XXX.2: [Business rule statement]
- BR-XXX.3: [Business rule statement]

---

## 2. Functional Specification

### 2.1 Feature Description
_Describe what the system should do from a user/system perspective._

### 2.2 User Flows

**Flow 1: [Primary Flow Name]**
```
Step 1: User/System action
Step 2: System response
Step 3: ...
```

**Flow 2: [Alternative Flow Name]**
```
Step 1: ...
```

### 2.3 Inputs
| Input | Type | Required | Validation |
|-------|------|----------|-----------|
| [field name] | String/Number/Date/UUID | Yes/No | [validation rule] |

### 2.4 Outputs
| Output | Type | Description |
|--------|------|-------------|
| [field name] | String/Number/Date | [description] |

### 2.5 Validation Rules
- VR-001: [Rule description]
- VR-002: [Rule description]
- VR-003: [Rule description]

### 2.6 API Endpoints
```
METHOD  /api/[path]             — [Description]
METHOD  /api/[path]/{id}        — [Description]
```

### 2.7 UI Screens
- [Screen Name] (`/route/path`) — [Description]

---

## 3. Technical Specification

### 3.1 Architecture Decisions
_Document any architecture decisions specific to this requirement. Link to TRD patterns._

### 3.2 Data Model
```sql
-- New tables or columns required
CREATE TABLE [table_name] (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    -- columns...
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

ALTER TABLE [table_name] ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON [table_name]
    USING (tenant_id = current_setting('app.current_tenant_id')::uuid);
```

### 3.3 Key Algorithms / Logic
```java
// Pseudocode or actual code snippet for complex logic
```

### 3.4 Performance Considerations
- Expected data volume: [X rows/year per tenant]
- Target response time: [Xms P95]
- Caching strategy: [None / Cache-Aside with TTL Xmin]
- Index requirements: [list indexes needed]

### 3.5 Security Considerations
- Fields requiring encryption: [list fields]
- Blind indexes needed for: [list searchable encrypted fields]
- RLS: [Yes/No — table names]
- Audit trail: [Events that trigger audit entries]

---

## 4. Acceptance Criteria

```gherkin
Feature: [Feature Name]

  Scenario: [Primary happy path]
    Given [precondition]
    When [action]
    Then [expected outcome]
    And [additional assertion]

  Scenario: [Validation error path]
    Given [precondition]
    When [invalid action]
    Then the response status should be [4XX]
    And the error message should [describe expected error]

  Scenario: [Security / tenant isolation]
    Given [tenant context]
    When [cross-tenant attempt]
    Then [access denied / no data returned]
```

---

## 5. Implementation

### 5.1 New Files Created
| File | Package | Purpose |
|------|---------|---------|
| `[ClassName].java` | `com.nexus.onebook.[package]` | [Description] |
| `[ClassName].java` | `com.nexus.onebook.[package]` | [Description] |

### 5.2 Modified Files
| File | Change Description |
|------|-------------------|
| `[ClassName].java` | [What was changed] |
| `V[X]__[description].sql` | [Migration changes] |

### 5.3 Frontend Changes
| File | Component/Service | Change |
|------|-----------------|--------|
| `[component].component.ts` | [ComponentName] | [Description] |

### 5.4 Migration
- Migration file: `V[X]__[description].sql`
- Tables: [List tables created/modified]
- Indexes: [List indexes added]
- RLS policies: [List policies added]

---

## 6. Testing

### 6.1 Unit Tests
| Test Class | Test Method | Scenario |
|------------|-------------|---------|
| `[ServiceTest].java` | `test[MethodName]` | [What it tests] |

### 6.2 Integration Tests
| Test Class | Test Method | Scenario |
|------------|-------------|---------|
| `[ControllerTest].java` | `test[Endpoint]` | [What it tests] |

### 6.3 Test Coverage
- Service layer: [X]% coverage
- Controller layer: [X]% coverage
- Total new tests: [X]

---

## 7. Traceability

| Artifact | Link |
|---------|------|
| BRD Section | [docs/business/BRD.md#br-xxx](../../business/BRD.md#br-xxx) |
| FRD Section | [docs/business/FRD.md#fr-xxx](../../business/FRD.md#fr-xxx) |
| TRD Section | [docs/business/TRD.md#tr-xxx](../../business/TRD.md#tr-xxx) |
| RTM Row | [docs/requirements/RTM.md](../RTM.md) |
| User Story | [US-XXX](../../business/user-stories.md#us-xxx) |
| Agent Owner | [.github/agents/[agent].md](../../../.github/agents/) |
| DB Migration | `backend/src/main/resources/db/migration/V[X]__[name].sql` |

---

## 8. Change History

| Date | Author | Change |
|------|--------|--------|
| YYYY-MM-DD | @AgentName | Initial draft |

---

*Template maintained by @RequirementsAnalyzer. Version 1.0.*
