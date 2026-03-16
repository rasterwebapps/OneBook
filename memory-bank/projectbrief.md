# Project Brief — OneBook (Nexus Universal)

> **What this project is, why it exists, and what it must achieve.**  
> This is the north star for every architectural and implementation decision.

---

## The Vision

**OneBook — Nexus Universal** is a sector-agnostic, Zero-Trust, High-Performance Accounting OS.

A one-stop solution for any industry (Healthcare, Auto, Retail, Manufacturing, Pharma, etc.) that combines:
- The **keyboard speed** of Tally (India's dominant accounting software)
- **2026-grade security** (zero-knowledge, blind DBA model)
- **AI-driven intelligence** (forecasting, anomaly detection, mark-to-market)

---

## Original Architect Prompt (Founder's Intent)

**Role:** Principal Technical Architect & FinTech Visionary  
**Project:** "Nexus Universal" – A sector-agnostic, Zero-Trust, High-Performance Accounting OS

### Blueprint Requirements

#### 1. "Better-than-Tally" Keyboard Navigation
- **Legacy Mapping:** All classic Tally shortcuts (F4 Contra, F5 Payment, F7 Journal, Alt+C Create Master, Ctrl+A Save)
- **Command Palette (CMD+K / Ctrl+K):** Global "Omni-Search" — users type "New Invoice," "Jump to Pharmacy Ledger," "Show Stock" without touching the mouse
- **Contextual Power Keys:** Shortcuts adapt to the screen (in a Report: Enter = Drill-down, + = Add Column, / = Filter)

#### 2. Sector-Agnostic Universal Ingestion Layer
- **Adapter Pattern:** "Financial Event Gateway" ingesting from any system (HL7 for Healthcare, DMS for Auto, REST for SaaS)
- **Universal Ledger:** All external inputs standardized into Double-Entry format, using JSONB for industry-specific tags (Patient ID, VIN, SKU)

#### 3. Zero-Knowledge "Blind DBA" Security
- **Selective Field-Level Encryption (CSFLE):** AES-256-GCM in the JVM — sensitive values encrypted before reaching the DB
- **Searchable Ciphertext:** Blind Indexing (HMAC-SHA256) for fast encrypted-field search without DBA access to plaintext
- **Hash-Chained Audit Trail:** Tamper-proof log where each record is cryptographically linked to the previous one

#### 4. "Lightning-Fast" Performance Strategy
- **Redis Warm Cache:** Avoid encryption lag — decrypt the current working set into Redis upon login for instant response
- **Project Loom:** Virtual Threads for thousands of concurrent API pings from linked industrial apps (Hospital HMS, Factory ERPs)

#### 5. Advanced Intelligence & Scale
- **Predictive Forecasting:** AI-driven cash flow and MTM (Mark-to-Market) valuation for share market investments
- **Multi-Everything:** Global hierarchy (Enterprise → Branch → Cost Center) with localized tax compliance (GST/VAT/IFRS) via Feature Entitlement Engine

### Deliverables Achieved
- [x] Architecture Diagram (Mermaid.js) — External Adapters → Encryption → Redis → Postgres
- [x] Key-Binding Registry — Tally keys vs. Command Palette logic
- [x] SQL Schema — Universal_Secured_Ledger with encryption + industry-agnostic metadata

---

## Target Sectors
- Healthcare (HMS/HL7 integration)
- Automotive (DMS integration)
- Retail / FMCG
- Manufacturing / Pharma (batch tracking, BOM)
- Professional Services
- Any sector using JSONB metadata tags

---

## Success Criteria

| Criterion | Metric |
|-----------|--------|
| Keyboard speed | All Tally shortcuts mapped; command palette < 100ms response |
| Security | Zero plaintext in DB; blind index queries work; audit chain tamper-detectable |
| Performance | Redis cache hit rate > 90%; Virtual Threads handle 10k+ concurrent requests |
| Compliance | GST/TDS/TCS automated; e-Invoice + e-Way Bill generation; IFRS toggleable |
| AI | Cash flow forecast accuracy; anomaly detection < 5% false positive rate |
| Test coverage | 405+ backend tests; 105+ frontend tests; all passing |

---

## Scope Boundaries

**In Scope:**
- Core double-entry accounting engine
- Universal ingestion adapters (HL7, DMS, ISO 20022, Webhooks)
- Field-level encryption + blind indexes
- Redis warm cache
- Angular keyboard navigation + command palette
- AI forecasting, MTM valuation, anomaly detection
- GST/TDS/TCS compliance, e-Invoice, e-Way Bill
- Fixed Asset Register + depreciation
- Auditor portal + observability
- Multi-tenant, multi-currency, multi-entity

**Out of Scope (for now):**
- Mobile native apps
- Third-party payment gateway integration (beyond Open Banking)
- HR/Payroll core (only connector, not core HR)
