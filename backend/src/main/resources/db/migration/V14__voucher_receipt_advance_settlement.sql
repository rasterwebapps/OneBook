-- ============================================================================
-- V14: Voucher-Receipt-Advance Settlement System
-- Creates foundation tables (departments, payers, payees, advances, etc.)
-- and core voucher workflow tables with RLS policies and indexes.
-- ============================================================================

-- ============================================================
-- 1. Foundation Tables
-- ============================================================

CREATE TABLE departments (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       VARCHAR(255) NOT NULL,
    code            VARCHAR(50)  NOT NULL,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, code)
);

CREATE TABLE sub_departments (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       VARCHAR(255) NOT NULL,
    department_id   BIGINT       NOT NULL REFERENCES departments(id),
    code            VARCHAR(50)  NOT NULL,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, code)
);

CREATE TABLE payers (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       VARCHAR(255) NOT NULL,
    code            VARCHAR(50)  NOT NULL,
    name            VARCHAR(255) NOT NULL,
    contact_person  VARCHAR(255),
    email           VARCHAR(255),
    phone           VARCHAR(50),
    address_text    TEXT,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, code)
);

CREATE TABLE payer_bank_accounts (
    id                BIGSERIAL PRIMARY KEY,
    tenant_id         VARCHAR(255) NOT NULL,
    payer_id          BIGINT       NOT NULL REFERENCES payers(id),
    account_number    VARCHAR(50)  NOT NULL,
    bank_name         VARCHAR(255) NOT NULL,
    branch_name       VARCHAR(255),
    ifsc_code         VARCHAR(20),
    bank_account_type VARCHAR(30),
    is_default        BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE payees (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       VARCHAR(255) NOT NULL,
    code            VARCHAR(50)  NOT NULL,
    name            VARCHAR(255) NOT NULL,
    contact_person  VARCHAR(255),
    email           VARCHAR(255),
    phone           VARCHAR(50),
    address_text    TEXT,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, code)
);

CREATE TABLE payee_bank_accounts (
    id                BIGSERIAL PRIMARY KEY,
    tenant_id         VARCHAR(255) NOT NULL,
    payee_id          BIGINT       NOT NULL REFERENCES payees(id),
    account_number    VARCHAR(50)  NOT NULL,
    bank_name         VARCHAR(255) NOT NULL,
    branch_name       VARCHAR(255),
    ifsc_code         VARCHAR(20),
    bank_account_type VARCHAR(30),
    is_default        BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE applications (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           VARCHAR(255) NOT NULL,
    application_number  VARCHAR(50)  NOT NULL,
    applicant_name      VARCHAR(255) NOT NULL,
    description         TEXT,
    amount              NUMERIC(19, 4),
    status              VARCHAR(30),
    submitted_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, application_number)
);

CREATE TABLE advances (
    id               BIGSERIAL PRIMARY KEY,
    tenant_id        VARCHAR(255)   NOT NULL,
    advance_number   VARCHAR(50)    NOT NULL,
    payer_id         BIGINT         REFERENCES payers(id),
    payee_id         BIGINT         REFERENCES payees(id),
    amount           NUMERIC(19, 4) NOT NULL CHECK (amount >= 0),
    settled_amount   NUMERIC(19, 4) NOT NULL DEFAULT 0 CHECK (settled_amount >= 0),
    unsettled_amount NUMERIC(19, 4),
    description      TEXT,
    advance_date     TIMESTAMPTZ,
    is_settled       BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, advance_number)
);

CREATE TABLE payment_approval_limits (
    id             BIGSERIAL PRIMARY KEY,
    tenant_id      VARCHAR(255)   NOT NULL,
    approver_name  VARCHAR(255)   NOT NULL,
    approver_role  VARCHAR(100)   NOT NULL,
    min_amount     NUMERIC(19, 4) NOT NULL CHECK (min_amount >= 0),
    max_amount     NUMERIC(19, 4) NOT NULL CHECK (max_amount >= 0),
    is_active      BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CHECK (max_amount >= min_amount)
);

-- ============================================================
-- 2. Core Voucher Workflow Tables
-- ============================================================

CREATE TABLE vouchers (
    id                    BIGSERIAL PRIMARY KEY,
    tenant_id             VARCHAR(255)   NOT NULL,
    voucher_number        VARCHAR(50)    NOT NULL,
    voucher_type_id       BIGINT         REFERENCES voucher_types(id),
    department_id         BIGINT         REFERENCES departments(id),
    sub_department_id     BIGINT         REFERENCES sub_departments(id),
    payer_id              BIGINT         REFERENCES payers(id),
    payer_bank_account_id BIGINT         REFERENCES payer_bank_accounts(id),
    voucher_date          TIMESTAMPTZ,
    status                VARCHAR(30)    NOT NULL DEFAULT 'CREATED',
    closure_type          VARCHAR(30),
    total_amount          NUMERIC(19, 4) NOT NULL CHECK (total_amount >= 0),
    approved_amount       NUMERIC(19, 4),
    tds_amount            NUMERIC(19, 4) DEFAULT 0,
    net_amount            NUMERIC(19, 4) NOT NULL CHECK (net_amount >= 0),
    payment_mode          VARCHAR(20),
    description           TEXT,
    remarks               TEXT,
    approved_by           VARCHAR(255),
    approved_at           TIMESTAMPTZ,
    classified_by         VARCHAR(255),
    classified_at         TIMESTAMPTZ,
    cancelled_by          VARCHAR(255),
    cancelled_at          TIMESTAMPTZ,
    cancellation_reason   TEXT,
    is_cancelled          BOOLEAN        NOT NULL DEFAULT FALSE,
    created_by            VARCHAR(255)   NOT NULL,
    created_at            TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, voucher_number)
);

CREATE TABLE voucher_items (
    id                    BIGSERIAL PRIMARY KEY,
    tenant_id             VARCHAR(255)   NOT NULL,
    voucher_id            BIGINT         NOT NULL REFERENCES vouchers(id),
    item_number           INTEGER,
    payee_id              BIGINT         REFERENCES payees(id),
    payee_bank_account_id BIGINT         REFERENCES payee_bank_accounts(id),
    ledger_account_id     BIGINT         REFERENCES ledger_accounts(id),
    cost_center_id        BIGINT         REFERENCES cost_centers(id),
    description           TEXT,
    amount                NUMERIC(19, 4) NOT NULL CHECK (amount >= 0),
    tds_applicable        BOOLEAN        NOT NULL DEFAULT FALSE,
    tds_percentage        NUMERIC(5, 2),
    tds_amount            NUMERIC(19, 4) DEFAULT 0,
    net_amount            NUMERIC(19, 4) NOT NULL CHECK (net_amount >= 0),
    status                VARCHAR(30)    NOT NULL DEFAULT 'CREATED',
    remarks               TEXT,
    created_at            TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE TABLE receipts (
    id                      BIGSERIAL PRIMARY KEY,
    tenant_id               VARCHAR(255)   NOT NULL,
    receipt_number          VARCHAR(50)    NOT NULL,
    voucher_id              BIGINT         REFERENCES vouchers(id),
    payer_id                BIGINT         REFERENCES payers(id),
    payer_bank_account_id   BIGINT         REFERENCES payer_bank_accounts(id),
    payee_id                BIGINT         REFERENCES payees(id),
    payee_bank_account_id   BIGINT         REFERENCES payee_bank_accounts(id),
    from_ledger_account_id  BIGINT         REFERENCES ledger_accounts(id),
    to_ledger_account_id    BIGINT         REFERENCES ledger_accounts(id),
    amount                  NUMERIC(19, 4) NOT NULL CHECK (amount >= 0),
    payment_mode            VARCHAR(20),
    reference_number        VARCHAR(100),
    status                  VARCHAR(20)    NOT NULL DEFAULT 'CREATED',
    receipt_date            TIMESTAMPTZ,
    description             TEXT,
    created_by              VARCHAR(255)   NOT NULL,
    created_at              TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, receipt_number)
);

CREATE TABLE payment_advices (
    id                      BIGSERIAL PRIMARY KEY,
    tenant_id               VARCHAR(255)   NOT NULL,
    advice_number           VARCHAR(50)    NOT NULL,
    voucher_id              BIGINT         REFERENCES vouchers(id),
    application_id          BIGINT         REFERENCES applications(id),
    department_id           BIGINT         REFERENCES departments(id),
    payer_id                BIGINT         REFERENCES payers(id),
    payer_bank_account_id   BIGINT         REFERENCES payer_bank_accounts(id),
    payee_id                BIGINT         REFERENCES payees(id),
    payee_bank_account_id   BIGINT         REFERENCES payee_bank_accounts(id),
    amount                  NUMERIC(19, 4) NOT NULL CHECK (amount >= 0),
    payment_mode            VARCHAR(20),
    status                  VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    two_step_verification   VARCHAR(20),
    approved_by             VARCHAR(255),
    approved_at             TIMESTAMPTZ,
    rejected_by             VARCHAR(255),
    rejected_at             TIMESTAMPTZ,
    rejection_reason        TEXT,
    paid_at                 TIMESTAMPTZ,
    transaction_reference   VARCHAR(100),
    description             TEXT,
    created_by              VARCHAR(255)   NOT NULL,
    created_at              TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, advice_number)
);

-- ============================================================
-- 3. Settlement Tables
-- ============================================================

CREATE TABLE advance_voucher_item_settlements (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       VARCHAR(255)   NOT NULL,
    advance_id      BIGINT         NOT NULL REFERENCES advances(id),
    voucher_item_id BIGINT         NOT NULL REFERENCES voucher_items(id),
    settled_amount  NUMERIC(19, 4) NOT NULL CHECK (settled_amount > 0),
    settlement_date TIMESTAMPTZ    NOT NULL,
    remarks         TEXT,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE TABLE advance_receipt_settlements (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       VARCHAR(255)   NOT NULL,
    advance_id      BIGINT         NOT NULL REFERENCES advances(id),
    receipt_id      BIGINT         NOT NULL REFERENCES receipts(id),
    settled_amount  NUMERIC(19, 4) NOT NULL CHECK (settled_amount > 0),
    settlement_date TIMESTAMPTZ    NOT NULL,
    remarks         TEXT,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE TABLE advance_payment_advice_settlements (
    id                BIGSERIAL PRIMARY KEY,
    tenant_id         VARCHAR(255)   NOT NULL,
    advance_id        BIGINT         NOT NULL REFERENCES advances(id),
    payment_advice_id BIGINT         NOT NULL REFERENCES payment_advices(id),
    settled_amount    NUMERIC(19, 4) NOT NULL CHECK (settled_amount > 0),
    settlement_date   TIMESTAMPTZ    NOT NULL,
    remarks           TEXT,
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 4. Uploaded Files & Department Approval Limits
-- ============================================================

CREATE TABLE uploaded_files (
    id                 BIGSERIAL PRIMARY KEY,
    tenant_id          VARCHAR(255)   NOT NULL,
    file_name          VARCHAR(500)   NOT NULL,
    original_file_name VARCHAR(500)   NOT NULL,
    content_type       VARCHAR(100),
    file_size          BIGINT,
    storage_path       VARCHAR(1000),
    status             VARCHAR(20)    NOT NULL DEFAULT 'UPLOADED',
    processed_at       TIMESTAMPTZ,
    error_message      TEXT,
    uploaded_by        VARCHAR(255)   NOT NULL,
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE TABLE department_level_approval_limits (
    id                        BIGSERIAL PRIMARY KEY,
    tenant_id                 VARCHAR(255)   NOT NULL,
    department_id             BIGINT         NOT NULL REFERENCES departments(id),
    payer_id                  BIGINT         NOT NULL REFERENCES payers(id),
    payment_approval_limit_id BIGINT         NOT NULL REFERENCES payment_approval_limits(id),
    max_amount                NUMERIC(19, 4) NOT NULL CHECK (max_amount >= 0),
    is_active                 BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at                TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at                TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 5. Indexes for Efficient Tenant-Scoped Queries
-- ============================================================

CREATE INDEX idx_departments_tenant              ON departments(tenant_id);
CREATE INDEX idx_sub_departments_tenant          ON sub_departments(tenant_id);
CREATE INDEX idx_sub_departments_dept            ON sub_departments(department_id);
CREATE INDEX idx_payers_tenant                   ON payers(tenant_id);
CREATE INDEX idx_payer_bank_accounts_tenant      ON payer_bank_accounts(tenant_id);
CREATE INDEX idx_payer_bank_accounts_payer       ON payer_bank_accounts(payer_id);
CREATE INDEX idx_payees_tenant                   ON payees(tenant_id);
CREATE INDEX idx_payee_bank_accounts_tenant      ON payee_bank_accounts(tenant_id);
CREATE INDEX idx_payee_bank_accounts_payee       ON payee_bank_accounts(payee_id);
CREATE INDEX idx_applications_tenant             ON applications(tenant_id);
CREATE INDEX idx_advances_tenant                 ON advances(tenant_id);
CREATE INDEX idx_advances_tenant_settled         ON advances(tenant_id, is_settled);
CREATE INDEX idx_payment_approval_limits_tenant  ON payment_approval_limits(tenant_id);

CREATE INDEX idx_vouchers_tenant                 ON vouchers(tenant_id);
CREATE INDEX idx_vouchers_tenant_status          ON vouchers(tenant_id, status);
CREATE INDEX idx_vouchers_tenant_dept            ON vouchers(tenant_id, department_id);
CREATE INDEX idx_voucher_items_tenant            ON voucher_items(tenant_id);
CREATE INDEX idx_voucher_items_voucher           ON voucher_items(voucher_id);
CREATE INDEX idx_receipts_tenant                 ON receipts(tenant_id);
CREATE INDEX idx_receipts_tenant_status          ON receipts(tenant_id, status);
CREATE INDEX idx_receipts_voucher                ON receipts(voucher_id);
CREATE INDEX idx_payment_advices_tenant          ON payment_advices(tenant_id);
CREATE INDEX idx_payment_advices_tenant_status   ON payment_advices(tenant_id, status);
CREATE INDEX idx_payment_advices_voucher         ON payment_advices(voucher_id);

CREATE INDEX idx_adv_vi_settlements_tenant       ON advance_voucher_item_settlements(tenant_id);
CREATE INDEX idx_adv_vi_settlements_advance      ON advance_voucher_item_settlements(advance_id);
CREATE INDEX idx_adv_receipt_settlements_tenant   ON advance_receipt_settlements(tenant_id);
CREATE INDEX idx_adv_receipt_settlements_advance  ON advance_receipt_settlements(advance_id);
CREATE INDEX idx_adv_pa_settlements_tenant        ON advance_payment_advice_settlements(tenant_id);
CREATE INDEX idx_adv_pa_settlements_advance       ON advance_payment_advice_settlements(advance_id);

CREATE INDEX idx_uploaded_files_tenant           ON uploaded_files(tenant_id);
CREATE INDEX idx_uploaded_files_tenant_status    ON uploaded_files(tenant_id, status);
CREATE INDEX idx_dept_approval_limits_tenant     ON department_level_approval_limits(tenant_id);
CREATE INDEX idx_dept_approval_limits_dept       ON department_level_approval_limits(department_id);

-- ============================================================
-- 6. Enable Row Level Security (RLS)
-- ============================================================

ALTER TABLE departments                        ENABLE ROW LEVEL SECURITY;
ALTER TABLE sub_departments                    ENABLE ROW LEVEL SECURITY;
ALTER TABLE payers                             ENABLE ROW LEVEL SECURITY;
ALTER TABLE payer_bank_accounts                ENABLE ROW LEVEL SECURITY;
ALTER TABLE payees                             ENABLE ROW LEVEL SECURITY;
ALTER TABLE payee_bank_accounts                ENABLE ROW LEVEL SECURITY;
ALTER TABLE applications                       ENABLE ROW LEVEL SECURITY;
ALTER TABLE advances                           ENABLE ROW LEVEL SECURITY;
ALTER TABLE payment_approval_limits            ENABLE ROW LEVEL SECURITY;
ALTER TABLE vouchers                           ENABLE ROW LEVEL SECURITY;
ALTER TABLE voucher_items                      ENABLE ROW LEVEL SECURITY;
ALTER TABLE receipts                           ENABLE ROW LEVEL SECURITY;
ALTER TABLE payment_advices                    ENABLE ROW LEVEL SECURITY;
ALTER TABLE advance_voucher_item_settlements   ENABLE ROW LEVEL SECURITY;
ALTER TABLE advance_receipt_settlements        ENABLE ROW LEVEL SECURITY;
ALTER TABLE advance_payment_advice_settlements ENABLE ROW LEVEL SECURITY;
ALTER TABLE uploaded_files                     ENABLE ROW LEVEL SECURITY;
ALTER TABLE department_level_approval_limits   ENABLE ROW LEVEL SECURITY;

-- ============================================================
-- 7. RLS Policies: Tenant Isolation
-- ============================================================

CREATE POLICY departments_tenant_isolation ON departments
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE POLICY sub_departments_tenant_isolation ON sub_departments
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE POLICY payers_tenant_isolation ON payers
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE POLICY payer_bank_accounts_tenant_isolation ON payer_bank_accounts
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE POLICY payees_tenant_isolation ON payees
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE POLICY payee_bank_accounts_tenant_isolation ON payee_bank_accounts
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE POLICY applications_tenant_isolation ON applications
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE POLICY advances_tenant_isolation ON advances
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE POLICY payment_approval_limits_tenant_isolation ON payment_approval_limits
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE POLICY vouchers_tenant_isolation ON vouchers
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE POLICY voucher_items_tenant_isolation ON voucher_items
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE POLICY receipts_tenant_isolation ON receipts
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE POLICY payment_advices_tenant_isolation ON payment_advices
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE POLICY adv_voucher_item_settlements_tenant_isolation ON advance_voucher_item_settlements
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE POLICY adv_receipt_settlements_tenant_isolation ON advance_receipt_settlements
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE POLICY adv_payment_advice_settlements_tenant_isolation ON advance_payment_advice_settlements
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE POLICY uploaded_files_tenant_isolation ON uploaded_files
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE POLICY dept_level_approval_limits_tenant_isolation ON department_level_approval_limits
    USING (tenant_id = current_setting('app.tenant_id', true));
