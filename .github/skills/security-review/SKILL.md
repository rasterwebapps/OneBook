---
name: security-review
description: >-
  Perform a comprehensive security review of OneBook code changes. Checks AES-256-GCM
  encryption, RLS policies, blind indexes, audit trail integrity, secret management,
  and OWASP top-10 vulnerabilities.
---

# Security Review

Perform a comprehensive security review of code changes against OneBook's zero-trust security model.

## When to Use

- After adding new database tables with sensitive data
- After adding new API endpoints
- After modifying authentication or authorization
- After changing encryption or key management code
- As part of the SDLC Phase 4 (@security review)

## Steps

### 1. RLS Policy Verification

Check that every new tenant-scoped table has Row-Level Security enabled:

```sql
-- Every tenant table MUST have:
ALTER TABLE {table_name} ENABLE ROW LEVEL SECURITY;
CREATE POLICY {table_name}_tenant_isolation ON {table_name}
    USING (tenant_id = current_setting('app.tenant_id', true));
```

**Check command:**
```bash
# Find tables without RLS in migrations
grep -rn "CREATE TABLE" backend/src/main/resources/db/migration/ | while read line; do
  table=$(echo "$line" | grep -oP 'CREATE TABLE \K\w+')
  if ! grep -q "ENABLE ROW LEVEL SECURITY" "$(echo "$line" | cut -d: -f1)"; then
    echo "WARNING: $table may be missing RLS"
  fi
done
```

### 2. Encryption Verification

For fields containing sensitive data (PII, financial details):

- Verify `@Convert(converter = EncryptedStringConverter.class)` annotation on entity field
- Verify corresponding blind index column exists (for searchable encrypted fields)
- Verify `TEXT` column type for encrypted storage
- Verify `VARCHAR(64)` for blind index columns

**Wire format check:** `[version byte][IV (12 bytes)][ciphertext+tag]` → Base64

### 3. Secret Management Verification

```bash
# Check for hardcoded secrets
grep -rn "password\|secret\|key\|token" backend/src/main/resources/application.yml | \
  grep -v "^\s*#" | grep -v "\${" | grep -v "master-key"
```

**Rules:**
- Keys MUST come from environment variables: `${ENV_VAR:default}`
- Default values are ONLY for local development
- NEVER commit real secrets

### 4. Authentication/Authorization Review

For new API endpoints:
- Verify endpoints are protected by authentication (Keycloak OIDC)
- Verify role-based access control where needed
- Verify auditor portal endpoints are GET-only (read-only)
- Verify tokens stored in memory only (never localStorage)

### 5. Audit Trail Verification

For operations on sensitive data:
- Verify audit log entries are created
- Verify hash chain integrity: `hash = SHA-256(previous_hash + current_record_data)`
- Verify no plaintext sensitive data in log messages

### 6. Automated Security Checks

Run the 5 automated security checks:

1. **RLS enabled** on all tenant-scoped tables
2. **Encrypted fields** have corresponding blind indexes
3. **Audit chain** hash verification passes
4. **No hardcoded secrets** in configuration files
5. **CORS** not set to `*` in production

### 7. OWASP Top-10 Quick Check

- [ ] Injection: Parameterized queries only (JPA/JPQL)
- [ ] Broken Auth: Keycloak OIDC with proper token validation
- [ ] Sensitive Data Exposure: AES-256-GCM encryption at rest
- [ ] XXE: No XML processing (JSON only)
- [ ] Broken Access Control: RLS + tenant isolation
- [ ] Security Misconfiguration: No debug in production
- [ ] XSS: Angular template auto-escaping
- [ ] Insecure Deserialization: Jackson with type safety
- [ ] Known Vulnerabilities: Dependency scanning in CI
- [ ] Insufficient Logging: Structured logging with MDC

## References

- Security agent: `.github/agents/security.agent.md`
- Blind DBA model: `memory-bank/systempatterns.md`
- NIST SP 800-38D (AES-GCM), NIST SP 800-57 (Key Management)
