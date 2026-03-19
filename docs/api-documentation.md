# OneBook — REST API Documentation

> Complete reference for all REST API endpoints in the Nexus Universal Accounting OS.

---

## Table of Contents

1. [API Overview](#api-overview)
2. [Authentication & Multi-Tenancy](#authentication--multi-tenancy)
3. [Ledger APIs](#ledger-apis)
4. [Journal APIs](#journal-apis)
5. [Report APIs](#report-apis)
6. [Ingestion APIs](#ingestion-apis)
7. [Forecast & AI APIs](#forecast--ai-apis)
8. [Asset Management APIs](#asset-management-apis)
9. [Compliance APIs](#compliance-apis)
10. [Reconciliation APIs](#reconciliation-apis)
11. [Consolidation APIs](#consolidation-apis)
12. [Configuration APIs](#configuration-apis)
13. [Cache Management APIs](#cache-management-apis)
14. [Health & Actuator](#health--actuator)

---

## API Overview

### Base URL

```
http://localhost:8080/api
```

### Content Type

All endpoints accept and return `application/json` by default, configured via `HeadlessApiConfig`.

### CORS

CORS is enabled for all `/api/**` endpoints:
- **Allowed Origins**: `*` (all origins)
- **Allowed Methods**: `GET`, `POST`, `PUT`, `DELETE`, `PATCH`, `OPTIONS`
- **Allowed Headers**: `*`
- **Max Age**: 3600 seconds

### Headless API Design

The API is fully decoupled from any frontend framework. It can serve Angular, Flutter, React Native, or any HTTP client without modification.

---

## Authentication & Multi-Tenancy

### Tenant Context

Every request operates within a tenant context. The backend sets the PostgreSQL session variable `app.current_tenant` which is enforced by Row-Level Security (RLS) at the database level.

All responses are automatically filtered to the current tenant — there is no way to access another tenant's data even with direct SQL access.

---

## Ledger APIs

**Controller**: `LedgerController`
**Base Path**: `/api/ledger`

### List Accounts

```http
GET /api/ledger/accounts?tenantId={tenantId}
```

Returns the Chart of Accounts for the specified tenant.

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "tenantId": "default-tenant",
    "accountCode": "1000",
    "accountName": "Cash",
    "accountType": "ASSET",
    "balance": 50000.0000,
    "metadata": {}
  }
]
```

### Get Account by ID

```http
GET /api/ledger/accounts/{id}?tenantId={tenantId}
```

**Response**: `200 OK` — Single account object
**Error**: `404 Not Found` — Account not found

### Create Account

```http
POST /api/ledger/accounts?tenantId={tenantId}
```

**Request Body**:
```json
{
  "accountCode": "1300",
  "accountName": "Prepaid Expenses",
  "accountType": "ASSET",
  "costCenterId": 1,
  "metadata": { "category": "current-assets" }
}
```

**Response**: `200 OK` — Created account object

### Get Trial Balance

```http
GET /api/ledger/trial-balance?tenantId={tenantId}
```

**Response**: `200 OK`
```json
{
  "tenantId": "default-tenant",
  "generatedAt": "2026-03-11T09:30:00",
  "accounts": [
    {
      "accountCode": "1000",
      "accountName": "Cash",
      "accountType": "ASSET",
      "debitBalance": 50000.0000,
      "creditBalance": 0.0000
    }
  ],
  "totalDebits": 150000.0000,
  "totalCredits": 150000.0000,
  "isBalanced": true
}
```

---

## Journal APIs

**Controller**: `JournalController`
**Base Path**: `/api/journal`

### Create Journal Transaction

```http
POST /api/journal/transactions
```

Creates a balanced double-entry journal transaction (unposted by default).

**Request Body**:
```json
{
  "tenantId": "default-tenant",
  "transactionDate": "2026-03-11",
  "description": "Office supplies purchase",
  "entries": [
    { "accountId": 12, "entryType": "DEBIT", "amount": 500.00 },
    { "accountId": 1, "entryType": "CREDIT", "amount": 500.00 }
  ]
}
```

**Response**: `201 Created` — Created transaction with UUID and `posted: false`
**Error**: `400 Bad Request` — Unbalanced or invalid entries

### Post a Transaction

```http
POST /api/journal/transactions/{uuid}/post
```

Marks an existing transaction as posted, making it visible in the trial balance and other reports.
The transaction must be balanced (debits == credits). This operation is idempotent.

**Path Parameter**: `uuid` — the transaction UUID

**Response**: `200 OK` — Updated transaction with `posted: true`
**Error**: `400 Bad Request` — Transaction not found or unbalanced

### List Transactions

```http
GET /api/journal/transactions?tenantId={tenantId}
```

**Response**: `200 OK` — Array of journal transactions

---

## Report APIs

**Controller**: `ReportController`
**Base Path**: `/api/reports`

### Profit & Loss Statement

```http
GET /api/reports/profit-and-loss?tenantId={tenantId}&startDate={date}&endDate={date}
```

**Response**: `200 OK`
```json
{
  "tenantId": "default-tenant",
  "startDate": "2026-01-01",
  "endDate": "2026-03-31",
  "revenueAccounts": [...],
  "expenseAccounts": [...],
  "totalRevenue": 200000.0000,
  "totalExpenses": 150000.0000,
  "netIncome": 50000.0000
}
```

### Balance Sheet

```http
GET /api/reports/balance-sheet?tenantId={tenantId}&asOfDate={date}
```

**Response**: `200 OK`
```json
{
  "tenantId": "default-tenant",
  "asOfDate": "2026-03-31",
  "assets": [...],
  "liabilities": [...],
  "equity": [...],
  "totalAssets": 500000.0000,
  "totalLiabilities": 200000.0000,
  "totalEquity": 300000.0000,
  "isBalanced": true
}
```

### Cash Flow Statement

```http
GET /api/reports/cash-flow?tenantId={tenantId}&startDate={date}&endDate={date}
```

**Response**: `200 OK` — Cash flow breakdown by operating, investing, and financing activities

---

## Ingestion APIs

**Controller**: `IngestionController`
**Base Path**: `/api/ingest`

### Ingest Financial Event

```http
POST /api/ingest/{adapterType}?tenantId={tenantId}
```

Submits a raw financial event payload for processing through the specified adapter.

**Path Parameters**:
- `adapterType`: `HL7`, `DMS`, `ISO20022`, or `WEBHOOK`

**Request Body**: Raw payload in the adapter's native format

**Response**: `200 OK` — Processed `FinancialEvent` object

### Three-Way Match

```http
POST /api/ingest/three-way-match?tenantId={tenantId}
```

Performs automated matching of Purchase Order, Goods Receipt, and Vendor Invoice.

**Request Body**:
```json
{
  "poNumber": "PO-2026-001",
  "grNumber": "GR-2026-001",
  "invoiceNumber": "INV-2026-001"
}
```

**Response**: `200 OK`
```json
{
  "matched": true,
  "poAmount": 10000.00,
  "grAmount": 10000.00,
  "invoiceAmount": 10000.00,
  "discrepancies": []
}
```

### OCR Invoice Extraction

```http
POST /api/ingest/ocr-invoice?tenantId={tenantId}
```

Extracts invoice data from uploaded documents using OCR.

**Response**: `200 OK` — Extracted invoice details with auto-drafted journal entry

---

## Forecast & AI APIs

**Controller**: `ForecastController`
**Base Path**: `/api/forecast`

### Cash Flow Forecast

```http
GET /api/forecast/cash-flow?tenantId={tenantId}&days={30|60|90}
```

Generates AI-driven cash flow predictions based on historical ledger data.

**Response**: `200 OK`
```json
{
  "tenantId": "default-tenant",
  "forecastDays": 30,
  "predictions": [
    { "date": "2026-04-01", "predictedInflow": 15000.00, "predictedOutflow": 12000.00 }
  ],
  "confidence": 0.87
}
```

### Scenario Modeling (What-If)

```http
POST /api/forecast/scenario?tenantId={tenantId}
```

Simulates financial impacts of hypothetical changes.

**Request Body**:
```json
{
  "scenarioName": "Sales drop 20%",
  "adjustments": [
    { "accountCode": "4000", "adjustmentPercent": -20 }
  ],
  "forecastDays": 90
}
```

**Response**: `200 OK` — Scenario results with projected P&L impact

### Anomaly Detection

**Controller**: `AnomalyController`
**Base Path**: `/api/anomalies`

```http
GET /api/anomalies/detect?tenantId={tenantId}
```

Scans recent transactions for anomalies (duplicate entries, unusual amounts, fraud indicators).

**Response**: `200 OK` — List of detected anomalies with severity ratings

---

## Asset Management APIs

### Fixed Assets

**Controller**: `FixedAssetController`
**Base Path**: `/api/fixed-assets`

#### List Fixed Assets

```http
GET /api/fixed-assets?tenantId={tenantId}
```

#### Create Fixed Asset

```http
POST /api/fixed-assets?tenantId={tenantId}
```

**Request Body**:
```json
{
  "assetCode": "FA-001",
  "assetName": "Office Building",
  "purchaseDate": "2025-01-15",
  "purchaseCost": 500000.00,
  "salvageValue": 50000.00,
  "usefulLifeMonths": 360,
  "depreciationMethod": "STRAIGHT_LINE",
  "assetAccountId": 1,
  "depreciationAccountId": 12,
  "branchId": 1
}
```

#### Compute Depreciation

```http
POST /api/fixed-assets/{id}/depreciate?tenantId={tenantId}
```

Calculates and posts depreciation entries for the asset.

### Digital Assets

**Controller**: `DigitalAssetController`
**Base Path**: `/api/digital-assets`

```http
GET /api/digital-assets?tenantId={tenantId}
POST /api/digital-assets?tenantId={tenantId}
```

Tracks cryptocurrency, stablecoins, tokens, and NFTs with mark-to-market valuations.

### Market & Investments

**Controller**: `MarketController`
**Base Path**: `/api/market`

```http
GET /api/market/holdings?tenantId={tenantId}
POST /api/market/holdings?tenantId={tenantId}
POST /api/market/valuate?tenantId={tenantId}
GET /api/market/sentiment?tenantId={tenantId}
```

Manages investment portfolios, MTM valuations, corporate actions, and market sentiment.

---

## Compliance APIs

**Controller**: `ComplianceController`
**Base Path**: `/api/compliance`

### Generate E-Invoice

```http
POST /api/compliance/e-invoice?tenantId={tenantId}
```

**Request Body**:
```json
{
  "invoiceNumber": "INV-2026-001",
  "invoiceDate": "2026-03-11",
  "buyerGstin": "29XXXXX1234X1Z5",
  "sellerGstin": "27XXXXX5678X1Z3",
  "totalAmount": 10000.00,
  "taxAmount": 1800.00,
  "journalTransactionId": 42
}
```

### Generate E-Way Bill

```http
POST /api/compliance/e-way-bill?tenantId={tenantId}
```

Generates an electronic waybill for goods transportation (required for Indian GST compliance).

---

## Reconciliation APIs

**Controller**: `ReconciliationController`
**Base Path**: `/api/reconciliation`

### Import Bank Feed

```http
POST /api/reconciliation/bank-feed?tenantId={tenantId}
```

Imports bank transactions from Open Banking APIs, CSV, or manual entry.

### Auto-Match Transactions

```http
POST /api/reconciliation/auto-match?tenantId={tenantId}
```

Automatically matches bank feed transactions to journal entries.

**Response**: `200 OK`
```json
{
  "totalTransactions": 150,
  "matched": 142,
  "unmatched": 8,
  "matchedItems": [...],
  "unmatchedItems": [...]
}
```

---

## Consolidation APIs

**Controller**: `ConsolidationController`
**Base Path**: `/api/consolidation`

### Consolidated Report

```http
GET /api/consolidation/report?tenantId={tenantId}
```

Generates a consolidated financial report across all branches with intercompany eliminations.

### Intercompany Eliminations

```http
POST /api/consolidation/eliminate?tenantId={tenantId}
```

Identifies and eliminates intercompany transactions for accurate consolidated reporting.

---

## Configuration APIs

### Tenant Locale

**Controller**: `TenantLocaleController`
**Base Path**: `/api/tenant-locale`

```http
GET /api/tenant-locale?tenantId={tenantId}
PUT /api/tenant-locale?tenantId={tenantId}
```

Manages tenant-specific locale settings (country, currency, tax regime, fiscal year).

### Feature Entitlements

**Controller**: `FeatureEntitlementController`
**Base Path**: `/api/entitlements`

```http
GET /api/entitlements?tenantId={tenantId}
PUT /api/entitlements/{featureCode}?tenantId={tenantId}
```

Toggles feature availability per tenant (e.g., MTM_VALUATION, EWAY_BILL).

---

## Cache Management APIs

**Controller**: `WarmCacheController`
**Base Path**: `/api/cache`

### Warm Cache

```http
POST /api/cache/warm/{tenantId}
```

Triggers cache warm-up: decrypts the tenant's Chart of Accounts and loads it into Redis.

### Cache Status

```http
GET /api/cache/status/{tenantId}
```

Returns the current cache warm status for the tenant.

**Response**: `200 OK`
```json
{
  "tenantId": "default-tenant",
  "isWarm": true,
  "cachedAccounts": 13,
  "lastWarmedAt": "2026-03-11T09:00:00"
}
```

### Evict Cache

```http
DELETE /api/cache/evict/{tenantId}
```

Evicts all cached data for the specified tenant from Redis.

---

## Health & Actuator

### Application Health

```http
GET /api/health
```

Custom health endpoint returning application status.

### Spring Actuator

```http
GET /actuator/health
GET /actuator/info
GET /actuator/metrics
```

Standard Spring Boot Actuator endpoints (configured in `application.yml`).

---

## Error Handling

All API errors are handled by the `GlobalExceptionHandler` (`@RestControllerAdvice`).

### Standard Error Response

```json
{
  "timestamp": "2026-03-11T09:37:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Transaction is not balanced: debits=5000.0000 credits=3000.0000",
  "path": "/api/journal/post"
}
```

### Common Error Codes

| Status | Meaning |
|--------|---------|
| `200` | Success |
| `400` | Bad Request (validation failure, unbalanced transaction) |
| `404` | Not Found (account, transaction, or asset not found) |
| `409` | Conflict (duplicate account code, duplicate entry) |
| `500` | Internal Server Error |

---

## Related Documentation

- [Architecture Diagram](architecture-diagram.md)
- [Key-Binding Registry Design](key-binding-registry.md)
- [SQL Schema Documentation](sql-schema.md)
- [Developer Onboarding Guide](developer-guide.md)
- [Operational Runbook](operational-runbook.md)
