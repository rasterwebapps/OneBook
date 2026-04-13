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

-- 2. Check if financial_events table exists and migrate data
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'financial_events') THEN
        -- Migrate orphan financial_events into payment_register
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
        FROM financial_events fe;

        -- Drop the old financial_events table
        DROP TABLE IF EXISTS financial_events CASCADE;
    END IF;
END $$;

-- 3. Generate event_uuid for any rows that still lack one
UPDATE payment_register SET event_uuid = gen_random_uuid() WHERE event_uuid IS NULL;

-- 4. Make event_uuid NOT NULL and add UNIQUE constraint if not exists
ALTER TABLE payment_register ALTER COLUMN event_uuid SET NOT NULL;
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_payment_register_event_uuid') THEN
        ALTER TABLE payment_register ADD CONSTRAINT uq_payment_register_event_uuid UNIQUE (event_uuid);
    END IF;
END $$;

-- 5. Add useful index on event_uuid if not exists
CREATE INDEX IF NOT EXISTS idx_payment_register_event_uuid ON payment_register(event_uuid);
