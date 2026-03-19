# REQ-004: Voucher Posting

**Status:** COMPLETED  
**Priority:** CRITICAL  
**Owner:** @LedgerExpert  
**Milestone:** M2  
**Created:** 2026-01-05  
**Last Updated:** 2026-03-18  
**Linked BRD:** [BR-004](../../business/BRD.md#br-004-voucher-posting)  
**Linked FRD:** [FR-002](../../business/FRD.md#fr-002-journal-entry--voucher-posting)  
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
The fundamental operation of any accounting system is recording financial transactions as journal vouchers. OneBook must support all standard Indian accounting voucher types (as used in Tally), enforce double-entry integrity at multiple layers, and provide keyboard-speed voucher entry compatible with trained Tally users.

### 1.2 Business Value
- Core accounting functionality — without this, the system cannot function
- Tally compatibility — trained accountants can switch without relearning
- Integrity — triple-layer double-entry validation prevents corrupt books

### 1.3 Business Rules
- BR-004.1: Voucher types: Payment, Receipt, Journal, Contra, Sales, Purchase, Debit Note, Credit Note, Stock Journal
- BR-004.2: Sum of debits must equal sum of credits (BigDecimal comparison)
- BR-004.3: Three-layer validation: service, DB trigger, exception
- BR-004.4: Vouchers support narration, reference, cost center, multi-currency
- BR-004.5: Posted vouchers immutable; corrections via reversing entries

---

## 2. Functional Specification

### 2.1 Voucher Types
| Voucher Type | Shortcut | Use Case |
|-------------|----------|---------|
| Payment | F5 | Cash/bank payments to parties |
| Receipt | F6 | Cash/bank receipts from parties |
| Journal | F7 | General adjusting entries |
| Contra | F4 | Cash/bank transfers |
| Sales | F8 | Revenue from sales |
| Purchase | F9 | Purchase of goods/services |
| Debit Note | — | Purchase return / debit adjustments |
| Credit Note | — | Sales return / credit adjustments |
| Stock Journal | — | Inventory stock movements |

### 2.2 Inputs
| Input | Type | Required | Validation |
|-------|------|----------|-----------|
| voucherType | Enum | Yes | One of standard types |
| transactionDate | Date | Yes | Not in locked period |
| narration | String | No | Encrypted before storage |
| referenceNumber | String | No | Free text |
| entries | List<JournalEntryRequest> | Yes | Min 2 entries |
| entries[].accountId | UUID | Yes | Must belong to same tenant |
| entries[].entryType | Enum | Yes | DEBIT or CREDIT |
| entries[].amount | BigDecimal | Yes | Positive, ≥ 0.01 |
| entries[].costCenterId | UUID | No | Cost center allocation |

### 2.3 API Endpoints
```
POST   /api/journal/transactions                — Post voucher
GET    /api/journal/transactions                — List transactions
GET    /api/journal/transactions/{id}           — Get with entries
POST   /api/journal/transactions/{id}/reverse   — Reverse posted voucher
GET    /api/journal/voucher-types               — List voucher types
POST   /api/journal/voucher-types               — Create custom voucher type
```

---

## 3. Technical Specification

### 3.1 Triple-Layer Double-Entry Validation

**Layer 1 — Java Service:**
```java
// JournalService.java
BigDecimal totalDebits = entries.stream()
    .filter(e -> DEBIT.equals(e.getEntryType()))
    .map(JournalEntryRequest::getAmount)
    .reduce(BigDecimal.ZERO, BigDecimal::add);
BigDecimal totalCredits = entries.stream()
    .filter(e -> CREDIT.equals(e.getEntryType()))
    .map(JournalEntryRequest::getAmount)
    .reduce(BigDecimal.ZERO, BigDecimal::add);
if (totalDebits.compareTo(totalCredits) != 0) {
    throw new UnbalancedTransactionException(totalDebits, totalCredits);
}
```

**Layer 2 — PostgreSQL Trigger:**
```sql
CREATE OR REPLACE FUNCTION check_balanced_transaction() RETURNS TRIGGER AS $$
DECLARE net DECIMAL;
BEGIN
    SELECT SUM(CASE WHEN entry_type='DEBIT' THEN amount ELSE -amount END)
    INTO net FROM journal_entries WHERE transaction_id = NEW.id;
    IF net != 0 THEN
        RAISE EXCEPTION 'Unbalanced: debit/credit mismatch = %', net;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

**Layer 3 — HTTP Exception:** `UnbalancedTransactionException` → HTTP 422 with debit/credit totals.

### 3.2 Data Model
```sql
-- V3__ledger_and_journal.sql
CREATE TABLE journal_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    voucher_type VARCHAR(30) NOT NULL,
    voucher_number VARCHAR(50),
    transaction_date DATE NOT NULL,
    narration_encrypted TEXT,
    reference_number VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'POSTED',
    currency_code VARCHAR(3) DEFAULT 'INR',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE journal_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL REFERENCES journal_transactions(id),
    account_id UUID NOT NULL REFERENCES ledger_accounts(id),
    entry_type VARCHAR(6) NOT NULL CHECK (entry_type IN ('DEBIT','CREDIT')),
    amount DECIMAL(19,4) NOT NULL CHECK (amount > 0),
    cost_center_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

---

## 4. Acceptance Criteria

```gherkin
Feature: Voucher Posting

  Scenario: Post balanced payment voucher
    Given accounts Cash (1001, ASSET) and Expense (5001, EXPENSE) exist
    When I POST /api/journal/transactions with:
      voucherType: PAYMENT, entries: [DEBIT 5001 ₹5000, CREDIT 1001 ₹5000]
    Then response status is 201
    And transaction status is POSTED
    And account 5001 balance increases by ₹5000

  Scenario: Reject unbalanced entry
    When I POST with entries [DEBIT 5001 ₹5000, CREDIT 1001 ₹4999]
    Then response status is 422
    And error contains "Debits (5000) != Credits (4999)"

  Scenario: Posted voucher is immutable
    Given voucher V-001 is POSTED
    When I PUT /api/journal/transactions/V-001 to change an amount
    Then response status is 403
    And message states "posted transactions are immutable"

  Scenario: Reverse a posted voucher
    Given voucher V-001 has Dr Office Exp ₹5000, Cr Cash ₹5000
    When I POST /api/journal/transactions/V-001/reverse
    Then a new voucher is created with Dr Cash ₹5000, Cr Office Exp ₹5000
    And both vouchers remain in audit trail
```

---

## 5. Implementation

### 5.1 New Files
| File | Purpose |
|------|---------|
| `JournalService.java` | Double-entry validation + posting |
| `JournalController.java` | REST endpoints |
| `VoucherTypeService.java` | Voucher type management |
| `VoucherTypeController.java` | Voucher type REST endpoints |
| `JournalTransaction.java` | JPA entity |
| `JournalEntry.java` | JPA entry line entity |
| `UnbalancedTransactionException.java` | Custom exception |

### 5.2 Migration
- `V3__ledger_and_journal.sql` — journal_transactions, journal_entries, voucher_types tables + DB trigger

---

## 7. Traceability

| Artifact | Link |
|---------|------|
| BRD | [BR-004](../../business/BRD.md#br-004-voucher-posting) |
| FRD | [FR-002](../../business/FRD.md#fr-002-journal-entry--voucher-posting) |
| TRD | [TR-005](../../business/TRD.md#6-tr-005-double-entry-validation) |
| RTM | [RTM Row REQ-004](../RTM.md) |
| User Stories | [US-002, US-005](../../business/user-stories.md) |
| Agent Owner | [@LedgerExpert](../../../.github/agents/ledger-expert.md) |
| Migration | `V3__ledger_and_journal.sql` |
