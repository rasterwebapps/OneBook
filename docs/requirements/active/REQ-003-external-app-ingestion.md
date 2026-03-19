# REQ-003: External App Ingestion

**Status:** COMPLETED  
**Priority:** HIGH  
**Owner:** @IntegrationBot  
**Milestone:** M6  
**Created:** 2026-02-01  
**Last Updated:** 2026-03-18  
**Linked BRD:** [BR-003](../../business/BRD.md#br-003-external-app-integration)  
**Linked FRD:** [FR-005, FR-006](../../business/FRD.md#4-ingestion--integration)  
**Linked TRD:** [TR-006](../../business/TRD.md#7-tr-006-pluggable-adapter-pattern)

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
Enterprises across industries use sector-specific applications that generate financial events: hospital management systems (billing), logistics platforms (freight), e-commerce systems (orders), and banking platforms (payments). Finance teams currently re-enter these events manually into accounting systems — a slow, error-prone process.

OneBook's Universal Ingestion Gateway eliminates manual re-entry by accepting financial events from any external application through a pluggable adapter pattern.

### 1.2 Business Value
- **Eliminating manual data entry:** Reduces accounting staff time by 40–60% for businesses with high transaction volumes
- **Real-time posting:** Financial events posted within seconds of occurring in the source system
- **Sector-agnostic:** One platform serves healthcare, logistics, e-commerce, banking
- **Audit trail:** All ingested events are logged with source, adapter type, and status

### 1.3 Stakeholders
| Role | Interest |
|------|---------|
| Integration Partners | Adapter API specification |
| Accountants | Automated posting reduces manual work |
| IT Administrators | Adapter configuration and monitoring |
| Finance Managers | Real-time book updates from all systems |

### 1.4 Business Rules
- BR-003.1: Universal gateway accepts events from any adapter type via HTTP POST
- BR-003.2: Adapters for HL7, DMS, ISO20022, Webhook are provided out-of-the-box
- BR-003.3: Event lifecycle: RECEIVED → VALIDATED → MAPPED → POSTED (or FAILED)
- BR-003.4: New adapters pluggable without core code changes
- BR-003.5: Failed events stored with error details and are retryable

---

## 2. Functional Specification

### 2.1 Feature Description
The External App Ingestion module provides a single REST endpoint (`POST /api/ingestion/events`) that accepts financial event payloads from any registered external application. The `AdapterRegistry` routes the payload to the appropriate adapter, which parses, validates, maps to journal entries, and posts them.

### 2.2 Ingestion Pipeline
```
HTTP POST /api/ingestion/events
  → IngestionController creates IngestionEvent (status: RECEIVED)
  → ExternalAppIngestionService.process(event)
    → AdapterRegistry.resolve(adapterType) → returns ExternalAppAdapter
    → adapter.parse(payload) → FinancialEvent (status: VALIDATED or FAILED)
    → adapter.map(financialEvent) → List<JournalEntryRequest> (status: MAPPED or FAILED)
    → JournalService.post(entries) → JournalTransaction (status: POSTED or FAILED)
  → Update IngestionEvent with final status and journalTransactionId
```

### 2.3 Inputs
| Input | Type | Required | Validation |
|-------|------|----------|-----------|
| adapterType | String | Yes | Must exist in AdapterRegistry |
| payload | String (JSON/XML/HL7) | Yes | Adapter-specific schema |
| applicationName | String | Yes | Source system identifier |
| externalReferenceId | String | No | Source system transaction ID |
| tenantId | UUID | Yes | From JWT context |

### 2.4 Outputs
| Output | Type | Description |
|--------|------|-------------|
| id | UUID | Ingestion event ID |
| status | Enum | RECEIVED/VALIDATED/MAPPED/POSTED/FAILED |
| journalTransactionId | UUID | Posted journal transaction (if successful) |
| errorDetails | String | Error message (if FAILED) |

### 2.5 Supported Adapters
| Adapter Type | Format | Use Case |
|-------------|--------|---------|
| HL7 | HL7 v2.x/FHIR | Hospital billing events |
| DMS | XML/JSON | Document management invoices |
| ISO20022 | XML camt/pain | Banking/SWIFT messages |
| WEBHOOK | JSON | Generic webhook payloads |
| OCR_INVOICE | Image/PDF | Scanned invoice auto-posting |
| CORPORATE_CARD | CSV/JSON | Corporate card transaction feeds |

### 2.6 API Endpoints
```
POST   /api/ingestion/events              — Submit financial event
GET    /api/ingestion/events              — List ingestion events (paginated)
GET    /api/ingestion/events/{id}         — Get event status and details
POST   /api/ingestion/events/{id}/retry   — Retry failed event
GET    /api/ingestion/adapters            — List registered adapters
```

---

## 3. Technical Specification

### 3.1 Architecture — Pluggable Adapter Pattern
```java
// ExternalAppAdapter.java
public interface ExternalAppAdapter {
    String getAdapterType();
    boolean canHandle(String adapterType);
    FinancialEvent parse(String payload) throws AdapterParseException;
    List<JournalEntryRequest> map(FinancialEvent event);
}

// Adding a new adapter — just implement and annotate:
@Component
public class Iso20022Adapter implements ExternalAppAdapter {
    @Override
    public String getAdapterType() { return "ISO20022"; }
    // ...
}
// Spring DI auto-registers it — zero changes to core code
```

### 3.2 Data Model
```sql
-- V6__ingestion_layer.sql
CREATE TABLE financial_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    adapter_type VARCHAR(50) NOT NULL,
    application_name VARCHAR(100) NOT NULL,
    external_reference_id VARCHAR(255),
    payload TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'RECEIVED',
    journal_transaction_id UUID,
    error_details TEXT,
    retry_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMP
);

ALTER TABLE financial_events ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON financial_events
    USING (tenant_id = current_setting('app.current_tenant_id')::uuid);
```

### 3.3 Performance Considerations
- Target processing time: < 500ms per event end-to-end
- Async processing option for high-volume sources (batch ingestion)
- FAILED events stored for retry; retry_count tracked to prevent infinite retry loops

---

## 4. Acceptance Criteria

```gherkin
Feature: External App Ingestion Pipeline

  Scenario: HL7 event successfully ingested and posted
    Given the HL7 adapter is registered
    When I POST /api/ingestion/events with adapterType "HL7" and valid HL7 payload
    Then the response status should be 201 Created
    And the event status should be RECEIVED
    When the pipeline processes the event
    Then the final status should be POSTED
    And a journal transaction should be created

  Scenario: Invalid payload fails at VALIDATED stage
    When I POST /api/ingestion/events with adapterType "HL7" and malformed payload
    Then the event should be persisted with status FAILED
    And the errorDetails should describe the parse error

  Scenario: Failed event is retryable
    Given a FAILED ingestion event with id "evt-001"
    When I POST /api/ingestion/events/evt-001/retry
    Then the event should be re-processed from the beginning
    And retry_count should be incremented

  Scenario: New adapter registered without core code changes
    Given a new @Component class "Tally9Adapter" implements ExternalAppAdapter
    When the application starts
    Then "TALLY9" should appear in GET /api/ingestion/adapters
    And events with adapterType "TALLY9" should be routed to it
```

---

## 5. Implementation

### 5.1 New Files Created
| File | Package | Purpose |
|------|---------|---------|
| `ExternalAppIngestionService.java` | `com.nexus.onebook.ledger.ingestion.externalapp` | Pipeline orchestration |
| `IngestionController.java` | `com.nexus.onebook.ledger.ingestion.controller` | REST endpoints |
| `FinancialEventRepository.java` | `com.nexus.onebook.ledger.ingestion.repository` | Event persistence |
| `OcrInvoiceService.java` | `com.nexus.onebook.ledger.ingestion.automation` | OCR adapter |
| `ThreeWayMatchingService.java` | `com.nexus.onebook.ledger.ingestion.automation` | PO/GR/Invoice matching |
| `CorporateCardService.java` | `com.nexus.onebook.ledger.ingestion.connector` | Corporate card adapter |

### 5.2 Migration
- Migration file: `V6__ingestion_layer.sql`
- Tables: `financial_events`, `vendor_invoices`, `goods_receipts`, `purchase_orders`

---

## 7. Traceability

| Artifact | Link |
|---------|------|
| BRD | [BR-003](../../business/BRD.md#br-003-external-app-integration) |
| FRD | [FR-005, FR-006](../../business/FRD.md#4-ingestion--integration) |
| TRD | [TR-006](../../business/TRD.md#7-tr-006-pluggable-adapter-pattern) |
| RTM | [RTM Row REQ-003](../RTM.md) |
| User Stories | [US-016](../../business/user-stories.md) |
| Agent Owner | [@IntegrationBot](../../../.github/agents/integration-bot.md) |
| Migration | `V6__ingestion_layer.sql` |
