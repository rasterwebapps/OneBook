# REQ-002: Zero-Knowledge Encryption

**Status:** COMPLETED  
**Priority:** CRITICAL  
**Owner:** @SecurityWarden  
**Milestone:** M3  
**Created:** 2026-01-15  
**Last Updated:** 2026-03-18  
**Linked BRD:** [BR-002](../../business/BRD.md#br-002-zero-knowledge-security)  
**Linked FRD:** [FR-003, FR-004](../../business/FRD.md#3-security)  
**Linked TRD:** [TR-002, TR-007](../../business/TRD.md#3-tr-002-field-level-encryption-aes-256-gcm)

---

## Quality Gate Checklist

- [x] Business Context documented
- [x] Functional Specification documented
- [x] Technical Specification documented
- [x] Acceptance Criteria (Gherkin) defined
- [x] Implementation complete
- [x] Unit tests written and passing
- [x] Integration tests written and passing
- [x] BRD updated
- [x] FRD updated
- [x] TRD updated
- [x] RTM updated
- [x] Agent ownership updated

---

## 1. Business Context

### 1.1 Problem Statement
Financial data is among the most sensitive categories of business information. Traditional accounting systems store sensitive fields (party names, amounts, narrations, tax IDs) as plaintext in the database. This creates an insider threat risk: database administrators, cloud provider employees, or attackers who gain database access can read all financial data.

OneBook eliminates this risk with a zero-knowledge model: all sensitive fields are encrypted in the Java application layer before the data reaches the database. Even with direct database access, an attacker sees only Base64-encoded ciphertext.

### 1.2 Business Value
- **Regulatory compliance:** DPDP Act 2023, GDPR, PCI-DSS require encryption of sensitive financial data
- **Insider threat mitigation:** DBA and cloud provider access cannot expose sensitive data
- **Audit defense:** Tamper-evident hash-chained audit trail provides immutable evidence for disputes
- **Trust:** Zero-knowledge guarantee increases enterprise customer confidence

### 1.3 Stakeholders
| Role | Interest |
|------|---------|
| IT Administrators | Key management, rotation procedures |
| Security Auditors | Encryption standard verification, key audit |
| Compliance Officers | DPDP/GDPR compliance evidence |
| C-Suite | Enterprise security posture |

### 1.4 Business Rules
- BR-002.1: Sensitive fields encrypted with AES-256-GCM before storage
- BR-002.2: Blind indexes (HMAC-SHA256) enable equality search on encrypted fields
- BR-002.3: Every audit log entry chained by SHA-256 hash
- BR-002.4: Keys stored in environment variables only — never in source code
- BR-002.5: Key rotation supported without data loss

---

## 2. Functional Specification

### 2.1 Feature Description
All sensitive fields (narrations, party names, PAN numbers, amounts above threshold) are encrypted at the JPA `@Convert` layer using `EncryptedStringConverter`. This is transparent to service-layer code — fields appear as plaintext when read. For searchable encrypted fields, a parallel blind index column stores `HMAC-SHA256(plaintext)` for equality queries.

### 2.2 User Flows

**Flow 1: Encrypting a journal entry narration on save**
```
Step 1: JournalService calls repository.save(journalEntry)
Step 2: Hibernate calls EncryptedStringConverter.convertToDatabaseColumn(narration)
Step 3: FieldEncryptionService generates 12-byte random IV
Step 4: AES-256-GCM encrypts plaintext with current key + IV
Step 5: Wire format: Base64(version_byte + IV + ciphertext) stored to DB
Step 6: Blind index computed: HMAC-SHA256(blindKey, normalize(narration)) stored to DB
```

**Flow 2: Decrypting on read**
```
Step 1: Repository loads JournalEntry from DB
Step 2: Hibernate calls EncryptedStringConverter.convertToEntityAttribute(ciphertext)
Step 3: FieldEncryptionService decodes Base64 → extracts version byte, IV, ciphertext
Step 4: Resolves key by version byte from KeyManagementService
Step 5: AES-256-GCM decrypts with IV and verifies GCM authentication tag
Step 6: Plaintext returned to service layer
```

**Flow 3: Auditor verifies hash chain**
```
Step 1: Auditor calls GET /api/audit/verify/{entityId}
Step 2: AuditLogService loads all entries for entity in sequence
Step 3: For each entry n: computedHash = SHA256(entry[n-1].hash + entry[n].data)
Step 4: If computedHash != stored hash → TAMPERED detected
Step 5: Response reports VALID or TAMPERED with first corrupted index
```

### 2.3 Inputs (Encryption)
| Input | Type | Required | Notes |
|-------|------|----------|-------|
| plaintext | String | Yes | Sensitive field value |
| encryptionKey | byte[32] | Yes | Loaded from env var |
| iv | byte[12] | Yes | SecureRandom-generated per call |

### 2.4 Outputs (Encryption)
| Output | Type | Description |
|--------|------|-------------|
| ciphertext | String | Base64(version + IV + AES-GCM output) |
| blindIndex | String | Base64(HMAC-SHA256(key, normalized_plaintext)) |

### 2.5 Validation Rules
- VR-001: IV must be freshly generated per encryption call (never reused)
- VR-002: GCM auth tag must verify on decryption (throws on tampered ciphertext)
- VR-003: Key version byte must resolve to a known key
- VR-004: Blind index stored but never used for range queries (equality only)
- VR-005: Keys never logged or included in exception messages

### 2.6 API Endpoints (Audit Trail)
```
GET    /api/audit/entries              — List audit entries for tenant
GET    /api/audit/entries/{entityId}   — Audit history for specific entity
GET    /api/audit/verify/{entityId}    — Verify hash chain integrity
```

---

## 3. Technical Specification

### 3.1 Architecture Decisions
- **AES-256-GCM** was chosen over AES-256-CBC because GCM provides both encryption and authentication in one pass (authenticated encryption), eliminating padding oracle attacks.
- **JPA AttributeConverter** pattern was chosen to make encryption transparent to service-layer code. Services work with plaintext `String` fields; conversion is automatic at the ORM layer.
- **Blind indexes** solve the search-on-encrypted-data problem for equality queries. Range queries on encrypted data are intentionally unsupported.

### 3.2 Data Model
```sql
-- V5__blind_dba_infrastructure.sql
ALTER TABLE journal_entries ADD COLUMN narration_encrypted TEXT;
ALTER TABLE journal_entries ADD COLUMN narration_idx VARCHAR(64);

ALTER TABLE ledger_accounts ADD COLUMN account_name_encrypted TEXT;
ALTER TABLE ledger_accounts ADD COLUMN account_name_idx VARCHAR(64);

CREATE TABLE encryption_key_registry (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key_version SMALLINT UNIQUE NOT NULL,
    key_reference VARCHAR(100) NOT NULL,  -- env var name
    is_current BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    actor_id VARCHAR(255),
    payload JSONB,
    previous_hash VARCHAR(64),
    hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### 3.3 Wire Format
```
Byte 0:      version byte (key version, 1 byte)
Bytes 1-12:  random IV (12 bytes, SecureRandom)
Bytes 13-N:  AES-256-GCM ciphertext + 16-byte authentication tag
→ Entire sequence Base64-encoded → stored as VARCHAR
```

### 3.4 Performance Considerations
- AES-GCM operates in ~100ns per field on modern JVMs
- Warm cache eliminates repeated decryption for frequently accessed data
- Blind index lookup: O(1) indexed equality scan
- Key in-memory: loaded once at startup by KeyManagementService

### 3.5 Security Considerations
- Keys: `ONEBOOK_ENCRYPTION_KEY` and `ONEBOOK_BLIND_INDEX_KEY` env vars only
- IV reuse prevention: `SecureRandom.generateSeed()` per operation
- GCM tag verification: throws `AEADBadTagException` on tampered ciphertext
- Hash chain: breaks if any audit entry is modified

---

## 4. Acceptance Criteria

```gherkin
Feature: Zero-Knowledge Field Encryption

  Scenario: Sensitive field encrypted at rest
    Given I post a journal entry with narration "Payment to John Doe"
    When I query the database table journal_entries directly
    Then the narration_encrypted column should contain Base64 ciphertext
    And the ciphertext should NOT contain "John Doe"
    And decoding the Base64 should show version byte + IV + ciphertext

  Scenario: Decryption returns original plaintext
    Given an encrypted journal entry with narration "Payment to John Doe"
    When I GET /api/journal/transactions/{id}
    Then the narration in the JSON response should be "Payment to John Doe"

  Scenario: Two encryptions of same plaintext produce different ciphertexts
    Given plaintext "Test Value"
    When I encrypt it twice with FieldEncryptionService.encrypt()
    Then the two ciphertexts should be different (different IV)
    And both should decrypt to "Test Value"

  Scenario: Search via blind index
    Given 100 accounts exist, 3 with name containing "Acme"
    When I GET /api/ledger/accounts/search?q=Acme
    Then exactly 3 accounts should be returned
    And no database decryption should have occurred (blind index used)

  Scenario: Audit hash chain verified as valid
    Given 20 journal entries posted sequentially
    When I GET /api/audit/verify/{entityId}
    Then the response should state "integrity: VALID"
    And all 20 hash entries should be reported as valid

  Scenario: Tampered audit entry detected
    Given 10 audit log entries exist for entity X
    When I directly update entry 5's payload in the database
    And I GET /api/audit/verify/X
    Then the response should state "integrity: TAMPERED"
    And the tampered entry index should be reported as 5
```

---

## 5. Implementation

### 5.1 New Files Created
| File | Package | Purpose |
|------|---------|---------|
| `FieldEncryptionService.java` | `com.nexus.onebook.ledger.security` | AES-256-GCM encrypt/decrypt |
| `BlindIndexService.java` | `com.nexus.onebook.ledger.security` | HMAC-SHA256 blind index computation |
| `KeyManagementService.java` | `com.nexus.onebook.ledger.security` | Key loading, versioning, rotation |
| `AuditLogService.java` | `com.nexus.onebook.ledger.security` | Hash-chained audit trail |
| `EncryptedStringConverter.java` | `com.nexus.onebook.ledger.security` | JPA AttributeConverter |
| `AuditLog.java` | `com.nexus.onebook.ledger.entity` | Audit log JPA entity |
| `AuditLogRepository.java` | `com.nexus.onebook.ledger.repository` | Audit log repository |

### 5.2 Migration
- Migration file: `V5__blind_dba_infrastructure.sql`
- Tables: `audit_logs`, `encryption_key_registry`
- Columns: `*_encrypted` and `*_idx` blind index columns on sensitive tables

---

## 6. Testing

### 6.1 Unit Tests
| Test Class | Scenario |
|------------|---------|
| `FieldEncryptionServiceTest` | Encrypt/decrypt roundtrip, IV uniqueness, tamper detection |
| `BlindIndexServiceTest` | Deterministic blind index, different plaintexts produce different indexes |
| `AuditLogServiceTest` | Hash chain creation, chain verification, tamper detection |
| `TamperDetectionTest` | End-to-end tamper scenario |

---

## 7. Traceability

| Artifact | Link |
|---------|------|
| BRD | [BR-002](../../business/BRD.md#br-002-zero-knowledge-security) |
| FRD | [FR-003, FR-004](../../business/FRD.md#3-security) |
| TRD | [TR-002, TR-007](../../business/TRD.md#3-tr-002-field-level-encryption-aes-256-gcm) |
| RTM | [RTM Row REQ-002](../RTM.md) |
| User Stories | [US-006, US-007](../../business/user-stories.md) |
| Agent Owner | [@security](../../../.github/agents/security.agent.md) |
| Migration | `V5__blind_dba_infrastructure.sql` |

---

## 8. Change History

| Date | Author | Change |
|------|--------|--------|
| 2026-01-15 | @SecurityWarden | Initial implementation (M3) |
| 2026-03-18 | @RequirementsAnalyzer | Documentation formalized |
