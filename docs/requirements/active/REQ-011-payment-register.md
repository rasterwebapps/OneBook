# REQ-011: Payment Register

**Status:** IN_PROGRESS  
**Priority:** HIGH  
**Owner:** @LedgerExpert  
**Milestone:** M11  
**Created:** 2026-03-19  
**Last Updated:** 2026-03-19  
**Linked BRD:** [BR-011](../../business/BRD.md#br-011-payment-register)  
**Linked FRD:** [FR-015](../../business/FRD.md#req-011-payment-register)  
**Linked TRD:** [TR-008](../../business/TRD.md#tr-008-payment-processing-pipeline)

---

## Quality Gate Checklist

- [x] Business Context documented
- [x] Functional Specification documented
- [x] Technical Specification documented
- [x] Acceptance Criteria (Gherkin) defined
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
Payment requests arrive from external apps (REQ-003) and OneBook vouchers (REQ-004). There is no unified view of outstanding payables by vendor. Accountants must manually track which invoices are due, from which vendors, and in what amounts — a fragmented, error-prone process that delays vendor payments and harms vendor relationships.

### 1.2 Business Value
Accountants need a single, real-time view of all outstanding Accounts Payable items grouped by vendor sorted by due date for payment planning. This eliminates the need for manual tracking, reduces missed payments, and provides accurate cash flow visibility for the Finance Manager and CFO.

### 1.3 Stakeholders
| Stakeholder | Need |
|-------------|------|
| Accountant | Visibility into outstanding AP items per vendor |
| Finance Manager | Oversight of total payables and upcoming due dates |
| CFO | Cash flow planning based on net outstanding payables |

### 1.4 Business Rules
- **BR-011.1**: All PURCHASE, PURCHASE_RETURN, and CREDIT_NOTE transactions create payment register entries
- **BR-011.2**: Items are grouped by vendor (`vendorAccountId`) and sorted by due date ascending
- **BR-011.3**: Register item statuses: `AVAILABLE_FOR_PROCESSING` → `IN_BATCH` → `APPROVED` → `PAYMENT_GENERATED` → `PAID`
- **BR-011.4**: On batch rejection, items return to `AVAILABLE_FOR_PROCESSING`
- **BR-011.5**: The payment register is a pre-ledger workflow view; it does NOT create journal entries

---

## 2. Functional Specification

### 2.1 Payment Register View
The register displays the following fields per entry:
| Field | Description |
|-------|-------------|
| Vendor Name | Name of the vendor (resolved from `vendorAccountId`) |
| Invoice Number | Original invoice/voucher reference |
| Invoice Date | Date of the original transaction |
| Due Date | Payment due date (ascending sort key) |
| Transaction Type | PURCHASE, PURCHASE_RETURN, or CREDIT_NOTE |
| Amount | Transaction amount in original currency |
| Currency | Transaction currency |
| Status | Current register item status |

### 2.2 Vendor Grouping
Entries are grouped by vendor with the following totals per vendor group:
- **Total Purchases**: Σ(PURCHASE amounts)
- **Total Returns**: Σ(PURCHASE_RETURN amounts)
- **Total Credit Notes**: Σ(CREDIT_NOTE amounts)
- **Net Outstanding**: Total Purchases − Total Returns − Total Credit Notes

### 2.3 API Endpoints
```
GET  /api/payment-register                          — All vendors grouped, sorted by due date
GET  /api/payment-register/vendor/{vendorAccountId} — Specific vendor entries
```

Both endpoints require `tenantId` as a query parameter for multi-tenant isolation.

---

## 3. Technical Specification

### 3.1 Data Model
```sql
-- V11__payment_processing.sql
CREATE TABLE payment_register (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       UUID          NOT NULL,
    vendor_account_id BIGINT      NOT NULL,
    invoice_number  VARCHAR(100)  NOT NULL,
    invoice_date    DATE          NOT NULL,
    due_date        DATE          NOT NULL,
    transaction_type VARCHAR(30)  NOT NULL,  -- PURCHASE, PURCHASE_RETURN, CREDIT_NOTE
    amount          NUMERIC(19,4) NOT NULL,
    currency        VARCHAR(10)   NOT NULL DEFAULT 'INR',
    status          VARCHAR(40)   NOT NULL DEFAULT 'AVAILABLE_FOR_PROCESSING',
    source_transaction_id BIGINT,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

ALTER TABLE payment_register ENABLE ROW LEVEL SECURITY;
CREATE POLICY payment_register_tenant_isolation ON payment_register
    USING (tenant_id = current_tenant_id());
```

### 3.2 Indexes
```sql
CREATE INDEX idx_payment_register_tenant_status
    ON payment_register (tenant_id, status);
CREATE INDEX idx_payment_register_tenant_vendor_status
    ON payment_register (tenant_id, vendor_account_id, status);
CREATE INDEX idx_payment_register_tenant_due_date
    ON payment_register (tenant_id, due_date);
```

### 3.3 Implementation Files
| File | Package | Description |
|------|---------|-------------|
| `PaymentRegisterEntry.java` | `ledger/payment/model/` | JPA entity for the register table |
| `PaymentRegisterStatus.java` | `ledger/payment/model/` | Enum: AVAILABLE_FOR_PROCESSING, IN_BATCH, APPROVED, PAYMENT_GENERATED, PAID |
| `PaymentRegisterRepository.java` | `ledger/payment/repository/` | JPA repository with vendor grouping queries |
| `PaymentRegisterService.java` | `ledger/payment/service/` | Business logic for register view and status transitions |
| `PaymentRegisterController.java` | `ledger/payment/controller/` | REST endpoints |
| `V11__payment_processing.sql` | `db/migration/` | Schema migration |

---

## 4. Acceptance Criteria

```gherkin
Feature: Payment Register

  Scenario: View payment register grouped by vendor
    Given I am an accountant with outstanding payables
    When I call GET /api/payment-register?tenantId=T1
    Then I receive a list of vendor groups sorted by due date
    And each group shows totalPurchases, totalReturns, totalCreditNotes, netOutstanding

  Scenario: Filter by specific vendor
    Given vendor account 100 has 3 outstanding invoices
    When I call GET /api/payment-register/vendor/100?tenantId=T1
    Then I receive only the entries for vendor 100

  Scenario: Only AVAILABLE_FOR_PROCESSING items appear in register view
    Given vendor 100 has 2 AVAILABLE_FOR_PROCESSING and 1 IN_BATCH entry
    When I call GET /api/payment-register/vendor/100?tenantId=T1
    Then I receive only the 2 AVAILABLE_FOR_PROCESSING entries

  Scenario: Items from another tenant are not visible
    Given tenant T2 has payable entries for vendor 100
    When I call GET /api/payment-register/vendor/100?tenantId=T1
    Then no T2 entries are returned
```

---

## 7. Traceability

| Artifact | Link |
|---------|------|
| BRD | [BR-011](../../business/BRD.md#br-011-payment-register) |
| FRD | [FR-015](../../business/FRD.md#req-011-payment-register) |
| TRD | [TR-008](../../business/TRD.md#tr-008-payment-processing-pipeline) |
| RTM | [RTM Row REQ-011](../RTM.md) |
| User Stories | [US-021](../../business/user-stories.md) |
| Agent Owner | [@backend](../../../.github/agents/backend.agent.md) |
| Migration | `V11__payment_processing.sql` |
