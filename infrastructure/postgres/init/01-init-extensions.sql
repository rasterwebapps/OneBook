-- ============================================================
-- OneBook PostgreSQL Initialization Script
-- ⚠️  DEVELOPMENT ONLY — runs once when the container is first created.
-- For production, use Flyway migrations and secure credentials.
-- ============================================================

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- Row-Level Security helper function
-- Sets the current tenant context for RLS policies.
-- Application must call: SET LOCAL app.tenant_id = '<uuid>';
-- ============================================================
CREATE OR REPLACE FUNCTION set_tenant_context(p_tenant_id TEXT)
RETURNS VOID AS $$
BEGIN
    IF p_tenant_id IS NULL OR length(p_tenant_id) = 0 THEN
        RAISE EXCEPTION 'tenant_id must not be null or empty';
    END IF;
    PERFORM set_config('app.tenant_id', p_tenant_id, true);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Grant execution to the application user
GRANT EXECUTE ON FUNCTION set_tenant_context(TEXT) TO onebook;
