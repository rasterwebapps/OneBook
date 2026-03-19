# Workflow Diagrams
## OneBook — Nexus Universal Accounting OS

> **Mermaid process flow diagrams for all major OneBook workflows.**  
> Last Updated: 2026-03-18 | Owner: @Architect

---

## 1. Journal Entry Posting Flow

```mermaid
flowchart TD
    A([Accountant]) -->|POST /api/journal/transactions| B[JournalController]
    B --> C{JWT Valid?}
    C -->|No| ERR1[401 Unauthorized]
    C -->|Yes| D[TenantContextFilter\nSET app.current_tenant_id]
    D --> E[JournalService.postTransaction]
    E --> F{Validate Entries\nLayer 1: Java}
    F -->|Debits ≠ Credits| ERR2[422 UnbalancedTransactionException]
    F -->|Balanced| G[Encrypt narration\nFieldEncryptionService]
    G --> H[repository.save\nJournalTransaction]
    H --> I{DB Trigger\nLayer 2: PostgreSQL}
    I -->|Net ≠ 0| ERR3[DB Exception → 500]
    I -->|Balanced| J[Update Account Balances]
    J --> K[AuditLogService\nHash-chained entry]
    K --> L[WarmCacheService\nInvalidate cache keys]
    L --> M{Maker-Checker\nThreshold?}
    M -->|Above threshold| N[Status: PENDING_CHECK\nNotify Checkers]
    M -->|Below threshold| O[Status: POSTED]
    N --> P([201 Response\nStatus: PENDING_CHECK])
    O --> P2([201 Response\nStatus: POSTED])

    style ERR1 fill:#ff6b6b,color:#fff
    style ERR2 fill:#ff6b6b,color:#fff
    style ERR3 fill:#ff6b6b,color:#fff
    style O fill:#51cf66,color:#fff
    style N fill:#ffd43b,color:#333
```

---

## 2. Encryption/Decryption Flow

```mermaid
flowchart LR
    subgraph WRITE ["Write Path (Encrypt)"]
        direction TB
        W1[Plaintext value\ne.g., 'Payment to John Doe'] 
        W2[KeyManagementService\ngetCurrentKey version=1]
        W3[SecureRandom\ngenerate 12-byte IV]
        W4[AES-256-GCM\nencrypt with key + IV]
        W5["Wire Format:\n[ver][IV 12B][ciphertext+tag]"]
        W6[Base64 encode]
        W7[(Database column\nciphertext VARCHAR)]
        W8[HMAC-SHA256\nblind index compute]
        W9[(Database column\nblind_index VARCHAR)]
        W1 --> W2 --> W3 --> W4 --> W5 --> W6 --> W7
        W1 --> W8 --> W9
    end

    subgraph READ ["Read Path (Decrypt)"]
        direction TB
        R1[(Ciphertext from DB)]
        R2[Base64 decode]
        R3[Extract version byte]
        R4[KeyManagementService\nresolve key by version]
        R5[Extract IV bytes 1-12]
        R6[AES-256-GCM decrypt\n+ verify auth tag]
        R7{Auth tag OK?}
        R8[Plaintext returned\nto service layer]
        R9[AEADBadTagException\nTAMPERED data]
        R1 --> R2 --> R3 --> R4 --> R5 --> R6 --> R7
        R7 -->|Valid| R8
        R7 -->|Invalid| R9
    end

    style R9 fill:#ff6b6b,color:#fff
    style R8 fill:#51cf66,color:#fff
```

---

## 3. Cache-Aside Pattern

```mermaid
flowchart TD
    A[API Request] --> B{Check Redis\nonebook:cache:domain:id}
    B -->|HIT| C[Return cached value\nP95 < 50ms]
    B -->|MISS| D{Redis available?}
    D -->|Yes, miss| E[Query PostgreSQL\n+ AES-GCM decrypt]
    D -->|Redis down| F[Log WARN\nFallback to PostgreSQL]
    F --> E
    E --> G[Store in Redis\nTTL: 30min]
    G --> H[Return value]
    C --> END([Response to Client])
    H --> END

    subgraph WRITE ["Write/Invalidation Path"]
        W1[Write to PostgreSQL] --> W2[DEL Redis key\nCache invalidated]
        W2 --> W3[Next read repopulates]
    end

    style C fill:#51cf66,color:#fff
    style F fill:#ffd43b,color:#333
```

---

## 4. Financial Event Ingestion Pipeline

```mermaid
flowchart TD
    SRC1[Hospital Management\nSystem HL7] -->|POST /api/ingestion/events| GW
    SRC2[Banking System\nISO20022] -->|POST /api/ingestion/events| GW
    SRC3[E-Commerce\nWebhook] -->|POST /api/ingestion/events| GW
    SRC4[Document Scanner\nOCR Invoice] -->|POST /api/ingestion/events| GW

    GW[IngestionController] --> RECV[Store FinancialEvent\nstatus: RECEIVED]
    RECV --> ROUTE[AdapterRegistry\nresolve adapter by type]
    ROUTE --> PARSE{adapter.parse\nraw payload}
    PARSE -->|ParseException| FAIL1[status: FAILED\nerror details stored]
    PARSE -->|Success| VALIDATE{Schema\nValidation}
    VALIDATE -->|Invalid| FAIL2[status: FAILED]
    VALIDATE -->|Valid| MAP{adapter.map\nto JournalEntryRequest}
    MAP -->|MappingError| FAIL3[status: FAILED]
    MAP -->|Mapped| POST[JournalService.post\nDouble-entry validation]
    POST -->|Unbalanced| FAIL4[status: FAILED]
    POST -->|Posted| SUCCESS[status: POSTED\njournalTransactionId stored]

    FAIL1 & FAIL2 & FAIL3 & FAIL4 --> RETRY[Available for retry\nPOST /events/id/retry]

    style SUCCESS fill:#51cf66,color:#fff
    style FAIL1 fill:#ff6b6b,color:#fff
    style FAIL2 fill:#ff6b6b,color:#fff
    style FAIL3 fill:#ff6b6b,color:#fff
    style FAIL4 fill:#ff6b6b,color:#fff
    style RETRY fill:#ffd43b,color:#333
```

---

## 5. Maker-Checker-Approver Workflow

```mermaid
stateDiagram-v2
    [*] --> DRAFT : Maker creates voucher

    DRAFT --> PENDING_CHECK : Maker submits\n(amount > checker threshold)
    DRAFT --> POSTED : Amount ≤ threshold\n(auto-post)

    PENDING_CHECK --> CHECKED : Checker approves
    PENDING_CHECK --> REJECTED_BY_CHECKER : Checker rejects\n(rejection reason required)

    CHECKED --> PENDING_APPROVAL : Amount > approver threshold
    CHECKED --> POSTED : Amount ≤ approver threshold

    PENDING_APPROVAL --> APPROVED : Approver approves
    PENDING_APPROVAL --> REJECTED_BY_APPROVER : Approver rejects

    APPROVED --> POSTED : Auto-post after approval

    REJECTED_BY_CHECKER --> DRAFT : Maker corrects and resubmits
    REJECTED_BY_APPROVER --> DRAFT : Maker corrects and resubmits

    POSTED --> REVERSED : Reversal entry created

    note right of PENDING_CHECK
        Notifications sent to
        all Checker-role users
    end note

    note right of REJECTED_BY_CHECKER
        Rejection reason visible
        to Maker only
    end note
```

---

## 6. Bank Reconciliation Process

```mermaid
flowchart TD
    START([Finance Manager]) --> IMPORT[Import Bank Statement\nCSV / OFX / MT940]
    IMPORT --> PARSE[Parse & Store\nBankFeedTransactions]
    PARSE --> AUTO[Auto-Matching Algorithm\nAmount + Date ±3 days + Reference]
    
    AUTO --> HIGH{Confidence\n≥ 85%?}
    HIGH -->|Yes| MATCHED[Auto-Accept Match\nstatus: MATCHED]
    HIGH -->|No| REVIEW[Flag for Manual Review\nstatus: UNMATCHED]
    
    REVIEW --> MANUAL{Finance Manager\nManual Match}
    MANUAL -->|Match found| MATCHED
    MANUAL -->|No match: bank charge| NEW_ENTRY[Create New Journal Entry\nBank Charges Dr / Bank Cr]
    MANUAL -->|No match: timing diff| OUTSTANDING[Mark as Outstanding\nUncleared cheque / deposit in transit]
    
    MATCHED & NEW_ENTRY & OUTSTANDING --> REPORT[Generate Reconciliation\nStatement]
    
    REPORT --> CHECK{Book Balance =\nBank Balance\n± Adjustments?}
    CHECK -->|Yes - Difference = 0| FINALIZE[Finalize & Lock\nReconciliation Period]
    CHECK -->|No - Difference ≠ 0| INVESTIGATE[Investigate\nUnexplained Difference]
    INVESTIGATE --> REVIEW

    FINALIZE --> LOCKED([Period Locked\nEntries Immutable])

    style FINALIZE fill:#51cf66,color:#fff
    style LOCKED fill:#51cf66,color:#fff
    style INVESTIGATE fill:#ff6b6b,color:#fff
```

---

## 7. Trial Balance Generation

```mermaid
flowchart TD
    REQ[GET /api/reports/trial-balance\n?from=2026-01-01&to=2026-03-31] --> AUTH{JWT Valid\n+ Tenant Context}
    AUTH -->|Invalid| ERR[401/403]
    AUTH -->|Valid| CACHE{Check Redis\nonebook:cache:reports:trial-balance:...}
    
    CACHE -->|HIT - < 100ms| CACHED([Return cached report])
    CACHE -->|MISS| QUERY[Query PostgreSQL\nSUM journal entries by account]
    
    QUERY --> AGG[Aggregate by account:\n- Opening balance\n- Period debit movements\n- Period credit movements\n- Closing balance]
    
    AGG --> BALANCE{Total Debits =\nTotal Credits?}
    BALANCE -->|Yes| STORE[Store in Redis\nTTL: 30 minutes]
    BALANCE -->|No - data error| ALERT[Alert: books are out of balance\nLog critical error]
    
    STORE --> FORMAT[Format Response\nAdd totals row]
    FORMAT --> RESPOND([Return TrialBalanceResponse])

    style CACHED fill:#51cf66,color:#fff
    style RESPOND fill:#51cf66,color:#fff
    style ALERT fill:#ff6b6b,color:#fff
```

---

## 8. TDS/TCS Deduction Flow

```mermaid
flowchart TD
    START([Accountant posts\nPayment Voucher]) --> CHECK_TYPE{Voucher Type\nincludes payee PAN\n+ section code?}
    
    CHECK_TYPE -->|No| SKIP[Post directly\nNo TDS deduction]
    CHECK_TYPE -->|Yes| FETCH[Fetch TDS section\nrate and threshold]
    
    FETCH --> THRESHOLD{Payment amount\n> section threshold?}
    THRESHOLD -->|No| SKIP
    THRESHOLD -->|Yes| LOWER_CERT{Lower deduction\nForm 13 submitted?}
    
    LOWER_CERT -->|Yes| LOWER_RATE[Apply lower rate\nfrom Form 13]
    LOWER_CERT -->|No| STD_RATE[Apply standard\nsection rate]
    
    LOWER_RATE & STD_RATE --> COMPUTE[TDS Amount =\npayment × rate]
    
    COMPUTE --> ENTRIES[Create Journal Entries:\nDr: Expense (full amount)\nCr: TDS Payable (TDS amount)\nCr: Accounts Payable (net amount)]
    
    ENTRIES --> REGISTER[Record in\nTDS Deduction Register]
    REGISTER --> EINVOICE{Invoice ≥ ₹5L\nB2B transaction?}
    
    EINVOICE -->|Yes| IRN[Generate e-Invoice\nIRN via GSTN API]
    EINVOICE -->|No| POST([Post Voucher\nStatus: POSTED])
    
    IRN --> POST

    style POST fill:#51cf66,color:#fff
    style SKIP fill:#74c0fc,color:#333
```

---

## 9. System Architecture Overview

```mermaid
graph TB
    subgraph CLIENT ["Client Layer"]
        BROWSER[Angular 19+ SPA\nSignals State\nCommand Palette Ctrl+K]
    end

    subgraph API ["Spring Boot 3.4+ API (Virtual Threads)"]
        CTRL[Controllers\nDTO-only REST]
        SVC[Services\nBusiness Logic]
        SEC[Security Layer\nAES-256-GCM\nBlind Indexes\nAudit Trail]
        REPO[Repositories\nSpring Data JPA]
    end

    subgraph DATA ["Data Layer"]
        PG[(PostgreSQL 17+\nRLS + JSONB\nFlyway V1-V10)]
        REDIS[(Redis 7+\nWarm Cache\n30min TTL)]
    end

    subgraph EXTERNAL ["External Systems"]
        HL7[Hospital\nHL7 Adapter]
        ISO[Banking\nISO20022]
        DMS[Document\nDMS Adapter]
        WH[Webhook\nAdapter]
        GSTN[GSTN\ne-Invoice API]
    end

    BROWSER -->|HTTPS JWT| CTRL
    CTRL --> SVC
    SVC --> SEC
    SEC --> REPO
    REPO --> PG
    SVC -->|Cache-Aside| REDIS
    SVC -->|Adapter Pattern| HL7 & ISO & DMS & WH
    SVC -->|e-Invoice| GSTN
```

---

*Diagrams use Mermaid.js syntax. Render with any Mermaid-compatible viewer (GitHub, GitLab, Notion, etc.).*
