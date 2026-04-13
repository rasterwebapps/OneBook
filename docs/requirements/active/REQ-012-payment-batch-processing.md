# REQ-012: Payment Batch Processing

**Status:** COMPLETED  
**Priority:** HIGH  
**Owner:** @LedgerExpert  
**Milestone:** M11  
**Created:** 2026-03-19  
**Last Updated:** 2026-04-13  
**Linked BRD:** [BR-012](../../business/BRD.md#br-012-payment-batch-processing)  
**Linked FRD:** [FR-016](../../business/FRD.md#req-012-payment-batch-processing)  
**Linked TRD:** [TR-008](../../business/TRD.md#tr-008-payment-processing-pipeline)

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
Once AP items are in the register, there is no mechanism to select them, calculate net payable, and submit for approval. Accountants must manually compute net amounts (netting purchases against returns and credit notes) and submit for Finance Manager sign-off — a process that is both error-prone and lacks an auditable approval trail.

### 1.2 Business Value
Payment batch processing provides a structured workflow to group outstanding AP items for a single vendor, automatically compute the net payable amount (netting off returns and credit notes), and route the batch through an approval workflow before disbursement. This ensures accuracy, enforces the maker-checker principle for payments, and provides a complete audit trail.

### 1.3 Stakeholders
| Stakeholder | Need |
|-------------|------|
| Accountant | Select register items and create a batch with auto-calculated net payable |
| Finance Manager | Approve or reject batches with a reason before funds are disbursed |
| CFO | Audit trail of all payment approvals and rejections |

### 1.4 Business Rules
- **BR-012.1**: User selects one or more items (PURCHASE, PURCHASE_RETURN, CREDIT_NOTE) for the same vendor
- **BR-012.2**: Net payable = Σ(PURCHASE) − Σ(PURCHASE_RETURN) − Σ(CREDIT_NOTE)
- **BR-012.3**: Batch number generated as `PB-YYYY-MM-NNN` (sequential per tenant per month)
- **BR-012.4**: Batch statuses: `PENDING_APPROVAL` → `APPROVED` / `REJECTED` → `PAYMENT_GENERATED` → `COMPLETED`
- **BR-012.5**: On approval, post PAYMENT journal entry: Dr Accounts Payable (vendor), Cr Bank Account (net payable)
- **BR-012.6**: On rejection, items released back to `AVAILABLE_FOR_PROCESSING`

---

## 2. Functional Specification

### 2.1 Batch Status State Machine
```
PENDING_APPROVAL (Accountant creates batch)
    ↓ Finance Manager approves     ↓ Finance Manager rejects
APPROVED                          REJECTED (items → AVAILABLE_FOR_PROCESSING)
    ↓ generate file
PAYMENT_GENERATED
    ↓ confirmation
COMPLETED
```

### 2.2 API Endpoints
```
POST  /api/payment-batches                         — Create a new payment batch
GET   /api/payment-batches                         — List batches (with ?status= filter)
GET   /api/payment-batches/{batchId}               — Get batch detail with all items
POST  /api/payment-batches/{batchId}/approve       — Approve or reject a batch
```

### 2.3 Create Batch Request
```json
{
  "tenantId": "T1",
  "vendorAccountId": 100,
  "bankAccountId": 200,
  "registerEntryIds": [101, 102, 103],
  "notes": "March batch for vendor ABC"
}
```

### 2.4 Batch Response
```json
{
  "batchId": 1,
  "batchNumber": "PB-2026-03-001",
  "vendorAccountId": 100,
  "vendorName": "ABC Supplies",
  "status": "PENDING_APPROVAL",
  "netPayable": 45000.00,
  "currency": "INR",
  "items": [...]
}
```

### 2.5 Approval Request
```json
{
  "tenantId": "T1",
  "action": "APPROVE",
  "reason": "Reviewed and approved"
}
```

---

## 3. Technical Specification

### 3.1 Data Model
```sql
-- V11__payment_processing.sql
CREATE TABLE payment_batches (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       UUID          NOT NULL,
    batch_number    VARCHAR(20)   NOT NULL,
    vendor_account_id BIGINT      NOT NULL,
    bank_account_id BIGINT        NOT NULL,
    status          VARCHAR(30)   NOT NULL DEFAULT 'PENDING_APPROVAL',
    net_payable     NUMERIC(19,4) NOT NULL,
    currency        VARCHAR(10)   NOT NULL DEFAULT 'INR',
    notes           TEXT,
    created_by      VARCHAR(255)  NOT NULL,
    approved_by     VARCHAR(255),
    rejection_reason TEXT,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_payment_batches_tenant_number UNIQUE (tenant_id, batch_number)
);

CREATE TABLE payment_batch_items (
    id                       BIGSERIAL PRIMARY KEY,
    tenant_id                UUID          NOT NULL,
    batch_id                 BIGINT        NOT NULL REFERENCES payment_batches(id),
    payment_register_entry_id BIGINT       NOT NULL REFERENCES payment_register(id),
    transaction_type         VARCHAR(30)   NOT NULL,
    amount                   NUMERIC(19,4) NOT NULL,
    created_at               TIMESTAMP     NOT NULL DEFAULT NOW()
);

ALTER TABLE payment_batches ENABLE ROW LEVEL SECURITY;
CREATE POLICY payment_batches_tenant_isolation ON payment_batches
    USING (tenant_id = current_tenant_id());

ALTER TABLE payment_batch_items ENABLE ROW LEVEL SECURITY;
CREATE POLICY payment_batch_items_tenant_isolation ON payment_batch_items
    USING (tenant_id = current_tenant_id());
```

### 3.2 Journal Posting on Approval
When a batch is approved, a PAYMENT journal entry is posted automatically:
```
Dr  Accounts Payable (vendorAccountId)   netPayable
Cr  Bank Account (bankAccountId)         netPayable
```
This integrates with the existing `JournalService` (REQ-004).

### 3.3 Implementation Files
| File | Package | Description |
|------|---------|-------------|
| `PaymentBatch.java` | `ledger/payment/model/` | JPA entity for payment_batches |
| `PaymentBatchItem.java` | `ledger/payment/model/` | JPA entity for payment_batch_items |
| `PaymentBatchStatus.java` | `ledger/payment/model/` | Enum: PENDING_APPROVAL, APPROVED, REJECTED, PAYMENT_GENERATED, COMPLETED |
| `PaymentBatchRepository.java` | `ledger/payment/repository/` | JPA repository with status filter queries |
| `PaymentBatchItemRepository.java` | `ledger/payment/repository/` | JPA repository for batch items |
| `CreateBatchRequest.java` | `ledger/payment/dto/` | Request DTO for batch creation |
| `PaymentBatchResponse.java` | `ledger/payment/dto/` | Response DTO |
| `BatchApprovalRequest.java` | `ledger/payment/dto/` | Approve/reject request DTO |
| `PaymentBatchService.java` | `ledger/payment/service/` | Business logic: batch creation, net payable calculation, journal posting |
| `PaymentBatchController.java` | `ledger/payment/controller/` | REST endpoints |

---

## 4. Acceptance Criteria

```gherkin
Feature: Payment Batch Processing

  Scenario: Create payment batch with mixed transaction types
    Given vendor 100 has 3 AVAILABLE_FOR_PROCESSING entries
      | type              | amount  |
      | PURCHASE          | 50000   |
      | PURCHASE_RETURN   | 5000    |
      | CREDIT_NOTE       | 2000    |
    When accountant creates a batch with all 3 entries
    Then batch is created with PENDING_APPROVAL status
    And netPayable = 50000 - 5000 - 2000 = 43000
    And all 3 entries move to IN_BATCH status

  Scenario: Entries from different vendors are rejected
    Given entries from vendors 100 and 200
    When accountant tries to create a batch mixing both vendors
    Then a 400 error is returned
    And error message indicates "all entries must belong to the same vendor"

  Scenario: Approve batch posts journal entry
    Given a batch in PENDING_APPROVAL status with netPayable = 43000
    When finance manager approves the batch
    Then batch moves to APPROVED status
    And a PAYMENT journal entry is posted: Dr AP (vendor 100), Cr Bank

  Scenario: Reject batch releases entries
    Given a batch in PENDING_APPROVAL status
    When finance manager rejects the batch with reason "amount mismatch"
    Then batch moves to REJECTED status
    And all register entries return to AVAILABLE_FOR_PROCESSING
    And rejection reason "amount mismatch" is stored on the batch

  Scenario: List batches filtered by status
    Given 3 batches exist: 2 PENDING_APPROVAL, 1 APPROVED
    When GET /api/payment-batches?tenantId=T1&status=PENDING_APPROVAL
    Then only the 2 PENDING_APPROVAL batches are returned
```

---

## 7. Traceability

| Artifact | Link |
|---------|------|
| BRD | [BR-012](../../business/BRD.md#br-012-payment-batch-processing) |
| FRD | [FR-016](../../business/FRD.md#req-012-payment-batch-processing) |
| TRD | [TR-008](../../business/TRD.md#tr-008-payment-processing-pipeline) |
| RTM | [RTM Row REQ-012](../RTM.md) |
| User Stories | [US-022, US-023](../../business/user-stories.md) |
| Agent Owner | [@backend](../../../.github/agents/backend.agent.md) |
| Migration | `V11__payment_processing.sql` |
