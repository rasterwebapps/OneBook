# Feature Catalog
## OneBook — Nexus Universal Accounting OS

> **Complete feature inventory with status, milestone, and API endpoints.**  
> Last Updated: 2026-03-18 | All 10 Milestones Complete

---

## Core Accounting

| Feature | Description | Status | Milestone | API Endpoint | UI Screen |
|---------|-------------|--------|-----------|-------------|-----------|
| Chart of Accounts | Hierarchical ledger account management with unlimited nesting | ✅ Live | M2 | `GET/POST /api/ledger/accounts` | `/accounting` |
| Account Tree View | Hierarchical tree visualization of all accounts | ✅ Live | M2 | `GET /api/ledger/accounts/tree` | `/accounting` |
| Account Search | Blind-index-powered search on encrypted account names | ✅ Live | M3 | `GET /api/ledger/accounts/search` | `/accounting` |
| Payment Voucher | F5 keyboard shortcut, auto-TDS deduction | ✅ Live | M2 | `POST /api/journal/transactions` | `/accounting/voucher-entry` |
| Receipt Voucher | F6 keyboard shortcut | ✅ Live | M2 | `POST /api/journal/transactions` | `/accounting/voucher-entry` |
| Journal Voucher | F7 keyboard shortcut | ✅ Live | M2 | `POST /api/journal/transactions` | `/accounting/voucher-entry` |
| Contra Voucher | F4 keyboard shortcut, cash↔bank | ✅ Live | M2 | `POST /api/journal/transactions` | `/accounting/voucher-entry` |
| Sales Voucher | F8 keyboard shortcut | ✅ Live | M2 | `POST /api/journal/transactions` | `/accounting/voucher-entry` |
| Purchase Voucher | F9 keyboard shortcut | ✅ Live | M2 | `POST /api/journal/transactions` | `/accounting/voucher-entry` |
| Debit Note | Purchase return | ✅ Live | M2 | `POST /api/journal/transactions` | `/accounting/voucher-entry` |
| Credit Note | Sales return | ✅ Live | M2 | `POST /api/journal/transactions` | `/accounting/voucher-entry` |
| Stock Journal | Inventory movement voucher | ✅ Live | M10 | `POST /api/journal/transactions` | `/accounting/voucher-entry` |
| Voucher Reversal | Immutable correction via reversing entries | ✅ Live | M2 | `POST /api/journal/transactions/{id}/reverse` | `/accounting` |
| Custom Voucher Types | Tenant-specific voucher type creation | ✅ Live | M2 | `POST /api/journal/voucher-types` | `/accounting` |
| Multi-Currency | Journal entries in foreign currencies with exchange rates | ✅ Live | M7 | `GET /api/currency/rates` | `/accounting` |
| Cost Center Allocation | Line-level cost center tagging | ✅ Live | M2 | `POST /api/journal/transactions` | `/accounting/voucher-entry` |

---

## Multi-Tenancy & Security

| Feature | Description | Status | Milestone | Implementation |
|---------|-------------|--------|-----------|----------------|
| Multi-Tenant RLS | PostgreSQL Row-Level Security on all tenant tables | ✅ Live | M1 | `V1__rls_infrastructure.sql` |
| Tenant Configuration | Per-tenant locale, currency, fiscal year | ✅ Live | M1 | `TenantLocaleService.java` |
| AES-256-GCM Encryption | Field-level encryption before DB storage | ✅ Live | M3 | `FieldEncryptionService.java` |
| Blind Index Search | HMAC-SHA256 search on encrypted fields | ✅ Live | M3 | `BlindIndexService.java` |
| Hash-Chained Audit Trail | Tamper-evident SHA-256 hash chain | ✅ Live | M3 | `AuditLogService.java` |
| Key Management | Version-based key rotation support | ✅ Live | M3 | `KeyManagementService.java` |
| Audit Trail Verification | API endpoint for hash chain integrity check | ✅ Live | M10 | `GET /api/audit/verify/{entityId}` |

---

## Reporting

| Feature | Description | Status | Milestone | API Endpoint | UI Screen |
|---------|-------------|--------|-----------|-------------|-----------|
| Trial Balance | Debit/Credit movement report with period filter | ✅ Live | M7 | `GET /api/reports/trial-balance` | `/reports` |
| Profit & Loss | Income statement with comparative periods | ✅ Live | M7 | `GET /api/reports/profit-loss` | `/reports` |
| Balance Sheet | Point-in-time assets/liabilities/equity | ✅ Live | M7 | `GET /api/reports/balance-sheet` | `/reports` |
| Cash Flow Statement | Operating/Investing/Financing activities | ✅ Live | M7 | `GET /api/reports/cash-flow` | `/reports` |
| Redis Report Cache | 30-minute cache for report results | ✅ Live | M4 | (internal) | — |
| Export to PDF | All reports exportable as PDF | ✅ Live | M9 | `GET /api/export/*?format=pdf` | `/reports` |
| Export to Excel | All reports exportable as XLSX | ✅ Live | M9 | `GET /api/export/*?format=xlsx` | `/reports` |
| Export to CSV | Transaction data as CSV | ✅ Live | M9 | `GET /api/export/transactions?format=csv` | `/reports` |

---

## Performance & Caching

| Feature | Description | Status | Milestone | Implementation |
|---------|-------------|--------|-----------|----------------|
| Redis Warm Cache | Pre-populated on login for instant response | ✅ Live | M4 | `WarmCacheService.java` |
| Cache-Aside Pattern | Check cache → DB fallback → repopulate | ✅ Live | M4 | `WarmCacheService.java` |
| Write-Through Invalidation | Cache invalidated on every write | ✅ Live | M4 | `WarmCacheService.java` |
| Redis Failure Fallback | Graceful DB fallback on Redis error | ✅ Live | M4 | `WarmCacheService.java` |
| Virtual Threads | Java 21 Project Loom for HTTP handling | ✅ Live | M1 | `application.yml` |

---

## Keyboard Navigation & UX

| Feature | Description | Status | Milestone | Implementation |
|---------|-------------|--------|-----------|----------------|
| Command Palette | Ctrl+K global fuzzy-search navigation | ✅ Live | M5 | `command-palette.component.ts` |
| F4–F9 Tally Shortcuts | Legacy Tally voucher shortcuts | ✅ Live | M5 | `key-binding-registry.service.ts` |
| Ctrl+A to Save | Tally-compatible save/accept shortcut | ✅ Live | M5 | `key-binding-registry.service.ts` |
| Alt+C to Create | Quick account creation from voucher | ✅ Live | M5 | `key-binding-registry.service.ts` |
| Angular Signals State | Reactive UI state without RxJS overhead | ✅ Live | M5 | All components |
| i18n (Transloco) | Multi-language support | ✅ Live | M5 | `@jsverse/transloco` |

---

## External Integrations

| Feature | Description | Status | Milestone | API Endpoint |
|---------|-------------|--------|-----------|-------------|
| Universal Ingestion Gateway | Accept events from any external app | ✅ Live | M6 | `POST /api/ingestion/events` |
| HL7 Adapter | Healthcare billing event ingestion | ✅ Live | M6 | adapterType: HL7 |
| ISO20022 Adapter | Banking/SWIFT message ingestion | ✅ Live | M6 | adapterType: ISO20022 |
| DMS Adapter | Document management invoice ingestion | ✅ Live | M6 | adapterType: DMS |
| Webhook Adapter | Generic JSON webhook ingestion | ✅ Live | M6 | adapterType: WEBHOOK |
| OCR Invoice | Scanned invoice auto-extraction | ✅ Live | M6 | `OcrInvoiceService.java` |
| Corporate Card Feed | Corporate card transaction import | ✅ Live | M6 | `CorporateCardService.java` |
| Three-Way Matching | PO / GR / Invoice matching | ✅ Live | M6 | `ThreeWayMatchingService.java` |

---

## GST & Tax Compliance

| Feature | Description | Status | Milestone | API Endpoint | UI Screen |
|---------|-------------|--------|-----------|-------------|-----------|
| TDS Auto-Computation | Auto TDS on applicable payments | ✅ Live | M7 | `POST /api/compliance/tds/compute` | `/gst` |
| TDS Register | All TDS deductions log | ✅ Live | M7 | `GET /api/compliance/tds/register` | `/gst` |
| Form 26Q Export | Quarterly TDS return data | ✅ Live | M7 | `GET /api/compliance/tds/26q` | `/gst` |
| TCS Computation | Tax Collected at Source on sales | ✅ Live | M7 | `POST /api/compliance/tcs/compute` | `/gst` |
| e-Invoice (IRN) | GSTN e-Invoice generation with QR | ✅ Live | M7 | `POST /api/compliance/einvoice/generate` | `/gst` |
| e-Way Bill | Goods movement e-Way Bill | ✅ Live | M7 | `POST /api/compliance/ewaybill/generate` | `/gst` |
| GSTR-1 Export | Monthly GST return data | ✅ Live | M7 | `GET /api/compliance/gst/r1` | `/gst` |
| Compliance Certification | Compliance status certifications | ✅ Live | M10 | `GET /api/compliance/certifications` | `/gst` |

---

## Fixed Asset Management

| Feature | Description | Status | Milestone | API Endpoint | UI Screen |
|---------|-------------|--------|-----------|-------------|-----------|
| Fixed Asset Register | Register and track capital assets | ✅ Live | M7 | `POST /api/fixed-assets` | `/accounting/fixed-assets` |
| SLM Depreciation | Straight-Line Method auto-computation | ✅ Live | M7 | `POST /api/fixed-assets/{id}/depreciate` | `/accounting/fixed-assets` |
| WDV Depreciation | Written Down Value auto-computation | ✅ Live | M7 | `POST /api/fixed-assets/{id}/depreciate` | `/accounting/fixed-assets` |
| Asset Disposal | Sale/scrap with gain/loss journal | ✅ Live | M7 | `POST /api/fixed-assets/{id}/dispose` | `/accounting/fixed-assets` |
| FAR Schedule Report | Full depreciation schedule export | ✅ Live | M7 | `GET /api/fixed-assets/schedule` | `/reports` |

---

## Bank Reconciliation

| Feature | Description | Status | Milestone | API Endpoint | UI Screen |
|---------|-------------|--------|-----------|-------------|-----------|
| Bank Statement Import | CSV / OFX / MT940 import | ✅ Live | M7 | `POST /api/reconciliation/import` | `/banking` |
| Auto-Matching | Amount + date + reference matching | ✅ Live | M7 | (internal) | `/banking` |
| Manual Matching | User-assisted transaction matching | ✅ Live | M7 | `POST /api/reconciliation/match` | `/banking` |
| Reconciliation Report | Adjusted balance statement | ✅ Live | M7 | `GET /api/reconciliation/report` | `/banking` |
| Period Lock | Finalize and lock reconciliation | ✅ Live | M7 | `POST /api/reconciliation/finalize` | `/banking` |

---

## AI & Intelligence (M8)

| Feature | Description | Status | Milestone | API Endpoint | UI Screen |
|---------|-------------|--------|-----------|-------------|-----------|
| Cash Flow Forecasting | AI 30/60/90/180-day cash flow forecast | ✅ Live | M8 | `GET /api/forecast/cashflow` | `/ai` |
| Anomaly Detection | Real-time transaction anomaly scoring | ✅ Live | M8 | `GET /api/anomaly/alerts` | `/ai` |
| Mark-to-Market (MTM) | Investment portfolio market valuation | ✅ Live | M8 | `GET /api/market/mtm` | `/market` |
| Market Sentiment | Market sentiment analysis | ✅ Live | M8 | `GET /api/market/sentiment` | `/market` |
| Scenario Modeling | Custom financial scenario simulation | ✅ Live | M8 | `POST /api/forecast/scenario/create` | `/ai` |
| Digital Asset Tracking | Cryptocurrency/digital asset positions | ✅ Live | M8 | `GET /api/digital-assets` | `/market` |
| Corporate Actions | Dividend, bonus, rights issue processing | ✅ Live | M8 | `POST /api/corporate-actions` | `/market` |
| AR Dashboard | Accounts receivable aging dashboard | ✅ Live | M8 | `GET /api/receivable` | `/receivable` |

---

## Workflow & Audit (M10)

| Feature | Description | Status | Milestone | API Endpoint | UI Screen |
|---------|-------------|--------|-----------|-------------|-----------|
| Maker-Checker Workflow | 3-tier approval for high-value vouchers | ✅ Live | M10 | `POST /api/workflow/submit/{id}` | `/auditor` |
| Auditor Portal | Read-only portal for external auditors | ✅ Live | M10 | `GET /api/audit/*` | `/auditor` |
| Security Audit Service | Security event monitoring | ✅ Live | M10 | `GET /api/security/audit` | `/auditor` |
| Observability | Health, metrics, monitoring endpoints | ✅ Live | M10 | `GET /api/observability/*` | — |
| Disaster Recovery | DR procedures and health endpoints | ✅ Live | M10 | `GET /api/dr/*` | — |
| Feature Entitlements | Per-tenant feature flag management | ✅ Live | M10 | `GET /api/entitlements` | — |

---

## Tally Feature Parity (M10)

| Feature | Description | Status | Milestone |
|---------|-------------|--------|-----------|
| Cheque Management | Cheque tracking and printing | ✅ Live | M10 |
| Bill of Materials (BOM) | Manufacturing BOM management | ✅ Live | M10 |
| Batch Tracking | Item batch/lot tracking | ✅ Live | M10 |
| Reorder Level | Inventory reorder alerts | ✅ Live | M10 |
| Payroll | Basic payroll processing | ✅ Live | M10 |
| Document Vault | Voucher attachment storage | ✅ Live | M10 |
| Connected Payments | Payment gateway integration | ✅ Live | M10 |
| WhatsApp Integration | WhatsApp notification channel | ✅ Live | M10 |
| Credit Management | Customer credit limit enforcement | ✅ Live | M10 |

---

## Feature Count Summary

| Module | Features | Status |
|--------|----------|--------|
| Core Accounting | 16 | 100% Live |
| Multi-Tenancy & Security | 7 | 100% Live |
| Reporting | 8 | 100% Live |
| Performance & Caching | 5 | 100% Live |
| Keyboard Navigation & UX | 6 | 100% Live |
| External Integrations | 8 | 100% Live |
| GST & Tax Compliance | 7 | 100% Live |
| Fixed Asset Management | 5 | 100% Live |
| Bank Reconciliation | 5 | 100% Live |
| AI & Intelligence | 8 | 100% Live |
| Workflow & Audit | 6 | 100% Live |
| Tally Feature Parity | 9 | 100% Live |
| **Total** | **90+** | **100% Live** |
