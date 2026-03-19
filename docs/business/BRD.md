# Business Requirements Document (BRD)
## OneBook — Nexus Universal Accounting OS

> **Auto-generated from REQ-*.md files. Version: Living Document.**  
> Last Updated: 2026-03-18 | Owner: @RequirementsAnalyzer | Status: APPROVED

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Business Objectives](#2-business-objectives)
3. [Stakeholders](#3-stakeholders)
4. [Business Requirements](#4-business-requirements)
5. [Success Criteria](#5-success-criteria)
6. [Constraints and Assumptions](#6-constraints-and-assumptions)
7. [Risks and Mitigation](#7-risks-and-mitigation)
8. [Appendix](#8-appendix)

---

## 1. Executive Summary

OneBook (Nexus Universal) is a sector-agnostic, zero-trust, high-performance accounting operating system designed to serve enterprises across healthcare, manufacturing, retail, financial services, and government verticals. It delivers Tally-speed keyboard UX combined with 2026-grade cryptographic security and AI-powered financial intelligence.

The platform consolidates the accounting needs of multi-tenant enterprises into a single, unified system — replacing fragmented, sector-specific accounting tools with a secure, extensible OS that ingests financial events from any external application (HL7, DMS, ISO20022, Webhooks), enforces double-entry bookkeeping integrity, and provides real-time compliance with GST, TDS, TCS, e-Invoice, and e-Way Bill regulations.

**Core value proposition:**
- Zero-knowledge field encryption: sensitive financial data is encrypted before it reaches the database, making it unreadable even to database administrators.
- Tally-speed UX: power users achieve full keyboard navigation at Tally-equivalent speed via a Command Palette and legacy shortcut compatibility.
- Universal ingestion: any external system — healthcare, logistics, banking — can feed financial events into OneBook through pluggable adapters.
- Regulatory compliance: built-in TDS/TCS deduction, e-Invoice generation, e-Way Bill, and GST reconciliation.
- AI intelligence: cash flow forecasting, anomaly detection, mark-to-market valuation, and scenario modeling.

---

## 2. Business Objectives

| ID | Objective | KPI | Target |
|----|-----------|-----|--------|
| OBJ-001 | Provide a single accounting OS for all industry verticals | Number of adapter integrations | ≥ 6 adapters |
| OBJ-002 | Achieve zero-knowledge data security | % sensitive fields encrypted | 100% |
| OBJ-003 | Deliver Tally-speed keyboard navigation | Keyboard shortcut coverage vs. Tally | ≥ 90% parity |
| OBJ-004 | Support multi-tenant SaaS deployment | Tenant isolation score | Full RLS enforcement |
| OBJ-005 | Ensure regulatory compliance (GST/TDS/TCS) | Compliance gap | 0 gaps |
| OBJ-006 | Achieve sub-300ms API response times | P95 response latency | < 300ms |
| OBJ-007 | Eliminate manual financial report generation | Report generation time | < 5 seconds |
| OBJ-008 | Provide tamper-evident audit trails | Hash chain integrity | 100% verifiable |
| OBJ-009 | Enable AI-driven financial forecasting | Forecast accuracy | ± 10% variance |
| OBJ-010 | Support intercompany consolidation | Entities per consolidation | Unlimited |

---

## 3. Stakeholders

### Primary Stakeholders

| Role | Responsibilities | Key Requirements |
|------|-----------------|-----------------|
| **Accountants** | Day-to-day bookkeeping, journal entry, voucher posting, bank reconciliation | Tally-speed UX, keyboard shortcuts, voucher types (Payment, Receipt, Journal, Contra) |
| **Finance Managers** | Financial reporting, cost center management, budget vs. actuals, period close | P&L, Balance Sheet, Cash Flow, Trial Balance with drill-down |
| **Auditors** | Read-only access to transactions, hash-chain verification, compliance sampling | Auditor portal, tamper-evident logs, document vault access |
| **IT Administrators** | Tenant provisioning, user management, encryption key rotation, system monitoring | Multi-tenant RLS, key management service, observability dashboard |
| **C-Suite / CFO** | Strategic financial visibility, consolidation, forecasting, risk monitoring | AI dashboards, intercompany consolidation, scenario modeling |

### Secondary Stakeholders

| Role | Responsibilities |
|------|-----------------|
| **Tax Officers** | GST filing, TDS deduction certificates, e-Invoice validation |
| **Compliance Officers** | Regulatory reporting, certifications, FEMA/SEBI compliance |
| **External Auditors** | Independent verification of financial statements |
| **Integration Partners** | Third-party app developers using the ingestion API |

---

## 4. Business Requirements

### BR-001: Multi-Tenant Accounting
**Priority:** CRITICAL | **Owner:** @LedgerExpert | **Milestone:** M1/M2 | **Status:** ✅ COMPLETED

**Business Need:**  
The platform must serve multiple independent organizations (tenants) from a single deployment without data leakage between tenants. Each tenant must have full isolation of their chart of accounts, ledger entries, journal transactions, and configuration.

**Requirement Statements:**
- BR-001.1: The system shall isolate all tenant data using Row-Level Security (RLS) at the PostgreSQL layer, such that no query can return data belonging to another tenant.
- BR-001.2: The system shall support a multi-entity hierarchy: Enterprise → Company → Branch → Cost Center, with each level inheriting permissions from above.
- BR-001.3: Tenant configuration (locale, currency, fiscal year) shall be independently configurable per tenant.
- BR-001.4: The system shall support simultaneous operations from multiple tenants without performance degradation.
- BR-001.5: All API endpoints shall require tenant context validation before processing any request.

**Linked FRDs:** FR-001, FR-002  
**Linked TRDs:** TR-001 (Multi-Tenant RLS)

---

### BR-002: Zero-Knowledge Security
**Priority:** CRITICAL | **Owner:** @SecurityWarden | **Milestone:** M3 | **Status:** ✅ COMPLETED

**Business Need:**  
Financial data is highly sensitive. The system must ensure that even database administrators cannot read sensitive field values. Searches on encrypted data must remain possible without decryption at the database level.

**Requirement Statements:**
- BR-002.1: Sensitive fields (party names, amounts, notes) shall be encrypted with AES-256-GCM in the application layer before writing to the database.
- BR-002.2: Blind indexes (HMAC-SHA256) shall enable equality search on encrypted fields without decrypting them.
- BR-002.3: Each audit log entry shall include a cryptographic hash chained to the previous entry for tamper detection.
- BR-002.4: Encryption keys shall be stored in environment variables, never in source code or application configuration files.
- BR-002.5: The system shall support key rotation without data loss or service interruption.

**Linked FRDs:** FR-003, FR-004  
**Linked TRDs:** TR-002 (Field-Level Encryption), TR-007 (Hash-Chained Audit Trail)

---

### BR-003: External App Integration
**Priority:** HIGH | **Owner:** @IntegrationBot | **Milestone:** M6 | **Status:** ✅ COMPLETED

**Business Need:**  
Enterprises use sector-specific applications (hospital management, logistics, e-commerce) that generate financial events. These events must be automatically ingested into OneBook without manual data entry.

**Requirement Statements:**
- BR-003.1: The system shall provide a universal financial event gateway that accepts events from any external application via HTTP POST.
- BR-003.2: Sector-specific adapters shall be provided for Healthcare (HL7), Document Management (DMS), Banking (ISO20022), and generic Webhook sources.
- BR-003.3: Each ingested event shall follow a lifecycle: RECEIVED → VALIDATED → MAPPED → POSTED (or FAILED).
- BR-003.4: New adapters shall be pluggable without modifying core ingestion logic.
- BR-003.5: Failed ingestion events shall be stored with error details and be retryable.

**Linked FRDs:** FR-005, FR-006  
**Linked TRDs:** TR-006 (Pluggable Adapter Pattern)

---

### BR-004: Voucher Posting
**Priority:** CRITICAL | **Owner:** @LedgerExpert | **Milestone:** M2 | **Status:** ✅ COMPLETED

**Business Need:**  
All financial transactions must be recorded as double-entry journal vouchers. The system must support all standard voucher types used in Indian accounting (Tally-compatible) and enforce accounting integrity at multiple layers.

**Requirement Statements:**
- BR-004.1: The system shall support voucher types: Payment, Receipt, Journal, Contra, Sales, Purchase, Debit Note, Credit Note, Stock Journal.
- BR-004.2: Every posted voucher must have balanced debits and credits (sum of debits = sum of credits).
- BR-004.3: Double-entry validation shall be enforced at three levels: service layer, database trigger, and exception surfacing.
- BR-004.4: Vouchers shall support narration, reference numbers, cost center allocation, and multi-currency amounts.
- BR-004.5: Posted vouchers shall be immutable; corrections must be made through reversing entries.

**Linked FRDs:** FR-001, FR-002  
**Linked TRDs:** TR-005 (Double-Entry Validation)

---

### BR-005: Financial Reports
**Priority:** HIGH | **Owner:** @LedgerExpert | **Milestone:** M7 | **Status:** ✅ COMPLETED

**Business Need:**  
Management and regulatory stakeholders require accurate, real-time financial statements. Reports must be cacheable for performance and exportable in standard formats.

**Requirement Statements:**
- BR-005.1: The system shall generate Trial Balance, Profit & Loss, Balance Sheet, and Cash Flow statements.
- BR-005.2: Reports shall be generated within 5 seconds for datasets up to 1 million journal entries.
- BR-005.3: Reports shall support date range filtering, cost center filtering, and comparative periods.
- BR-005.4: Report data shall be cached in Redis to serve subsequent requests without database queries.
- BR-005.5: Reports shall be exportable in PDF and Excel formats.

**Linked FRDs:** FR-007, FR-008  
**Linked TRDs:** TR-003 (Cache-Aside Pattern)

---

### BR-006: Cost Center & Branch Management
**Priority:** HIGH | **Owner:** @LedgerExpert | **Milestone:** M2 | **Status:** ✅ COMPLETED

**Business Need:**  
Large enterprises need to track financial performance at department, branch, and project levels without creating separate books of accounts.

**Requirement Statements:**
- BR-006.1: The system shall support a multi-level cost center hierarchy (Enterprise → Branch → Cost Center).
- BR-006.2: Journal entries shall be allocatable to one or more cost centers.
- BR-006.3: Cost center reports shall show P&L for each center independently.
- BR-006.4: Branches shall have their own chart of accounts that rolls up to the parent entity.
- BR-006.5: Intercompany transactions shall be eliminatable during consolidation.

**Linked FRDs:** FR-001, FR-008  
**Linked TRDs:** TR-001 (Multi-Tenant RLS)

---

### BR-007: Fixed Asset Management
**Priority:** HIGH | **Owner:** @LedgerExpert | **Milestone:** M7 | **Status:** ✅ COMPLETED

**Business Need:**  
Enterprises must track capital assets, compute depreciation per accounting standards, and maintain a Fixed Asset Register (FAR) for compliance and audit purposes.

**Requirement Statements:**
- BR-007.1: The system shall maintain a Fixed Asset Register with asset code, purchase date, cost, and useful life.
- BR-007.2: Depreciation shall be computable using Straight-Line (SLM) and Written Down Value (WDV) methods.
- BR-007.3: Depreciation journals shall be auto-posted at period close.
- BR-007.4: Asset disposal shall generate gain/loss journal entries automatically.
- BR-007.5: The FAR shall be reportable as a schedule (Schedule II compliant for Indian companies).

**Linked FRDs:** FR-009, FR-010  
**Linked TRDs:** TR-005 (Double-Entry Validation)

---

### BR-008: TDS/TCS Compliance
**Priority:** HIGH | **Owner:** @ComplianceAgent | **Milestone:** M7 | **Status:** ✅ COMPLETED

**Business Need:**  
Indian enterprises are legally required to deduct Tax Deducted at Source (TDS) and collect Tax Collected at Source (TCS) on specified payments and receipts. The system must automate these deductions and generate compliant returns.

**Requirement Statements:**
- BR-008.1: The system shall automatically compute TDS on applicable payment vouchers based on TDS section codes (194A, 194C, 194H, etc.).
- BR-008.2: The system shall generate TCS on applicable sales transactions.
- BR-008.3: TDS/TCS ledger accounts shall be automatically credited/debited on voucher posting.
- BR-008.4: The system shall generate Form 26Q, 27Q, and 27EQ data files.
- BR-008.5: e-Invoice shall be generated in IRN format with QR code for B2B transactions above ₹5 lakh.

**Linked FRDs:** FR-011, FR-012  
**Linked TRDs:** TR-005 (Double-Entry Validation)

---

### BR-009: Bank Reconciliation
**Priority:** HIGH | **Owner:** @LedgerExpert | **Milestone:** M7 | **Status:** ✅ COMPLETED

**Business Need:**  
Finance teams need to reconcile bank statements against book balances to identify discrepancies, uncleared cheques, and unauthorised transactions.

**Requirement Statements:**
- BR-009.1: The system shall import bank statements in CSV/OFX/MT940 formats.
- BR-009.2: Automatic matching shall reconcile book entries with bank transactions using amount, date, and reference matching.
- BR-009.3: Unmatched transactions shall be flagged for manual review.
- BR-009.4: The reconciliation report shall show closing balance, uncleared items, and adjusted balance.
- BR-009.5: Reconciled entries shall be locked to prevent modification.

**Linked FRDs:** FR-013  
**Linked TRDs:** TR-005 (Double-Entry Validation)

---

### BR-010: Maker-Checker-Approver Workflow
**Priority:** HIGH | **Owner:** @AuditAgent | **Milestone:** M10 | **Status:** ✅ COMPLETED

**Business Need:**  
Internal controls require a separation of duties: the person creating a financial transaction (Maker) cannot also approve it (Checker/Approver). This prevents fraud and errors.

**Requirement Statements:**
- BR-010.1: All voucher postings above a configurable threshold shall require a Checker approval before posting.
- BR-010.2: High-value transactions shall require a second approval from an Approver role.
- BR-010.3: The workflow status shall track: DRAFT → PENDING_CHECK → CHECKED → PENDING_APPROVAL → APPROVED → POSTED.
- BR-010.4: Rejected transactions shall return to the Maker with rejection reason.
- BR-010.5: All workflow state transitions shall be logged in the audit trail.

**Linked FRDs:** FR-014  
**Linked TRDs:** TR-007 (Hash-Chained Audit Trail)

---

## 5. Success Criteria

| Criterion | Measurement | Target |
|-----------|-------------|--------|
| All 10 BRs implemented | Feature completeness audit | 100% |
| Zero data leakage between tenants | Penetration test | Pass |
| Double-entry integrity | Database constraint verification | 100% |
| API response time | P95 latency under load | < 300ms |
| Encryption coverage | Fields-encrypted audit | 100% sensitive fields |
| Keyboard shortcut parity | Tally shortcut coverage test | ≥ 90% |
| Test coverage | Backend test suite | 405+ tests passing |
| Audit trail integrity | Hash chain verification | 100% verifiable |
| Report generation time | Performance test (1M entries) | < 5 seconds |
| Compliance filing | TDS/GST data accuracy | 100% |

---

## 6. Constraints and Assumptions

### Constraints
- **Technology Stack:** Backend must use Java 21+ with Spring Boot 3.4+; Frontend must use Angular 19+.
- **Database:** PostgreSQL 17+ is mandatory for RLS support.
- **Encryption Standard:** AES-256-GCM is mandated; no weaker algorithms permitted.
- **Indian Compliance:** TDS/TCS/GST/e-Invoice are required for Indian market; international tax frameworks are out of scope for v1.
- **Multi-tenancy Model:** Single-database multi-tenancy with RLS (not schema-per-tenant or database-per-tenant).
- **Monetary Precision:** All monetary values must use `BigDecimal` with minimum 2 decimal places; `double`/`float` are prohibited.

### Assumptions
- Each tenant has a designated IT Administrator responsible for initial setup and user management.
- External application integration partners will provide API specifications for their adapter implementations.
- The deployment environment provides secure secret management (environment variables or vault).
- Network connectivity to GST/IRN APIs is available in production environments.
- End users have modern web browsers (Chrome 120+, Firefox 120+, Edge 120+).

---

## 7. Risks and Mitigation

| Risk ID | Risk | Probability | Impact | Mitigation |
|---------|------|------------|--------|-----------|
| RSK-001 | Encryption key loss resulting in data inaccessibility | Low | Critical | Key rotation strategy; environment-variable key management; backup procedures in operational runbook |
| RSK-002 | RLS policy bypass via SQL injection | Low | Critical | Parameterized queries everywhere; RLS policies independently verified; security audit service |
| RSK-003 | Double-entry constraint violation at scale | Very Low | High | Three-layer validation; database trigger as last resort; `UnbalancedTransactionException` surfaced to client |
| RSK-004 | External adapter producing malformed data causing ingestion failure | Medium | Medium | Validation stage in pipeline; FAILED status persists for retry; error details stored |
| RSK-005 | GST API downtime blocking e-Invoice generation | Medium | Medium | Async queue for e-Invoice submission; local IRN caching; fallback to draft mode |
| RSK-006 | Redis failure causing report generation degradation | Low | Medium | Failure-safe fallback to PostgreSQL; cache miss logged as warning only |
| RSK-007 | Multi-tenant performance degradation under high concurrent load | Low | High | Virtual Threads (Project Loom); connection pool tuning; Redis warm cache |
| RSK-008 | Audit trail hash chain corruption | Very Low | Critical | Hash chain verification endpoint; backup chain snapshots; tamper detection alerts |

---

## 8. Appendix

### A. Requirement Status Summary

| Priority | Count | Completed | In Progress | Pending |
|----------|-------|-----------|------------|---------|
| CRITICAL | 3 | 3 | 0 | 0 |
| HIGH | 7 | 7 | 0 | 0 |
| MEDIUM | 0 | 0 | 0 | 0 |
| LOW | 0 | 0 | 0 | 0 |
| **Total** | **10** | **10** | **0** | **0** |

### B. Requirement File Index

| Req ID | File | Milestone |
|--------|------|-----------|
| REQ-001 | docs/requirements/active/REQ-001-multi-tenant-ledger.md | M1/M2 |
| REQ-002 | docs/requirements/active/REQ-002-zero-knowledge-encryption.md | M3 |
| REQ-003 | docs/requirements/active/REQ-003-external-app-ingestion.md | M6 |
| REQ-004 | docs/requirements/active/REQ-004-voucher-posting.md | M2 |
| REQ-005 | docs/requirements/active/REQ-005-trial-balance-reports.md | M7 |
| REQ-006 | docs/requirements/active/REQ-006-cost-center-management.md | M2 |
| REQ-007 | docs/requirements/active/REQ-007-fixed-asset-management.md | M7 |
| REQ-008 | docs/requirements/active/REQ-008-tds-tcs-compliance.md | M7 |
| REQ-009 | docs/requirements/active/REQ-009-bank-reconciliation.md | M7 |
| REQ-010 | docs/requirements/active/REQ-010-maker-checker-workflow.md | M10 |

### C. Related Documents

- `docs/business/FRD.md` — Functional Requirements Document
- `docs/business/TRD.md` — Technical Requirements Document
- `docs/requirements/RTM.md` — Requirement Traceability Matrix
- `docs/technical/data-dictionary.md` — Data Model Documentation
- `docs/technical/api-contracts.md` — API Specification
- `memory-bank/projectbrief.md` — Original project vision

---

*This document is auto-generated from REQ-*.md files by `docs/automation/generate-brd.js`. Do not edit manually — changes will be overwritten on next generation.*
