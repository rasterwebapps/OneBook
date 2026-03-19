-- Payment Register table: individual invoice/debit notes awaiting payment
CREATE TABLE payment_register (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           VARCHAR(255) NOT NULL,
    vendor_account_id   BIGINT,
    vendor_name         VARCHAR(255),
    source_type         VARCHAR(100),
    source_reference_id VARCHAR(255),
    transaction_type    VARCHAR(50),
    invoice_number      VARCHAR(100),
    invoice_date        DATE,
    due_date            DATE,
    amount              NUMERIC(19, 4),
    currency            VARCHAR(3),
    payment_mode        VARCHAR(50),
    bank_account_number VARCHAR(100),
    bank_ifsc_code      VARCHAR(20),
    bank_name           VARCHAR(255),
    status              VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE_FOR_PROCESSING',
    batch_id            BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Payment Batches table: grouped payments awaiting approval
CREATE TABLE payment_batches (
    id                      BIGSERIAL PRIMARY KEY,
    tenant_id               VARCHAR(255) NOT NULL,
    batch_number            VARCHAR(100) NOT NULL,
    vendor_account_id       BIGINT,
    vendor_name             VARCHAR(255),
    total_purchases         NUMERIC(19, 4) DEFAULT 0,
    total_returns           NUMERIC(19, 4) DEFAULT 0,
    total_credit_notes      NUMERIC(19, 4) DEFAULT 0,
    net_payable             NUMERIC(19, 4) DEFAULT 0,
    bank_account_id         BIGINT,
    payment_mode            VARCHAR(50),
    status                  VARCHAR(50) NOT NULL DEFAULT 'PENDING_APPROVAL',
    created_by              VARCHAR(255),
    approved_by             VARCHAR(255),
    approved_at             TIMESTAMPTZ,
    rejected_by             VARCHAR(255),
    rejected_at             TIMESTAMPTZ,
    rejection_reason        TEXT,
    payment_journal_id      BIGINT,
    payment_file_generated  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, batch_number)
);

-- Payment Batch Items table: line items within each batch
CREATE TABLE payment_batch_items (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           VARCHAR(255) NOT NULL,
    batch_id            BIGINT NOT NULL REFERENCES payment_batches(id),
    register_entry_id   BIGINT NOT NULL REFERENCES payment_register(id),
    transaction_type    VARCHAR(50),
    amount              NUMERIC(19, 4),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes for efficient tenant-scoped queries
CREATE INDEX idx_payment_register_tenant_status ON payment_register(tenant_id, status);
CREATE INDEX idx_payment_register_tenant_vendor ON payment_register(tenant_id, vendor_account_id);
CREATE INDEX idx_payment_register_due_date ON payment_register(tenant_id, due_date);
CREATE INDEX idx_payment_batches_tenant_status ON payment_batches(tenant_id, status);
CREATE INDEX idx_payment_batch_items_batch_id ON payment_batch_items(batch_id);

-- Enable RLS on all payment tables
ALTER TABLE payment_register ENABLE ROW LEVEL SECURITY;
ALTER TABLE payment_batches ENABLE ROW LEVEL SECURITY;
ALTER TABLE payment_batch_items ENABLE ROW LEVEL SECURITY;

-- RLS policies: app_user can only see their own tenant's data
CREATE POLICY payment_register_tenant_isolation ON payment_register
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE POLICY payment_batches_tenant_isolation ON payment_batches
    USING (tenant_id = current_setting('app.tenant_id', true));

CREATE POLICY payment_batch_items_tenant_isolation ON payment_batch_items
    USING (tenant_id = current_setting('app.tenant_id', true));
