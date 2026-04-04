# OneBook — System Architecture

> High-level Mermaid.js architecture diagram for the Nexus Universal Accounting OS.
> For detailed diagrams (data flow, security, cache, deployment), see [`technical/architecture-diagram.md`](technical/architecture-diagram.md).

```mermaid
flowchart TD
    %% Define Styles
    classDef frontend fill:#dd0031,stroke:#fff,stroke-width:2px,color:#fff;
    classDef backend fill:#6db33f,stroke:#fff,stroke-width:2px,color:#fff;
    classDef database fill:#336791,stroke:#fff,stroke-width:2px,color:#fff;
    classDef cache fill:#dc382d,stroke:#fff,stroke-width:2px,color:#fff;
    classDef external fill:#f2a900,stroke:#fff,stroke-width:2px,color:#fff;
    classDef security fill:#000000,stroke:#fff,stroke-width:2px,color:#fff;

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

    %% Backend Layer (Java Spring Boot)
    subgraph Application_Layer [Backend — Java 21 / Spring Boot 3.4+]
        API_Gateway[API Gateway / Router<br/>HeadlessApiConfig · CORS]:::backend

        Gateway[Financial Event Gateway<br/>AdapterRegistry · UniversalMapper]:::backend
        Gateway -->|Normalized Events| CoreEngine

        API_Gateway --> CoreEngine
        CoreEngine[Double-Entry Ledger Engine<br/>Virtual Threads · Loom]:::backend

        AIEngine[AI & Intelligence Engine<br/>Forecasting · MTM · Anomaly]:::backend
        CoreEngine <--> AIEngine

        %% Security & Encryption Barrier
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

## Architecture Documentation

| Document | Description |
|----------|-------------|
| [Architecture Diagrams](technical/architecture-diagram.md) | Mermaid.js diagrams: system, data flow, security, cache, deployment |
| [Key-Binding Registry](technical/key-binding-registry.md) | Tally shortcuts, Command Palette, conflict resolution, extensibility |
| [SQL Schema](technical/sql-schema.md) | Universal Secured Ledger schema (V1–V8 migrations) |
| [API Documentation](technical/api-documentation.md) | REST API reference for all endpoints |
| [Developer Guide](technical/developer-guide.md) | Onboarding, setup, coding standards, workflows |
| [Operational Runbook](technical/operational-runbook.md) | Deployment, monitoring, troubleshooting, backup |
