# REQ-009: Bank Reconciliation

**Status:** COMPLETED  
**Priority:** HIGH  
**Owner:** @LedgerExpert  
**Milestone:** M7  
**Created:** 2026-02-16  
**Last Updated:** 2026-03-18  
**Linked BRD:** [BR-009](../../business/BRD.md#br-009-bank-reconciliation)  
**Linked FRD:** [FR-013](../../business/FRD.md#8-bank-reconciliation)  
**Linked TRD:** [TR-005](../../business/TRD.md#6-tr-005-double-entry-validation)

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
Bank balances in books frequently diverge from actual bank statement balances due to timing differences (uncleared cheques, bank charges not recorded, deposits in transit). Manual reconciliation is time-consuming and error-prone. OneBook automates statement import and transaction matching.

### 1.2 Business Rules
- BR-009.1: Import bank statements in CSV/OFX/MT940 formats
- BR-009.2: Auto-match by amount + date proximity + reference
- BR-009.3: Unmatched transactions flagged for manual review
- BR-009.4: Reconciliation report shows closing balance, uncleared items, adjusted balance
- BR-009.5: Reconciled entries locked to prevent modification

---

## 2. Functional Specification

### 2.1 Reconciliation Process
```
Step 1: Finance Manager imports bank statement file
Step 2: System parses and stores bank transactions
Step 3: Auto-matching algorithm runs:
  - Match by: amount (exact) + date (±3 days) + reference (fuzzy)
  - Confidence score ≥ 85% = auto-accept
  - Confidence score < 85% = flag for manual review
Step 4: Finance Manager reviews and manually matches remaining
Step 5: Add explanatory entries for bank charges (new journal entries)
Step 6: Finalize reconciliation (lock period)
Step 7: Generate reconciliation statement
```

### 2.2 API Endpoints
```
POST   /api/reconciliation/import          — Import bank statement file
GET    /api/reconciliation/sessions        — List reconciliation sessions
GET    /api/reconciliation/sessions/{id}   — Session with matched/unmatched items
POST   /api/reconciliation/match           — Manually match entries
POST   /api/reconciliation/finalize        — Lock reconciliation
GET    /api/reconciliation/report          — Reconciliation statement
```

---

## 3. Technical Specification

### 3.1 Data Model
```sql
-- V7__reporting_compliance_far.sql
CREATE TABLE bank_feed_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    bank_account_id UUID NOT NULL,
    transaction_date DATE NOT NULL,
    value_date DATE,
    amount DECIMAL(19,4) NOT NULL,
    transaction_type VARCHAR(6) NOT NULL,
    description TEXT,
    reference VARCHAR(100),
    reconciliation_status VARCHAR(20) DEFAULT 'UNMATCHED',
    matched_journal_entry_id UUID,
    imported_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE reconciliation_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    bank_account_id UUID NOT NULL,
    period_from DATE NOT NULL,
    period_to DATE NOT NULL,
    opening_book_balance DECIMAL(19,4),
    closing_book_balance DECIMAL(19,4),
    closing_bank_balance DECIMAL(19,4),
    status VARCHAR(20) DEFAULT 'IN_PROGRESS',
    finalized_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);
```

### 3.2 Implementation Files
- `BankReconciliationService.java`
- `ReconciliationController.java`
- `BankFeedTransactionRepository.java`

---

## 4. Acceptance Criteria

```gherkin
Feature: Bank Reconciliation

  Scenario: Auto-match bank statement transactions
    Given 50 bank statement transactions imported
    And matching book entries exist
    When auto-matching runs
    Then transactions matching by amount + date ±2 days are auto-matched
    And unmatched items are flagged

  Scenario: Reconciliation report shows zero difference after matching
    Given all transactions matched and bank charges posted
    When I generate the reconciliation report
    Then Adjusted Book Balance = Adjusted Bank Balance
    And Difference = ₹0

  Scenario: Finalized reconciliation is locked
    Given reconciliation session finalized
    When I attempt to modify a reconciled journal entry
    Then response is 403 Forbidden
    And error states "reconciliation period locked"
```

---

## 7. Traceability

| Artifact | Link |
|---------|------|
| BRD | [BR-009](../../business/BRD.md#br-009-bank-reconciliation) |
| FRD | [FR-013](../../business/FRD.md#8-bank-reconciliation) |
| TRD | [TR-005](../../business/TRD.md#6-tr-005-double-entry-validation) |
| RTM | [RTM Row REQ-009](../RTM.md) |
| User Stories | [US-009](../../business/user-stories.md) |
| Agent Owner | [@backend](../../../.github/agents/backend.agent.md) |
| Migration | `V7__reporting_compliance_far.sql` |
