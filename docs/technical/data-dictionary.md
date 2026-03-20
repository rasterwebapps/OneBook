# Data Dictionary (Generated)
## OneBook — Nexus Universal Accounting OS

> **Auto-generated from REQ-*.md files SQL sections.**
> Generated: 2026-03-20 by `docs/automation/generate-data-dictionary.js`
> For the curated, human-authored version see `docs/technical/data-dictionary.md`

---

## Discovered Tables (21)

- [`tenant_config`](#tenant-config)
- [`ledger_accounts`](#ledger-accounts)
- [`encryption_key_registry`](#encryption-key-registry)
- [`audit_logs`](#audit-logs)
- [`journal_entries`](#journal-entries)
- [`ledger_accounts`](#ledger-accounts)
- [`financial_events`](#financial-events)
- [`journal_transactions`](#journal-transactions)
- [`journal_entries`](#journal-entries)
- [`companies`](#companies)
- [`branches`](#branches)
- [`cost_centers`](#cost-centers)
- [`fixed_assets`](#fixed-assets)
- [`tds_deductions`](#tds-deductions)
- [`bank_feed_transactions`](#bank-feed-transactions)
- [`reconciliation_sessions`](#reconciliation-sessions)
- [`audit_workflows`](#audit-workflows)
- [`audit_workflow_events`](#audit-workflow-events)
- [`payment_register`](#payment-register)
- [`payment_batches`](#payment-batches)
- [`payment_batch_items`](#payment-batch-items)

---

## `tenant_config`

**Source:** REQ-001 — Multi-Tenant Ledger | **Milestone:** M1/M2

| Column | Type | Nullable | Default | Encrypted |
|--------|------|----------|---------|----------|
| `id` | UUID | Yes | gen_random_uuid() | No |
| `tenant_id` | UUID | No | — | No |
| `business_name` | VARCHAR(255) | No | — | No |
| `locale` | VARCHAR(20) | Yes | 'en-IN' | No |
| `currency_code` | VARCHAR(3) | Yes | 'INR' | No |
| `fiscal_year_start` | INTEGER | Yes | 4 | No |
| `encryption_key_id` | VARCHAR(100) | Yes | — | No |
| `created_at` | TIMESTAMP | No | NOW() | No |

**SQL Definition:**
```sql
-- V1__rls_infrastructure.sql
CREATE TABLE tenant_config (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID UNIQUE NOT NULL,
    business_name VARCHAR(255) NOT NULL,
    locale VARCHAR(20) DEFAULT 'en-IN',
    currency_code VARCHAR(3) DEFAULT 'INR',
    fiscal_year_start INTEGER DEFAULT 4,
    encryption_key_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- V3__ledger_and_journal.sql
CREATE TABLE ledger_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    account_code VARCHAR(50) NOT NULL,
    account_name VARCHAR(255) NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    parent_id UUID REFERENCES ledger_accounts(id),
    opening_balance DECIMAL(19,4) DEFAULT 0,
    currency_code VARCHAR(3) DEFAULT 'INR',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, account_code)
);

ALTER TABLE ledger_accounts ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON ledger_accounts
    USING (tenant_id = current_setting('app.current_tenant_id')::uuid);
```

---

## `ledger_accounts`

**Source:** REQ-001 — Multi-Tenant Ledger | **Milestone:** M1/M2

| Column | Type | Nullable | Default | Encrypted |
|--------|------|----------|---------|----------|
| `id` | UUID | Yes | gen_random_uuid() | No |
| `tenant_id` | UUID | No | — | No |
| `business_name` | VARCHAR(255) | No | — | No |
| `locale` | VARCHAR(20) | Yes | 'en-IN' | No |
| `currency_code` | VARCHAR(3) | Yes | 'INR' | No |
| `fiscal_year_start` | INTEGER | Yes | 4 | No |
| `encryption_key_id` | VARCHAR(100) | Yes | — | No |
| `created_at` | TIMESTAMP | No | NOW() | No |

**SQL Definition:**
```sql
-- V1__rls_infrastructure.sql
CREATE TABLE tenant_config (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID UNIQUE NOT NULL,
    business_name VARCHAR(255) NOT NULL,
    locale VARCHAR(20) DEFAULT 'en-IN',
    currency_code VARCHAR(3) DEFAULT 'INR',
    fiscal_year_start INTEGER DEFAULT 4,
    encryption_key_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- V3__ledger_and_journal.sql
CREATE TABLE ledger_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    account_code VARCHAR(50) NOT NULL,
    account_name VARCHAR(255) NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    parent_id UUID REFERENCES ledger_accounts(id),
    opening_balance DECIMAL(19,4) DEFAULT 0,
    currency_code VARCHAR(3) DEFAULT 'INR',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, account_code)
);

ALTER TABLE ledger_accounts ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON ledger_accounts
    USING (tenant_id = current_setting('app.current_tenant_id')::uuid);
```

---

## `encryption_key_registry`

**Source:** REQ-002 — Zero-Knowledge Encryption | **Milestone:** M3

| Column | Type | Nullable | Default | Encrypted |
|--------|------|----------|---------|----------|
| `id` | UUID | Yes | gen_random_uuid() | No |
| `key_version` | SMALLINT | No | — | No |
| `key_reference` | VARCHAR(100) | No | — | No |
| `created_at` | TIMESTAMP | No | NOW() | No |

**SQL Definition:**
```sql
-- V5__blind_dba_infrastructure.sql
ALTER TABLE journal_entries ADD COLUMN narration_encrypted TEXT;
ALTER TABLE journal_entries ADD COLUMN narration_idx VARCHAR(64);

ALTER TABLE ledger_accounts ADD COLUMN account_name_encrypted TEXT;
ALTER TABLE ledger_accounts ADD COLUMN account_name_idx VARCHAR(64);

CREATE TABLE encryption_key_registry (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key_version SMALLINT UNIQUE NOT NULL,
    key_reference VARCHAR(100) NOT NULL,  -- env var name
    is_current BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    actor_id VARCHAR(255),
    payload JSONB,
    previous_hash VARCHAR(64),
    hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

---

## `audit_logs`

**Source:** REQ-002 — Zero-Knowledge Encryption | **Milestone:** M3

| Column | Type | Nullable | Default | Encrypted |
|--------|------|----------|---------|----------|
| `id` | UUID | Yes | gen_random_uuid() | No |
| `key_version` | SMALLINT | No | — | No |
| `key_reference` | VARCHAR(100) | No | — | No |
| `created_at` | TIMESTAMP | No | NOW() | No |

**SQL Definition:**
```sql
-- V5__blind_dba_infrastructure.sql
ALTER TABLE journal_entries ADD COLUMN narration_encrypted TEXT;
ALTER TABLE journal_entries ADD COLUMN narration_idx VARCHAR(64);

ALTER TABLE ledger_accounts ADD COLUMN account_name_encrypted TEXT;
ALTER TABLE ledger_accounts ADD COLUMN account_name_idx VARCHAR(64);

CREATE TABLE encryption_key_registry (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key_version SMALLINT UNIQUE NOT NULL,
    key_reference VARCHAR(100) NOT NULL,  -- env var name
    is_current BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    actor_id VARCHAR(255),
    payload JSONB,
    previous_hash VARCHAR(64),
    hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

---

## `journal_entries`

**Source:** REQ-002 — Zero-Knowledge Encryption | **Milestone:** M3

| Column | Type | Nullable | Default | Encrypted |
|--------|------|----------|---------|----------|
| `id` | UUID | Yes | gen_random_uuid() | No |
| `key_version` | SMALLINT | No | — | No |
| `key_reference` | VARCHAR(100) | No | — | No |
| `created_at` | TIMESTAMP | No | NOW() | No |

**SQL Definition:**
```sql
-- V5__blind_dba_infrastructure.sql
ALTER TABLE journal_entries ADD COLUMN narration_encrypted TEXT;
ALTER TABLE journal_entries ADD COLUMN narration_idx VARCHAR(64);

ALTER TABLE ledger_accounts ADD COLUMN account_name_encrypted TEXT;
ALTER TABLE ledger_accounts ADD COLUMN account_name_idx VARCHAR(64);

CREATE TABLE encryption_key_registry (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key_version SMALLINT UNIQUE NOT NULL,
    key_reference VARCHAR(100) NOT NULL,  -- env var name
    is_current BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    actor_id VARCHAR(255),
    payload JSONB,
    previous_hash VARCHAR(64),
    hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

---

## `ledger_accounts`

**Source:** REQ-002 — Zero-Knowledge Encryption | **Milestone:** M3

| Column | Type | Nullable | Default | Encrypted |
|--------|------|----------|---------|----------|
| `id` | UUID | Yes | gen_random_uuid() | No |
| `key_version` | SMALLINT | No | — | No |
| `key_reference` | VARCHAR(100) | No | — | No |
| `created_at` | TIMESTAMP | No | NOW() | No |

**SQL Definition:**
```sql
-- V5__blind_dba_infrastructure.sql
ALTER TABLE journal_entries ADD COLUMN narration_encrypted TEXT;
ALTER TABLE journal_entries ADD COLUMN narration_idx VARCHAR(64);

ALTER TABLE ledger_accounts ADD COLUMN account_name_encrypted TEXT;
ALTER TABLE ledger_accounts ADD COLUMN account_name_idx VARCHAR(64);

CREATE TABLE encryption_key_registry (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key_version SMALLINT UNIQUE NOT NULL,
    key_reference VARCHAR(100) NOT NULL,  -- env var name
    is_current BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    actor_id VARCHAR(255),
    payload JSONB,
    previous_hash VARCHAR(64),
    hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

---

## `financial_events`

**Source:** REQ-003 — External App Ingestion | **Milestone:** M6

| Column | Type | Nullable | Default | Encrypted |
|--------|------|----------|---------|----------|
| `id` | UUID | Yes | gen_random_uuid() | No |
| `tenant_id` | UUID | No | — | No |
| `adapter_type` | VARCHAR(50) | No | — | No |
| `application_name` | VARCHAR(100) | No | — | No |
| `external_reference_id` | VARCHAR(255) | Yes | — | No |
| `payload` | TEXT | Yes | — | No |
| `status` | VARCHAR(20) | No | 'RECEIVED' | No |
| `journal_transaction_id` | UUID | Yes | — | No |
| `error_details` | TEXT | Yes | — | No |
| `retry_count` | INTEGER | Yes | 0 | No |
| `created_at` | TIMESTAMP | No | NOW() | No |
| `processed_at` | TIMESTAMP | Yes | — | No |

**SQL Definition:**
```sql
-- V6__ingestion_layer.sql
CREATE TABLE financial_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    adapter_type VARCHAR(50) NOT NULL,
    application_name VARCHAR(100) NOT NULL,
    external_reference_id VARCHAR(255),
    payload TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'RECEIVED',
    journal_transaction_id UUID,
    error_details TEXT,
    retry_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMP
);

ALTER TABLE financial_events ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON financial_events
    USING (tenant_id = current_setting('app.current_tenant_id')::uuid);
```

---

## `journal_transactions`

**Source:** REQ-004 — Voucher Posting | **Milestone:** M2

| Column | Type | Nullable | Default | Encrypted |
|--------|------|----------|---------|----------|
| `id` | UUID | Yes | gen_random_uuid() | No |
| `tenant_id` | UUID | No | — | No |
| `voucher_type` | VARCHAR(30) | No | — | No |
| `voucher_number` | VARCHAR(50) | Yes | — | No |
| `transaction_date` | DATE | No | — | No |
| `narration_encrypted` | TEXT | Yes | — | ✅ AES-256-GCM |
| `reference_number` | VARCHAR(100) | Yes | — | No |
| `status` | VARCHAR(20) | No | 'POSTED' | No |
| `currency_code` | VARCHAR(3) | Yes | 'INR' | No |
| `created_at` | TIMESTAMP | No | NOW() | No |

**SQL Definition:**
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

## `journal_entries`

**Source:** REQ-004 — Voucher Posting | **Milestone:** M2

| Column | Type | Nullable | Default | Encrypted |
|--------|------|----------|---------|----------|
| `id` | UUID | Yes | gen_random_uuid() | No |
| `tenant_id` | UUID | No | — | No |
| `voucher_type` | VARCHAR(30) | No | — | No |
| `voucher_number` | VARCHAR(50) | Yes | — | No |
| `transaction_date` | DATE | No | — | No |
| `narration_encrypted` | TEXT | Yes | — | ✅ AES-256-GCM |
| `reference_number` | VARCHAR(100) | Yes | — | No |
| `status` | VARCHAR(20) | No | 'POSTED' | No |
| `currency_code` | VARCHAR(3) | Yes | 'INR' | No |
| `created_at` | TIMESTAMP | No | NOW() | No |

**SQL Definition:**
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

## `companies`

**Source:** REQ-006 — Cost Center & Branch Management | **Milestone:** M2

| Column | Type | Nullable | Default | Encrypted |
|--------|------|----------|---------|----------|
| `id` | UUID | Yes | gen_random_uuid() | No |
| `tenant_id` | UUID | No | — | No |
| `company_name` | VARCHAR(255) | No | — | No |
| `parent_company_id` | UUID | Yes | — | No |
| `gstin` | VARCHAR(15) | Yes | — | No |
| `created_at` | TIMESTAMP | No | NOW() | No |

**SQL Definition:**
```sql
-- V2__organizational_hierarchy.sql
CREATE TABLE companies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    parent_company_id UUID REFERENCES companies(id),
    gstin VARCHAR(15),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE branches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    company_id UUID NOT NULL REFERENCES companies(id),
    branch_name VARCHAR(255) NOT NULL,
    branch_code VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE cost_centers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    branch_id UUID REFERENCES branches(id),
    cost_center_name VARCHAR(255) NOT NULL,
    cost_center_code VARCHAR(20),
    parent_cost_center_id UUID REFERENCES cost_centers(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

---

## `branches`

**Source:** REQ-006 — Cost Center & Branch Management | **Milestone:** M2

| Column | Type | Nullable | Default | Encrypted |
|--------|------|----------|---------|----------|
| `id` | UUID | Yes | gen_random_uuid() | No |
| `tenant_id` | UUID | No | — | No |
| `company_name` | VARCHAR(255) | No | — | No |
| `parent_company_id` | UUID | Yes | — | No |
| `gstin` | VARCHAR(15) | Yes | — | No |
| `created_at` | TIMESTAMP | No | NOW() | No |

**SQL Definition:**
```sql
-- V2__organizational_hierarchy.sql
CREATE TABLE companies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    parent_company_id UUID REFERENCES companies(id),
    gstin VARCHAR(15),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE branches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    company_id UUID NOT NULL REFERENCES companies(id),
    branch_name VARCHAR(255) NOT NULL,
    branch_code VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE cost_centers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    branch_id UUID REFERENCES branches(id),
    cost_center_name VARCHAR(255) NOT NULL,
    cost_center_code VARCHAR(20),
    parent_cost_center_id UUID REFERENCES cost_centers(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

---

## `cost_centers`

**Source:** REQ-006 — Cost Center & Branch Management | **Milestone:** M2

| Column | Type | Nullable | Default | Encrypted |
|--------|------|----------|---------|----------|
| `id` | UUID | Yes | gen_random_uuid() | No |
| `tenant_id` | UUID | No | — | No |
| `company_name` | VARCHAR(255) | No | — | No |
| `parent_company_id` | UUID | Yes | — | No |
| `gstin` | VARCHAR(15) | Yes | — | No |
| `created_at` | TIMESTAMP | No | NOW() | No |

**SQL Definition:**
```sql
-- V2__organizational_hierarchy.sql
CREATE TABLE companies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    parent_company_id UUID REFERENCES companies(id),
    gstin VARCHAR(15),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE branches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    company_id UUID NOT NULL REFERENCES companies(id),
    branch_name VARCHAR(255) NOT NULL,
    branch_code VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE cost_centers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    branch_id UUID REFERENCES branches(id),
    cost_center_name VARCHAR(255) NOT NULL,
    cost_center_code VARCHAR(20),
    parent_cost_center_id UUID REFERENCES cost_centers(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

---

## `fixed_assets`

**Source:** REQ-007 — Fixed Asset Management | **Milestone:** M7

| Column | Type | Nullable | Default | Encrypted |
|--------|------|----------|---------|----------|
| `id` | UUID | Yes | gen_random_uuid() | No |
| `tenant_id` | UUID | No | — | No |
| `asset_name` | VARCHAR(255) | No | — | No |
| `asset_code` | VARCHAR(50) | No | — | No |
| `asset_category` | VARCHAR(100) | Yes | — | No |
| `purchase_date` | DATE | No | — | No |
| `cost` | DECIMAL(19,4) | No | — | No |
| `residual_value` | DECIMAL(19,4) | Yes | 0 | No |
| `useful_life_years` | INTEGER | Yes | — | No |
| `depreciation_method` | VARCHAR(10) | No | — | No |
| `depreciation_rate` | DECIMAL(5,4) | Yes | — | No |
| `accumulated_depreciation` | DECIMAL(19,4) | Yes | 0 | No |
| `disposal_date` | DATE | Yes | — | No |
| `disposal_amount` | DECIMAL(19,4) | Yes | — | No |
| `is_active` | BOOLEAN | Yes | TRUE | No |
| `created_at` | TIMESTAMP | No | NOW() | No |

**SQL Definition:**
```sql
-- V7__reporting_compliance_far.sql
CREATE TABLE fixed_assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    asset_name VARCHAR(255) NOT NULL,
    asset_code VARCHAR(50) NOT NULL,
    asset_category VARCHAR(100),
    purchase_date DATE NOT NULL,
    cost DECIMAL(19,4) NOT NULL,
    residual_value DECIMAL(19,4) DEFAULT 0,
    useful_life_years INTEGER,
    depreciation_method VARCHAR(10) NOT NULL,
    depreciation_rate DECIMAL(5,4),
    accumulated_depreciation DECIMAL(19,4) DEFAULT 0,
    disposal_date DATE,
    disposal_amount DECIMAL(19,4),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

ALTER TABLE fixed_assets ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON fixed_assets
    USING (tenant_id = current_setting('app.current_tenant_id')::uuid);
```

---

## `tds_deductions`

**Source:** REQ-008 — TDS/TCS Compliance | **Milestone:** M7

| Column | Type | Nullable | Default | Encrypted |
|--------|------|----------|---------|----------|
| `id` | UUID | Yes | gen_random_uuid() | No |
| `tenant_id` | UUID | No | — | No |
| `journal_transaction_id` | UUID | No | — | No |
| `deductee_pan` | VARCHAR(10) | Yes | — | No |
| `section_code` | VARCHAR(10) | No | — | No |
| `payment_amount` | DECIMAL(19,4) | No | — | No |
| `tds_rate` | DECIMAL(5,4) | No | — | No |
| `tds_amount` | DECIMAL(19,4) | No | — | No |
| `deduction_date` | DATE | No | — | No |
| `challan_number` | VARCHAR(50) | Yes | — | No |
| `deposit_date` | DATE | Yes | — | No |
| `created_at` | TIMESTAMP | No | NOW() | No |

**SQL Definition:**
```sql
-- V7__reporting_compliance_far.sql
CREATE TABLE tds_deductions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    journal_transaction_id UUID NOT NULL,
    deductee_pan VARCHAR(10),
    section_code VARCHAR(10) NOT NULL,
    payment_amount DECIMAL(19,4) NOT NULL,
    tds_rate DECIMAL(5,4) NOT NULL,
    tds_amount DECIMAL(19,4) NOT NULL,
    deduction_date DATE NOT NULL,
    challan_number VARCHAR(50),
    deposit_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

---

## `bank_feed_transactions`

**Source:** REQ-009 — Bank Reconciliation | **Milestone:** M7

| Column | Type | Nullable | Default | Encrypted |
|--------|------|----------|---------|----------|
| `id` | UUID | Yes | gen_random_uuid() | No |
| `tenant_id` | UUID | No | — | No |
| `bank_account_id` | UUID | No | — | No |
| `transaction_date` | DATE | No | — | No |
| `value_date` | DATE | Yes | — | No |
| `amount` | DECIMAL(19,4) | No | — | No |
| `transaction_type` | VARCHAR(6) | No | — | No |
| `description` | TEXT | Yes | — | No |
| `reference` | VARCHAR(100) | Yes | — | No |
| `reconciliation_status` | VARCHAR(20) | Yes | 'UNMATCHED' | No |
| `matched_journal_entry_id` | UUID | Yes | — | No |
| `imported_at` | TIMESTAMP | Yes | NOW() | No |

**SQL Definition:**
```sql
-- V7__reporting_compliance_far.sql
CREATE TABLE bank_feed_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    bank_account_id UUID NOT NULL,
    transaction_date DATE NOT NULL,
    value_date DATE,
    amount DECIMAL(19,4) NOT NULL,
    transaction_type VARCHAR(6) NOT NULL,
    description TEXT,
    reference VARCHAR(100),
    reconciliation_status VARCHAR(20) DEFAULT 'UNMATCHED',
    matched_journal_entry_id UUID,
    imported_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE reconciliation_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    bank_account_id UUID NOT NULL,
    period_from DATE NOT NULL,
    period_to DATE NOT NULL,
    opening_book_balance DECIMAL(19,4),
    closing_book_balance DECIMAL(19,4),
    closing_bank_balance DECIMAL(19,4),
    status VARCHAR(20) DEFAULT 'IN_PROGRESS',
    finalized_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);
```

---

## `reconciliation_sessions`

**Source:** REQ-009 — Bank Reconciliation | **Milestone:** M7

| Column | Type | Nullable | Default | Encrypted |
|--------|------|----------|---------|----------|
| `id` | UUID | Yes | gen_random_uuid() | No |
| `tenant_id` | UUID | No | — | No |
| `bank_account_id` | UUID | No | — | No |
| `transaction_date` | DATE | No | — | No |
| `value_date` | DATE | Yes | — | No |
| `amount` | DECIMAL(19,4) | No | — | No |
| `transaction_type` | VARCHAR(6) | No | — | No |
| `description` | TEXT | Yes | — | No |
| `reference` | VARCHAR(100) | Yes | — | No |
| `reconciliation_status` | VARCHAR(20) | Yes | 'UNMATCHED' | No |
| `matched_journal_entry_id` | UUID | Yes | — | No |
| `imported_at` | TIMESTAMP | Yes | NOW() | No |

**SQL Definition:**
```sql
-- V7__reporting_compliance_far.sql
CREATE TABLE bank_feed_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    bank_account_id UUID NOT NULL,
    transaction_date DATE NOT NULL,
    value_date DATE,
    amount DECIMAL(19,4) NOT NULL,
    transaction_type VARCHAR(6) NOT NULL,
    description TEXT,
    reference VARCHAR(100),
    reconciliation_status VARCHAR(20) DEFAULT 'UNMATCHED',
    matched_journal_entry_id UUID,
    imported_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE reconciliation_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    bank_account_id UUID NOT NULL,
    period_from DATE NOT NULL,
    period_to DATE NOT NULL,
    opening_book_balance DECIMAL(19,4),
    closing_book_balance DECIMAL(19,4),
    closing_bank_balance DECIMAL(19,4),
    status VARCHAR(20) DEFAULT 'IN_PROGRESS',
    finalized_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);
```

---

## `audit_workflows`

**Source:** REQ-010 — Maker-Checker-Approver Workflow | **Milestone:** M10

| Column | Type | Nullable | Default | Encrypted |
|--------|------|----------|---------|----------|
| `id` | UUID | Yes | gen_random_uuid() | No |
| `tenant_id` | UUID | No | — | No |
| `entity_type` | VARCHAR(50) | No | — | No |
| `entity_id` | UUID | No | — | No |
| `current_status` | VARCHAR(30) | No | 'DRAFT' | No |
| `maker_id` | VARCHAR(255) | Yes | — | No |
| `checker_id` | VARCHAR(255) | Yes | — | No |
| `approver_id` | VARCHAR(255) | Yes | — | No |
| `rejection_reason` | TEXT | Yes | — | No |
| `created_at` | TIMESTAMP | No | NOW() | No |
| `updated_at` | TIMESTAMP | No | NOW() | No |

**SQL Definition:**
```sql
-- V9__hardening_audit_production.sql
CREATE TABLE audit_workflows (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    current_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    maker_id VARCHAR(255),
    checker_id VARCHAR(255),
    approver_id VARCHAR(255),
    rejection_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_workflow_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workflow_id UUID NOT NULL REFERENCES audit_workflows(id),
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,
    actor_id VARCHAR(255) NOT NULL,
    action VARCHAR(20) NOT NULL,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

---

## `audit_workflow_events`

**Source:** REQ-010 — Maker-Checker-Approver Workflow | **Milestone:** M10

| Column | Type | Nullable | Default | Encrypted |
|--------|------|----------|---------|----------|
| `id` | UUID | Yes | gen_random_uuid() | No |
| `tenant_id` | UUID | No | — | No |
| `entity_type` | VARCHAR(50) | No | — | No |
| `entity_id` | UUID | No | — | No |
| `current_status` | VARCHAR(30) | No | 'DRAFT' | No |
| `maker_id` | VARCHAR(255) | Yes | — | No |
| `checker_id` | VARCHAR(255) | Yes | — | No |
| `approver_id` | VARCHAR(255) | Yes | — | No |
| `rejection_reason` | TEXT | Yes | — | No |
| `created_at` | TIMESTAMP | No | NOW() | No |
| `updated_at` | TIMESTAMP | No | NOW() | No |

**SQL Definition:**
```sql
-- V9__hardening_audit_production.sql
CREATE TABLE audit_workflows (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    current_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    maker_id VARCHAR(255),
    checker_id VARCHAR(255),
    approver_id VARCHAR(255),
    rejection_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_workflow_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workflow_id UUID NOT NULL REFERENCES audit_workflows(id),
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,
    actor_id VARCHAR(255) NOT NULL,
    action VARCHAR(20) NOT NULL,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

---

## `payment_register`

**Source:** REQ-011 — Payment Register | **Milestone:** M11

| Column | Type | Nullable | Default | Encrypted |
|--------|------|----------|---------|----------|
| `id` | BIGSERIAL | Yes | — | No |
| `tenant_id` | UUID | No | — | No |
| `vendor_account_id` | BIGINT | No | — | No |
| `invoice_number` | VARCHAR(100) | No | — | No |
| `invoice_date` | DATE | No | — | No |
| `due_date` | DATE | No | — | No |
| `transaction_type` | VARCHAR(30) | No | — | No |
| `currency` | VARCHAR(10) | No | 'INR' | No |
| `status` | VARCHAR(40) | No | 'AVAILABLE_FOR_PROCESSING' | No |
| `source_transaction_id` | BIGINT | Yes | — | No |
| `created_at` | TIMESTAMP | No | NOW() | No |
| `updated_at` | TIMESTAMP | No | NOW() | No |

**SQL Definition:**
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

---

## `payment_batches`

**Source:** REQ-012 — Payment Batch Processing | **Milestone:** M11

| Column | Type | Nullable | Default | Encrypted |
|--------|------|----------|---------|----------|
| `id` | BIGSERIAL | Yes | — | No |
| `tenant_id` | UUID | No | — | No |
| `batch_number` | VARCHAR(20) | No | — | No |
| `vendor_account_id` | BIGINT | No | — | No |
| `bank_account_id` | BIGINT | No | — | No |
| `status` | VARCHAR(30) | No | 'PENDING_APPROVAL' | No |
| `net_payable` | NUMERIC(19,4) | No | — | No |
| `currency` | VARCHAR(10) | No | 'INR' | No |
| `notes` | TEXT | Yes | — | No |
| `created_by` | VARCHAR(255) | No | — | No |
| `approved_by` | VARCHAR(255) | Yes | — | No |
| `rejection_reason` | TEXT | Yes | — | No |
| `created_at` | TIMESTAMP | No | NOW() | No |
| `updated_at` | TIMESTAMP | No | NOW() | No |

**SQL Definition:**
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

---

## `payment_batch_items`

**Source:** REQ-012 — Payment Batch Processing | **Milestone:** M11

| Column | Type | Nullable | Default | Encrypted |
|--------|------|----------|---------|----------|
| `id` | BIGSERIAL | Yes | — | No |
| `tenant_id` | UUID | No | — | No |
| `batch_number` | VARCHAR(20) | No | — | No |
| `vendor_account_id` | BIGINT | No | — | No |
| `bank_account_id` | BIGINT | No | — | No |
| `status` | VARCHAR(30) | No | 'PENDING_APPROVAL' | No |
| `net_payable` | NUMERIC(19,4) | No | — | No |
| `currency` | VARCHAR(10) | No | 'INR' | No |
| `notes` | TEXT | Yes | — | No |
| `created_by` | VARCHAR(255) | No | — | No |
| `approved_by` | VARCHAR(255) | Yes | — | No |
| `rejection_reason` | TEXT | Yes | — | No |
| `created_at` | TIMESTAMP | No | NOW() | No |
| `updated_at` | TIMESTAMP | No | NOW() | No |

**SQL Definition:**
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

---

*Auto-generated by `docs/automation/generate-data-dictionary.js` on 2026-03-20.*
