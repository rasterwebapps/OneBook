-- V15__employee_advances_settlement.sql
-- Employee Advances & Settlement (Milestone 12)
-- Implements REQ-014: Advance → Expense Settlement → Receipt / Payment Advice cycle

-- 1. Configurable per-employee advance limit
CREATE TABLE employee_advance_config (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       VARCHAR(255) NOT NULL,
    employee_id     BIGINT NOT NULL,
    advance_limit   NUMERIC(19,4) NOT NULL DEFAULT 10000.00,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, employee_id)
);

ALTER TABLE employee_advance_config ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_eac ON employee_advance_config
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE INDEX idx_employee_advance_config_tenant ON employee_advance_config (tenant_id);

-- 2. Outstanding advance balance cache (updated on every posting)
CREATE TABLE employee_advance_balance (
    id                   BIGSERIAL PRIMARY KEY,
    tenant_id            VARCHAR(255) NOT NULL,
    employee_id          BIGINT NOT NULL,
    outstanding_advance  NUMERIC(19,4) NOT NULL DEFAULT 0.00,
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, employee_id)
);

ALTER TABLE employee_advance_balance ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_eab ON employee_advance_balance
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE INDEX idx_employee_advance_balance_tenant ON employee_advance_balance (tenant_id);

-- 3. Employee advance voucher header
CREATE TABLE employee_advances (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           VARCHAR(255) NOT NULL,
    employee_id         BIGINT NOT NULL,
    department_id       BIGINT NOT NULL,
    amount              NUMERIC(19,4) NOT NULL,
    purpose             TEXT NOT NULL,
    status              VARCHAR(40) NOT NULL DEFAULT 'DRAFT',
    -- Approval tier based on amount: HOD (≤10k), CEO (10k-20k), MD (>20k)
    current_approver_role VARCHAR(20),
    override_flag       BOOLEAN NOT NULL DEFAULT FALSE,
    override_reason     TEXT,
    voucher_date        DATE NOT NULL,
    approved_amount     NUMERIC(19,4),
    journal_entry_id    BIGINT,
    -- Approval tracking
    hod_approved_by     VARCHAR(255),
    hod_approved_at     TIMESTAMPTZ,
    ceo_approved_by     VARCHAR(255),
    ceo_approved_at     TIMESTAMPTZ,
    md_approved_by      VARCHAR(255),
    md_approved_at      TIMESTAMPTZ,
    rejected_by         VARCHAR(255),
    rejected_at         TIMESTAMPTZ,
    rejection_reason    TEXT,
    created_by          VARCHAR(255) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE employee_advances ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_ea ON employee_advances
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE INDEX idx_employee_advances_tenant_employee ON employee_advances (tenant_id, employee_id);
CREATE INDEX idx_employee_advances_tenant_department ON employee_advances (tenant_id, department_id);
CREATE INDEX idx_employee_advances_status ON employee_advances (tenant_id, status);

-- 4. Expense voucher header
CREATE TABLE expense_vouchers (
    id                    BIGSERIAL PRIMARY KEY,
    tenant_id             VARCHAR(255) NOT NULL,
    employee_id           BIGINT NOT NULL,
    department_id         BIGINT NOT NULL,
    amount                NUMERIC(19,4) NOT NULL,
    expense_type          VARCHAR(100) NOT NULL,
    description           TEXT NOT NULL,
    voucher_date          DATE NOT NULL,
    status                VARCHAR(40) NOT NULL DEFAULT 'DRAFT',
    supporting_doc_ref    TEXT,
    -- Settlement tracking (populated on HOD approval)
    advance_settlement    NUMERIC(19,4),  -- portion settled against advance
    reimbursement_amount  NUMERIC(19,4),  -- portion that became Payment Advice
    payment_advice_id     BIGINT,         -- FK to payment_advices
    journal_entry_id      BIGINT,
    -- Approval tracking
    approved_by           VARCHAR(255),
    approved_at           TIMESTAMPTZ,
    rejected_by           VARCHAR(255),
    rejected_at           TIMESTAMPTZ,
    rejection_reason      TEXT,
    created_by            VARCHAR(255) NOT NULL,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE expense_vouchers ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_ev ON expense_vouchers
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE INDEX idx_expense_vouchers_tenant_employee ON expense_vouchers (tenant_id, employee_id);
CREATE INDEX idx_expense_vouchers_tenant_department ON expense_vouchers (tenant_id, department_id);
CREATE INDEX idx_expense_vouchers_status ON expense_vouchers (tenant_id, status);

-- 5. Expense voucher linked advances (for multi-advance settlement)
CREATE TABLE expense_voucher_advances (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           VARCHAR(255) NOT NULL,
    expense_voucher_id  BIGINT NOT NULL REFERENCES expense_vouchers(id),
    employee_advance_id BIGINT NOT NULL REFERENCES employee_advances(id),
    settlement_amount   NUMERIC(19,4) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE expense_voucher_advances ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_eva ON expense_voucher_advances
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE INDEX idx_expense_voucher_advances_voucher ON expense_voucher_advances (expense_voucher_id);
CREATE INDEX idx_expense_voucher_advances_advance ON expense_voucher_advances (employee_advance_id);

-- 6. Advance receipt voucher
CREATE TABLE advance_receipts (
    id               BIGSERIAL PRIMARY KEY,
    tenant_id        VARCHAR(255) NOT NULL,
    employee_id      BIGINT NOT NULL,
    department_id    BIGINT NOT NULL,
    amount           NUMERIC(19,4) NOT NULL,
    payment_mode     VARCHAR(20) NOT NULL,  -- CASH, BANK, UPI
    receipt_date     DATE NOT NULL,
    override_flag    BOOLEAN NOT NULL DEFAULT FALSE,
    override_reason  TEXT,
    journal_entry_id BIGINT,
    status           VARCHAR(20) NOT NULL DEFAULT 'POSTED',
    created_by       VARCHAR(255) NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE advance_receipts ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_ar ON advance_receipts
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE INDEX idx_advance_receipts_tenant_employee ON advance_receipts (tenant_id, employee_id);

-- 7. Payment advice (reimbursement payable to employee)
CREATE TABLE payment_advices_m12 (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           VARCHAR(255) NOT NULL,
    employee_id         BIGINT NOT NULL,
    department_id       BIGINT NOT NULL,
    amount              NUMERIC(19,4) NOT NULL,
    expense_voucher_id  BIGINT NOT NULL REFERENCES expense_vouchers(id),
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING_PAYMENT',  -- PENDING_PAYMENT, PAID
    payment_voucher_id  BIGINT,             -- Payment voucher when paid
    paid_by             VARCHAR(255),
    paid_at             TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE payment_advices_m12 ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_pam ON payment_advices_m12
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE INDEX idx_payment_advices_m12_tenant_employee ON payment_advices_m12 (tenant_id, employee_id);
CREATE INDEX idx_payment_advices_m12_status ON payment_advices_m12 (tenant_id, status);

-- 8. Approval workflow history for employee advances
CREATE TABLE advance_approval_history (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           VARCHAR(255) NOT NULL,
    employee_advance_id BIGINT NOT NULL REFERENCES employee_advances(id),
    approver_role       VARCHAR(20) NOT NULL,  -- HOD, CEO, MD
    action              VARCHAR(20) NOT NULL,  -- APPROVED, REJECTED
    actor_id            VARCHAR(255) NOT NULL,
    comment             TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE advance_approval_history ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_aah ON advance_approval_history
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE INDEX idx_advance_approval_history_advance ON advance_approval_history (employee_advance_id);

-- Add foreign key from expense_vouchers to payment_advices_m12
ALTER TABLE expense_vouchers
    ADD CONSTRAINT fk_expense_voucher_payment_advice
    FOREIGN KEY (payment_advice_id) REFERENCES payment_advices_m12(id);
