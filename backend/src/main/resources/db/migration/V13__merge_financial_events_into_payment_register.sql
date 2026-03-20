-- V13: Merge financial_events into payment_register (single unified table)
-- FinancialEvent and PaymentRegisterEntry are now one entity: PaymentRegisterEntry.

-- 1. Add ingestion columns to payment_register
ALTER TABLE payment_register ADD COLUMN IF NOT EXISTS event_uuid    UUID;
ALTER TABLE payment_register ADD COLUMN IF NOT EXISTS adapter_type  VARCHAR(30);
ALTER TABLE payment_register ADD COLUMN IF NOT EXISTS event_type    VARCHAR(100);
ALTER TABLE payment_register ADD COLUMN IF NOT EXISTS description   TEXT;
ALTER TABLE payment_register ADD COLUMN IF NOT EXISTS event_date    DATE;
ALTER TABLE payment_register ADD COLUMN IF NOT EXISTS source_reference VARCHAR(255);
ALTER TABLE payment_register ADD COLUMN IF NOT EXISTS debit_account_code  VARCHAR(50);
ALTER TABLE payment_register ADD COLUMN IF NOT EXISTS credit_account_code VARCHAR(50);
ALTER TABLE payment_register ADD COLUMN IF NOT EXISTS raw_payload   TEXT;
ALTER TABLE payment_register ADD COLUMN IF NOT EXISTS industry_tags TEXT DEFAULT '{}';
ALTER TABLE payment_register ADD COLUMN IF NOT EXISTS error_message TEXT;

-- 2. Back-fill ingestion columns for rows already linked to financial_events
UPDATE payment_register pr SET
    event_uuid         = fe.event_uuid,
    adapter_type       = fe.adapter_type,
    event_type         = fe.event_type,
    description        = fe.description,
    event_date         = fe.event_date,
    source_reference   = fe.source_reference,
    debit_account_code = fe.debit_account_code,
    credit_account_code= fe.credit_account_code,
    raw_payload        = fe.raw_payload,
    industry_tags      = fe.industry_tags,
    error_message      = fe.error_message
FROM financial_events fe
WHERE pr.event_id = fe.id;

-- 3. Migrate orphan financial_events (no linked payment_register row) into payment_register
INSERT INTO payment_register (
    tenant_id, event_uuid, adapter_type, event_type, description, event_date,
    source_reference, debit_account_code, credit_account_code, raw_payload,
    industry_tags, error_message, amount, currency, status, created_at, updated_at
)
SELECT
    fe.tenant_id, fe.event_uuid, fe.adapter_type, fe.event_type, fe.description,
    fe.event_date, fe.source_reference, fe.debit_account_code, fe.credit_account_code,
    fe.raw_payload, fe.industry_tags, fe.error_message, fe.amount, fe.currency,
    CASE fe.status
        WHEN 'RECEIVED'  THEN 'RECEIVED'
        WHEN 'VALIDATED' THEN 'VALIDATED'
        WHEN 'MAPPED'    THEN 'VALIDATED'
        WHEN 'POSTED'    THEN 'POSTED'
        WHEN 'FAILED'    THEN 'FAILED'
        WHEN 'REJECTED'  THEN 'REJECTED'
        ELSE 'RECEIVED'
    END,
    fe.created_at, fe.updated_at
FROM financial_events fe
WHERE NOT EXISTS (SELECT 1 FROM payment_register pr WHERE pr.event_id = fe.id);

-- 4. Generate event_uuid for any rows that still lack one
UPDATE payment_register SET event_uuid = gen_random_uuid() WHERE event_uuid IS NULL;

-- 5. Make event_uuid NOT NULL and UNIQUE
ALTER TABLE payment_register ALTER COLUMN event_uuid SET NOT NULL;
ALTER TABLE payment_register ADD CONSTRAINT uq_payment_register_event_uuid UNIQUE (event_uuid);

-- 6. Drop the event_id FK column (no longer needed)
DROP INDEX IF EXISTS idx_payment_register_event_id;
ALTER TABLE payment_register DROP COLUMN IF EXISTS event_id;

-- 7. Drop the old financial_events table
DROP TABLE IF EXISTS financial_events CASCADE;

-- 8. Add useful index on event_uuid
CREATE INDEX IF NOT EXISTS idx_payment_register_event_uuid ON payment_register(event_uuid);
