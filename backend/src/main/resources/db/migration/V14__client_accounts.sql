-- V14: Client Accounts — party sub-ledger for customers, vendors, employees, intercompany
-- Follows project conventions: tenant_id, TIMESTAMPTZ, RLS, tenant-scoped indexes, NUMERIC(19,4)

CREATE TABLE IF NOT EXISTS client_accounts (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       VARCHAR(64)  NOT NULL,
    ledger_account_id BIGINT     NOT NULL REFERENCES ledger_accounts(id),
    client_type     VARCHAR(20)  NOT NULL,
    client_name     VARCHAR(255) NOT NULL,
    contact_person  VARCHAR(255),
    email           VARCHAR(255),
    phone           VARCHAR(50),
    billing_address TEXT,
    shipping_address TEXT,
    gstin           VARCHAR(20),
    pan             VARCHAR(10),
    credit_limit    NUMERIC(19,4),
    payment_terms_days INTEGER,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- RLS policy
ALTER TABLE client_accounts ENABLE ROW LEVEL SECURITY;
CREATE POLICY client_accounts_tenant_isolation ON client_accounts
    USING (tenant_id = current_setting('app.tenant_id', true));

-- Tenant-scoped indexes
CREATE INDEX idx_client_accounts_tenant ON client_accounts(tenant_id);
CREATE INDEX idx_client_accounts_tenant_type ON client_accounts(tenant_id, client_type);
CREATE INDEX idx_client_accounts_tenant_ledger ON client_accounts(tenant_id, ledger_account_id);
CREATE INDEX idx_client_accounts_tenant_active ON client_accounts(tenant_id, is_active);
