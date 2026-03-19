# User Stories
## OneBook — Nexus Universal Accounting OS

> **Acceptance criteria in Gherkin format for all major features.**  
> Last Updated: 2026-03-18 | Owner: @RequirementsAnalyzer | Status: APPROVED

---

## Table of Contents

- [US-001 through US-005: Core Ledger & Voucher](#core-ledger--voucher)
- [US-006 through US-010: Security & Compliance](#security--compliance)
- [US-011 through US-015: Reporting & Fixed Assets](#reporting--fixed-assets)
- [US-016 through US-020: Integration, AI & Workflow](#integration-ai--workflow)

---

## Core Ledger & Voucher

### US-001: Create a Ledger Account
**Priority:** CRITICAL | **Milestone:** M2 | **Owner:** @LedgerExpert

> **As an** accountant,  
> **I want to** create a new ledger account in the Chart of Accounts,  
> **So that** I can record financial transactions against it.

**Acceptance Criteria:**
```gherkin
Feature: Chart of Accounts — Create Account

  Scenario: Successfully create a new asset account
    Given I am logged in as an accountant for tenant "Acme Corp"
    When I POST to /api/ledger/accounts with:
      | accountCode | 1001          |
      | accountName | Cash in Hand  |
      | accountType | ASSET         |
    Then the response status should be 201 Created
    And the response body should contain an "id" field
    And the account should appear in GET /api/ledger/accounts

  Scenario: Reject duplicate account code within same tenant
    Given account code "1001" already exists for tenant "Acme Corp"
    When I POST to /api/ledger/accounts with accountCode "1001"
    Then the response status should be 409 Conflict
    And the error message should mention "duplicate account code"

  Scenario: Tenant isolation — account not visible to other tenant
    Given account "1001" exists for tenant "Acme Corp"
    When I GET /api/ledger/accounts as tenant "Beta Ltd"
    Then account "1001" should NOT appear in the response
```

---

### US-002: Post a Payment Voucher
**Priority:** CRITICAL | **Milestone:** M2 | **Owner:** @LedgerExpert

> **As an** accountant,  
> **I want to** post a payment voucher with balanced debit and credit entries,  
> **So that** the payment is recorded in the books.

**Acceptance Criteria:**
```gherkin
Feature: Voucher Posting — Payment Voucher

  Scenario: Successfully post a balanced payment voucher
    Given accounts "Cash" (1001) and "Office Expenses" (5001) exist
    When I POST to /api/journal/transactions with:
      | voucherType | PAYMENT         |
      | date        | 2026-01-15      |
      | narration   | Office supplies |
      | entries     | [{accountId: 5001, type: DEBIT, amount: 5000.00},
                      {accountId: 1001, type: CREDIT, amount: 5000.00}] |
    Then the response status should be 201 Created
    And transaction status should be "POSTED"
    And the account balance for 1001 should decrease by 5000.00
    And the account balance for 5001 should increase by 5000.00

  Scenario: Reject unbalanced journal entry
    When I POST a transaction with debits 5000.00 and credits 4999.00
    Then the response status should be 422 Unprocessable Entity
    And the error should state "Debits (5000.00) != Credits (4999.00)"

  Scenario: Keyboard shortcut triggers payment voucher screen
    Given I am on any screen in the application
    When I press the F5 key
    Then the Payment Voucher entry screen should open
```

---

### US-003: Multi-Tenant Data Isolation
**Priority:** CRITICAL | **Milestone:** M1 | **Owner:** @SecurityWarden

> **As an** IT administrator,  
> **I want** tenant data to be completely isolated at the database level,  
> **So that** one tenant can never access another tenant's financial data.

**Acceptance Criteria:**
```gherkin
Feature: Multi-Tenant Row-Level Security

  Scenario: RLS prevents cross-tenant data access
    Given tenant "Acme Corp" has journal entries
    And tenant "Beta Ltd" is a different tenant
    When a database query runs without setting app.current_tenant_id
    Then no tenant-scoped rows should be returned
    
  Scenario: RLS enforced even on direct database query
    Given a SQL injection bypasses application-level tenant filter
    When the injected query tries to SELECT from journal_transactions
    Then PostgreSQL RLS should return only the attacker's tenant rows
    
  Scenario: Tenant context set per-request
    Given I am authenticated as a user of tenant "Acme Corp"
    When I make any API request
    Then the tenant context should be set to "Acme Corp" before DB access
    And no other tenant's data should appear in any response
```

---

### US-004: Cost Center Allocation
**Priority:** HIGH | **Milestone:** M2 | **Owner:** @LedgerExpert

> **As a** finance manager,  
> **I want to** allocate journal entry lines to cost centers,  
> **So that** I can track expenses and income by department.

**Acceptance Criteria:**
```gherkin
Feature: Cost Center Management

  Scenario: Allocate journal entry to a cost center
    Given cost center "Marketing" exists with id "cc-001"
    When I post a journal entry with costCenterId "cc-001" on the expense line
    Then the entry should be tagged with cost center "Marketing"
    And the cost center P&L should reflect the expense

  Scenario: Cost center roll-up to branch
    Given "Marketing" and "Engineering" cost centers belong to "HQ Branch"
    When I request the Branch P&L report for "HQ Branch"
    Then costs from both cost centers should aggregate in the report

  Scenario: Multi-level hierarchy navigation
    Given hierarchy: Enterprise → Company → Branch → Cost Center
    When I request the company-level P&L
    Then it should consolidate all branches under the company
```

---

### US-005: Voucher Reversal
**Priority:** HIGH | **Milestone:** M2 | **Owner:** @LedgerExpert

> **As an** accountant,  
> **I want to** reverse a posted voucher,  
> **So that** I can correct an erroneous entry without deleting financial records.

**Acceptance Criteria:**
```gherkin
Feature: Voucher Reversal

  Scenario: Successfully reverse a posted voucher
    Given voucher V-001 is in POSTED status
    When I POST to /api/journal/transactions/V-001/reverse
    Then a new reversal voucher should be created with opposite entries
    And the original voucher should be linked to the reversal
    And both vouchers should remain in the audit trail
    
  Scenario: Cannot reverse an already-reversed voucher
    Given voucher V-001 has already been reversed
    When I attempt to reverse V-001 again
    Then the response should be 409 Conflict
    And the error should state "voucher already reversed"
```

---

## Security & Compliance

### US-006: Field-Level Encryption
**Priority:** CRITICAL | **Milestone:** M3 | **Owner:** @SecurityWarden

> **As an** IT administrator,  
> **I want** sensitive financial data to be encrypted before storage,  
> **So that** even database administrators cannot read sensitive values.

**Acceptance Criteria:**
```gherkin
Feature: Zero-Knowledge Field Encryption

  Scenario: Sensitive field is encrypted in database
    Given I create a journal entry with narration "Payment to John Doe"
    When I query the database directly (bypassing the application)
    Then the narration column should contain Base64 ciphertext, not plaintext
    And the ciphertext should start with a version byte prefix
    
  Scenario: Encrypted data is correctly decrypted via API
    Given a journal entry has an encrypted narration
    When I GET /api/journal/transactions/{id}
    Then the narration in the response should be the original plaintext
    
  Scenario: Search works via blind index
    Given multiple journal entries with party name containing "Acme Supplies"
    When I GET /api/ledger/accounts/search?q=Acme Supplies
    Then accounts matching "Acme Supplies" should be returned
    And the search should use the blind index column, not decrypt all records
    
  Scenario: IV is unique per encryption operation
    Given the same plaintext "Test Value" is encrypted twice
    When I compare the two ciphertexts
    Then they should be different (different random IV used each time)
```

---

### US-007: Audit Trail Tamper Detection
**Priority:** HIGH | **Milestone:** M3 | **Owner:** @SecurityWarden

> **As an** auditor,  
> **I want** a tamper-evident audit trail for all financial transactions,  
> **So that** I can verify that no records have been modified after posting.

**Acceptance Criteria:**
```gherkin
Feature: Hash-Chained Audit Trail

  Scenario: Audit entry is created on voucher posting
    Given I post a payment voucher
    When the voucher status changes to POSTED
    Then an audit log entry should be created with action "CREATE"
    And the entry should contain a SHA-256 hash

  Scenario: Hash chain is valid after 10 sequential entries
    Given 10 journal entries have been posted sequentially
    When I GET /api/audit/verify/{entityId}
    Then all 10 entries should have valid chain hashes
    And the response should state "chain integrity: VALID"

  Scenario: Tampered audit record is detected
    Given an auditor modifies an audit log entry directly in the database
    When I GET /api/audit/verify/{entityId}
    Then the response should state "chain integrity: TAMPERED"
    And the specific tampered entry index should be identified
```

---

### US-008: TDS Automatic Deduction
**Priority:** HIGH | **Milestone:** M7 | **Owner:** @ComplianceAgent

> **As an** accountant,  
> **I want** TDS to be automatically computed on applicable payments,  
> **So that** I don't miss statutory deductions and can file returns accurately.

**Acceptance Criteria:**
```gherkin
Feature: TDS/TCS Compliance

  Scenario: TDS computed on professional fees payment (Section 194J)
    Given a payment of ₹50,000 to a consultant with Section 194J applicable
    When I POST a payment voucher for ₹50,000
    Then TDS at 10% (₹5,000) should be computed automatically
    And journal entries should be: Dr Professional Fees ₹50,000
                                   Cr TDS Payable ₹5,000
                                   Cr Net Payable ₹45,000
                                   
  Scenario: TDS not deducted below threshold
    Given Section 194C threshold is ₹30,000 per transaction
    When I post a payment of ₹15,000 for contract services
    Then TDS should NOT be deducted
    And the full ₹15,000 should be credited to the payable account

  Scenario: e-Invoice generated for B2B sales above ₹5 lakh
    Given a B2B invoice for ₹6,00,000 with buyer GSTIN
    When the invoice voucher is posted
    Then an IRN request should be sent to the e-Invoice API
    And the IRN and QR code should be stored with the voucher
```

---

### US-009: Bank Reconciliation
**Priority:** HIGH | **Milestone:** M7 | **Owner:** @LedgerExpert

> **As a** finance manager,  
> **I want to** reconcile bank statements with book entries,  
> **So that** I can identify discrepancies and uncleared items.

**Acceptance Criteria:**
```gherkin
Feature: Bank Reconciliation

  Scenario: Auto-match bank statement transactions
    Given a bank statement with 50 transactions
    And book entries for the same period
    When I import the bank statement and run auto-matching
    Then transactions matching by amount + date ± 2 days should be auto-matched
    And unmatched transactions should be flagged for manual review

  Scenario: Reconciliation report shows correct adjusted balance
    Given book balance = ₹1,00,000
    And uncleared cheques = ₹5,000 (book only, not on bank statement)
    And bank charges = ₹500 (bank statement only, not in books)
    When I generate the reconciliation report
    Then Adjusted Book Balance = ₹99,500
    And Adjusted Bank Balance = ₹99,500
    And Difference = ₹0

  Scenario: Finalized reconciliation is locked
    Given a reconciliation session is finalized
    When I attempt to modify a reconciled entry
    Then the response should be 403 Forbidden
    And the error should state "reconciliation period locked"
```

---

### US-010: Maker-Checker Workflow
**Priority:** HIGH | **Milestone:** M10 | **Owner:** @AuditAgent

> **As a** finance manager,  
> **I want** high-value vouchers to require a second approval,  
> **So that** no single person can post large transactions unilaterally.

**Acceptance Criteria:**
```gherkin
Feature: Maker-Checker-Approver Workflow

  Scenario: High-value payment requires checker approval
    Given workflow threshold is ₹1,00,000 for Payment vouchers
    When a Maker creates a payment voucher for ₹2,00,000
    Then the voucher status should be PENDING_CHECK (not POSTED)
    And a notification should be sent to all Checker-role users

  Scenario: Checker approves and moves to CHECKED
    Given a voucher in PENDING_CHECK status
    When a Checker user POST to /api/workflow/check/{id} with action APPROVE
    Then the voucher should move to CHECKED status
    And if threshold requires Approver, move to PENDING_APPROVAL

  Scenario: Maker cannot approve their own voucher
    Given Maker user "alice" created voucher V-001
    When "alice" attempts to check or approve V-001
    Then the response should be 403 Forbidden
    And the error should state "cannot approve own transaction"
```

---

## Reporting & Fixed Assets

### US-011: Generate Trial Balance
**Priority:** HIGH | **Milestone:** M7 | **Owner:** @LedgerExpert

> **As a** finance manager,  
> **I want to** generate a Trial Balance for any date range,  
> **So that** I can verify that total debits equal total credits.

**Acceptance Criteria:**
```gherkin
Feature: Trial Balance Report

  Scenario: Trial balance totals are balanced
    Given journal entries have been posted for Q1 2026
    When I GET /api/reports/trial-balance?from=2026-01-01&to=2026-03-31
    Then the response should contain all active accounts
    And total closing debits should equal total closing credits
    And the report should be generated within 5 seconds

  Scenario: Trial balance is cached after first generation
    Given trial balance has been generated once for Q1 2026
    When I request the same trial balance again
    Then the response time should be < 100ms (cache hit)
    And no database query should be executed (Redis hit)

  Scenario: Export trial balance to Excel
    Given a trial balance for Q1 2026 is available
    When I GET /api/reports/trial-balance/export?format=xlsx
    Then the response should be an Excel file download
    And all account rows should be present in the spreadsheet
```

---

### US-012: Fixed Asset Depreciation
**Priority:** HIGH | **Milestone:** M7 | **Owner:** @LedgerExpert

> **As an** accountant,  
> **I want** depreciation to be computed and posted automatically,  
> **So that** asset values are correctly stated in the Balance Sheet.

**Acceptance Criteria:**
```gherkin
Feature: Fixed Asset Depreciation

  Scenario: SLM depreciation computed correctly
    Given asset "Office Computer" with cost ₹1,00,000, useful life 5 years, residual ₹10,000
    When I compute SLM depreciation for Year 1
    Then annual depreciation should be (₹1,00,000 - ₹10,000) / 5 = ₹18,000
    And the journal entry should be:
      Dr Depreciation Expense ₹18,000
      Cr Accumulated Depreciation ₹18,000

  Scenario: Book value cannot go below residual
    Given an asset has current book value = residual value
    When depreciation is computed for the next period
    Then depreciation amount should be ₹0
    And no journal entry should be posted

  Scenario: Asset disposal generates gain/loss entry
    Given an asset with book value ₹40,000 is sold for ₹45,000
    When I POST to /api/fixed-assets/{id}/dispose with salePrice 45000
    Then a journal entry should be generated:
      Dr Cash ₹45,000
      Dr Accumulated Depreciation ₹60,000
      Cr Fixed Asset ₹1,00,000
      Cr Profit on Sale ₹5,000
```

---

### US-013: Profit & Loss Statement
**Priority:** HIGH | **Milestone:** M7 | **Owner:** @LedgerExpert

> **As a** CFO,  
> **I want to** view the Profit & Loss statement for any period,  
> **So that** I can assess business performance.

**Acceptance Criteria:**
```gherkin
Feature: Profit & Loss Report

  Scenario: P&L shows correct net profit
    Given revenue entries totaling ₹10,00,000 and expense entries totaling ₹7,50,000
    When I GET /api/reports/profit-loss?from=2026-01-01&to=2026-12-31
    Then Net Profit should be ₹2,50,000
    And Gross Profit should reflect Revenue - COGS
    And Operating Profit should reflect Gross Profit - Operating Expenses

  Scenario: Comparative P&L shows year-over-year
    Given financial data for 2025 and 2026
    When I request P&L with comparative=true
    Then both years should appear side by side
    And variance amount and percentage should be shown
```

---

### US-014: AI Forecasting Dashboard
**Priority:** MEDIUM | **Milestone:** M8 | **Owner:** @AIEngineer

> **As a** CFO,  
> **I want** AI-generated cash flow forecasts,  
> **So that** I can plan treasury operations and avoid cash shortfalls.

**Acceptance Criteria:**
```gherkin
Feature: AI Cash Flow Forecasting

  Scenario: Generate 90-day cash flow forecast
    Given 12+ months of historical journal data
    When I GET /api/forecast/cashflow?horizon=90
    Then the response should contain daily/weekly projections for 90 days
    And confidence intervals should be included (base/optimistic/pessimistic)
    And the forecast should complete within 10 seconds

  Scenario: Anomaly alert triggered for unusual transaction
    Given historical average monthly expense is ₹5,00,000
    When a single expense entry of ₹50,00,000 is posted
    Then an anomaly alert should be generated with score > 0.8
    And the Finance Manager should receive a notification
```

---

### US-015: Command Palette Navigation
**Priority:** HIGH | **Milestone:** M5 | **Owner:** @UXSpecialist

> **As a** power user (accountant),  
> **I want to** open any screen instantly using the Command Palette,  
> **So that** I can navigate without using a mouse.

**Acceptance Criteria:**
```gherkin
Feature: Command Palette (Ctrl+K)

  Scenario: Open command palette with keyboard shortcut
    Given I am on any screen in the application
    When I press Ctrl+K (or CMD+K on Mac)
    Then the Command Palette overlay should open within 50ms
    And a search input should be focused automatically

  Scenario: Navigate to voucher entry via command palette
    Given the Command Palette is open
    When I type "payment"
    Then "Payment Voucher (F5)" should appear in the results
    When I press Enter to select it
    Then the Payment Voucher screen should open

  Scenario: Command palette closes on Escape
    Given the Command Palette is open
    When I press Escape
    Then the Command Palette should close
    And focus should return to the previous element
```

---

## Integration, AI & Workflow

### US-016: HL7 Healthcare Integration
**Priority:** HIGH | **Milestone:** M6 | **Owner:** @IntegrationBot

> **As a** hospital CFO,  
> **I want** billing events from the Hospital Management System to flow into OneBook automatically,  
> **So that** I don't need to manually re-enter patient billing data.

**Acceptance Criteria:**
```gherkin
Feature: HL7 External App Ingestion

  Scenario: HL7 billing event ingested and posted
    Given the HL7 adapter is registered
    When I POST to /api/ingestion/events with adapterType "HL7" and a valid HL7 payload
    Then the event status should move from RECEIVED → VALIDATED → MAPPED → POSTED
    And a journal entry for the billing amount should be created

  Scenario: Invalid HL7 payload is rejected at VALIDATED stage
    When I POST a malformed HL7 payload
    Then the event status should be FAILED
    And the error details should describe the validation failure
    And the event should be retryable via POST /api/ingestion/events/{id}/retry
```

---

### US-017: Redis Warm Cache Performance
**Priority:** HIGH | **Milestone:** M4 | **Owner:** @PerfEngineer

> **As a** user,  
> **I want** the application to feel instant after login,  
> **So that** I can work productively without waiting for data to load.

**Acceptance Criteria:**
```gherkin
Feature: Redis Warm Cache

  Scenario: Cache populated on login
    Given a user logs in successfully
    When the login response is returned
    Then frequently accessed accounts should be pre-cached in Redis
    And subsequent API calls should have cache hit ratio > 80%

  Scenario: Application continues working if Redis is unavailable
    Given Redis is down (simulated failure)
    When I make an API request for account data
    Then the request should succeed (fallback to PostgreSQL)
    And a warning should appear in the logs
    And no 500 error should be returned to the user

  Scenario: Cache invalidated on account update
    Given account "1001" is cached in Redis
    When the account balance changes due to a new journal entry
    Then the Redis cache entry for account "1001" should be invalidated
    And the next request should fetch fresh data from the database
```

---

### US-018: Intercompany Consolidation
**Priority:** HIGH | **Milestone:** M7 | **Owner:** @LedgerExpert

> **As a** Group CFO,  
> **I want** to consolidate financial statements across multiple entities,  
> **So that** I can view the group's overall financial position.

**Acceptance Criteria:**
```gherkin
Feature: Intercompany Consolidation

  Scenario: Consolidated P&L eliminates intercompany transactions
    Given Entity A sold goods worth ₹10,00,000 to Entity B (intercompany)
    When I request the consolidated P&L for the group
    Then the intercompany revenue and expense should be eliminated
    And the consolidated net profit should exclude intercompany transactions

  Scenario: Consolidation report includes all subsidiaries
    Given 5 subsidiary companies under a parent group
    When I POST to /api/consolidation/generate
    Then the response should aggregate figures from all 5 subsidiaries
    And minority interests should be separately disclosed
```

---

### US-019: Auditor Portal Access
**Priority:** HIGH | **Milestone:** M10 | **Owner:** @AuditAgent

> **As an** external auditor,  
> **I want** read-only access to financial records with hash chain verification,  
> **So that** I can independently verify the integrity of financial data.

**Acceptance Criteria:**
```gherkin
Feature: Auditor Portal

  Scenario: Auditor has read-only access
    Given an auditor role user
    When they attempt to POST/PUT/DELETE any resource
    Then all write operations should return 403 Forbidden
    And read operations (GET) should succeed

  Scenario: Auditor can verify audit trail integrity
    When the auditor accesses /api/audit/verify/{entityId}
    Then the response should confirm hash chain VALID or TAMPERED
    And the verification should be completable within 30 seconds for 10,000 entries

  Scenario: Document vault accessible to auditor
    Given supporting documents are attached to journal entries
    When the auditor requests document vault access
    Then they should be able to view (not download) attached documents
```

---

### US-020: Export Financial Data
**Priority:** MEDIUM | **Milestone:** M9 | **Owner:** @LedgerExpert

> **As a** finance manager,  
> **I want to** export financial reports and data in standard formats,  
> **So that** I can share them with stakeholders and regulators.

**Acceptance Criteria:**
```gherkin
Feature: Data Export

  Scenario: Export Trial Balance to Excel
    Given trial balance data for Q1 2026
    When I GET /api/export/trial-balance?format=xlsx
    Then the response should be an Excel file
    And the file should contain all account rows with correct figures

  Scenario: Export journal transactions to CSV
    When I GET /api/export/transactions?from=2026-01-01&to=2026-03-31&format=csv
    Then the response should be a CSV file
    And each row should contain: date, voucher number, account, description, debit, credit

  Scenario: Export GST data in government-prescribed format
    When I GET /api/export/gst/r1?period=2026-01
    Then the response should be in GSTR-1 JSON format
    And all B2B invoice details should be included
```

---

## Story Status Summary

| Story ID | Feature | Priority | Milestone | Status |
|----------|---------|----------|-----------|--------|
| US-001 | Create Ledger Account | CRITICAL | M2 | ✅ Implemented |
| US-002 | Post Payment Voucher | CRITICAL | M2 | ✅ Implemented |
| US-003 | Multi-Tenant Isolation | CRITICAL | M1 | ✅ Implemented |
| US-004 | Cost Center Allocation | HIGH | M2 | ✅ Implemented |
| US-005 | Voucher Reversal | HIGH | M2 | ✅ Implemented |
| US-006 | Field-Level Encryption | CRITICAL | M3 | ✅ Implemented |
| US-007 | Audit Trail Tamper Detection | HIGH | M3 | ✅ Implemented |
| US-008 | TDS Automatic Deduction | HIGH | M7 | ✅ Implemented |
| US-009 | Bank Reconciliation | HIGH | M7 | ✅ Implemented |
| US-010 | Maker-Checker Workflow | HIGH | M10 | ✅ Implemented |
| US-011 | Trial Balance | HIGH | M7 | ✅ Implemented |
| US-012 | Fixed Asset Depreciation | HIGH | M7 | ✅ Implemented |
| US-013 | Profit & Loss Statement | HIGH | M7 | ✅ Implemented |
| US-014 | AI Forecasting Dashboard | MEDIUM | M8 | ✅ Implemented |
| US-015 | Command Palette Navigation | HIGH | M5 | ✅ Implemented |
| US-016 | HL7 Healthcare Integration | HIGH | M6 | ✅ Implemented |
| US-017 | Redis Warm Cache Performance | HIGH | M4 | ✅ Implemented |
| US-018 | Intercompany Consolidation | HIGH | M7 | ✅ Implemented |
| US-019 | Auditor Portal Access | HIGH | M10 | ✅ Implemented |
| US-020 | Export Financial Data | MEDIUM | M9 | ✅ Implemented |

**Total: 20 user stories | All ✅ Implemented**
