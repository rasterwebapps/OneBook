# Data Dictionary
## OneBook — Nexus Universal Accounting OS

> **Complete data model documentation for all major entities.**  
> Last Updated: 2026-03-18 | Owner: @Architect | Status: CURRENT

---

## Table of Contents

1. [Core Accounting Entities](#1-core-accounting-entities)
2. [Security & Audit Entities](#2-security--audit-entities)
3. [Ingestion Entities](#3-ingestion-entities)
4. [Compliance & Fixed Asset Entities](#4-compliance--fixed-asset-entities)
5. [Organizational Hierarchy Entities](#5-organizational-hierarchy-entities)
6. [AI & Intelligence Entities](#6-ai--intelligence-entities)
7. [Entity Relationship Summary](#7-entity-relationship-summary)

---

## 1. Core Accounting Entities

### 1.1 `tenant_config`
Stores configuration for each tenant in the multi-tenant system.

| Column | Type | Nullable | Default | Encrypted | Description |
|--------|------|----------|---------|-----------|-------------|
| `id` | UUID | No | gen_random_uuid() | No | Primary key |
| `tenant_id` | UUID | No | — | No | Unique tenant identifier |
| `business_name` | VARCHAR(255) | No | — | No | Company display name |
| `locale` | VARCHAR(20) | No | 'en-IN' | No | Default locale (e.g., en-IN, hi-IN) |
| `currency_code` | VARCHAR(3) | No | 'INR' | No | ISO 4217 currency code |
| `fiscal_year_start` | INTEGER | No | 4 | No | Month number (4 = April for Indian fiscal year) |
| `encryption_key_id` | VARCHAR(100) | Yes | — | No | Reference to active encryption key |
| `created_at` | TIMESTAMP | No | NOW() | No | Record creation time |

**RLS:** Not tenant-scoped (tenant config is the root table).  
**Indexes:** `UNIQUE(tenant_id)`  
**Migration:** `V1__rls_infrastructure.sql`

---

### 1.2 `ledger_accounts`
The chart of accounts — all ledger accounts for all tenants.

| Column | Type | Nullable | Default | Encrypted | Description |
|--------|------|----------|---------|-----------|-------------|
| `id` | UUID | No | gen_random_uuid() | No | Primary key |
| `tenant_id` | UUID | No | — | No | Owning tenant |
| `account_code` | VARCHAR(50) | No | — | No | Unique code within tenant |
| `account_name` | VARCHAR(255) | No | — | No | Display name |
| `account_name_encrypted` | TEXT | Yes | — | ✅ AES-256-GCM | Encrypted account name (for sensitive accounts) |
| `account_name_idx` | VARCHAR(64) | Yes | — | No | HMAC-SHA256 blind index for name search |
| `account_type` | VARCHAR(20) | No | — | No | ASSET, LIABILITY, INCOME, EXPENSE, EQUITY |
| `parent_id` | UUID | Yes | — | No | Parent account for hierarchy |
| `opening_balance` | DECIMAL(19,4) | No | 0 | No | Opening balance (BigDecimal precision) |
| `currency_code` | VARCHAR(3) | No | 'INR' | No | Account currency |
| `is_active` | BOOLEAN | No | TRUE | No | Soft-delete flag |
| `created_at` | TIMESTAMP | No | NOW() | No | Creation time |

**RLS:** Yes — `tenant_id = current_setting('app.current_tenant_id')::uuid`  
**Indexes:** `UNIQUE(tenant_id, account_code)`, `INDEX(parent_id)`, `INDEX(account_name_idx)`  
**Relationships:** Self-referencing (parent_id); referenced by `journal_entries.account_id`  
**Migration:** `V3__ledger_and_journal.sql`, `V5__blind_dba_infrastructure.sql`

---

### 1.3 `journal_transactions`
Journal voucher header — one per financial transaction.

| Column | Type | Nullable | Default | Encrypted | Description |
|--------|------|----------|---------|-----------|-------------|
| `id` | UUID | No | gen_random_uuid() | No | Primary key |
| `tenant_id` | UUID | No | — | No | Owning tenant |
| `voucher_type` | VARCHAR(30) | No | — | No | PAYMENT, RECEIPT, JOURNAL, CONTRA, SALES, PURCHASE, etc. |
| `voucher_number` | VARCHAR(50) | Yes | — | No | Auto-generated sequential number |
| `transaction_date` | DATE | No | — | No | Date of transaction |
| `narration_encrypted` | TEXT | Yes | — | ✅ AES-256-GCM | Encrypted narration/description |
| `narration_idx` | VARCHAR(64) | Yes | — | No | Blind index for narration search |
| `reference_number` | VARCHAR(100) | Yes | — | No | External reference (cheque no., invoice no.) |
| `status` | VARCHAR(20) | No | 'POSTED' | No | DRAFT, PENDING_CHECK, POSTED, REVERSED |
| `currency_code` | VARCHAR(3) | No | 'INR' | No | Transaction currency |
| `exchange_rate` | DECIMAL(15,6) | Yes | 1.0 | No | Exchange rate to base currency |
| `reversed_by` | UUID | Yes | — | No | ID of reversing transaction |
| `workflow_id` | UUID | Yes | — | No | Associated workflow (if applicable) |
| `created_at` | TIMESTAMP | No | NOW() | No | Creation time |

**RLS:** Yes  
**Indexes:** `INDEX(tenant_id, transaction_date)`, `INDEX(voucher_number)`, `INDEX(status)`  
**Relationships:** One-to-many with `journal_entries`  
**Migration:** `V3__ledger_and_journal.sql`

---

### 1.4 `journal_entries`
Journal entry lines — debits and credits within a transaction. Every transaction must have balanced debits and credits.

| Column | Type | Nullable | Default | Encrypted | Description |
|--------|------|----------|---------|-----------|-------------|
| `id` | UUID | No | gen_random_uuid() | No | Primary key |
| `transaction_id` | UUID | No | — | No | FK → journal_transactions.id |
| `account_id` | UUID | No | — | No | FK → ledger_accounts.id |
| `entry_type` | VARCHAR(6) | No | — | No | DEBIT or CREDIT (CHECK constraint) |
| `amount` | DECIMAL(19,4) | No | — | No | Positive amount (CHECK amount > 0) |
| `cost_center_id` | UUID | Yes | — | No | FK → cost_centers.id |
| `notes_encrypted` | TEXT | Yes | — | ✅ AES-256-GCM | Encrypted line-level notes |
| `created_at` | TIMESTAMP | No | NOW() | No | Creation time |

**RLS:** Inherits tenant isolation via join to journal_transactions  
**Indexes:** `INDEX(transaction_id)`, `INDEX(account_id)`  
**Constraints:** `CHECK(entry_type IN ('DEBIT','CREDIT'))`, `CHECK(amount > 0)`  
**Migration:** `V3__ledger_and_journal.sql`

---

### 1.5 `voucher_types`
Custom voucher type configurations per tenant.

| Column | Type | Nullable | Default | Encrypted | Description |
|--------|------|----------|---------|-----------|-------------|
| `id` | UUID | No | gen_random_uuid() | No | Primary key |
| `tenant_id` | UUID | No | — | No | Owning tenant |
| `type_code` | VARCHAR(30) | No | — | No | Internal code (PAYMENT, RECEIPT, etc.) |
| `display_name` | VARCHAR(100) | No | — | No | UI display name |
| `is_system` | BOOLEAN | No | FALSE | No | True for built-in types |
| `keyboard_shortcut` | VARCHAR(10) | Yes | — | No | Tally-compatible shortcut key |
| `created_at` | TIMESTAMP | No | NOW() | No | Creation time |

**Migration:** `V3__ledger_and_journal.sql`

---

## 2. Security & Audit Entities

### 2.1 `audit_logs`
Hash-chained audit trail for all state-changing operations.

| Column | Type | Nullable | Default | Encrypted | Description |
|--------|------|----------|---------|-----------|-------------|
| `id` | UUID | No | gen_random_uuid() | No | Primary key |
| `tenant_id` | UUID | No | — | No | Owning tenant |
| `entity_type` | VARCHAR(100) | No | — | No | Entity class name (JournalTransaction, LedgerAccount) |
| `entity_id` | UUID | No | — | No | ID of affected entity |
| `action` | VARCHAR(50) | No | — | No | CREATE, UPDATE, DELETE, APPROVE, REJECT, POST |
| `actor_id` | VARCHAR(255) | Yes | — | No | User who performed the action |
| `payload` | JSONB | Yes | — | No | Before/after state as JSON |
| `previous_hash` | VARCHAR(64) | Yes | — | No | SHA-256 hash of previous entry |
| `hash` | VARCHAR(64) | No | — | No | SHA-256 hash of this entry (chain link) |
| `created_at` | TIMESTAMP | No | NOW() | No | Immutable creation time |

**RLS:** Yes  
**Immutability:** No UPDATE or DELETE allowed on this table  
**Migration:** `V5__blind_dba_infrastructure.sql`

---

### 2.2 `encryption_key_registry`
Tracks encryption key versions for key rotation support.

| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id` | UUID | No | gen_random_uuid() | Primary key |
| `key_version` | SMALLINT | No | — | UNIQUE version byte used in wire format |
| `key_reference` | VARCHAR(100) | No | — | Environment variable name holding the key |
| `is_current` | BOOLEAN | No | FALSE | Marks the active encryption key |
| `created_at` | TIMESTAMP | No | NOW() | Key introduction date |

**Migration:** `V5__blind_dba_infrastructure.sql`

---

## 3. Ingestion Entities

### 3.1 `financial_events`
External financial events received through the ingestion gateway.

| Column | Type | Nullable | Default | Encrypted | Description |
|--------|------|----------|---------|-----------|-------------|
| `id` | UUID | No | gen_random_uuid() | No | Primary key |
| `tenant_id` | UUID | No | — | No | Owning tenant |
| `adapter_type` | VARCHAR(50) | No | — | No | HL7, DMS, ISO20022, WEBHOOK, OCR_INVOICE, CORPORATE_CARD |
| `application_name` | VARCHAR(100) | No | — | No | Source system identifier |
| `external_reference_id` | VARCHAR(255) | Yes | — | No | Source system transaction ID |
| `payload` | TEXT | Yes | — | No | Raw event payload (JSON/XML/HL7) |
| `status` | VARCHAR(20) | No | 'RECEIVED' | No | RECEIVED, VALIDATED, MAPPED, POSTED, FAILED |
| `journal_transaction_id` | UUID | Yes | — | No | FK to posted journal transaction |
| `error_details` | TEXT | Yes | — | No | Error message if FAILED |
| `retry_count` | INTEGER | No | 0 | No | Number of retry attempts |
| `created_at` | TIMESTAMP | No | NOW() | No | Receipt time |
| `processed_at` | TIMESTAMP | Yes | — | No | Processing completion time |

**RLS:** Yes  
**Migration:** `V6__ingestion_layer.sql`

---

## 4. Compliance & Fixed Asset Entities

### 4.1 `fixed_assets`
Fixed Asset Register — capital assets and depreciation tracking.

| Column | Type | Nullable | Default | Encrypted | Description |
|--------|------|----------|---------|-----------|-------------|
| `id` | UUID | No | gen_random_uuid() | No | Primary key |
| `tenant_id` | UUID | No | — | No | Owning tenant |
| `asset_name` | VARCHAR(255) | No | — | No | Asset description |
| `asset_code` | VARCHAR(50) | No | — | No | Unique asset code per tenant |
| `asset_category` | VARCHAR(100) | Yes | — | No | Plant & Machinery, Furniture, etc. |
| `purchase_date` | DATE | No | — | No | Acquisition date |
| `cost` | DECIMAL(19,4) | No | — | No | Original cost (BigDecimal) |
| `residual_value` | DECIMAL(19,4) | No | 0 | No | Scrap value at end of useful life |
| `useful_life_years` | INTEGER | Yes | — | No | For SLM computation |
| `depreciation_method` | VARCHAR(10) | No | — | No | SLM or WDV |
| `depreciation_rate` | DECIMAL(5,4) | Yes | — | No | Rate for WDV method |
| `accumulated_depreciation` | DECIMAL(19,4) | No | 0 | No | Total depreciation charged to date |
| `disposal_date` | DATE | Yes | — | No | Asset disposal date |
| `disposal_amount` | DECIMAL(19,4) | Yes | — | No | Sale/scrap proceeds |
| `is_active` | BOOLEAN | No | TRUE | No | False after disposal |
| `created_at` | TIMESTAMP | No | NOW() | No | Record creation |

**Computed:** `book_value = cost - accumulated_depreciation`  
**RLS:** Yes  
**Migration:** `V7__reporting_compliance_far.sql`

---

### 4.2 `tds_deductions`
TDS deduction register — tracks all Tax Deducted at Source.

| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id` | UUID | No | gen_random_uuid() | Primary key |
| `tenant_id` | UUID | No | — | Owning tenant |
| `journal_transaction_id` | UUID | No | — | FK to the payment voucher |
| `deductee_pan` | VARCHAR(10) | Yes | — | Payee PAN number |
| `section_code` | VARCHAR(10) | No | — | TDS section (194A, 194C, etc.) |
| `payment_amount` | DECIMAL(19,4) | No | — | Gross payment before TDS |
| `tds_rate` | DECIMAL(5,4) | No | — | Applied TDS rate |
| `tds_amount` | DECIMAL(19,4) | No | — | TDS deducted |
| `deduction_date` | DATE | No | — | Date of deduction |
| `challan_number` | VARCHAR(50) | Yes | — | BSR/challan reference for deposit |
| `deposit_date` | DATE | Yes | — | Date TDS deposited to govt |

**RLS:** Yes  
**Migration:** `V7__reporting_compliance_far.sql`

---

### 4.3 `bank_feed_transactions`
Bank statement transactions imported for reconciliation.

| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id` | UUID | No | gen_random_uuid() | Primary key |
| `tenant_id` | UUID | No | — | Owning tenant |
| `bank_account_id` | UUID | No | — | FK to ledger_accounts (bank account) |
| `transaction_date` | DATE | No | — | Bank statement date |
| `value_date` | DATE | Yes | — | Value date (for interest computation) |
| `amount` | DECIMAL(19,4) | No | — | Transaction amount |
| `transaction_type` | VARCHAR(6) | No | — | DEBIT or CREDIT (from bank's perspective) |
| `description` | TEXT | Yes | — | Bank-provided description |
| `reference` | VARCHAR(100) | Yes | — | Cheque/UTR/transaction reference |
| `reconciliation_status` | VARCHAR(20) | No | 'UNMATCHED' | UNMATCHED, MATCHED, MANUAL |
| `matched_journal_entry_id` | UUID | Yes | — | FK to matched journal entry |
| `imported_at` | TIMESTAMP | No | NOW() | Import time |

**RLS:** Yes  
**Migration:** `V7__reporting_compliance_far.sql`

---

## 5. Organizational Hierarchy Entities

### 5.1 `companies`
Legal entity within a tenant group.

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| `id` | UUID | No | Primary key |
| `tenant_id` | UUID | No | Owning tenant |
| `company_name` | VARCHAR(255) | No | Legal company name |
| `parent_company_id` | UUID | Yes | FK self-reference for subsidiaries |
| `gstin` | VARCHAR(15) | Yes | GST Identification Number |
| `pan` | VARCHAR(10) | Yes | Company PAN |
| `created_at` | TIMESTAMP | No | Creation time |

---

### 5.2 `branches`
Operating location within a company.

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| `id` | UUID | No | Primary key |
| `tenant_id` | UUID | No | Owning tenant |
| `company_id` | UUID | No | FK → companies.id |
| `branch_name` | VARCHAR(255) | No | Branch display name |
| `branch_code` | VARCHAR(20) | Yes | Short code |
| `created_at` | TIMESTAMP | No | Creation time |

---

### 5.3 `cost_centers`
Departmental or project-level tracking unit.

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| `id` | UUID | No | Primary key |
| `tenant_id` | UUID | No | Owning tenant |
| `branch_id` | UUID | Yes | FK → branches.id |
| `cost_center_name` | VARCHAR(255) | No | Display name |
| `cost_center_code` | VARCHAR(20) | Yes | Short code |
| `parent_cost_center_id` | UUID | Yes | Hierarchical nesting |
| `created_at` | TIMESTAMP | No | Creation time |

---

## 6. AI & Intelligence Entities

### 6.1 `forecast_models`
AI cash flow forecast results (V8 migration).

| Column | Type | Description |
|--------|------|-------------|
| `id` | UUID | Primary key |
| `tenant_id` | UUID | Owning tenant |
| `horizon_days` | INTEGER | Forecast horizon (30/60/90/180) |
| `forecast_data` | JSONB | Projected cash flows by period |
| `confidence_level` | DECIMAL(5,4) | Model confidence |
| `generated_at` | TIMESTAMP | Forecast generation time |

---

### 6.2 `anomaly_alerts`
AI-detected anomalous transactions.

| Column | Type | Description |
|--------|------|-------------|
| `id` | UUID | Primary key |
| `tenant_id` | UUID | Owning tenant |
| `journal_transaction_id` | UUID | Flagged transaction |
| `anomaly_score` | DECIMAL(5,4) | 0.0–1.0 severity score |
| `anomaly_type` | VARCHAR(50) | AMOUNT, FREQUENCY, COUNTERPARTY, TIMING |
| `explanation` | TEXT | Human-readable explanation |
| `is_dismissed` | BOOLEAN | False positive flag |
| `created_at` | TIMESTAMP | Detection time |

---

## 7. Entity Relationship Summary

```
tenant_config (1)
  └── (N) companies
        └── (N) branches
              └── (N) cost_centers

tenant_config (1)
  └── (N) ledger_accounts (hierarchical tree)

ledger_accounts (1)
  └── (N) journal_entries
        └── (N to 1) journal_transactions
              └── (1) audit_logs (hash chain)
              └── (1) financial_events (if ingested)
              └── (1) tds_deductions (if TDS applied)
              └── (1) audit_workflows (if maker-checker)

ledger_accounts (1)
  └── (N) bank_feed_transactions (bank account link)

tenant_config (1)
  └── (N) fixed_assets
  └── (N) forecast_models
  └── (N) anomaly_alerts
```

---

## Encryption Coverage Summary

| Table | Encrypted Columns | Blind Index Columns |
|-------|------------------|-------------------|
| `journal_transactions` | `narration_encrypted` | `narration_idx` |
| `journal_entries` | `notes_encrypted` | — |
| `ledger_accounts` | `account_name_encrypted` | `account_name_idx` |
| `financial_events` | — (payload encrypted at transport) | — |

**Encryption algorithm:** AES-256-GCM  
**Key management:** Environment variables only (`ONEBOOK_ENCRYPTION_KEY`)  
**Blind index algorithm:** HMAC-SHA256 (`ONEBOOK_BLIND_INDEX_KEY`)

---

*This document describes the data model as of schema version V10 (all migrations applied).*
