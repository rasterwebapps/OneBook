# REQ-014: Employee Advances, Expense Settlement, Advance Receipt & Payment Advice

**Status:** COMPLETED  
**Priority:** HIGH  
**Owner:** @LedgerExpert  
**Milestone:** M12  
**Created:** 2026-04-04  
**Last Updated:** 2026-04-13  
**Linked BRD:** [BR-014](../../business/BRD.md#br-014-employee-advances-and-settlement)  
**Linked FRD:** [FR-018](../../business/FRD.md#fr-018-employee-advances-and-settlement)  
**Linked TRD:** [TR-009](../../business/TRD.md#tr-009-employee-advance-settlement-pipeline)

---

## Quality Gate Checklist

- [x] Business Context documented
- [x] Functional Specification documented
- [x] Technical Specification documented
- [x] Acceptance Criteria (Gherkin) defined
- [x] Implementation complete (backend)
- [x] Unit tests written and passing (35 backend tests)
- [x] Frontend components implemented (4 components, 27 tests)
- [x] Integration tests (covered by unit tests)
- [x] BRD updated
- [x] FRD updated
- [x] TRD updated
- [x] RTM updated
- [x] Agent ownership updated

---

## 1. Business Context

### 1.1 Problem Statement

Organizations frequently provide employees with cash or bank advances to cover business expenses (travel, site visits, purchases, petty cash). Without a structured control cycle, these advances remain unreconciled on the books, cannot be audited efficiently, and expose the organization to fraud risk. Equally, employees who spend from personal funds on behalf of the organization have no reliable reimbursement workflow, leading to delayed repayments and disputes.

OneBook requires a fully integrated Advance → Expense Settlement → Receipt / Payment Advice cycle that: enforces configurable per-employee advance limits; requires tiered multi-level approvals based on amount; restricts visibility of departmental data to the mapped HOD; auto-generates Payment Advice when valid expenses exceed the outstanding advance; and maintains an immutable, hash-chained audit trail for every state transition.

### 1.2 Business Value

- **Fraud prevention**: advance limits and multi-level approvals prevent unauthorized disbursements.
- **Working-capital accuracy**: outstanding advances are immediately visible as assets; expense posting reduces them in real time.
- **HOD accountability**: expense vouchers post only after HOD approval, ensuring departmental sign-off before any balance impact.
- **Reimbursement speed**: automated Payment Advice creation on overspend eliminates manual calculation and delayed repayments.
- **Audit readiness**: complete lifecycle audit trail satisfies internal and external auditors.

### 1.3 Stakeholders

| Role | Interest |
|------|---------|
| Employee | Request advances, submit expense vouchers, receive reimbursements |
| HOD (Head of Department) | Approve/reject advances ≤ ₹10,000 and all expense vouchers for mapped departments; department-scoped visibility |
| CEO | Co-approve advances in range ₹10,001–₹20,000 (after HOD approval) |
| MD (Managing Director) | Final approval for advances > ₹20,000 (after HOD + CEO approval) |
| Finance / Accounts Team | View and post settled vouchers; process actual payment of Payment Advice |
| Elevated-Rights User | Override advance limit with mandatory reason; subject to override audit report |
| Internal Auditor | Review override report, aging report, and reimbursement payable report |

### 1.4 Business Rules

- **BR-014.1 — Advance limit per employee**: Each employee has a configurable maximum outstanding advance limit (default ₹10,000). A new advance request is blocked when `currentOutstandingAdvance + requestedAmount > limit`, unless the requester holds override rights.
- **BR-014.2 — Continuous settlement (Option A)**: Employees may submit expense vouchers at any time. Each approved expense voucher reduces `OutstandingAdvance` immediately on posting. As the outstanding balance decreases below the limit, additional advances become eligible.
- **BR-014.3 — Override by elevated-rights user**: A user with the `ADVANCE_LIMIT_OVERRIDE` permission may create or approve an advance irrespective of the employee's outstanding amount. The override requires a mandatory reason string. Every override is recorded and surfaced in the Overrides Report.
- **BR-014.4 — HOD expense-voucher accountability**: Expense vouchers submitted by employees do **not** affect ledger balances on submission. They post (reduce outstanding advance / create Payment Advice) only after the HOD of the employee's department explicitly approves. HOD approval is the control point; HOD is accountable to management for any approved expense.
- **BR-014.5 — Department-based visibility**: An HOD may only view advance requests, expense vouchers, advance receipts, and payment advices belonging to employees of departments mapped to that HOD. Advances/expenses for other departments are not visible.
- **BR-014.6 — Advance approval tiers** (amount-based, sequential):
  - ≤ ₹10,000: HOD approval only.
  - ₹10,001–₹20,000: HOD approval first, then CEO approval.
  - > ₹20,000: HOD approval first, then CEO approval, then MD approval.
  - The next approver in the chain is notified only after the preceding approver approves (sequential, not parallel).
- **BR-014.7 — Expense settlement split on approval**:
  - On HOD approval of an expense voucher, compute `OutstandingAdvance` for the employee.
  - If `expenseAmount ≤ OutstandingAdvance`: post full expense against outstanding advance.
    - `Dr Expense Account` / `Cr Employee Advance (Asset)` for the full amount.
  - If `expenseAmount > OutstandingAdvance`:
    - Reduce outstanding advance to zero: `Dr Expense` / `Cr Employee Advance` for `OutstandingAdvance`.
    - Create a Payment Advice for excess: `Dr Expense` / `Cr Employee Reimbursement Payable (Liability)` for `(expenseAmount − OutstandingAdvance)`.
  - If `OutstandingAdvance = 0` (employee spent with no prior advance): full expense amount becomes a Payment Advice payable.
- **BR-014.8 — No-advance expense (self-funded)**: An employee may create an expense voucher without any linked advance. On HOD approval, the entire expense amount is treated as reimbursable and a Payment Advice is created for the full amount.
- **BR-014.9 — Advance receipt validation**: When an employee returns unspent cash or bank funds, an Advance Receipt voucher is created. The receipt amount must not exceed the employee's current `OutstandingAdvance`. If the system detects the receipt would exceed outstanding, it is rejected with an error. An elevated-rights user may override this rule with a mandatory reason.
- **BR-014.10 — Payment of Payment Advice**: The Payment Advice represents a liability (`Employee Reimbursement Payable`). It is paid by the Finance team via a Payment voucher: `Dr Employee Reimbursement Payable` / `Cr Cash/Bank`.
- **BR-014.11 — Immutability and reversal**: Once an advance, expense voucher, receipt, or payment advice is posted (status `POSTED`), it cannot be edited or deleted. Corrections require a reversal entry consistent with the existing voucher-posting immutability rules (see REQ-004). Reversal must be approved through the same workflow as the original.
- **BR-014.12 — Full audit trail**: Every state transition (create, submit, approve, reject, pay, reverse) must be logged with actor ID, timestamp, old state, new state, and optional reason/comment. Audit entries are immutable and hash-chained (consistent with REQ-002 and REQ-010).

---

## 2. Functional Specification

### 2.1 Feature Description

The Employee Advance & Settlement module introduces four new voucher sub-types under the existing voucher framework (REQ-004):

1. **EMPLOYEE_ADVANCE** — Records a cash/bank disbursement to an employee, creating or increasing an outstanding advance balance. Subject to tiered approval workflow.
2. **EXPENSE_VOUCHER** — Employee records business expenses against (or without) an outstanding advance. Requires HOD approval before posting. On posting, splits between advance reduction and reimbursement payable as per BR-014.7.
3. **ADVANCE_RECEIPT** — Employee returns unspent funds; reduces outstanding advance via a cash/bank receipt entry.
4. **PAYMENT_ADVICE** — System-generated liability entry for reimbursable expenses. Paid by Finance via a standard Payment voucher.

### 2.2 User Flows

**Flow 1: Employee requests advance (amount ≤ ₹10,000)**
```
Step 1: Employee creates EMPLOYEE_ADVANCE request (purpose, amount, date, cost centre).
Step 2: System validates: currentOutstanding + requestedAmount ≤ employeeLimit (BR-014.1).
        If validation fails → 400 error "advance limit exceeded".
        If requester has ADVANCE_LIMIT_OVERRIDE → prompt for override reason; proceed.
Step 3: Advance enters status PENDING_HOD_APPROVAL.
        HOD for employee's department is notified.
Step 4: HOD approves → status moves to APPROVED; accounting entry posted:
        Dr Employee Advance (Asset) / Cr Cash/Bank.
        OutstandingAdvance for employee increases.
   OR:  HOD rejects with reason → status REJECTED; employee notified.
```

**Flow 2: Employee requests advance (₹10,001–₹20,000)**
```
Step 1–2: Same as Flow 1 (limit check, PENDING_HOD_APPROVAL).
Step 3: HOD approves → status PENDING_CEO_APPROVAL; CEO notified.
Step 4: CEO approves → status APPROVED; entry posted.
   OR:  CEO rejects → REJECTED; employee notified.
   OR:  HOD rejects → REJECTED; employee notified.
```

**Flow 3: Employee requests advance (> ₹20,000)**
```
Step 1–2: Same limit check.
Step 3: HOD approves → PENDING_CEO_APPROVAL.
Step 4: CEO approves → PENDING_MD_APPROVAL; MD notified.
Step 5: MD approves → APPROVED; entry posted.
   OR:  Any approver rejects → REJECTED; employee notified.
```

**Flow 4: Employee submits expense voucher (against existing advance)**
```
Step 1: Employee creates EXPENSE_VOUCHER (description, amount, expense type, date,
        supporting document reference). Optionally links to one or more advance IDs.
Step 2: Voucher enters PENDING_HOD_APPROVAL.
Step 3: HOD reviews and approves → system executes BR-014.7 settlement logic:
        a) Fetch employee OutstandingAdvance.
        b) If expenseAmount ≤ outstanding:
             Post: Dr Expense / Cr Employee Advance for full amount.
             Reduce OutstandingAdvance by expenseAmount.
        c) If expenseAmount > outstanding:
             Post: Dr Expense / Cr Employee Advance for outstanding portion.
             Create PAYMENT_ADVICE for excess (Dr Expense / Cr Reimbursement Payable).
             Set OutstandingAdvance to 0.
   OR:  HOD rejects → REJECTED; employee notified with reason.
```

**Flow 5: Employee submits self-funded expense (no advance)**
```
Step 1: Employee creates EXPENSE_VOUCHER with no linked advance.
Step 2: Voucher enters PENDING_HOD_APPROVAL.
Step 3: HOD approves → full expense amount creates PAYMENT_ADVICE.
        Dr Expense / Cr Employee Reimbursement Payable.
Step 4: Finance pays via Payment voucher:
        Dr Employee Reimbursement Payable / Cr Cash/Bank.
```

**Flow 6: Employee returns unspent cash (Advance Receipt)**
```
Step 1: Employee creates ADVANCE_RECEIPT (amount, date, mode: Cash/Bank).
Step 2: System validates: receiptAmount ≤ OutstandingAdvance (BR-014.9).
        If fails → 400 error.
        If user has ADVANCE_LIMIT_OVERRIDE → prompt override reason; proceed.
Step 3: Post: Dr Cash/Bank / Cr Employee Advance.
        Reduce OutstandingAdvance.
```

**Flow 7: Finance pays a Payment Advice**
```
Step 1: Finance team views pending Payment Advice list.
Step 2: Creates Payment voucher linked to Payment Advice.
Step 3: Post: Dr Employee Reimbursement Payable / Cr Cash/Bank.
        Payment Advice status moves to PAID.
```

### 2.3 Inputs

| Input | Type | Required | Validation |
|-------|------|----------|-----------|
| `employeeId` | UUID | Yes | Must be a valid employee/party ledger in tenant |
| `departmentId` | UUID | Yes | Must be a department mapped in org hierarchy |
| `amount` | BigDecimal (NUMERIC 19,4) | Yes | Must be > 0; never float/double |
| `purpose` | String (500 chars) | Yes | Free text description |
| `voucherType` | Enum | Yes | EMPLOYEE_ADVANCE, EXPENSE_VOUCHER, ADVANCE_RECEIPT, PAYMENT_ADVICE |
| `expenseType` | String / Enum | Conditional | Required for EXPENSE_VOUCHER (Travel, Fuel, Meals, Accommodation, Misc, etc.) |
| `linkedAdvanceIds` | UUID[] | No | For EXPENSE_VOUCHER; references prior approved advances |
| `supportingDocumentRef` | String | No | External document/file reference for expense claims |
| `overrideReason` | String (1000 chars) | Conditional | Required when ADVANCE_LIMIT_OVERRIDE permission is exercised |
| `approvalComment` | String (500 chars) | No | Comment at each approval step |
| `paymentMode` | Enum | Conditional | Required for ADVANCE_RECEIPT and Payment of PAYMENT_ADVICE: CASH, BANK, UPI |

### 2.4 Outputs

| Output | Type | Description |
|--------|------|-------------|
| `voucherId` | UUID | Unique identifier for the created voucher |
| `status` | Enum | Current workflow status (see §2.5) |
| `outstandingAdvance` | BigDecimal | Employee's current outstanding advance balance |
| `paymentAdviceId` | UUID | Created only when overspend triggers reimbursement |
| `approvalHistory` | List | Ordered list of approval events with actor, timestamp, action, comment |
| `journalEntryIds` | UUID[] | Posted journal entry IDs (available only after posting) |

### 2.5 Voucher Status State Machine

```
EMPLOYEE_ADVANCE:
  DRAFT
    → PENDING_HOD_APPROVAL     (on submit)
    → PENDING_CEO_APPROVAL     (HOD approves; amount > ₹10,000)
    → PENDING_MD_APPROVAL      (CEO approves; amount > ₹20,000)
    → APPROVED / POSTED        (final approver approves)
    → REJECTED                 (any approver rejects)

EXPENSE_VOUCHER:
  DRAFT
    → PENDING_HOD_APPROVAL     (on submit)
    → POSTED                   (HOD approves; triggers settlement logic)
    → REJECTED                 (HOD rejects)

ADVANCE_RECEIPT:
  (No workflow; auto-posts on creation if user is Finance/Accounts)
  DRAFT → POSTED

PAYMENT_ADVICE:
  (System-generated on expense posting)
  PENDING_PAYMENT → PAID       (Finance posts payment voucher)
```

### 2.6 Validation Rules

- **VR-014.1**: `amount` must be a positive `BigDecimal`; zero or negative amounts are rejected.
- **VR-014.2**: For EMPLOYEE_ADVANCE, `currentOutstandingAdvance + amount` must not exceed `employeeAdvanceLimit` unless `ADVANCE_LIMIT_OVERRIDE` permission is present.
- **VR-014.3**: HOD can only approve vouchers where `voucher.departmentId ∈ hod.mappedDepartments`.
- **VR-014.4**: An approver cannot approve their own voucher (consistent with REQ-010 maker-checker rule).
- **VR-014.5**: ADVANCE_RECEIPT amount must not exceed `outstandingAdvance` unless override.
- **VR-014.6**: EXPENSE_VOUCHER amounts and linked advance IDs must belong to the same tenant.
- **VR-014.7**: Once status is `POSTED`, the voucher record is immutable; no field edits are allowed.
- **VR-014.8**: `overrideReason` is mandatory and must be non-empty when any override is exercised.

### 2.7 API Endpoints

```
POST   /api/advances                                  — Create EMPLOYEE_ADVANCE request
GET    /api/advances/{id}                             — Get advance detail (tenant-scoped)
GET    /api/advances?employeeId=&status=              — List advances (dept-filtered for HOD)

POST   /api/advances/{id}/approve                     — HOD / CEO / MD approve
POST   /api/advances/{id}/reject                      — HOD / CEO / MD reject (requires reason)

POST   /api/expense-vouchers                          — Submit EXPENSE_VOUCHER
GET    /api/expense-vouchers/{id}                     — Get expense voucher detail
GET    /api/expense-vouchers?employeeId=&status=      — List (dept-filtered for HOD)
POST   /api/expense-vouchers/{id}/approve             — HOD approves (triggers settlement)
POST   /api/expense-vouchers/{id}/reject              — HOD rejects

POST   /api/advance-receipts                          — Record unspent cash/bank return
GET    /api/advance-receipts/{id}                     — Get receipt detail

GET    /api/payment-advices?employeeId=&status=       — List Payment Advices
POST   /api/payment-advices/{id}/pay                  — Finance records payment

GET    /api/advances/reports/outstanding-aging        — Outstanding advances + aging
GET    /api/advances/reports/pending-approvals        — Pending approval queue
GET    /api/advances/reports/reimbursements-payable   — Unpaid Payment Advices
GET    /api/advances/reports/overrides                — Override audit report
```

### 2.8 UI Screens

- **Employee Advance Request** (`/advances/new`) — Employee creates advance request form.
- **My Advances** (`/advances/my`) — Employee views their own advances and status.
- **HOD Approval Queue** (`/advances/approvals`) — HOD views pending items for their departments.
- **Expense Voucher Entry** (`/expense-vouchers/new`) — Employee submits expense with document upload.
- **My Expenses** (`/expense-vouchers/my`) — Employee views own expense vouchers and status.
- **Advance Receipt Entry** (`/advance-receipts/new`) — Employee records returned funds.
- **Payment Advice List** (`/payment-advices`) — Finance views and processes reimbursements.
- **Reports** (`/advances/reports/*`) — Outstanding Aging, Pending Approvals, Reimbursements Payable, Overrides.

---

## 3. Technical Specification

### 3.1 Architecture Decisions

- The four new voucher sub-types (`EMPLOYEE_ADVANCE`, `EXPENSE_VOUCHER`, `ADVANCE_RECEIPT`, `PAYMENT_ADVICE`) extend the existing voucher framework from REQ-004. They share the same journal entry infrastructure but carry additional domain-specific metadata.
- Approval orchestration reuses and extends the `audit_workflows` state machine from REQ-010, adding support for a three-level sequential chain (HOD → CEO → MD) driven by `APPROVAL_TIER` configuration.
- `OutstandingAdvance` is maintained as a derived, cached value in the `employee_advance_balance` table (updated on every posting), not computed on-the-fly, to support fast limit checks at O(1) without full ledger aggregation.
- Department-based visibility is enforced at the service layer using the `DepartmentAccessPolicy` (consistent with REQ-006 cost-center access patterns). All repository queries include a `departmentId IN (hod.mappedDepartments)` predicate for HOD-role users.
- All monetary amounts use `BigDecimal` / `NUMERIC(19,4)`. Never `double` or `float`.
- Sensitive fields (e.g., `overrideReason`, `approvalComment`) are encrypted at rest using AES-256-GCM (consistent with REQ-002) where classified as PII or confidential.

### 3.2 Data Model

```sql
-- V15__employee_advances_settlement.sql

-- 1. Configurable per-employee advance limit
CREATE TABLE employee_advance_config (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    employee_id     UUID NOT NULL,
    advance_limit   NUMERIC(19,4) NOT NULL DEFAULT 10000.00,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, employee_id)
);

ALTER TABLE employee_advance_config ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_eac ON employee_advance_config
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid);

-- 2. Outstanding advance balance cache (updated on every posting)
CREATE TABLE employee_advance_balance (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id            UUID NOT NULL,
    employee_id          UUID NOT NULL,
    outstanding_advance  NUMERIC(19,4) NOT NULL DEFAULT 0.00,
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, employee_id)
);

ALTER TABLE employee_advance_balance ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_eab ON employee_advance_balance
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid);

-- 3. Employee advance voucher header
CREATE TABLE employee_advances (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL,
    employee_id         UUID NOT NULL,
    department_id       UUID NOT NULL,
    amount              NUMERIC(19,4) NOT NULL,
    purpose             TEXT NOT NULL,
    status              VARCHAR(40) NOT NULL DEFAULT 'DRAFT',
    override_flag       BOOLEAN NOT NULL DEFAULT FALSE,
    override_reason     TEXT,
    voucher_date        DATE NOT NULL,
    approved_amount     NUMERIC(19,4),
    journal_entry_id    UUID,
    created_by          VARCHAR(255) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE employee_advances ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_ea ON employee_advances
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid);

CREATE INDEX idx_employee_advances_tenant_employee ON employee_advances (tenant_id, employee_id);
CREATE INDEX idx_employee_advances_tenant_department ON employee_advances (tenant_id, department_id);
CREATE INDEX idx_employee_advances_status ON employee_advances (tenant_id, status);

-- 4. Expense voucher header
CREATE TABLE expense_vouchers (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id             UUID NOT NULL,
    employee_id           UUID NOT NULL,
    department_id         UUID NOT NULL,
    amount                NUMERIC(19,4) NOT NULL,
    expense_type          VARCHAR(100) NOT NULL,
    description           TEXT NOT NULL,
    voucher_date          DATE NOT NULL,
    status                VARCHAR(40) NOT NULL DEFAULT 'DRAFT',
    supporting_doc_ref    TEXT,
    advance_settlement    NUMERIC(19,4),  -- portion settled against advance
    reimbursement_amount  NUMERIC(19,4),  -- portion that became Payment Advice
    payment_advice_id     UUID,           -- FK to payment_advices
    journal_entry_id      UUID,
    created_by            VARCHAR(255) NOT NULL,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE expense_vouchers ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_ev ON expense_vouchers
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid);

CREATE INDEX idx_expense_vouchers_tenant_employee ON expense_vouchers (tenant_id, employee_id);
CREATE INDEX idx_expense_vouchers_tenant_department ON expense_vouchers (tenant_id, department_id);
CREATE INDEX idx_expense_vouchers_status ON expense_vouchers (tenant_id, status);

-- 5. Advance receipt voucher
CREATE TABLE advance_receipts (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        UUID NOT NULL,
    employee_id      UUID NOT NULL,
    department_id    UUID NOT NULL,
    amount           NUMERIC(19,4) NOT NULL,
    payment_mode     VARCHAR(20) NOT NULL,  -- CASH, BANK, UPI
    receipt_date     DATE NOT NULL,
    override_flag    BOOLEAN NOT NULL DEFAULT FALSE,
    override_reason  TEXT,
    journal_entry_id UUID,
    created_by       VARCHAR(255) NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE advance_receipts ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_ar ON advance_receipts
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid);

CREATE INDEX idx_advance_receipts_tenant_employee ON advance_receipts (tenant_id, employee_id);

-- 6. Payment advice (reimbursement payable to employee)
CREATE TABLE payment_advices (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         UUID NOT NULL,
    employee_id       UUID NOT NULL,
    department_id     UUID NOT NULL,
    amount            NUMERIC(19,4) NOT NULL,
    source_voucher_id UUID NOT NULL,     -- expense_voucher that triggered this
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING_PAYMENT',  -- PENDING_PAYMENT, PAID
    payment_voucher_id UUID,             -- Payment voucher when paid
    paid_at           TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE payment_advices ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_pa ON payment_advices
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid);

CREATE INDEX idx_payment_advices_tenant_employee ON payment_advices (tenant_id, employee_id);
CREATE INDEX idx_payment_advices_status ON payment_advices (tenant_id, status);

-- 7. Approval workflow events for advance vouchers
--    Extends audit_workflow_events from REQ-010 (V9 migration).
--    A new entity_type='EMPLOYEE_ADVANCE' is registered; existing tables are reused.
```

### 3.3 Key Algorithms / Logic

```java
// AdvanceSettlementService — core settlement logic on expense-voucher approval (BR-014.7)
public SettlementResult settleExpenseVoucher(UUID expenseVoucherId, String tenantId) {
    ExpenseVoucher voucher = expenseVoucherRepository
        .findByIdAndTenantId(expenseVoucherId, tenantId)
        .orElseThrow();

    BigDecimal outstanding = advanceBalanceRepository
        .findByEmployeeAndTenant(voucher.getEmployeeId(), tenantId)
        .map(EmployeeAdvanceBalance::getOutstandingAdvance)
        .orElse(BigDecimal.ZERO);

    BigDecimal expenseAmount = voucher.getAmount();
    BigDecimal advancePortion = outstanding.min(expenseAmount);
    BigDecimal reimbursePortion = expenseAmount.subtract(advancePortion);

    // Post journal entries
    if (advancePortion.compareTo(BigDecimal.ZERO) > 0) {
        journalService.post(DrExpense(expenseType, advancePortion),
                            CrEmployeeAdvance(employeeId, advancePortion), tenantId);
        advanceBalanceRepository.reduceOutstanding(employeeId, advancePortion, tenantId);
    }
    if (reimbursePortion.compareTo(BigDecimal.ZERO) > 0) {
        journalService.post(DrExpense(expenseType, reimbursePortion),
                            CrReimbursementPayable(employeeId, reimbursePortion), tenantId);
        PaymentAdvice pa = paymentAdviceRepository.save(
            new PaymentAdvice(employeeId, reimbursePortion, expenseVoucherId, tenantId));
        voucher.setPaymentAdviceId(pa.getId());
    }
    voucher.setAdvanceSettlement(advancePortion);
    voucher.setReimbursementAmount(reimbursePortion);
    voucher.setStatus(VoucherStatus.POSTED);
    return new SettlementResult(advancePortion, reimbursePortion);
}

// AdvanceLimitCheckService — validates limit before advance creation (BR-014.1)
public void validateAdvanceLimit(UUID employeeId, BigDecimal requestedAmount, String tenantId,
                                  boolean overrideRequested, String overrideReason) {
    BigDecimal limit = advanceConfigRepository
        .findByEmployeeAndTenant(employeeId, tenantId)
        .map(EmployeeAdvanceConfig::getAdvanceLimit)
        .orElse(DEFAULT_LIMIT);

    BigDecimal outstanding = advanceBalanceRepository
        .findByEmployeeAndTenant(employeeId, tenantId)
        .map(EmployeeAdvanceBalance::getOutstandingAdvance)
        .orElse(BigDecimal.ZERO);

    if (outstanding.add(requestedAmount).compareTo(limit) > 0) {
        if (!overrideRequested) {
            throw new AdvanceLimitExceededException(
                "Advance limit exceeded. Outstanding: " + outstanding + ", Limit: " + limit);
        }
        if (overrideReason == null || overrideReason.isBlank()) {
            throw new IllegalArgumentException("Override reason is mandatory.");
        }
        auditService.logOverride(employeeId, requestedAmount, outstanding, limit,
                                  overrideReason, tenantId);
    }
}
```

### 3.4 Approval Tier Resolution

```java
// ApprovalTierResolver — determines required approval chain from amount (BR-014.6)
public List<ApprovalLevel> resolveApprovalTiers(BigDecimal amount) {
    List<ApprovalLevel> tiers = new ArrayList<>();
    tiers.add(ApprovalLevel.HOD);                                // always required
    if (amount.compareTo(new BigDecimal("10000")) > 0) {
        tiers.add(ApprovalLevel.CEO);
    }
    if (amount.compareTo(new BigDecimal("20000")) > 0) {
        tiers.add(ApprovalLevel.MD);
    }
    return tiers;  // ordered: HOD, CEO, MD
}
```

### 3.5 Performance Considerations

- Expected data volume: ~500 advance/expense vouchers per month per mid-size tenant; ~6,000/year.
- Target response time: ≤ 200 ms P95 for list/detail APIs; ≤ 500 ms P95 for posting (includes journal write).
- Caching strategy: `employee_advance_balance` serves as a pre-computed cache; invalidated on every posting event. Warm cache (Redis) for the HOD approval queue using a short TTL (2 min).
- Index requirements: composite indexes on `(tenant_id, employee_id)`, `(tenant_id, department_id)`, `(tenant_id, status)` on all four new tables.

### 3.6 Security Considerations

- Fields requiring encryption: `override_reason`, `purpose`, `description` (classified as potentially sensitive business data) — encrypted via AES-256-GCM with unique random IV per operation (REQ-002).
- Blind indexes: `employee_id` (for encrypted search lookups, if employee names are encrypted).
- RLS: Enabled on all five new tables; policy uses `current_setting('app.tenant_id', true)`.
- Department visibility enforced at service layer via `DepartmentAccessPolicy`; API layer never returns vouchers outside the caller's authorized department set.
- Audit trail: every state transition triggers an `AuditLogService.log()` event (REQ-002 audit trail).
- Override events are logged separately and included in the Overrides Report; never suppressible.

---

## 4. Acceptance Criteria

```gherkin
Feature: Employee Advance Limit Enforcement

  Scenario: New advance blocked when limit reached
    Given employee E1 has advance limit ₹10,000
    And employee E1 has outstanding advance of ₹10,000
    When employee E1 requests a new advance of ₹1
    Then the response status should be 400
    And the error message should contain "advance limit exceeded"

  Scenario: Advance allowed when outstanding < limit
    Given employee E1 has advance limit ₹10,000
    And employee E1 has outstanding advance of ₹5,000
    When employee E1 requests an advance of ₹5,000
    Then the advance is created in status PENDING_HOD_APPROVAL
    And outstanding advance remains ₹5,000 until approved

  Scenario: Override bypasses limit with mandatory reason
    Given employee E1 has outstanding advance ₹10,000 (limit ₹10,000)
    And the requesting user has ADVANCE_LIMIT_OVERRIDE permission
    When the user creates an advance of ₹3,000 with reason "Emergency site procurement"
    Then the advance is created in PENDING_HOD_APPROVAL
    And an override audit record is created with the reason and actor ID
    And the override appears in the Overrides Report

  Scenario: Override without reason is rejected
    Given a user with ADVANCE_LIMIT_OVERRIDE permission
    When the user creates an advance that exceeds the limit without providing a reason
    Then the response status should be 400
    And the error message should contain "override reason is mandatory"

Feature: Advance Approval Tiering

  Scenario: Amount ≤ ₹10,000 requires only HOD approval
    Given an advance request of ₹8,000 for employee in Department D1
    When HOD of D1 approves the advance
    Then the advance status moves to APPROVED
    And the accounting entry Dr Employee Advance / Cr Cash-Bank is posted

  Scenario: Amount in ₹10,001–₹20,000 requires HOD then CEO
    Given an advance request of ₹15,000
    When HOD approves
    Then status moves to PENDING_CEO_APPROVAL (not APPROVED)
    And CEO is notified
    When CEO approves
    Then status moves to APPROVED and entry is posted

  Scenario: Amount > ₹20,000 requires HOD then CEO then MD
    Given an advance request of ₹25,000
    When HOD approves → status PENDING_CEO_APPROVAL
    When CEO approves → status PENDING_MD_APPROVAL
    When MD approves → status APPROVED and entry posted

  Scenario: CEO cannot approve before HOD
    Given an advance of ₹15,000 in PENDING_HOD_APPROVAL
    When CEO tries to approve directly
    Then the response is 403 Forbidden
    And error indicates "approval out of sequence"

  Scenario: Any approver rejection terminates the chain
    Given an advance of ₹25,000 in PENDING_CEO_APPROVAL (HOD already approved)
    When CEO rejects with reason "insufficient budget"
    Then status moves to REJECTED
    And employee is notified with reason "insufficient budget"
    And MD is not notified

Feature: Department-Based Visibility

  Scenario: HOD can only see advances for mapped departments
    Given HOD "Alice" is mapped to Department D1 only
    And advance A1 belongs to Department D1
    And advance A2 belongs to Department D2
    When Alice calls GET /api/advances
    Then the response contains A1
    And the response does NOT contain A2

  Scenario: HOD cannot approve advance from unmapped department
    Given HOD "Alice" mapped to D1 only
    And advance A2 belongs to D2
    When Alice tries to approve A2
    Then the response is 403 Forbidden

Feature: Expense Voucher Settlement (with advance)

  Scenario: Expense fully within outstanding advance
    Given employee E1 has outstanding advance of ₹8,000
    When E1 submits expense of ₹5,000 and HOD approves
    Then Dr Expense ₹5,000 / Cr Employee Advance ₹5,000 is posted
    And E1 outstanding advance becomes ₹3,000
    And no Payment Advice is created

  Scenario: Expense partially exceeds outstanding advance
    Given employee E1 has outstanding advance of ₹3,000
    When E1 submits expense of ₹5,000 and HOD approves
    Then Dr Expense ₹3,000 / Cr Employee Advance ₹3,000 is posted (clears advance)
    And Dr Expense ₹2,000 / Cr Employee Reimbursement Payable ₹2,000 is posted
    And a Payment Advice of ₹2,000 is created for E1
    And E1 outstanding advance becomes ₹0

  Scenario: Expense with zero outstanding advance (full reimbursement)
    Given employee E1 has outstanding advance of ₹0
    When E1 submits expense of ₹4,000 and HOD approves
    Then Dr Expense ₹4,000 / Cr Employee Reimbursement Payable ₹4,000 is posted
    And a Payment Advice of ₹4,000 is created for E1

Feature: Self-Funded Expense (No Advance)

  Scenario: Employee spends from personal funds without any advance
    Given employee E2 has no outstanding advance
    When E2 creates an EXPENSE_VOUCHER for ₹2,500 (no linked advance)
    And HOD approves
    Then a Payment Advice of ₹2,500 is created for E2
    And Finance sees the Payment Advice in the reimbursements-payable report

Feature: Advance Receipt

  Scenario: Employee returns unspent cash within outstanding balance
    Given employee E1 has outstanding advance of ₹6,000
    When E1 creates ADVANCE_RECEIPT for ₹4,000
    Then Dr Cash ₹4,000 / Cr Employee Advance ₹4,000 is posted
    And E1 outstanding advance becomes ₹2,000

  Scenario: Advance receipt exceeding outstanding is rejected
    Given employee E1 has outstanding advance of ₹2,000
    When E1 creates ADVANCE_RECEIPT for ₹3,000
    Then the response status is 400
    And error message contains "receipt cannot exceed outstanding advance"

Feature: Payment of Payment Advice

  Scenario: Finance pays a pending Payment Advice
    Given Payment Advice PA-001 for employee E1 of ₹2,000 is in PENDING_PAYMENT
    When Finance posts a Payment voucher linked to PA-001
    Then Dr Employee Reimbursement Payable ₹2,000 / Cr Cash-Bank ₹2,000 is posted
    And PA-001 status moves to PAID

Feature: HOD Expense Voucher Accountability

  Scenario: Expense voucher does NOT post on submission — only on HOD approval
    Given employee E1 submits expense voucher EV-001 for ₹3,000
    When EV-001 is in status PENDING_HOD_APPROVAL
    Then E1 outstanding advance is unchanged
    And no journal entry is created for EV-001
    When HOD approves EV-001
    Then the settlement logic executes and journal entries are posted

Feature: Immutability After Posting

  Scenario: Posted voucher cannot be edited
    Given advance A1 is in status POSTED
    When any user attempts to update the amount of A1
    Then the response is 409 Conflict
    And error message contains "posted voucher is immutable"

Feature: Audit Trail

  Scenario: Every state transition is logged
    Given an advance request A1
    When A1 transitions through DRAFT → PENDING_HOD_APPROVAL → APPROVED
    Then GET /api/advances/A1 returns approvalHistory with 2 events
    And each event contains actorId, timestamp, fromStatus, toStatus

Feature: Tenant Isolation

  Scenario: Advance data is isolated between tenants
    Given advance A1 belongs to tenant T1
    When a user from tenant T2 calls GET /api/advances/A1
    Then the response is 404 Not Found

Feature: Reports

  Scenario: Outstanding aging report shows overdue advances
    Given employee E1 has advance A1 posted 45 days ago with outstanding ₹5,000
    When Finance calls GET /api/advances/reports/outstanding-aging
    Then A1 appears in the "31–60 days" aging bucket with amount ₹5,000

  Scenario: Overrides report lists all override events
    Given an override was recorded for employee E1 by user U1 with reason R1
    When an authorized user calls GET /api/advances/reports/overrides
    Then the report contains employee E1, actor U1, reason R1, and timestamp
```

---

## 5. Implementation

### 5.1 New Files to Create

| File | Package | Purpose |
|------|---------|---------|
| `EmployeeAdvance.java` | `com.nexus.onebook.advance.model` | JPA entity for employee advance header |
| `ExpenseVoucher.java` | `com.nexus.onebook.advance.model` | JPA entity for expense voucher |
| `AdvanceReceipt.java` | `com.nexus.onebook.advance.model` | JPA entity for advance receipt |
| `PaymentAdvice.java` | `com.nexus.onebook.advance.model` | JPA entity for payment advice (reimbursement) |
| `EmployeeAdvanceConfig.java` | `com.nexus.onebook.advance.model` | JPA entity for per-employee advance limit config |
| `EmployeeAdvanceBalance.java` | `com.nexus.onebook.advance.model` | JPA entity for outstanding advance cache |
| `EmployeeAdvanceDto.java` | `com.nexus.onebook.advance.dto` | DTO for advance request/response |
| `ExpenseVoucherDto.java` | `com.nexus.onebook.advance.dto` | DTO for expense voucher |
| `AdvanceReceiptDto.java` | `com.nexus.onebook.advance.dto` | DTO for advance receipt |
| `PaymentAdviceDto.java` | `com.nexus.onebook.advance.dto` | DTO for payment advice |
| `AdvanceApprovalRequest.java` | `com.nexus.onebook.advance.dto` | DTO for approve/reject action |
| `SettlementResult.java` | `com.nexus.onebook.advance.dto` | DTO returned by settlement logic |
| `EmployeeAdvanceRepository.java` | `com.nexus.onebook.advance.repository` | Spring Data JPA repository |
| `ExpenseVoucherRepository.java` | `com.nexus.onebook.advance.repository` | Spring Data JPA repository |
| `AdvanceReceiptRepository.java` | `com.nexus.onebook.advance.repository` | Spring Data JPA repository |
| `PaymentAdviceRepository.java` | `com.nexus.onebook.advance.repository` | Spring Data JPA repository |
| `EmployeeAdvanceConfigRepository.java` | `com.nexus.onebook.advance.repository` | Advance limit config per employee |
| `EmployeeAdvanceBalanceRepository.java` | `com.nexus.onebook.advance.repository` | Outstanding advance balance cache |
| `AdvanceLimitCheckService.java` | `com.nexus.onebook.advance.service` | Validates advance limit; handles override logging |
| `ApprovalTierResolver.java` | `com.nexus.onebook.advance.service` | Resolves approval chain from amount |
| `AdvanceSettlementService.java` | `com.nexus.onebook.advance.service` | Core settlement logic on expense approval |
| `EmployeeAdvanceService.java` | `com.nexus.onebook.advance.service` | CRUD + workflow for advances |
| `ExpenseVoucherService.java` | `com.nexus.onebook.advance.service` | CRUD + workflow for expense vouchers |
| `AdvanceReceiptService.java` | `com.nexus.onebook.advance.service` | CRUD + posting for advance receipts |
| `PaymentAdviceService.java` | `com.nexus.onebook.advance.service` | List + mark-paid for payment advices |
| `AdvanceReportService.java` | `com.nexus.onebook.advance.service` | Outstanding aging, pending approvals, reimbursements, overrides |
| `EmployeeAdvanceController.java` | `com.nexus.onebook.advance.controller` | REST endpoints for advances |
| `ExpenseVoucherController.java` | `com.nexus.onebook.advance.controller` | REST endpoints for expense vouchers |
| `AdvanceReceiptController.java` | `com.nexus.onebook.advance.controller` | REST endpoints for advance receipts |
| `PaymentAdviceController.java` | `com.nexus.onebook.advance.controller` | REST endpoints for payment advices |
| `AdvanceReportController.java` | `com.nexus.onebook.advance.controller` | REST endpoints for reports |
| `AdvanceLimitExceededException.java` | `com.nexus.onebook.advance.exception` | Domain exception for limit violation |

### 5.2 Modified Files

| File | Change Description |
|------|-------------------|
| `VoucherType.java` (or enum) | Add `EMPLOYEE_ADVANCE`, `EXPENSE_VOUCHER`, `ADVANCE_RECEIPT`, `PAYMENT_ADVICE` values |
| `audit_workflows` (REQ-010 table) | Register new `entity_type = 'EMPLOYEE_ADVANCE'` |
| `DepartmentAccessPolicy.java` | Extend with department-visibility predicate for HOD in advance context |

### 5.3 Frontend Changes

| File | Component/Service | Change |
|------|-----------------|--------|
| `advance-request.component.ts` | `AdvanceRequestComponent` | New form for employee advance request |
| `advance-approval.component.ts` | `AdvanceApprovalComponent` | HOD/CEO/MD approval queue view |
| `expense-voucher.component.ts` | `ExpenseVoucherComponent` | Expense entry form with doc upload |
| `advance-receipt.component.ts` | `AdvanceReceiptComponent` | Return-cash entry form |
| `payment-advice-list.component.ts` | `PaymentAdviceListComponent` | Finance reimbursement list + pay action |
| `advance-reports.component.ts` | `AdvanceReportsComponent` | Tabbed report view (aging, pending, overrides) |
| `advance.service.ts` | `AdvanceService` | HTTP client for all advance APIs |

### 5.4 Migration

- Migration file: `V15__employee_advances_settlement.sql`
- Tables created: `employee_advance_config`, `employee_advance_balance`, `employee_advances`, `expense_vouchers`, `advance_receipts`, `payment_advices`
- Indexes: composite on `(tenant_id, employee_id)`, `(tenant_id, department_id)`, `(tenant_id, status)` for each table
- RLS policies: one per table using `current_setting('app.tenant_id', true)`

---

## 6. Testing

### 6.1 Unit Tests

| Test Class | Test Method | Scenario |
|------------|-------------|---------|
| `AdvanceLimitCheckServiceTest` | `testLimitNotExceeded_allows` | Outstanding + request ≤ limit; no exception |
| `AdvanceLimitCheckServiceTest` | `testLimitExceeded_throws` | Outstanding + request > limit; AdvanceLimitExceededException |
| `AdvanceLimitCheckServiceTest` | `testOverride_requiresReason` | Override without reason throws IllegalArgumentException |
| `AdvanceLimitCheckServiceTest` | `testOverride_logsAuditEvent` | Override with reason logs override audit record |
| `ApprovalTierResolverTest` | `testTier_hod_only_for_under_10k` | Amount ≤ 10,000 returns [HOD] |
| `ApprovalTierResolverTest` | `testTier_hod_ceo_for_10001_to_20000` | Amount ∈ (10k, 20k] returns [HOD, CEO] |
| `ApprovalTierResolverTest` | `testTier_hod_ceo_md_for_over_20k` | Amount > 20,000 returns [HOD, CEO, MD] |
| `AdvanceSettlementServiceTest` | `testSettle_fullyWithinAdvance` | Expense ≤ outstanding; no Payment Advice |
| `AdvanceSettlementServiceTest` | `testSettle_partiallyExceedsAdvance` | Expense > outstanding; split entry + Payment Advice created |
| `AdvanceSettlementServiceTest` | `testSettle_zeroOutstanding` | Expense with zero outstanding; full Payment Advice |
| `AdvanceReceiptServiceTest` | `testReceipt_exceedingOutstanding_rejected` | Receipt > outstanding; 400 error |
| `AdvanceReceiptServiceTest` | `testReceipt_withinOutstanding_posts` | Receipt ≤ outstanding; posts and reduces balance |

### 6.2 Integration Tests

| Test Class | Test Method | Scenario |
|------------|-------------|---------|
| `EmployeeAdvanceControllerTest` | `testCreateAdvance_limitExceeded_400` | POST returns 400 when limit breached |
| `EmployeeAdvanceControllerTest` | `testApprovalChain_hod_to_ceo_to_md` | Full three-level approval for > ₹20,000 |
| `EmployeeAdvanceControllerTest` | `testDepartmentVisibility_hodCannotSeeOtherDept` | HOD GET returns only own-dept advances |
| `ExpenseVoucherControllerTest` | `testApprove_triggersSettlement` | POST approve triggers split journal entries |
| `ExpenseVoucherControllerTest` | `testNoAdvance_fullReimbursement` | Self-funded expense creates full Payment Advice |
| `PaymentAdviceControllerTest` | `testPay_clearsLiability` | Finance pay action posts correct journal entry |
| `AdvanceReportControllerTest` | `testOverridesReport_containsOverrideRecord` | Override event appears in report |

### 6.3 Test Coverage

- Service layer: ≥ 80% coverage for all new service classes
- Controller layer: ≥ 80% coverage for all new controllers
- Total new tests: ~19 unit + ~7 integration = ~26 tests

---

## 7. Traceability

| Artifact | Link |
|---------|------|
| BRD Section | [BR-014](../../business/BRD.md#br-014-employee-advances-and-settlement) |
| FRD Section | [FR-018](../../business/FRD.md#fr-018-employee-advances-and-settlement) |
| TRD Section | [TR-009](../../business/TRD.md#tr-009-employee-advance-settlement-pipeline) |
| RTM Row | [RTM Row REQ-014](../RTM.md) |
| Related: Voucher Posting | [REQ-004](REQ-004-voucher-posting.md) — Voucher immutability and journal entry infrastructure |
| Related: Maker-Checker Workflow | [REQ-010](REQ-010-maker-checker-workflow.md) — Approval workflow state machine and audit trail |
| Related: Encryption | [REQ-002](REQ-002-zero-knowledge-encryption.md) — AES-256-GCM field encryption, audit log |
| Related: Cost Center / Org | [REQ-006](REQ-006-cost-center-management.md) — Department hierarchy and HOD mapping |
| User Stories | [US-025, US-026, US-027, US-028](../../business/user-stories.md) |
| Agent Owner | [@backend](../../../.github/agents/backend.agent.md) |
| DB Migration | `backend/src/main/resources/db/migration/V15__employee_advances_settlement.sql` |

---

## 8. Change History

| Date | Author | Change |
|------|--------|--------|
| 2026-04-04 | @LedgerExpert | Initial draft — full BR/FR/TRD specification |

---

*Requirement authored following the OneBook requirement template. Related requirements: REQ-004, REQ-010, REQ-002, REQ-006.*
