# Functional Requirements Document (FRD)
## OneBook — Nexus Universal Accounting OS

> **Auto-generated from REQ-*.md files. Version: Living Document.**  
> Last Updated: 2026-03-18 | Owner: @RequirementsAnalyzer | Status: APPROVED

---

## Table of Contents

1. [Document Purpose](#1-document-purpose)
2. [Ledger Management](#2-ledger-management)
3. [Security](#3-security)
4. [Ingestion & Integration](#4-ingestion--integration)
5. [Reporting](#5-reporting)
6. [Fixed Assets](#6-fixed-assets)
7. [Tax Compliance](#7-tax-compliance)
8. [Bank Reconciliation](#8-bank-reconciliation)
9. [Workflows](#9-workflows)
10. [AI Intelligence](#10-ai-intelligence)
11. [Keyboard Navigation](#11-keyboard-navigation)
12. [Implementation Status Summary](#12-implementation-status-summary)

---

## 1. Document Purpose

This Functional Requirements Document (FRD) defines how the OneBook system behaves from a user and system perspective. It translates the business requirements in the BRD into specific, testable functional specifications with defined inputs, outputs, validation rules, API endpoints, and UI screens.

**Scope:** All features implemented in M1–M10.  
**Linked BRD:** `docs/business/BRD.md`  
**Linked TRD:** `docs/business/TRD.md`

---

## 2. Ledger Management

### FR-001: Chart of Accounts
**Description:** The system shall maintain a hierarchical chart of accounts for each tenant. Accounts are organized into groups (Assets, Liabilities, Income, Expenses, Equity) with unlimited nesting depth.

**Linked BRs:** BR-001, BR-004  
**Status:** ✅ IMPLEMENTED (M2)

**Inputs:**
- Account code (unique within tenant)
- Account name
- Account type (ASSET, LIABILITY, INCOME, EXPENSE, EQUITY)
- Parent account ID (optional, for hierarchy)
- Tenant ID (from JWT context)
- Opening balance (BigDecimal)
- Currency code (ISO 4217)

**Outputs:**
- Created/updated account with system-generated UUID
- Account hierarchy tree (nested JSON)
- Account balance (aggregated from journal entries)

**Validation Rules:**
- Account code must be unique per tenant
- Account type must be one of the 5 standard types
- Circular parent references are prohibited
- Opening balance defaults to 0.00 if not provided
- Tenant ID must exist in tenant_config table

**API Endpoints:**
```
POST   /api/ledger/accounts          — Create account
GET    /api/ledger/accounts          — List all accounts for tenant
GET    /api/ledger/accounts/{id}     — Get account by ID
PUT    /api/ledger/accounts/{id}     — Update account
DELETE /api/ledger/accounts/{id}     — Deactivate account
GET    /api/ledger/accounts/tree     — Get full account hierarchy
GET    /api/ledger/accounts/search   — Search by name or code (blind index)
```

**UI Screens:**
- Chart of Accounts list (`/accounting/chart-of-accounts`)
- Account creation/edit modal
- Account hierarchy tree view

**Implementation Files:**
- `LedgerAccountService.java`
- `LedgerController.java`
- `LedgerAccountRepository.java`
- `LedgerAccount.java` (JPA entity)
- `V3__ledger_and_journal.sql`

---

### FR-002: Journal Entry / Voucher Posting
**Description:** All financial transactions shall be recorded as double-entry journal vouchers. Each voucher has a header (type, date, narration) and two or more entry lines (debit/credit).

**Linked BRs:** BR-001, BR-004  
**Status:** ✅ IMPLEMENTED (M2)

**Inputs:**
- Voucher type (PAYMENT, RECEIPT, JOURNAL, CONTRA, SALES, PURCHASE, DEBIT_NOTE, CREDIT_NOTE)
- Transaction date
- Narration / description
- Reference number (optional)
- Journal entry lines: account ID, entry type (DEBIT/CREDIT), amount (BigDecimal)
- Cost center allocation (optional)
- Currency and exchange rate (for multi-currency)

**Outputs:**
- Posted journal transaction with status POSTED
- Updated account balances
- Audit log entry with hash chain
- Voucher number (auto-generated sequence per voucher type)

**Validation Rules:**
- Sum of all DEBIT amounts must equal sum of all CREDIT amounts
- Minimum 2 entry lines required
- Amount must be positive BigDecimal
- Transaction date cannot be in a locked period
- All account IDs must belong to the same tenant
- Amount precision: minimum 2, maximum 6 decimal places

**API Endpoints:**
```
POST   /api/journal/transactions        — Post new journal voucher
GET    /api/journal/transactions        — List transactions (paginated)
GET    /api/journal/transactions/{id}   — Get transaction with entries
POST   /api/journal/transactions/{id}/reverse — Reverse a posted transaction
GET    /api/journal/voucher-types       — List available voucher types
POST   /api/journal/voucher-types       — Create custom voucher type
```

**UI Screens:**
- Voucher Entry screen (`/accounting/voucher-entry`)
- Transaction list with filters
- Voucher detail view with entry lines

**Implementation Files:**
- `JournalService.java`
- `JournalController.java`
- `VoucherTypeService.java`
- `JournalTransaction.java`, `JournalEntry.java` (JPA entities)
- `UnbalancedTransactionException.java`

---

## 3. Security

### FR-003: Field-Level Encryption
**Description:** Sensitive data fields shall be encrypted using AES-256-GCM in the application layer before storage. Decryption occurs in the application layer after retrieval.

**Linked BRs:** BR-002  
**Status:** ✅ IMPLEMENTED (M3)

**Inputs:**
- Plaintext sensitive value
- Encryption key (from environment variable via `KeyManagementService`)
- Random 12-byte IV (generated per operation)

**Outputs:**
- Ciphertext in wire format: `[version(1)][IV(12)][ciphertext+GCM tag]` → Base64
- Blind index (HMAC-SHA256 of plaintext) for searchability

**Validation Rules:**
- IV must be freshly generated per encryption operation (never reused)
- GCM authentication tag must be verified on decryption
- Key version byte must match a known key in the key registry
- Blind index stored alongside ciphertext but never used for range queries

**API Endpoints:** (Internal — not exposed via REST)  
`FieldEncryptionService.encrypt(String plaintext) → String ciphertext`  
`FieldEncryptionService.decrypt(String ciphertext) → String plaintext`

**Implementation Files:**
- `FieldEncryptionService.java`
- `BlindIndexService.java`
- `KeyManagementService.java`
- `EncryptedStringConverter.java` (JPA AttributeConverter)
- `V5__blind_dba_infrastructure.sql`

---

### FR-004: Audit Trail
**Description:** Every state-changing operation shall generate an immutable audit log entry. Entries are hash-chained so that any tampering is detectable.

**Linked BRs:** BR-002, BR-010  
**Status:** ✅ IMPLEMENTED (M3)

**Inputs:**
- Entity type (e.g., "JournalTransaction")
- Entity ID
- Action (CREATE, UPDATE, DELETE, APPROVE, REJECT)
- Actor (user ID)
- Change payload (before/after state as JSON)
- Previous audit entry hash

**Outputs:**
- Audit log entry with SHA-256 hash
- Hash = SHA-256(previousHash + entityType + entityId + action + timestamp + payload)

**Validation Rules:**
- Previous hash must reference the immediately preceding entry for the entity
- Hash computation is deterministic given the same inputs
- Audit entries are immutable (no UPDATE or DELETE permitted)

**API Endpoints:**
```
GET    /api/audit/entries              — List audit entries (paginated)
GET    /api/audit/entries/{entityId}   — Audit history for an entity
GET    /api/audit/verify/{entityId}    — Verify hash chain integrity
```

**Implementation Files:**
- `AuditLogService.java`
- `AuditLog.java` (JPA entity)
- `AuditLogRepository.java`

---

## 4. Ingestion & Integration

### FR-005: Universal Financial Event Gateway
**Description:** A REST gateway that accepts financial events from any external application, routes them to the appropriate adapter, and posts resulting journal entries.

**Linked BRs:** BR-003  
**Status:** ✅ IMPLEMENTED (M6)

**Inputs:**
- Adapter type identifier (HL7, DMS, ISO20022, WEBHOOK, CUSTOM)
- Event payload (format varies by adapter)
- Source application name
- Tenant ID (from JWT)

**Outputs:**
- Ingestion event record with status RECEIVED
- After pipeline: VALIDATED → MAPPED → POSTED (or FAILED with error details)
- Journal transaction ID (on successful posting)

**Validation Rules:**
- Adapter type must be registered in AdapterRegistry
- Payload must conform to adapter's expected schema
- Resulting journal entries must balance (double-entry validation)
- Tenant ID must be authorized to use the specified adapter

**API Endpoints:**
```
POST   /api/ingestion/events                   — Submit financial event
GET    /api/ingestion/events                   — List ingestion events
GET    /api/ingestion/events/{id}              — Get event with status
POST   /api/ingestion/events/{id}/retry        — Retry failed event
GET    /api/ingestion/adapters                 — List registered adapters
```

**Implementation Files:**
- `IngestionController.java`
- `ExternalAppIngestionService.java`
- `FinancialEventRepository.java`

---

### FR-006: External App Adapters
**Description:** Sector-specific adapters that parse external formats and map them to OneBook's internal financial event model.

**Linked BRs:** BR-003  
**Status:** ✅ IMPLEMENTED (M6)

**Supported Adapters:**
| Adapter | Format | Use Case |
|---------|--------|---------|
| HL7 Adapter | HL7 v2.x/FHIR | Hospital billing events |
| DMS Adapter | XML/JSON | Document management invoices |
| ISO20022 Adapter | XML (camt/pain) | Banking/SWIFT messages |
| Webhook Adapter | JSON | Generic webhook payloads |
| OCR Invoice | Image/PDF | Scanned invoice auto-posting |
| Corporate Card | CSV/JSON | Corporate card transaction feeds |

**Implementation Files:**
- `ExternalAppAdapter.java` (interface)
- `ExternalAppIngestionService.java`
- `OcrInvoiceService.java`
- `CorporateCardService.java`
- `V6__ingestion_layer.sql`

---

## 5. Reporting

### FR-007: Trial Balance
**Description:** Generate a Trial Balance report showing opening balance, debit movements, credit movements, and closing balance for all active accounts in a period.

**Linked BRs:** BR-005  
**Status:** ✅ IMPLEMENTED (M7)

**Inputs:**
- From date
- To date
- Cost center filter (optional)
- Branch filter (optional)
- Tenant ID

**Outputs:**
- Account-level rows: account code, name, opening debit/credit, period debit/credit, closing debit/credit
- Footer totals: total debits must equal total credits
- Export: PDF, Excel

**Validation Rules:**
- Date range must not span more than 3 fiscal years
- Total debits must equal total credits (otherwise report generation fails with error)
- Accounts with zero activity in period are optionally included

**API Endpoints:**
```
GET    /api/reports/trial-balance?from=&to=&costCenter=   — Generate trial balance
GET    /api/reports/trial-balance/export?format=pdf|xlsx  — Export
```

**Implementation Files:**
- `TrialBalanceService.java`
- `ReportController.java`
- `WarmCacheService.java` (cache layer)

---

### FR-008: P&L, Balance Sheet, Cash Flow
**Description:** Generate standard financial statements: Profit & Loss (Income Statement), Balance Sheet, and Cash Flow Statement.

**Linked BRs:** BR-005, BR-006  
**Status:** ✅ IMPLEMENTED (M7)

**Inputs:**
- From/To dates (P&L, Cash Flow)
- As-of date (Balance Sheet)
- Comparative period toggle
- Consolidation mode (roll up branches)

**Outputs:**
- P&L: Revenue, COGS, Gross Profit, Operating Expenses, EBIT, Net Profit
- Balance Sheet: Assets (Current/Non-Current), Liabilities (Current/Non-Current), Equity
- Cash Flow: Operating, Investing, Financing activities; Net cash change

**API Endpoints:**
```
GET    /api/reports/profit-loss         — Profit & Loss statement
GET    /api/reports/balance-sheet       — Balance Sheet
GET    /api/reports/cash-flow           — Cash Flow statement
GET    /api/reports/all/export          — Export all three reports
```

**Implementation Files:**
- `ProfitAndLossService.java`
- `BalanceSheetService.java`
- `CashFlowService.java`
- `ReportController.java`

---

## 6. Fixed Assets

### FR-009: Fixed Asset Register (FAR)
**Description:** Maintain a register of all capital assets owned by the tenant with lifecycle tracking from acquisition to disposal.

**Linked BRs:** BR-007  
**Status:** ✅ IMPLEMENTED (M7)

**Inputs:**
- Asset name, code, category
- Purchase date, cost (BigDecimal), vendor
- Useful life (years), residual value
- Asset location, department/cost center
- Depreciation method (SLM or WDV)

**Outputs:**
- Fixed Asset Register (FAR) list with current book value
- Depreciation schedule (year-by-year table)
- Asset movement report (additions, disposals, depreciation)

**API Endpoints:**
```
POST   /api/fixed-assets               — Register new asset
GET    /api/fixed-assets               — List all assets
GET    /api/fixed-assets/{id}          — Get asset details
PUT    /api/fixed-assets/{id}          — Update asset
POST   /api/fixed-assets/{id}/dispose  — Dispose/sell asset
GET    /api/fixed-assets/schedule      — Depreciation schedule report
```

**Implementation Files:**
- `FixedAssetService.java`
- `FixedAssetController.java`

---

### FR-010: Depreciation Computation
**Description:** Automatically compute and post depreciation journal entries for all active assets at period close.

**Linked BRs:** BR-007  
**Status:** ✅ IMPLEMENTED (M7)

**Inputs:**
- Period (month/quarter/year)
- Depreciation method per asset
- Current book value

**Outputs:**
- Depreciation amount per asset (BigDecimal)
- Auto-posted journal entries: Dr Depreciation Expense / Cr Accumulated Depreciation
- Period depreciation summary

**Validation Rules:**
- Depreciation cannot make book value negative
- SLM: (Cost − Residual) / Useful Life years
- WDV: Opening book value × Depreciation rate

**Implementation Files:**
- `FixedAssetService.java` (depreciation methods)
- `JournalService.java` (auto-posting)

---

## 7. Tax Compliance

### FR-011: TDS/TCS Deduction
**Description:** Automatically compute and deduct Tax Deducted at Source (TDS) and Tax Collected at Source (TCS) on applicable transactions.

**Linked BRs:** BR-008  
**Status:** ✅ IMPLEMENTED (M7)

**Inputs:**
- Payment voucher with payee PAN, payment nature
- Applicable TDS section code (194A, 194C, 194H, 194I, 194J, etc.)
- Payment amount (BigDecimal)

**Outputs:**
- TDS amount computed and deducted
- Journal entries: Dr Expense / Cr TDS Payable / Cr Net Payable
- TDS certificate details (Form 16A data)

**Validation Rules:**
- TDS applies only when payment exceeds section threshold
- Lower deduction certificate (Form 13) overrides default rate
- PAN validation against NSDL format

**API Endpoints:**
```
POST   /api/compliance/tds/compute    — Compute TDS on payment
GET    /api/compliance/tds/register   — TDS deduction register
GET    /api/compliance/tds/26q        — Generate 26Q data
POST   /api/compliance/tcs/compute    — Compute TCS on sale
```

**Implementation Files:**
- `TdsTcsService.java`
- `TdsTcsController.java`
- `ComplianceService.java`

---

### FR-012: e-Invoice / e-Way Bill
**Description:** Generate e-Invoice (IRN) and e-Way Bill in compliance with GST requirements for applicable B2B transactions.

**Linked BRs:** BR-008  
**Status:** ✅ IMPLEMENTED (M7)

**Inputs:**
- Sales invoice details (buyer GSTIN, items, HSN codes, tax amounts)
- Transaction value (for e-Way Bill threshold check)
- Transporter details (for e-Way Bill)

**Outputs:**
- IRN (Invoice Reference Number) from GSTN
- QR code (digitally signed)
- e-Way Bill number and validity period

**API Endpoints:**
```
POST   /api/compliance/einvoice/generate    — Generate e-Invoice IRN
GET    /api/compliance/einvoice/{irn}       — Get e-Invoice details
POST   /api/compliance/ewaybill/generate    — Generate e-Way Bill
GET    /api/compliance/gst/r1              — GSTR-1 data export
```

**Implementation Files:**
- `ComplianceService.java`
- `ComplianceController.java`
- `V7__reporting_compliance_far.sql`

---

## 8. Bank Reconciliation

### FR-013: Bank Reconciliation
**Description:** Match bank statement transactions against book entries to identify reconciled items, uncleared cheques, and discrepancies.

**Linked BRs:** BR-009  
**Status:** ✅ IMPLEMENTED (M7)

**Inputs:**
- Bank statement file (CSV/OFX/MT940)
- Bank account ledger ID
- Reconciliation period (from/to dates)

**Outputs:**
- Auto-matched transactions (by amount + date proximity + reference)
- Unmatched bank entries (to be explained)
- Unmatched book entries (outstanding cheques/deposits)
- Reconciliation summary: Book Balance, Bank Balance, Difference, Adjusted Balance

**Validation Rules:**
- Closing book balance must equal closing bank balance after adjustments
- Auto-match confidence score ≥ 85% required for auto-accept
- Manual matches require Maker-Checker approval

**API Endpoints:**
```
POST   /api/reconciliation/import       — Import bank statement
GET    /api/reconciliation/sessions     — List reconciliation sessions
GET    /api/reconciliation/sessions/{id} — Get session with matches
POST   /api/reconciliation/match        — Manually match entries
POST   /api/reconciliation/finalize     — Lock reconciliation
GET    /api/reconciliation/report       — Reconciliation statement
```

**Implementation Files:**
- `BankReconciliationService.java`
- `ReconciliationController.java`
- `BankFeedTransactionRepository.java`

---

## 9. Workflows

### FR-014: Maker-Checker-Approver Workflow
**Description:** Enforce separation of duties for voucher posting. Transactions above threshold require Checker + Approver sign-off before posting.

**Linked BRs:** BR-010  
**Status:** ✅ IMPLEMENTED (M10)

**Inputs:**
- Voucher in DRAFT status
- Workflow threshold configuration (per tenant, per voucher type)
- Checker/Approver assignments

**Outputs:**
- Workflow state transitions with timestamps and actor IDs
- Email/in-app notifications at each transition
- Audit trail entry at every state change

**State Machine:**
```
DRAFT → PENDING_CHECK → CHECKED → PENDING_APPROVAL → APPROVED → POSTED
                       ↓                            ↓
                    REJECTED                     REJECTED
                       ↓                            ↓
                  (back to Maker)             (back to Maker)
```

**API Endpoints:**
```
POST   /api/workflow/submit/{voucherId}    — Submit for checking
POST   /api/workflow/check/{voucherId}     — Checker approves/rejects
POST   /api/workflow/approve/{voucherId}   — Approver approves/rejects
GET    /api/workflow/pending               — List pending actions for user
GET    /api/workflow/history/{voucherId}   — Workflow history
```

**Implementation Files:**
- `AuditorPortalService.java`
- `AuditorPortalController.java`
- `AuditWorkflowRepository.java`
- `V9__hardening_audit_production.sql`

---

## 10. AI Intelligence

### FR-015: Cash Flow Forecasting
**Description:** AI-driven cash flow forecasting using historical journal data with configurable forecast horizons.

**Linked BRs:** (Enhancement — post M8)  
**Status:** ✅ IMPLEMENTED (M8)

**Inputs:**
- Historical journal entries (12–36 months)
- Forecast horizon (30/60/90/180 days)
- Seasonality flags

**Outputs:**
- Projected cash inflows and outflows by period
- Confidence interval bands (±10% / ±20%)
- Scenario comparison (optimistic / base / pessimistic)

**API Endpoints:**
```
GET    /api/forecast/cashflow?horizon=90     — Cash flow forecast
GET    /api/forecast/scenario                — Scenario modeling
POST   /api/forecast/scenario/create         — Create custom scenario
```

**Implementation Files:**
- `ForecastingService.java`
- `ForecastController.java`
- `ScenarioModelingService.java`
- `V8__ai_intelligence_features.sql`

---

### FR-016: Anomaly Detection
**Description:** Detect statistically anomalous transactions that deviate significantly from historical patterns.

**Linked BRs:** (Enhancement — post M8)  
**Status:** ✅ IMPLEMENTED (M8)

**Inputs:**
- Real-time journal entries
- Historical baseline (rolling 90-day window)
- Anomaly sensitivity threshold (configurable)

**Outputs:**
- Anomaly score (0.0–1.0) per transaction
- Anomaly type classification (amount, frequency, counterparty, timing)
- Alert notification to Finance Manager

**API Endpoints:**
```
GET    /api/anomaly/alerts              — List current anomaly alerts
GET    /api/anomaly/alerts/{id}         — Alert details with explanation
POST   /api/anomaly/alerts/{id}/dismiss — Dismiss false positive
GET    /api/anomaly/stats               — Anomaly statistics dashboard
```

**Implementation Files:**
- `AnomalyDetectionService.java`
- `AnomalyController.java`
- `MarkToMarketService.java`

---

## 11. Keyboard Navigation

### FR-017: Command Palette & Keyboard Shortcuts
**Description:** Power users shall navigate the entire application using keyboard shortcuts, with a Command Palette (Ctrl+K) for quick action discovery — achieving Tally-equivalent keyboard speed.

**Linked BRs:** (UX requirement — M5)  
**Status:** ✅ IMPLEMENTED (M5)

**Inputs:**
- Keyboard input (key combinations)
- Current screen context

**Outputs:**
- Navigation to target screen
- Form pre-population (where applicable)
- Command execution (post voucher, run report, open palette)

**Tally Legacy Shortcuts:**
| Shortcut | Action |
|----------|--------|
| F4 | Contra Entry |
| F5 | Payment Voucher |
| F6 | Receipt Voucher |
| F7 | Journal Voucher |
| F8 | Sales Voucher |
| F9 | Purchase Voucher |
| Ctrl+A | Accept/Save entry |
| Alt+C | Create new voucher |
| Ctrl+K / CMD+K | Open Command Palette |
| Escape | Close / Cancel |

**Implementation Files:**
- `key-binding-registry.service.ts`
- `command-palette.component.ts`
- `docs/key-binding-registry.md`

---

## 12. Implementation Status Summary

| FR ID | Feature | Milestone | Status | Test Count |
|-------|---------|-----------|--------|------------|
| FR-001 | Chart of Accounts | M2 | ✅ Complete | 25+ |
| FR-002 | Journal Entry / Voucher Posting | M2 | ✅ Complete | 40+ |
| FR-003 | Field-Level Encryption | M3 | ✅ Complete | 30+ |
| FR-004 | Audit Trail | M3 | ✅ Complete | 20+ |
| FR-005 | Universal Event Gateway | M6 | ✅ Complete | 25+ |
| FR-006 | External App Adapters | M6 | ✅ Complete | 20+ |
| FR-007 | Trial Balance | M7 | ✅ Complete | 15+ |
| FR-008 | P&L / Balance Sheet / Cash Flow | M7 | ✅ Complete | 30+ |
| FR-009 | Fixed Asset Register | M7 | ✅ Complete | 15+ |
| FR-010 | Depreciation Computation | M7 | ✅ Complete | 10+ |
| FR-011 | TDS/TCS Deduction | M7 | ✅ Complete | 20+ |
| FR-012 | e-Invoice / e-Way Bill | M7 | ✅ Complete | 15+ |
| FR-013 | Bank Reconciliation | M7 | ✅ Complete | 20+ |
| FR-014 | Maker-Checker-Approver Workflow | M10 | ✅ Complete | 25+ |
| FR-015 | Cash Flow Forecasting | M8 | ✅ Complete | 15+ |
| FR-016 | Anomaly Detection | M8 | ✅ Complete | 15+ |
| FR-017 | Command Palette & Keyboard Shortcuts | M5 | ✅ Complete | 20+ |

**Total: 17 functional requirements | All ✅ IMPLEMENTED**

---

*This document is auto-generated from REQ-*.md files by `docs/automation/generate-frd.js`. Do not edit manually.*
