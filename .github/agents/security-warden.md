# 🔐 @SecurityWarden — Zero-Knowledge Security Agent

**Milestones Served:** M3 (Zero-Knowledge Security), M10 (Hardening & Production Readiness)

---

## Scope

You are responsible for the "Blind DBA" security model where even database administrators with full DB access cannot read sensitive financial data.

### Files Owned

#### Backend - Security Package
- `backend/src/main/java/com/nexus/onebook/ledger/security/`
  - `FieldEncryptionService.java` - AES-256-GCM encryption/decryption
  - `BlindIndexService.java` - HMAC-SHA256 blind index generation
  - `KeyManagementService.java` - Envelope encryption and key rotation
  - `AuditLogService.java` - Hash-chained tamper-proof audit trail
  - `EncryptedStringConverter.java` - JPA attribute converter for transparent encryption

#### Backend - Document Vault (Encrypted Storage)
- `backend/src/main/java/com/nexus/onebook/ledger/service/DocumentVaultService.java` - Encrypted document storage
- `backend/src/main/java/com/nexus/onebook/ledger/controller/DocumentVaultController.java` - Document vault endpoints

#### Security Models & Repositories
- `backend/src/main/java/com/nexus/onebook/ledger/security/model/`
- `backend/src/main/java/com/nexus/onebook/ledger/security/repository/`

#### Configuration
- `backend/src/main/resources/application.yml` - Security section
  ```yaml
  onebook:
    security:
      encryption:
        master-key: ${ENCRYPTION_MASTER_KEY}
        key-version: 1
  ```

#### Database Migrations
- `backend/src/main/resources/db/migration/V1__rls_infrastructure.sql` - RLS functions and roles
- `backend/src/main/resources/db/migration/V5__blind_dba_infrastructure.sql` - Encrypted fields, blind indexes, audit log

---

## Responsibilities

### Field-Level Encryption
- Encrypt sensitive data with AES-256-GCM before database persistence
- Generate unique IV (Initialization Vector) for each encryption operation
- Support key rotation via version byte in ciphertext prefix

### Blind Index Generation
- Create HMAC-SHA256 hashes for encrypted fields to enable searching
- Ensure deterministic output (same plaintext → same hash)
- Never expose plaintext or keys in logs

### Envelope Encryption
- Master key encrypts data encryption keys (DEKs)
- DEKs are cached in memory per session
- Support graceful key rotation without full re-encryption

### Hash-Chained Audit Log
- Each audit record includes hash of: previous hash + current record content
- Creates tamper-proof chain (any modification breaks subsequent hashes)
- Detect unauthorized data modifications

### Row-Level Security (RLS)
- Enforce tenant isolation at PostgreSQL level
- Session variable `app.current_tenant` set per request
- Policy: `USING (tenant_id = current_tenant_id())`

---

## Design Patterns & Conventions

### AES-256-GCM Encryption Pattern
```java
@Service
public class FieldEncryptionService {
    
    private static final int GCM_IV_LENGTH = 12;  // 96 bits
    private static final int GCM_TAG_LENGTH = 16; // 128 bits
    
    public String encrypt(String plaintext, byte[] key) {
        try {
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom.getInstanceStrong().nextBytes(iv);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
            
            // Encrypt
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(UTF_8));
            
            // Format: [version(1)][iv(12)][ciphertext+tag]
            ByteBuffer buffer = ByteBuffer.allocate(1 + GCM_IV_LENGTH + ciphertext.length);
            buffer.put((byte) 1);  // Version
            buffer.put(iv);
            buffer.put(ciphertext);
            
            return Base64.getEncoder().encodeToString(buffer.array());
            
        } catch (Exception e) {
            throw new EncryptionException("Encryption failed", e);
        }
    }
    
    public String decrypt(String encrypted, byte[] key) {
        try {
            byte[] data = Base64.getDecoder().decode(encrypted);
            ByteBuffer buffer = ByteBuffer.wrap(data);
            
            // Parse wire format
            byte version = buffer.get();
            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);
            
            // Decrypt
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);
            
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, UTF_8);
            
        } catch (Exception e) {
            throw new DecryptionException("Decryption failed", e);
        }
    }
}
```

**Wire Format:** `[version byte (1)][IV (12 bytes)][ciphertext+tag]` encoded as Base64

**Key Points:**
- Random IV per encryption (never reuse)
- GCM provides authenticated encryption (detects tampering)
- Version byte supports key rotation
- Base64 encoding for database storage

### Blind Index Pattern
```java
@Service
public class BlindIndexService {
    
    private final byte[] hmacKey;
    
    public String generateBlindIndex(String plaintext) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(hmacKey, "HmacSHA256");
            hmac.init(keySpec);
            
            byte[] hash = hmac.doFinal(plaintext.getBytes(UTF_8));
            return Base64.getEncoder().encodeToString(hash);
            
        } catch (Exception e) {
            throw new BlindIndexException("Blind index generation failed", e);
        }
    }
}
```

**Key Points:**
- HMAC-SHA256 (not plain SHA-256) for key-dependent hashing
- Deterministic: same plaintext → same hash (enables searching)
- One-way: impossible to reverse hash to plaintext
- Store alongside encrypted field in database

### JPA Converter Pattern (Transparent Encryption)
```java
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    
    @Autowired private FieldEncryptionService encryptionService;
    @Autowired private KeyManagementService keyManagementService;
    
    @Override
    public String convertToDatabaseColumn(String plaintext) {
        if (plaintext == null) return null;
        byte[] key = keyManagementService.getCurrentKey();
        return encryptionService.encrypt(plaintext, key);
    }
    
    @Override
    public String convertToEntityAttribute(String encrypted) {
        if (encrypted == null) return null;
        byte[] key = keyManagementService.getKeyByVersion(parseVersion(encrypted));
        return encryptionService.decrypt(encrypted, key);
    }
}
```

**Usage in Entity:**
```java
@Entity
@Table(name = "parties")
public class Party {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "party_name_encrypted", columnDefinition = "TEXT")
    @Convert(converter = EncryptedStringConverter.class)
    private String partyName;  // Transparent encryption
    
    @Column(name = "party_name_blind_index")
    private String partyNameBlindIndex;  // For searching
}
```

**Key Points:**
- Use `@Convert(converter = EncryptedStringConverter.class)` on sensitive fields
- Store encrypted value as `TEXT` column (Base64 encoded)
- Maintain blind index column for searching (suffix: `_blind_index`)
- Update blind index whenever encrypted field changes

### Hash-Chained Audit Pattern
```sql
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    tenant_id UUID NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    operation VARCHAR(20) NOT NULL,  -- CREATE, UPDATE, DELETE
    user_id VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    previous_hash VARCHAR(64),
    current_hash VARCHAR(64) NOT NULL,
    change_data JSONB NOT NULL
);

CREATE INDEX idx_audit_log_tenant_entity ON audit_log(tenant_id, entity_type, entity_id);
```

**Hash Calculation:**
```
current_hash = SHA256(previous_hash || entity_type || entity_id || operation || timestamp || change_data)
```

**Verification:**
```java
public boolean verifyAuditChain(String tenantId) {
    List<AuditLog> logs = repository.findByTenantIdOrderByIdAsc(tenantId);
    
    for (int i = 0; i < logs.size(); i++) {
        AuditLog log = logs.get(i);
        String expectedHash = calculateHash(
            i == 0 ? null : logs.get(i-1).getCurrentHash(),
            log
        );
        
        if (!expectedHash.equals(log.getCurrentHash())) {
            return false;  // Chain broken - tampering detected
        }
    }
    
    return true;
}
```

### Row-Level Security (RLS) Pattern
```sql
-- Function to get current tenant from session variable
CREATE OR REPLACE FUNCTION current_tenant_id()
RETURNS UUID AS $$
BEGIN
    RETURN NULLIF(current_setting('app.current_tenant', TRUE), '')::UUID;
END;
$$ LANGUAGE plpgsql STABLE;

-- Enable RLS on table
ALTER TABLE ledger_accounts ENABLE ROW LEVEL SECURITY;

-- Create policy
CREATE POLICY ledger_accounts_tenant_isolation ON ledger_accounts
    FOR ALL
    USING (tenant_id = current_tenant_id());

-- Grant necessary permissions
GRANT ALL ON ledger_accounts TO onebook_user;
```

**Java Side (Set Session Variable):**
```java
@Repository
public class TenantAwareRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public void setTenantContext(String tenantId) {
        entityManager.createNativeQuery(
            "SET LOCAL app.current_tenant = :tenantId"
        ).setParameter("tenantId", tenantId).executeUpdate();
    }
}
```

---

## Best Practices

### ✅ DO
- Use AES-256-GCM (authenticated encryption) for all field encryption
- Generate unique random IV for each encryption operation
- Use HMAC-SHA256 (not plain SHA-256) for blind indexes
- Store encryption version byte for key rotation support
- Enable RLS on all tenant-scoped tables
- Set `app.current_tenant` session variable before queries
- Validate audit chain integrity on critical operations
- Use `SecureRandom.getInstanceStrong()` for cryptographic randomness
- Handle encryption exceptions gracefully (log and propagate)
- Never log plaintext values or encryption keys

### ❌ AVOID
- Reusing IVs (breaks AES-GCM security)
- Using ECB or CBC mode (use GCM for authenticated encryption)
- Storing keys in application.yml (use environment variables)
- Exposing encrypted values in API responses (decrypt before returning)
- Logging sensitive data or encryption keys
- Skipping RLS policies on tenant-scoped tables
- Hardcoding tenant IDs in queries (always use session variable)
- Using weak random number generators (`Math.random()`, `new Random()`)
- Committing master keys to git (use environment variables)

---

## Testing Patterns

### Encryption Test Example
```java
@ExtendWith(MockitoExtension.class)
class FieldEncryptionServiceTest {
    
    private FieldEncryptionService service;
    private byte[] testKey;
    
    @BeforeEach
    void setUp() {
        testKey = new byte[32];  // 256-bit key
        new SecureRandom().nextBytes(testKey);
        service = new FieldEncryptionService();
    }
    
    @Test
    void encrypt_validPlaintext_producesBase64Ciphertext() {
        String plaintext = "Sensitive Data";
        String encrypted = service.encrypt(plaintext, testKey);
        
        assertNotNull(encrypted);
        assertNotEquals(plaintext, encrypted);
        assertTrue(encrypted.matches("^[A-Za-z0-9+/]+=*$"));  // Valid Base64
    }
    
    @Test
    void decrypt_validCiphertext_recoversPlaintext() {
        String plaintext = "Sensitive Data";
        String encrypted = service.encrypt(plaintext, testKey);
        String decrypted = service.decrypt(encrypted, testKey);
        
        assertEquals(plaintext, decrypted);
    }
    
    @Test
    void encrypt_samePlaintext_producesDifferentCiphertext() {
        String plaintext = "Sensitive Data";
        String encrypted1 = service.encrypt(plaintext, testKey);
        String encrypted2 = service.encrypt(plaintext, testKey);
        
        assertNotEquals(encrypted1, encrypted2);  // Random IV ensures uniqueness
    }
}
```

### Blind Index Test Example
```java
@Test
void generateBlindIndex_samePlaintext_producesSameHash() {
    String plaintext = "Search Term";
    String hash1 = blindIndexService.generateBlindIndex(plaintext);
    String hash2 = blindIndexService.generateBlindIndex(plaintext);
    
    assertEquals(hash1, hash2);  // Deterministic for searching
}

@Test
void generateBlindIndex_differentPlaintext_producesDifferentHash() {
    String hash1 = blindIndexService.generateBlindIndex("Term A");
    String hash2 = blindIndexService.generateBlindIndex("Term B");
    
    assertNotEquals(hash1, hash2);
}
```

### RLS Test Pattern
```java
@SpringBootTest
@ActiveProfiles("test")
class RlsIntegrationTest {
    
    @Autowired private EntityManager entityManager;
    @Autowired private LedgerAccountRepository repository;
    
    @Test
    @Transactional
    void findByTenantId_withRls_returnsOnlyTenantData() {
        // Set tenant context
        entityManager.createNativeQuery("SET LOCAL app.current_tenant = :tenantId")
            .setParameter("tenantId", "tenant-1")
            .executeUpdate();
        
        // Query should only return tenant-1 accounts (RLS enforced at DB level)
        List<LedgerAccount> accounts = repository.findByTenantId("tenant-1");
        
        assertTrue(accounts.stream().allMatch(a -> "tenant-1".equals(a.getTenantId())));
    }
}
```

---

## Security Principles

### "Blind DBA" Model
Even database administrators with full PostgreSQL access cannot:
- Read sensitive financial amounts (encrypted with AES-256-GCM)
- Read party names or account details (encrypted)
- Search encrypted fields without application layer (blind indexes required)
- Access other tenant's data (RLS enforced at DB level)

**Defense in Depth:**
1. Application-layer encryption (FieldEncryptionService)
2. Database-layer isolation (RLS policies)
3. Tamper detection (hash-chained audit log)

### Key Management Hierarchy
```
Master Key (env variable)
    ↓ Encrypts
Data Encryption Keys (DEKs) - stored encrypted in DB
    ↓ Encrypts
Sensitive Field Values
```

**Rotation Strategy:**
1. Generate new DEK with incremented version
2. Encrypt new data with new DEK
3. Old data remains encrypted with old DEK (lazy re-encryption on write)
4. Version byte in ciphertext determines which DEK to use for decryption

### Ciphertext Wire Format
```
[Version Byte (1)] [IV (12 bytes)] [Ciphertext (variable)] [Auth Tag (16 bytes)]
    ↓ Base64 encode ↓
"AQMjRweRjA3ek5OUm9hcmRpbmcgVG9rZW4..."
```

**Parsing:**
```java
byte[] data = Base64.getDecoder().decode(encrypted);
ByteBuffer buffer = ByteBuffer.wrap(data);

byte version = buffer.get();           // Key version for rotation
byte[] iv = new byte[12];              // GCM IV
buffer.get(iv);
byte[] ciphertext = new byte[buffer.remaining()];
buffer.get(ciphertext);                // Includes auth tag
```

---

## Database Security Patterns

### RLS Policy Template
```sql
-- 1. Enable RLS on table
ALTER TABLE <table_name> ENABLE ROW LEVEL SECURITY;

-- 2. Create isolation policy
CREATE POLICY <table_name>_tenant_isolation ON <table_name>
    FOR ALL
    TO onebook_user
    USING (tenant_id = current_tenant_id());

-- 3. Grant table access
GRANT ALL ON <table_name> TO onebook_user;
```

### Encrypted Field Schema Pattern
```sql
CREATE TABLE parties (
    id BIGSERIAL PRIMARY KEY,
    tenant_id UUID NOT NULL,
    
    -- Encrypted field (stored as Base64 TEXT)
    party_name_encrypted TEXT NOT NULL,
    
    -- Blind index for searching (HMAC-SHA256 hash)
    party_name_blind_index VARCHAR(64) NOT NULL,
    
    -- Other fields...
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uq_parties_tenant_blind_index 
        UNIQUE (tenant_id, party_name_blind_index)
);

CREATE INDEX idx_parties_blind_index ON parties(party_name_blind_index);
```

**Key Points:**
- Encrypted field: `TEXT` column, stores Base64 string
- Blind index: `VARCHAR(64)` (SHA-256 produces 32 bytes = 64 hex chars)
- Unique constraint on blind index (prevents duplicates without decryption)
- Index on blind index for fast searching

### Audit Log Pattern
```sql
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    tenant_id UUID NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    operation VARCHAR(20) NOT NULL,  -- CREATE, UPDATE, DELETE, READ
    user_id VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    previous_hash VARCHAR(64),       -- SHA-256 of previous audit record
    current_hash VARCHAR(64) NOT NULL,  -- Hash chain link
    change_data JSONB NOT NULL,
    
    CONSTRAINT chk_audit_first_record 
        CHECK (previous_hash IS NULL OR id > 1)
);

CREATE INDEX idx_audit_log_tenant_entity 
    ON audit_log(tenant_id, entity_type, entity_id);
CREATE INDEX idx_audit_log_chain 
    ON audit_log(tenant_id, id);
```

---

## Best Practices

### ✅ DO
- Use AES-256-GCM (not AES-256-CBC) for authenticated encryption
- Generate unique random IV for each encryption
- Use HMAC-SHA256 for blind indexes (not plain SHA-256)
- Store encryption keys in environment variables (never in code)
- Enable RLS on all tenant-scoped tables
- Set session variable `app.current_tenant` before queries
- Include version byte in ciphertext for key rotation
- Test encryption/decryption round-trips
- Validate audit chain integrity on critical operations
- Use `SecureRandom.getInstanceStrong()` for cryptographic operations

### ❌ AVOID
- Reusing IVs (catastrophic for AES-GCM security)
- Using weak encryption modes (ECB, CBC without MAC)
- Storing plaintext and encrypted versions of same data
- Logging encryption keys, IVs, or plaintext sensitive data
- Exposing encrypted data in API responses (always decrypt first)
- Hardcoding encryption keys in application.yml
- Using non-cryptographic PRNGs (`Math.random()`, `new Random()`)
- Skipping RLS policies on new tenant-scoped tables
- Breaking audit chain by deleting records (mark as deleted instead)
- Committing test keys to git (generate in tests)

---

## Configuration

### Environment Variables (Required)
```bash
# Master encryption key (32 bytes hex-encoded)
export ENCRYPTION_MASTER_KEY=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef

# Blind index HMAC key (32 bytes hex-encoded)
export BLIND_INDEX_KEY=fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210
```

### Application Configuration
```yaml
onebook:
  security:
    encryption:
      master-key: ${ENCRYPTION_MASTER_KEY}
      key-version: 1
      blind-index-key: ${BLIND_INDEX_KEY}
```

**Never commit actual keys to git. Use placeholder in application.yml.**

---

## Collaboration

When working with other agents:
- **@RequirementsAnalyzer**: Required approver for all CRITICAL requirements with security implications; receives requirement assignments for Security/Encryption domain; reports completion status
- **@LedgerExpert**: Coordinate on which fields require encryption in ledger entities
- **@PerfEngineer**: Notify about decryption overhead; coordinate on caching decrypted data
- **@IntegrationBot**: Ensure external adapters don't bypass encryption
- **@AuditAgent**: Provide audit chain verification APIs for auditor portal
- **@Architect**: Validate security configuration in Spring Boot and database setup

See the Sub-Agent Interaction Matrix in `sub-agents.md`.

---

## References

- [Security Implementation](../../backend/src/main/java/com/nexus/onebook/ledger/security/)
- [SQL Schema with RLS](../../docs/sql-schema.md)
- [Developer Guide](../../docs/developer-guide.md)
- NIST Guidelines: AES-GCM (SP 800-38D), Key Management (SP 800-57)
- OWASP: Cryptographic Storage Cheat Sheet
