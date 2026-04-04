# OneBook — Architecture Diagram

> Comprehensive system architecture for the Nexus Universal Accounting OS.

---

## High-Level System Architecture

```mermaid
flowchart TD
    classDef frontend fill:#dd0031,stroke:#fff,stroke-width:2px,color:#fff
    classDef backend fill:#6db33f,stroke:#fff,stroke-width:2px,color:#fff
    classDef database fill:#336791,stroke:#fff,stroke-width:2px,color:#fff
    classDef cache fill:#dc382d,stroke:#fff,stroke-width:2px,color:#fff
    classDef external fill:#f2a900,stroke:#fff,stroke-width:2px,color:#fff
    classDef security fill:#000000,stroke:#fff,stroke-width:2px,color:#fff

    %% External Systems & Users
    User([👨‍💻 Accountant / CFO]) -->|Ctrl+K / Tally Keys| UI
    HMS([🏥 Healthcare HMS]) -->|HL7 / FHIR| Gateway
    DMS([🚗 Auto DMS]) -->|JSON / API| Gateway
    Bank([🏦 Open Banking]) -->|ISO 20022| Gateway
    SaaS([☁️ SaaS / Webhooks]) -->|REST / Webhook| Gateway

    %% Frontend Layer
    subgraph Client_Layer [Frontend — Angular 19+ Signals]
        UI[Angular SPA<br/>Command Palette · Keyboard Nav]:::frontend
    end
    UI -->|REST API| API_Gateway

    %% Backend Layer
    subgraph Application_Layer [Backend — Java 21 / Spring Boot 3.4+]
        API_Gateway[API Gateway / Router<br/>HeadlessApiConfig · CORS]:::backend

        Gateway[Financial Event Gateway<br/>AdapterRegistry · UniversalMapper]:::backend
        Gateway -->|Normalized Events| CoreEngine

        API_Gateway --> CoreEngine
        CoreEngine[Double-Entry Ledger Engine<br/>Virtual Threads · Loom]:::backend

        AIEngine[AI & Intelligence Engine<br/>Forecasting · MTM · Anomaly]:::backend
        CoreEngine <--> AIEngine

        SecurityService{CSFLE Encryption<br/>AES-256-GCM · HMAC Blind Index}:::security
        CoreEngine -->|Raw Financial Data| SecurityService
    end

    %% Data & Cache Layer
    subgraph Data_Layer [Data Storage Layer]
        Redis[(Redis 7+<br/>Warm Cache<br/>Decrypted Session Data)]:::cache
        Postgres[(PostgreSQL 17+<br/>Encrypted Ledger<br/>RLS · JSONB)]:::database
        S3[(AWS S3 / MinIO<br/>Document Vault)]:::database
    end

    %% Data Flows
    SecurityService -->|Decrypt on Login| Redis
    CoreEngine <-->|Cache-Aside Read| Redis
    SecurityService -->|Encrypted Blobs<br/>+ Blind Indexes| Postgres
    CoreEngine -->|Invoices / Receipts| S3
```

---

## Data Flow Architecture

```mermaid
flowchart LR
    subgraph Ingestion [External Ingestion]
        HL7[HL7 Adapter]
        DMS_A[DMS Adapter]
        ISO[ISO 20022 Adapter]
        WH[Webhook Adapter]
    end

    subgraph Gateway [Financial Event Gateway]
        AR[Adapter Registry]
        GW[Gateway Router]
        UM[Universal Mapper]
    end

    subgraph Ledger [Double-Entry Engine]
        JE[Journal Engine]
        TB[Trial Balance]
        COA[Chart of Accounts]
    end

    subgraph Security [Zero-Knowledge Layer]
        ENC[AES-256-GCM<br/>Field Encryption]
        BI[HMAC-SHA256<br/>Blind Index]
        AUD[Hash-Chained<br/>Audit Trail]
    end

    subgraph Storage [Persistence]
        RD[(Redis Cache)]
        PG[(PostgreSQL)]
    end

    HL7 & DMS_A & ISO & WH --> AR
    AR --> GW --> UM
    UM --> JE
    JE --> TB & COA
    JE --> ENC & BI & AUD
    ENC --> PG
    BI --> PG
    AUD --> PG
    ENC -.->|Decrypt on Login| RD
```

---

## Security Architecture

```mermaid
flowchart TD
    subgraph Application [JVM Encryption Boundary]
        KMS[Key Management Service<br/>Envelope Encryption]
        FE[Field Encryption Service<br/>AES-256-GCM]
        BIS[Blind Index Service<br/>HMAC-SHA256]
        ALS[Audit Log Service<br/>Hash-Chained]
        EC[Encrypted String Converter<br/>JPA Transparent]
    end

    subgraph WireFormat [Ciphertext Wire Format]
        WF["Base64( version ‖ IV ‖ ciphertext ‖ auth_tag )"]
    end

    subgraph Database [PostgreSQL — Encrypted at Rest]
        EF[account_name_encrypted<br/>description_encrypted]
        BIF[account_name_blind_index<br/>description_blind_index]
        AL[audit_log<br/>prev_hash → hash chain]
    end

    KMS -->|Key by Version| FE
    FE -->|Encrypt| WF --> EF
    BIS -->|HMAC| BIF
    ALS -->|SHA-256 Chain| AL

    style Application fill:#1a1a2e,color:#fff
    style WireFormat fill:#16213e,color:#fff
    style Database fill:#0f3460,color:#fff
```

---

## Cache Strategy

```mermaid
sequenceDiagram
    participant U as User
    participant API as Spring Boot API
    participant CS as WarmCacheService
    participant R as Redis 7+
    participant E as FieldEncryptionService
    participant DB as PostgreSQL

    Note over U,DB: Login — Cache Warm-Up
    U->>API: POST /api/cache/warm/{tenantId}
    API->>DB: Fetch encrypted accounts
    DB-->>API: Encrypted rows
    API->>E: Decrypt fields
    E-->>API: Plaintext data
    API->>CS: putAccountsByTenant()
    CS->>R: SET with TTL (30 min)
    R-->>CS: OK
    CS-->>API: Cache warmed
    API-->>U: 200 OK

    Note over U,DB: Subsequent Read — Cache Hit
    U->>API: GET /api/ledger/accounts
    API->>CS: getAccountsByTenant()
    CS->>R: GET key
    R-->>CS: Cached data
    CS-->>API: Return from cache
    API-->>U: 200 OK (sub-100ms)

    Note over U,DB: Cache Miss — Fallback to DB
    U->>API: GET /api/ledger/accounts
    API->>CS: getAccountsByTenant()
    CS->>R: GET key
    R-->>CS: null (miss)
    CS-->>API: null
    API->>DB: Fetch + Decrypt
    DB-->>API: Data
    API->>CS: putAccountsByTenant()
    CS->>R: SET with TTL
    API-->>U: 200 OK
```

---

## Deployment Architecture

```mermaid
flowchart TD
    subgraph CI [GitHub Actions CI/CD]
        BE_BUILD[Backend Build<br/>JDK 21 · Gradle]
        FE_BUILD[Frontend Build<br/>Node 20 · Angular CLI]
        BE_TEST[Backend Tests<br/>204 tests]
        FE_TEST[Frontend Tests<br/>101 tests<br/>ChromeHeadless]
    end

    subgraph Local [Local Development — Docker Compose]
        PG[PostgreSQL 17-alpine<br/>Port 5432]
        RD[Redis 7-alpine<br/>Port 6379]
        SB[Spring Boot<br/>Port 8080<br/>Virtual Threads]
        NG[Angular Dev Server<br/>Port 4200<br/>API Proxy]
    end

    BE_BUILD --> BE_TEST
    FE_BUILD --> FE_TEST
    SB <--> PG
    SB <--> RD
    NG -->|Proxy /api/*| SB
```

---

## Module Dependency Graph

```mermaid
flowchart BT
    subgraph Frontend
        KB[Keyboard Module<br/>KeyBindingRegistry<br/>CommandPalette]
        I18N[i18n Module<br/>Transloco]
        AI_UI[AI Dashboard]
    end

    subgraph Controllers
        LC[LedgerController]
        JC[JournalController]
        RC[ReportController]
        IC[IngestionController]
        FC[ForecastController]
        MC[MarketController]
        CC[ComplianceController]
        REC[ReconciliationController]
        CON[ConsolidationController]
        FAC[FixedAssetController]
        DAC[DigitalAssetController]
        AC[AnomalyController]
    end

    subgraph Services
        LS[LedgerAccountService]
        JS[JournalService]
        TBS[TrialBalanceService]
        RS[ReportingServices]
        FS[ForecastingService]
        MS[MarkToMarketService]
    end

    subgraph Security
        FES[FieldEncryptionService]
        BIS[BlindIndexService]
        KMS[KeyManagementService]
        ALS[AuditLogService]
    end

    subgraph Cache
        WCS[WarmCacheService]
    end

    subgraph Data
        PG[(PostgreSQL + RLS)]
        RD[(Redis)]
    end

    Controllers --> Services
    Services --> Security
    Services --> Cache
    Security --> PG
    Cache --> RD
    Services --> PG
```

---

## Related Documentation

- [Key-Binding Registry Design](key-binding-registry.md)
- [SQL Schema Documentation](sql-schema.md)
- [API Documentation](api-documentation.md)
- [Developer Onboarding Guide](developer-guide.md)
- [Operational Runbook](operational-runbook.md)
