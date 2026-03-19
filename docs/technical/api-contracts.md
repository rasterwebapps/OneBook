# API Contracts
## OneBook — Nexus Universal Accounting OS

> **Complete API specification for all endpoints.**  
> Base URL: `http://localhost:8080` (local) | `https://api.onebook.example.com` (production)  
> Authentication: `Authorization: Bearer <JWT>` required on all endpoints.  
> Last Updated: 2026-03-18 | Owner: @Architect

---

## Table of Contents

1. [Ledger API](#1-ledger-api)
2. [Journal API](#2-journal-api)
3. [Reports API](#3-reports-api)
4. [Ingestion API](#4-ingestion-api)
5. [Compliance API](#5-compliance-api)
6. [Fixed Assets API](#6-fixed-assets-api)
7. [Reconciliation API](#7-reconciliation-api)
8. [Export API](#8-export-api)
9. [AI Intelligence API](#9-ai-intelligence-api)
10. [Auditor Portal API](#10-auditor-portal-api)
11. [Common Response Formats](#11-common-response-formats)

---

## 1. Ledger API

Base path: `/api/ledger`

### POST /api/ledger/accounts
Create a new ledger account.

**Request Body:**
```json
{
  "accountCode": "1001",
  "accountName": "Cash in Hand",
  "accountType": "ASSET",
  "parentId": "550e8400-e29b-41d4-a716-446655440000",
  "openingBalance": "0.00",
  "currencyCode": "INR"
}
```

**Response 201:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "tenantId": "acme-tenant-uuid",
  "accountCode": "1001",
  "accountName": "Cash in Hand",
  "accountType": "ASSET",
  "parentId": "550e8400-e29b-41d4-a716-446655440000",
  "openingBalance": "0.00",
  "currentBalance": "0.00",
  "currencyCode": "INR",
  "isActive": true,
  "createdAt": "2026-01-15T10:30:00Z"
}
```

**Status Codes:** 201 Created | 400 Bad Request (validation) | 409 Conflict (duplicate code) | 401 Unauthorized

---

### GET /api/ledger/accounts
List all accounts for the current tenant.

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `accountType` | String | Filter by ASSET, LIABILITY, INCOME, EXPENSE, EQUITY |
| `isActive` | Boolean | Filter by active status (default: true) |
| `page` | Integer | Page number (default: 0) |
| `size` | Integer | Page size (default: 50, max: 200) |

**Response 200:**
```json
{
  "content": [
    {
      "id": "uuid",
      "accountCode": "1001",
      "accountName": "Cash in Hand",
      "accountType": "ASSET",
      "currentBalance": "50000.00"
    }
  ],
  "totalElements": 150,
  "totalPages": 3,
  "page": 0,
  "size": 50
}
```

---

### GET /api/ledger/accounts/tree
Get hierarchical account tree.

**Response 200:**
```json
{
  "accounts": [
    {
      "id": "uuid",
      "accountCode": "1000",
      "accountName": "Assets",
      "accountType": "ASSET",
      "children": [
        {
          "id": "uuid",
          "accountCode": "1001",
          "accountName": "Cash in Hand",
          "children": []
        }
      ]
    }
  ]
}
```

---

### GET /api/ledger/accounts/search
Search accounts using blind index (equality search on name).

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `q` | String | Yes | Search term (matched via blind index) |
| `accountType` | String | No | Filter by type |

**Response 200:** Same as list response.

---

### PUT /api/ledger/accounts/{id}
Update a ledger account.

**Request Body:** Same fields as POST (accountCode excluded from update).  
**Response 200:** Updated account object.  
**Status Codes:** 200 | 404 Not Found | 409 Conflict

---

## 2. Journal API

Base path: `/api/journal`

### POST /api/journal/transactions
Post a new journal voucher.

**Request Body:**
```json
{
  "voucherType": "PAYMENT",
  "transactionDate": "2026-01-15",
  "narration": "Office supplies payment",
  "referenceNumber": "CHQ-001",
  "currencyCode": "INR",
  "entries": [
    {
      "accountId": "expense-account-uuid",
      "entryType": "DEBIT",
      "amount": "5000.00",
      "costCenterId": "marketing-cc-uuid"
    },
    {
      "accountId": "cash-account-uuid",
      "entryType": "CREDIT",
      "amount": "5000.00"
    }
  ]
}
```

**Response 201:**
```json
{
  "id": "transaction-uuid",
  "voucherType": "PAYMENT",
  "voucherNumber": "PMT-2026-00001",
  "transactionDate": "2026-01-15",
  "narration": "Office supplies payment",
  "status": "POSTED",
  "entries": [
    {
      "id": "entry-uuid",
      "accountId": "expense-account-uuid",
      "accountName": "Office Expenses",
      "entryType": "DEBIT",
      "amount": "5000.00"
    },
    {
      "id": "entry-uuid-2",
      "accountId": "cash-account-uuid",
      "accountName": "Cash in Hand",
      "entryType": "CREDIT",
      "amount": "5000.00"
    }
  ],
  "createdAt": "2026-01-15T11:00:00Z"
}
```

**Status Codes:** 201 | 400 Bad Request | 422 Unprocessable Entity (unbalanced)

**Error Response (422):**
```json
{
  "error": "UnbalancedTransaction",
  "message": "Journal entries are not balanced",
  "totalDebits": "5000.00",
  "totalCredits": "4999.00",
  "difference": "1.00"
}
```

---

### GET /api/journal/transactions
List transactions with pagination and filtering.

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `from` | Date (YYYY-MM-DD) | Start date |
| `to` | Date (YYYY-MM-DD) | End date |
| `voucherType` | String | Filter by voucher type |
| `accountId` | UUID | Filter by account involvement |
| `status` | String | POSTED, DRAFT, PENDING_CHECK, REVERSED |
| `page` | Integer | Page number |
| `size` | Integer | Page size (max 100) |

---

### POST /api/journal/transactions/{id}/reverse
Reverse a posted transaction.

**Request Body:**
```json
{
  "reversalDate": "2026-01-20",
  "reversalNarration": "Reversal of PMT-2026-00001 — wrong account"
}
```

**Response 201:** New reversal transaction object.  
**Status Codes:** 201 | 404 | 409 (already reversed)

---

### GET /api/journal/voucher-types
List all available voucher types for the tenant.

**Response 200:**
```json
{
  "voucherTypes": [
    {"code": "PAYMENT", "displayName": "Payment", "shortcut": "F5"},
    {"code": "RECEIPT", "displayName": "Receipt", "shortcut": "F6"},
    {"code": "JOURNAL", "displayName": "Journal", "shortcut": "F7"},
    {"code": "CONTRA", "displayName": "Contra", "shortcut": "F4"},
    {"code": "SALES", "displayName": "Sales", "shortcut": "F8"},
    {"code": "PURCHASE", "displayName": "Purchase", "shortcut": "F9"}
  ]
}
```

---

## 3. Reports API

Base path: `/api/reports`

### GET /api/reports/trial-balance
Generate Trial Balance for a date range.

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `from` | Date | Yes | Start date |
| `to` | Date | Yes | End date |
| `costCenterId` | UUID | No | Filter by cost center |
| `includeZeroBalance` | Boolean | No | Include zero-balance accounts (default: false) |

**Response 200:**
```json
{
  "from": "2026-01-01",
  "to": "2026-03-31",
  "generatedAt": "2026-04-01T09:00:00Z",
  "cachedResult": false,
  "rows": [
    {
      "accountCode": "1001",
      "accountName": "Cash in Hand",
      "accountType": "ASSET",
      "openingDebit": "100000.00",
      "openingCredit": "0.00",
      "periodDebit": "50000.00",
      "periodCredit": "30000.00",
      "closingDebit": "120000.00",
      "closingCredit": "0.00"
    }
  ],
  "totals": {
    "totalOpeningDebit": "500000.00",
    "totalOpeningCredit": "500000.00",
    "totalPeriodDebit": "200000.00",
    "totalPeriodCredit": "200000.00",
    "totalClosingDebit": "700000.00",
    "totalClosingCredit": "700000.00"
  }
}
```

---

### GET /api/reports/profit-loss
Generate Profit & Loss statement.

**Query Parameters:** `from`, `to`, `comparative` (Boolean), `costCenterId`

**Response 200:**
```json
{
  "period": {"from": "2026-01-01", "to": "2026-12-31"},
  "revenue": {"gross": "1000000.00", "breakdown": []},
  "costOfGoods": "400000.00",
  "grossProfit": "600000.00",
  "operatingExpenses": {"total": "300000.00", "breakdown": []},
  "ebit": "300000.00",
  "otherIncome": "10000.00",
  "interest": "20000.00",
  "pbt": "290000.00",
  "tax": "58000.00",
  "netProfit": "232000.00"
}
```

---

### GET /api/reports/balance-sheet
Generate Balance Sheet as of a specific date.

**Query Parameters:** `asOf` (Date, required), `comparative` (Boolean)

**Response 200:**
```json
{
  "asOf": "2026-03-31",
  "assets": {
    "currentAssets": {"total": "500000.00", "accounts": []},
    "nonCurrentAssets": {"total": "800000.00", "accounts": []},
    "totalAssets": "1300000.00"
  },
  "liabilities": {
    "currentLiabilities": {"total": "200000.00", "accounts": []},
    "nonCurrentLiabilities": {"total": "300000.00", "accounts": []},
    "totalLiabilities": "500000.00"
  },
  "equity": {"total": "800000.00", "accounts": []},
  "checksum": "0.00"
}
```

---

### GET /api/reports/cash-flow
Generate Cash Flow Statement.

**Query Parameters:** `from`, `to`, `method` (DIRECT | INDIRECT, default: INDIRECT)

---

## 4. Ingestion API

Base path: `/api/ingestion`

### POST /api/ingestion/events
Submit a financial event for processing.

**Request Body:**
```json
{
  "adapterType": "HL7",
  "applicationName": "HospitalMS-v3",
  "externalReferenceId": "BILL-2026-789456",
  "payload": "<HL7-encoded-payload-here>"
}
```

**Response 202 Accepted:**
```json
{
  "id": "event-uuid",
  "adapterType": "HL7",
  "status": "RECEIVED",
  "message": "Event received. Processing asynchronously.",
  "pollUrl": "/api/ingestion/events/event-uuid"
}
```

---

### GET /api/ingestion/events/{id}
Get ingestion event status.

**Response 200:**
```json
{
  "id": "event-uuid",
  "adapterType": "HL7",
  "applicationName": "HospitalMS-v3",
  "status": "POSTED",
  "journalTransactionId": "txn-uuid",
  "retryCount": 0,
  "createdAt": "2026-01-15T10:00:00Z",
  "processedAt": "2026-01-15T10:00:05Z"
}
```

---

### GET /api/ingestion/adapters
List all registered adapters.

**Response 200:**
```json
{
  "adapters": [
    {"type": "HL7", "description": "Healthcare HL7 v2.x/FHIR"},
    {"type": "DMS", "description": "Document Management System"},
    {"type": "ISO20022", "description": "Banking ISO20022 messages"},
    {"type": "WEBHOOK", "description": "Generic webhook payloads"},
    {"type": "OCR_INVOICE", "description": "Scanned invoice OCR"},
    {"type": "CORPORATE_CARD", "description": "Corporate card feeds"}
  ]
}
```

---

## 5. Compliance API

Base path: `/api/compliance`

### POST /api/compliance/tds/compute
Compute TDS on a payment.

**Request Body:**
```json
{
  "paymentAmount": "50000.00",
  "sectionCode": "194J",
  "deducteePan": "ABCDE1234F"
}
```

**Response 200:**
```json
{
  "paymentAmount": "50000.00",
  "sectionCode": "194J",
  "sectionDescription": "Professional/Technical Services",
  "tdsRate": "0.1000",
  "tdsAmount": "5000.00",
  "netPayableAmount": "45000.00",
  "aboveThreshold": true
}
```

---

### POST /api/compliance/einvoice/generate
Generate e-Invoice IRN.

**Request Body:**
```json
{
  "buyerGstin": "29ABCDE1234F1ZS",
  "invoiceNumber": "INV-2026-001",
  "invoiceDate": "2026-01-15",
  "invoiceValue": "600000.00",
  "items": [
    {"description": "Software Services", "hsnCode": "998314", "qty": 1, "rate": "600000.00"}
  ]
}
```

**Response 200:**
```json
{
  "irn": "a1b2c3d4e5f6...",
  "ackNo": "232612345678901",
  "ackDate": "2026-01-15T10:00:00",
  "signedQrCode": "base64-encoded-qr-code",
  "signedInvoice": "base64-encoded-signed-json"
}
```

---

### GET /api/compliance/tds/26q
Export Form 26Q data for a quarter.

**Query Parameters:** `quarter` (Q1/Q2/Q3/Q4), `year` (e.g., 2026)  
**Response:** JSON data in Form 26Q format.

---

## 6. Fixed Assets API

Base path: `/api/fixed-assets`

### POST /api/fixed-assets
Register a new fixed asset.

**Request Body:**
```json
{
  "assetName": "Office Computer",
  "assetCode": "COMP-001",
  "assetCategory": "Computers & IT Equipment",
  "purchaseDate": "2026-01-01",
  "cost": "100000.00",
  "residualValue": "10000.00",
  "usefulLifeYears": 5,
  "depreciationMethod": "SLM"
}
```

**Response 201:** Asset object with computed depreciation schedule.

---

### POST /api/fixed-assets/{id}/depreciate
Run period depreciation for an asset.

**Request Body:**
```json
{
  "periodDate": "2026-03-31",
  "autoPost": true
}
```

**Response 200:**
```json
{
  "assetId": "uuid",
  "periodDate": "2026-03-31",
  "depreciationAmount": "4500.00",
  "newBookValue": "95500.00",
  "journalTransactionId": "txn-uuid"
}
```

---

### POST /api/fixed-assets/{id}/dispose
Dispose or sell an asset.

**Request Body:**
```json
{
  "disposalDate": "2026-06-30",
  "salePrice": "45000.00",
  "disposalReason": "Sold - upgradation"
}
```

**Response 200:** Disposal details with gain/loss journal transaction.

---

## 7. Reconciliation API

Base path: `/api/reconciliation`

### POST /api/reconciliation/import
Import a bank statement file.

**Request:** `multipart/form-data`
- `file`: Bank statement file (CSV/OFX/MT940)
- `bankAccountId`: UUID of bank ledger account
- `periodFrom`: Date
- `periodTo`: Date

**Response 201:** Reconciliation session with imported transaction count.

---

### GET /api/reconciliation/sessions/{id}
Get reconciliation session with matched/unmatched items.

**Response 200:**
```json
{
  "id": "session-uuid",
  "bankAccountId": "account-uuid",
  "periodFrom": "2026-01-01",
  "periodTo": "2026-01-31",
  "status": "IN_PROGRESS",
  "totalBankTransactions": 50,
  "matchedCount": 42,
  "unmatchedBankCount": 5,
  "unmatchedBookCount": 3,
  "closingBookBalance": "500000.00",
  "closingBankBalance": "505000.00",
  "difference": "5000.00"
}
```

---

### POST /api/reconciliation/finalize
Lock a reconciliation session.

**Request Body:** `{"sessionId": "uuid", "notes": "Q1 reconciliation complete"}`  
**Response 200:** Finalized session with locked status.

---

## 8. Export API

Base path: `/api/export`

### GET /api/export/trial-balance
Export trial balance.

**Query Parameters:** `from`, `to`, `format` (pdf | xlsx)  
**Response:** File download (Content-Disposition: attachment)

---

### GET /api/export/transactions
Export journal transactions.

**Query Parameters:** `from`, `to`, `voucherType`, `format` (csv | xlsx | pdf)  
**Response:** File download.

---

### GET /api/export/gst/r1
Export GSTR-1 data.

**Query Parameters:** `period` (YYYY-MM, e.g., 2026-01)  
**Response:** JSON in GSTN-prescribed GSTR-1 format.

---

## 9. AI Intelligence API

Base path: `/api`

### GET /api/forecast/cashflow
Generate cash flow forecast.

**Query Parameters:** `horizon` (30 | 60 | 90 | 180, days)

**Response 200:**
```json
{
  "horizon": 90,
  "generatedAt": "2026-04-01T09:00:00Z",
  "projections": [
    {
      "date": "2026-04-07",
      "projectedInflow": "500000.00",
      "projectedOutflow": "300000.00",
      "netCashFlow": "200000.00",
      "confidenceBase": "200000.00",
      "confidenceOptimistic": "250000.00",
      "confidencePessimistic": "150000.00"
    }
  ],
  "summary": {
    "totalProjectedInflow": "15000000.00",
    "totalProjectedOutflow": "12000000.00",
    "netCashPosition": "3000000.00"
  }
}
```

---

### GET /api/anomaly/alerts
List active anomaly alerts.

**Response 200:**
```json
{
  "alerts": [
    {
      "id": "alert-uuid",
      "transactionId": "txn-uuid",
      "anomalyScore": 0.92,
      "anomalyType": "AMOUNT",
      "explanation": "Transaction amount ₹50,00,000 is 10x historical average",
      "createdAt": "2026-01-15T14:30:00Z",
      "isDismissed": false
    }
  ],
  "totalCount": 3
}
```

---

## 10. Auditor Portal API

Base path: `/api/audit`

### GET /api/audit/entries
List audit log entries.

**Query Parameters:** `entityType`, `entityId`, `from`, `to`, `page`, `size`

---

### GET /api/audit/verify/{entityId}
Verify hash chain integrity for an entity.

**Response 200:**
```json
{
  "entityId": "txn-uuid",
  "totalEntries": 5,
  "chainIntegrity": "VALID",
  "verifiedAt": "2026-04-01T10:00:00Z",
  "entries": [
    {
      "index": 0,
      "entryId": "audit-uuid",
      "action": "CREATE",
      "hashValid": true,
      "hash": "abc123..."
    }
  ]
}
```

**Response (tampered):**
```json
{
  "chainIntegrity": "TAMPERED",
  "tamperedAtIndex": 2,
  "message": "Hash mismatch detected at entry index 2"
}
```

---

## 11. Common Response Formats

### Error Response
```json
{
  "error": "ErrorType",
  "message": "Human-readable error description",
  "details": {},
  "timestamp": "2026-01-15T10:00:00Z",
  "path": "/api/journal/transactions"
}
```

### Standard HTTP Status Codes

| Code | Meaning | When Used |
|------|---------|----------|
| 200 | OK | Successful GET/PUT |
| 201 | Created | Successful POST that creates a resource |
| 202 | Accepted | Async operation initiated |
| 400 | Bad Request | Invalid request body/parameters |
| 401 | Unauthorized | Missing or invalid JWT |
| 403 | Forbidden | Valid JWT but insufficient permissions |
| 404 | Not Found | Resource doesn't exist or not visible to tenant |
| 409 | Conflict | Duplicate resource or state conflict |
| 422 | Unprocessable Entity | Business rule violation (e.g., unbalanced entry) |
| 500 | Internal Server Error | Unexpected server error |

### Pagination Response Wrapper
```json
{
  "content": [],
  "totalElements": 0,
  "totalPages": 0,
  "page": 0,
  "size": 50,
  "first": true,
  "last": true
}
```

---

*For detailed Swagger/OpenAPI spec, start the backend and navigate to `/swagger-ui.html`.*
