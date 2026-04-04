-- ============================================================
-- V10 — Tally Feature Parity
-- Missing features from docs/business/tally-features.md:
--   Credit Management, Multi-Currency, Stock/Inventory,
--   Godowns, BOM, Batch & Expiry, Voucher Types, TDS/TCS,
--   Cheque Management, Re-order Levels, Payroll
-- ============================================================

-- 1. Voucher Types — Configurable voucher types per tenant
CREATE TABLE voucher_types (
    id                  BIGSERIAL       PRIMARY KEY,
    tenant_id           TEXT            NOT NULL DEFAULT current_tenant_id(),
    voucher_code        VARCHAR(20)     NOT NULL,
    voucher_name        VARCHAR(100)    NOT NULL,
    category            VARCHAR(30)     NOT NULL,
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_voucher_category CHECK (
        category IN ('SALES', 'PURCHASE', 'PAYMENT', 'RECEIPT', 'CONTRA', 'JOURNAL', 'CREDIT_NOTE', 'DEBIT_NOTE')
    ),
    UNIQUE (tenant_id, voucher_code)
);

ALTER TABLE voucher_types ENABLE ROW LEVEL SECURITY;
CREATE POLICY voucher_type_tenant_isolation ON voucher_types
    USING (tenant_id = current_tenant_id())
    WITH CHECK (tenant_id = current_tenant_id());

-- 2. Credit Limits — Customer credit management
CREATE TABLE credit_limits (
    id                  BIGSERIAL       PRIMARY KEY,
    tenant_id           TEXT            NOT NULL DEFAULT current_tenant_id(),
    account_id          BIGINT          NOT NULL REFERENCES ledger_accounts(id),
    credit_limit        DECIMAL(19,4)   NOT NULL DEFAULT 0,
    current_outstanding DECIMAL(19,4)   NOT NULL DEFAULT 0,
    credit_period_days  INT             NOT NULL DEFAULT 30,
    is_blocked          BOOLEAN         NOT NULL DEFAULT FALSE,
    last_reviewed_at    TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_credit_limit_positive CHECK (credit_limit >= 0),
    UNIQUE (tenant_id, account_id)
);

ALTER TABLE credit_limits ENABLE ROW LEVEL SECURITY;
CREATE POLICY credit_limit_tenant_isolation ON credit_limits
    USING (tenant_id = current_tenant_id())
    WITH CHECK (tenant_id = current_tenant_id());

-- 3. Currency Exchange Rates — Multi-currency support
CREATE TABLE currency_exchange_rates (
    id                  BIGSERIAL       PRIMARY KEY,
    tenant_id           TEXT            NOT NULL DEFAULT current_tenant_id(),
    from_currency       VARCHAR(3)      NOT NULL,
    to_currency         VARCHAR(3)      NOT NULL,
    exchange_rate       DECIMAL(19,8)   NOT NULL,
    effective_date      DATE            NOT NULL,
    source              VARCHAR(50)     DEFAULT 'MANUAL',
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_rate_positive CHECK (exchange_rate > 0),
    UNIQUE (tenant_id, from_currency, to_currency, effective_date)
);

ALTER TABLE currency_exchange_rates ENABLE ROW LEVEL SECURITY;
CREATE POLICY currency_rate_tenant_isolation ON currency_exchange_rates
    USING (tenant_id = current_tenant_id())
    WITH CHECK (tenant_id = current_tenant_id());

-- 4. Units of Measure
CREATE TABLE units_of_measure (
    id                  BIGSERIAL       PRIMARY KEY,
    tenant_id           TEXT            NOT NULL DEFAULT current_tenant_id(),
    uom_code            VARCHAR(20)     NOT NULL,
    uom_name            VARCHAR(100)    NOT NULL,
    decimal_places      INT             NOT NULL DEFAULT 2,
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, uom_code)
);

ALTER TABLE units_of_measure ENABLE ROW LEVEL SECURITY;
CREATE POLICY uom_tenant_isolation ON units_of_measure
    USING (tenant_id = current_tenant_id())
    WITH CHECK (tenant_id = current_tenant_id());

-- 5. Stock Groups — Hierarchical stock classification
CREATE TABLE stock_groups (
    id                  BIGSERIAL       PRIMARY KEY,
    tenant_id           TEXT            NOT NULL DEFAULT current_tenant_id(),
    group_code          VARCHAR(50)     NOT NULL,
    group_name          VARCHAR(255)    NOT NULL,
    parent_group_id     BIGINT          REFERENCES stock_groups(id),
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, group_code)
);

ALTER TABLE stock_groups ENABLE ROW LEVEL SECURITY;
CREATE POLICY stock_group_tenant_isolation ON stock_groups
    USING (tenant_id = current_tenant_id())
    WITH CHECK (tenant_id = current_tenant_id());

-- 6. Godowns (Warehouses) — Multi-location inventory
CREATE TABLE godowns (
    id                  BIGSERIAL       PRIMARY KEY,
    tenant_id           TEXT            NOT NULL DEFAULT current_tenant_id(),
    godown_code         VARCHAR(50)     NOT NULL,
    godown_name         VARCHAR(255)    NOT NULL,
    address             TEXT,
    branch_id           BIGINT          REFERENCES branches(id),
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, godown_code)
);

ALTER TABLE godowns ENABLE ROW LEVEL SECURITY;
CREATE POLICY godown_tenant_isolation ON godowns
    USING (tenant_id = current_tenant_id())
    WITH CHECK (tenant_id = current_tenant_id());

-- 7. Stock Items — Inventory items
CREATE TABLE stock_items (
    id                  BIGSERIAL       PRIMARY KEY,
    tenant_id           TEXT            NOT NULL DEFAULT current_tenant_id(),
    item_code           VARCHAR(50)     NOT NULL,
    item_name           VARCHAR(255)    NOT NULL,
    description         TEXT,
    stock_group_id      BIGINT          REFERENCES stock_groups(id),
    primary_uom_id      BIGINT          NOT NULL REFERENCES units_of_measure(id),
    secondary_uom_id    BIGINT          REFERENCES units_of_measure(id),
    conversion_factor   DECIMAL(19,6)   DEFAULT 1,
    opening_balance     DECIMAL(19,4)   NOT NULL DEFAULT 0,
    current_balance     DECIMAL(19,4)   NOT NULL DEFAULT 0,
    rate_per_unit       DECIMAL(19,4)   DEFAULT 0,
    hsn_code            VARCHAR(20),
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    metadata            TEXT            DEFAULT '{}',
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, item_code)
);

ALTER TABLE stock_items ENABLE ROW LEVEL SECURITY;
CREATE POLICY stock_item_tenant_isolation ON stock_items
    USING (tenant_id = current_tenant_id())
    WITH CHECK (tenant_id = current_tenant_id());

-- 8. Stock Godown Allocation — Stock per location
CREATE TABLE stock_godown_allocations (
    id                  BIGSERIAL       PRIMARY KEY,
    tenant_id           TEXT            NOT NULL DEFAULT current_tenant_id(),
    stock_item_id       BIGINT          NOT NULL REFERENCES stock_items(id),
    godown_id           BIGINT          NOT NULL REFERENCES godowns(id),
    quantity            DECIMAL(19,4)   NOT NULL DEFAULT 0,
    rate                DECIMAL(19,4)   DEFAULT 0,
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, stock_item_id, godown_id)
);

ALTER TABLE stock_godown_allocations ENABLE ROW LEVEL SECURITY;
CREATE POLICY stock_godown_tenant_isolation ON stock_godown_allocations
    USING (tenant_id = current_tenant_id())
    WITH CHECK (tenant_id = current_tenant_id());

-- 9. Bill of Materials (BOM) — Manufacturing
CREATE TABLE bills_of_materials (
    id                  BIGSERIAL       PRIMARY KEY,
    tenant_id           TEXT            NOT NULL DEFAULT current_tenant_id(),
    bom_code            VARCHAR(50)     NOT NULL,
    finished_item_id    BIGINT          NOT NULL REFERENCES stock_items(id),
    quantity_produced   DECIMAL(19,4)   NOT NULL DEFAULT 1,
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, bom_code)
);

ALTER TABLE bills_of_materials ENABLE ROW LEVEL SECURITY;
CREATE POLICY bom_tenant_isolation ON bills_of_materials
    USING (tenant_id = current_tenant_id())
    WITH CHECK (tenant_id = current_tenant_id());

-- 10. BOM Components — Raw materials in a BOM
CREATE TABLE bom_components (
    id                  BIGSERIAL       PRIMARY KEY,
    tenant_id           TEXT            NOT NULL DEFAULT current_tenant_id(),
    bom_id              BIGINT          NOT NULL REFERENCES bills_of_materials(id) ON DELETE CASCADE,
    component_item_id   BIGINT          NOT NULL REFERENCES stock_items(id),
    quantity_required   DECIMAL(19,4)   NOT NULL,
    uom_id              BIGINT          NOT NULL REFERENCES units_of_measure(id),
    UNIQUE (tenant_id, bom_id, component_item_id)
);

ALTER TABLE bom_components ENABLE ROW LEVEL SECURITY;
CREATE POLICY bom_component_tenant_isolation ON bom_components
    USING (tenant_id = current_tenant_id())
    WITH CHECK (tenant_id = current_tenant_id());

-- 11. Batch Tracking — Batch & expiry management
CREATE TABLE batch_tracking (
    id                  BIGSERIAL       PRIMARY KEY,
    tenant_id           TEXT            NOT NULL DEFAULT current_tenant_id(),
    stock_item_id       BIGINT          NOT NULL REFERENCES stock_items(id),
    batch_number        VARCHAR(100)    NOT NULL,
    manufacturing_date  DATE,
    expiry_date         DATE,
    godown_id           BIGINT          REFERENCES godowns(id),
    quantity            DECIMAL(19,4)   NOT NULL DEFAULT 0,
    cost_per_unit       DECIMAL(19,4)   DEFAULT 0,
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_batch_status CHECK (
        status IN ('ACTIVE', 'EXPIRED', 'CONSUMED', 'RECALLED')
    ),
    UNIQUE (tenant_id, stock_item_id, batch_number)
);

ALTER TABLE batch_tracking ENABLE ROW LEVEL SECURITY;
CREATE POLICY batch_tenant_isolation ON batch_tracking
    USING (tenant_id = current_tenant_id())
    WITH CHECK (tenant_id = current_tenant_id());

-- 12. TDS/TCS Entries — Tax deducted/collected at source
CREATE TABLE tds_tcs_entries (
    id                  BIGSERIAL       PRIMARY KEY,
    tenant_id           TEXT            NOT NULL DEFAULT current_tenant_id(),
    entry_type          VARCHAR(3)      NOT NULL,
    section_code        VARCHAR(20)     NOT NULL,
    party_name          VARCHAR(255)    NOT NULL,
    party_pan           VARCHAR(20),
    transaction_date    DATE            NOT NULL,
    taxable_amount      DECIMAL(19,4)   NOT NULL,
    tax_rate            DECIMAL(8,4)    NOT NULL,
    tax_amount          DECIMAL(19,4)   NOT NULL,
    certificate_number  VARCHAR(100),
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    journal_transaction_id BIGINT       REFERENCES journal_transactions(id),
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_tds_tcs_type CHECK (entry_type IN ('TDS', 'TCS')),
    CONSTRAINT chk_tds_tcs_status CHECK (
        status IN ('PENDING', 'DEDUCTED', 'DEPOSITED', 'FILED')
    )
);

ALTER TABLE tds_tcs_entries ENABLE ROW LEVEL SECURITY;
CREATE POLICY tds_tcs_tenant_isolation ON tds_tcs_entries
    USING (tenant_id = current_tenant_id())
    WITH CHECK (tenant_id = current_tenant_id());

-- 13. Cheque Register — Cheque management
CREATE TABLE cheque_entries (
    id                  BIGSERIAL       PRIMARY KEY,
    tenant_id           TEXT            NOT NULL DEFAULT current_tenant_id(),
    cheque_number       VARCHAR(50)     NOT NULL,
    bank_account_id     BIGINT          NOT NULL REFERENCES ledger_accounts(id),
    party_name          VARCHAR(255)    NOT NULL,
    amount              DECIMAL(19,4)   NOT NULL,
    cheque_date         DATE            NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'ISSUED',
    cheque_type         VARCHAR(20)     NOT NULL DEFAULT 'PAYMENT',
    clearing_date       DATE,
    bounce_reason       VARCHAR(255),
    journal_transaction_id BIGINT       REFERENCES journal_transactions(id),
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_cheque_status CHECK (
        status IN ('ISSUED', 'CLEARED', 'BOUNCED', 'CANCELLED', 'STALE')
    ),
    CONSTRAINT chk_cheque_type CHECK (cheque_type IN ('PAYMENT', 'RECEIPT')),
    UNIQUE (tenant_id, cheque_number, bank_account_id)
);

ALTER TABLE cheque_entries ENABLE ROW LEVEL SECURITY;
CREATE POLICY cheque_tenant_isolation ON cheque_entries
    USING (tenant_id = current_tenant_id())
    WITH CHECK (tenant_id = current_tenant_id());

-- 14. Re-order Levels — Automated stock alerts
CREATE TABLE reorder_levels (
    id                  BIGSERIAL       PRIMARY KEY,
    tenant_id           TEXT            NOT NULL DEFAULT current_tenant_id(),
    stock_item_id       BIGINT          NOT NULL REFERENCES stock_items(id),
    godown_id           BIGINT          REFERENCES godowns(id),
    minimum_level       DECIMAL(19,4)   NOT NULL DEFAULT 0,
    reorder_level       DECIMAL(19,4)   NOT NULL DEFAULT 0,
    maximum_level       DECIMAL(19,4)   NOT NULL DEFAULT 0,
    reorder_quantity    DECIMAL(19,4)   NOT NULL DEFAULT 0,
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, stock_item_id, godown_id)
);

ALTER TABLE reorder_levels ENABLE ROW LEVEL SECURITY;
CREATE POLICY reorder_tenant_isolation ON reorder_levels
    USING (tenant_id = current_tenant_id())
    WITH CHECK (tenant_id = current_tenant_id());

-- 15. Payroll Employees — Employee master
CREATE TABLE payroll_employees (
    id                  BIGSERIAL       PRIMARY KEY,
    tenant_id           TEXT            NOT NULL DEFAULT current_tenant_id(),
    employee_code       VARCHAR(50)     NOT NULL,
    employee_name       VARCHAR(255)    NOT NULL,
    designation         VARCHAR(100),
    department          VARCHAR(100),
    date_of_joining     DATE            NOT NULL,
    pan_number          VARCHAR(20),
    bank_account        VARCHAR(50),
    salary_account_id   BIGINT          REFERENCES ledger_accounts(id),
    employee_group      VARCHAR(100)    DEFAULT 'DEFAULT',
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, employee_code)
);

ALTER TABLE payroll_employees ENABLE ROW LEVEL SECURITY;
CREATE POLICY payroll_employee_tenant_isolation ON payroll_employees
    USING (tenant_id = current_tenant_id())
    WITH CHECK (tenant_id = current_tenant_id());

-- 16. Payroll Components — Salary structure (Basic, HRA, PF, ESI, etc.)
CREATE TABLE payroll_components (
    id                  BIGSERIAL       PRIMARY KEY,
    tenant_id           TEXT            NOT NULL DEFAULT current_tenant_id(),
    employee_id         BIGINT          NOT NULL REFERENCES payroll_employees(id),
    component_name      VARCHAR(100)    NOT NULL,
    component_type      VARCHAR(20)     NOT NULL,
    amount              DECIMAL(19,4)   NOT NULL DEFAULT 0,
    percentage_of_basic DECIMAL(8,4),
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_component_type CHECK (
        component_type IN ('EARNING', 'DEDUCTION', 'EMPLOYER_CONTRIBUTION')
    )
);

ALTER TABLE payroll_components ENABLE ROW LEVEL SECURITY;
CREATE POLICY payroll_component_tenant_isolation ON payroll_components
    USING (tenant_id = current_tenant_id())
    WITH CHECK (tenant_id = current_tenant_id());

-- 17. Connected Payments — Bank payment initiation records
CREATE TABLE connected_payments (
    id                  BIGSERIAL       PRIMARY KEY,
    tenant_id           TEXT            NOT NULL DEFAULT current_tenant_id(),
    bank_account_id     BIGINT          NOT NULL REFERENCES ledger_accounts(id),
    beneficiary_name    VARCHAR(255)    NOT NULL,
    beneficiary_account VARCHAR(50)     NOT NULL,
    ifsc_code           VARCHAR(20),
    amount              DECIMAL(19,4)   NOT NULL,
    payment_mode        VARCHAR(20)     NOT NULL DEFAULT 'NEFT',
    reference_number    VARCHAR(100),
    status              VARCHAR(20)     NOT NULL DEFAULT 'INITIATED',
    initiated_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    completed_at        TIMESTAMPTZ,
    failure_reason      VARCHAR(255),
    journal_transaction_id BIGINT       REFERENCES journal_transactions(id),
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_payment_mode CHECK (
        payment_mode IN ('NEFT', 'RTGS', 'IMPS', 'UPI')
    ),
    CONSTRAINT chk_payment_status CHECK (
        status IN ('INITIATED', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED')
    )
);

ALTER TABLE connected_payments ENABLE ROW LEVEL SECURITY;
CREATE POLICY connected_payment_tenant_isolation ON connected_payments
    USING (tenant_id = current_tenant_id())
    WITH CHECK (tenant_id = current_tenant_id());
