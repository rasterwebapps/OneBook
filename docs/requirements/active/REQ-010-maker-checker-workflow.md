# REQ-010: Maker-Checker-Approver Workflow

**Status:** COMPLETED  
**Priority:** HIGH  
**Owner:** @AuditAgent  
**Milestone:** M10  
**Created:** 2026-03-01  
**Last Updated:** 2026-03-18  
**Linked BRD:** [BR-010](../../business/BRD.md#br-010-maker-checker-approver-workflow)  
**Linked FRD:** [FR-014](../../business/FRD.md#9-workflows)  
**Linked TRD:** [TR-007](../../business/TRD.md#8-tr-007-hash-chained-audit-trail)

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
Internal controls in financial systems require separation of duties to prevent fraud and errors. Without workflow enforcement, a single user can create and post a fraudulent payment without oversight. The Maker-Checker-Approver workflow ensures that transactions above configured thresholds require independent sign-off.

### 1.2 Business Rules
- BR-010.1: Vouchers above threshold require Checker approval before posting
- BR-010.2: High-value transactions require additional Approver sign-off
- BR-010.3: Workflow state: DRAFT → PENDING_CHECK → CHECKED → PENDING_APPROVAL → APPROVED → POSTED
- BR-010.4: Rejected transactions return to Maker with reason
- BR-010.5: All state transitions logged in audit trail

---

## 2. Functional Specification

### 2.1 Workflow State Machine
```
DRAFT (Maker creates)
  ↓ submit
PENDING_CHECK
  ↓ checker approves           ↓ checker rejects
CHECKED                       REJECTED (→ Maker)
  ↓ (if high-value)
PENDING_APPROVAL
  ↓ approver approves          ↓ approver rejects
APPROVED                      REJECTED (→ Maker)
  ↓ (auto)
POSTED
```

### 2.2 Threshold Configuration
| Voucher Type | Checker Threshold | Approver Threshold |
|-------------|------------------|-------------------|
| Payment | ₹1,00,000 | ₹10,00,000 |
| Journal | ₹50,000 | ₹5,00,000 |
| Configurable | Per-tenant | Per-tenant |

### 2.3 API Endpoints
```
POST   /api/workflow/submit/{voucherId}     — Maker submits for check
POST   /api/workflow/check/{voucherId}      — Checker approves/rejects
POST   /api/workflow/approve/{voucherId}    — Approver approves/rejects
GET    /api/workflow/pending                — Pending items for current user
GET    /api/workflow/history/{voucherId}    — Full workflow history
```

---

## 3. Technical Specification

### 3.1 Implementation Files
- `AuditorPortalService.java` — Workflow orchestration
- `AuditorPortalController.java` — REST endpoints
- `AuditWorkflowRepository.java` — Workflow persistence
- `SecurityAuditService.java` — Security audit integration
- `V9__hardening_audit_production.sql` — Workflow tables

### 3.2 Data Model
```sql
-- V9__hardening_audit_production.sql
CREATE TABLE audit_workflows (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    current_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    maker_id VARCHAR(255),
    checker_id VARCHAR(255),
    approver_id VARCHAR(255),
    rejection_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_workflow_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workflow_id UUID NOT NULL REFERENCES audit_workflows(id),
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,
    actor_id VARCHAR(255) NOT NULL,
    action VARCHAR(20) NOT NULL,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

---

## 4. Acceptance Criteria

```gherkin
Feature: Maker-Checker-Approver Workflow

  Scenario: High-value voucher requires checker approval
    Given workflow threshold ₹1,00,000 for Payment
    When Maker creates payment for ₹2,00,000
    Then voucher status = PENDING_CHECK (not POSTED)
    And notification sent to Checker users

  Scenario: Checker approves and moves to CHECKED
    Given voucher in PENDING_CHECK
    When Checker POST /api/workflow/check/{id} with action APPROVE
    Then status moves to CHECKED (or PENDING_APPROVAL if high-value)

  Scenario: Maker cannot approve own voucher
    Given user "alice" created voucher V-001
    When "alice" tries to check/approve V-001
    Then response is 403 Forbidden
    And error: "cannot approve own transaction"

  Scenario: Rejected voucher returns to Maker with reason
    Given voucher in PENDING_CHECK
    When Checker rejects with reason "wrong account"
    Then status = REJECTED
    And Maker sees rejection reason "wrong account"
    And can resubmit after correction
```

---

## 7. Traceability

| Artifact | Link |
|---------|------|
| BRD | [BR-010](../../business/BRD.md#br-010-maker-checker-approver-workflow) |
| FRD | [FR-014](../../business/FRD.md#9-workflows) |
| TRD | [TR-007](../../business/TRD.md#8-tr-007-hash-chained-audit-trail) |
| RTM | [RTM Row REQ-010](../RTM.md) |
| User Stories | [US-010, US-019](../../business/user-stories.md) |
| Agent Owner | [@security](../../../.github/agents/security.agent.md) |
| Migration | `V9__hardening_audit_production.sql` |
