---
name: create-flyway-migration
description: >-
  Create a new Flyway SQL migration file for OneBook with proper naming convention,
  RLS policies, tenant isolation, and PostgreSQL 17 best practices.
---

# Create Flyway Migration

Create a new Flyway database migration following OneBook conventions.

## When to Use

- Adding a new database table
- Modifying an existing table (columns, constraints, indexes)
- Adding or updating RLS policies
- Creating database functions or triggers
- Adding seed/reference data

## Steps

### 1. Determine the Next Migration Version

Check existing migrations to find the next available version number:

```bash
ls backend/src/main/resources/db/migration/ | sort -V | tail -5
```

Current migrations: V1–V14 (V12 intentionally skipped). Next available: **V15**.

### 2. Create the Migration File

File naming convention: `V{N}__{description}.sql` (two underscores).

Create the file at: `backend/src/main/resources/db/migration/V{N}__{description}.sql`

### 3. Use the Standard Table Template

Every new table MUST include:

```sql
-- V{N}: {Description}
-- Tables: {list of tables created/modified}
-- RLS: yes
-- Author: @database agent

CREATE TABLE {table_name} (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    -- domain-specific columns here
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Multi-tenant isolation (REQUIRED for every tenant-scoped table)
ALTER TABLE {table_name} ENABLE ROW LEVEL SECURITY;
CREATE POLICY {table_name}_tenant_isolation ON {table_name}
    USING (tenant_id = current_setting('app.tenant_id', true));

-- Indexes
CREATE INDEX idx_{table_name}_tenant ON {table_name}(tenant_id);
```

### 4. Column Type Rules

| Data Type | SQL Type | Notes |
|-----------|----------|-------|
| Money/amounts | `NUMERIC(19,4)` | Never FLOAT/DOUBLE |
| Timestamps | `TIMESTAMPTZ` | Never TIMESTAMP without TZ |
| Tenant ID | `VARCHAR(255) NOT NULL` | Every table |
| Primary key | `BIGSERIAL` | Auto-increment |
| Encrypted text | `TEXT` | Stores Base64 ciphertext |
| Metadata | `JSONB` | Flexible JSON |
| Status/enums | `VARCHAR(50)` | Short strings |

### 5. Validate

- Verify RLS policy exists for every tenant-scoped table
- Verify `NUMERIC(19,4)` for monetary columns
- Verify `TIMESTAMPTZ` for all timestamp columns
- Verify naming follows `snake_case` convention

## References

- Existing migrations: `backend/src/main/resources/db/migration/`
- Database agent: `.github/agents/database.agent.md`
- Schema docs: `docs/technical/sql-schema.md`
