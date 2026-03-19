# REQ-006: Cost Center & Branch Management

**Status:** COMPLETED  
**Priority:** HIGH  
**Owner:** @LedgerExpert  
**Milestone:** M2  
**Created:** 2026-01-08  
**Last Updated:** 2026-03-18  
**Linked BRD:** [BR-006](../../business/BRD.md#br-006-cost-center--branch-management)  
**Linked FRD:** [FR-001, FR-008](../../business/FRD.md#2-ledger-management)  
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
Large enterprises need to track financial performance at department, branch, and project levels for internal management reporting. Without cost center tracking, all expenses are pooled together — making it impossible to identify underperforming departments or allocate costs accurately.

### 1.2 Business Rules
- BR-006.1: Multi-level hierarchy: Enterprise → Company → Branch → Cost Center
- BR-006.2: Journal entries allocatable to cost centers
- BR-006.3: Cost center P&L reports available
- BR-006.4: Branches have their own accounts rolling up to parent
- BR-006.5: Intercompany transactions eliminatable during consolidation

---

## 2. Functional Specification

### 2.1 Hierarchy Model
```
Enterprise Group
  └── Company A (Legal entity)
        ├── Branch: HQ
        │     ├── Cost Center: Marketing
        │     ├── Cost Center: Engineering
        │     └── Cost Center: Sales
        └── Branch: Mumbai Office
              ├── Cost Center: Operations
              └── Cost Center: Support
```

### 2.2 API Endpoints
```
GET    /api/ledger/branches             — List branches for tenant
POST   /api/ledger/branches             — Create branch
GET    /api/ledger/cost-centers         — List cost centers
POST   /api/ledger/cost-centers         — Create cost center
GET    /api/reports/cost-center/{id}    — Cost center P&L
POST   /api/consolidation/generate      — Generate consolidated report
```

---

## 3. Technical Specification

### 3.1 Data Model
```sql
-- V2__organizational_hierarchy.sql
CREATE TABLE companies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    parent_company_id UUID REFERENCES companies(id),
    gstin VARCHAR(15),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE branches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    company_id UUID NOT NULL REFERENCES companies(id),
    branch_name VARCHAR(255) NOT NULL,
    branch_code VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE cost_centers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    branch_id UUID REFERENCES branches(id),
    cost_center_name VARCHAR(255) NOT NULL,
    cost_center_code VARCHAR(20),
    parent_cost_center_id UUID REFERENCES cost_centers(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### 3.2 Implementation Files
- `CostCenterRepository.java`
- `BranchRepository.java`
- `IntercompanyService.java`
- `ConsolidationController.java`
- `V2__organizational_hierarchy.sql`

---

## 4. Acceptance Criteria

```gherkin
Feature: Cost Center Management

  Scenario: Journal entry allocated to cost center
    Given cost center "Marketing" (cc-001) exists
    When I post a journal entry with costCenterId "cc-001" on the expense line
    Then the entry is tagged with cost center "Marketing"
    And cost center P&L for "Marketing" reflects the expense

  Scenario: Cost centers roll up to branch
    Given Marketing and Engineering belong to HQ Branch
    When I request Branch P&L for "HQ Branch"
    Then both cost centers' figures aggregate in the report

  Scenario: Intercompany elimination on consolidation
    Given Entity A sold ₹10L to Entity B (intercompany)
    When I POST /api/consolidation/generate
    Then the intercompany revenue and expense are eliminated
    And consolidated net profit excludes the intercompany transaction
```

---

## 7. Traceability

| Artifact | Link |
|---------|------|
| BRD | [BR-006](../../business/BRD.md#br-006-cost-center--branch-management) |
| FRD | [FR-001](../../business/FRD.md#2-ledger-management) |
| TRD | [TR-001](../../business/TRD.md#2-tr-001-multi-tenant-rls) |
| RTM | [RTM Row REQ-006](../RTM.md) |
| User Stories | [US-004, US-018](../../business/user-stories.md) |
| Agent Owner | [@LedgerExpert](../../../.github/agents/ledger-expert.md) |
| Migration | `V2__organizational_hierarchy.sql` |
