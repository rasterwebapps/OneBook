# REQ-007: Fixed Asset Management

**Status:** COMPLETED  
**Priority:** HIGH  
**Owner:** @LedgerExpert  
**Milestone:** M7  
**Created:** 2026-02-12  
**Last Updated:** 2026-03-18  
**Linked BRD:** [BR-007](../../business/BRD.md#br-007-fixed-asset-management)  
**Linked FRD:** [FR-009, FR-010](../../business/FRD.md#6-fixed-assets)  
**Linked TRD:** [TR-005](../../business/TRD.md#6-tr-005-double-entry-validation)

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
Capital assets represent significant investments that must be tracked throughout their lifecycle: acquisition, depreciation, and disposal. Manual depreciation computation is error-prone. OneBook automates depreciation and maintains a compliant Fixed Asset Register.

### 1.2 Business Rules
- BR-007.1: Fixed Asset Register with asset code, purchase date, cost, useful life
- BR-007.2: SLM and WDV depreciation methods
- BR-007.3: Depreciation journals auto-posted at period close
- BR-007.4: Asset disposal generates gain/loss journal entries
- BR-007.5: FAR report Schedule II compliant

---

## 2. Functional Specification

### 2.1 Depreciation Methods
**SLM:** `Annual Depreciation = (Cost − Residual Value) / Useful Life (years)`  
**WDV:** `Annual Depreciation = Opening Book Value × Depreciation Rate`

### 2.2 API Endpoints
```
POST   /api/fixed-assets               — Register new asset
GET    /api/fixed-assets               — List all assets
GET    /api/fixed-assets/{id}          — Get asset details + depreciation schedule
PUT    /api/fixed-assets/{id}          — Update asset details
POST   /api/fixed-assets/{id}/depreciate — Run period depreciation
POST   /api/fixed-assets/{id}/dispose  — Dispose/sell asset
GET    /api/fixed-assets/schedule      — FAR schedule report
GET    /api/fixed-assets/export        — Export FAR as Excel
```

### 2.3 Inputs
| Input | Type | Required | Notes |
|-------|------|----------|-------|
| assetName | String | Yes | Description |
| assetCode | String | Yes | Unique per tenant |
| purchaseDate | Date | Yes | Acquisition date |
| cost | BigDecimal | Yes | Original cost |
| residualValue | BigDecimal | No | Default 0 |
| usefulLifeYears | Integer | Yes | For SLM |
| depreciationMethod | Enum | Yes | SLM or WDV |
| depreciationRate | BigDecimal | No | Required for WDV |

---

## 3. Technical Specification

### 3.1 Data Model
```sql
-- V7__reporting_compliance_far.sql
CREATE TABLE fixed_assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    asset_name VARCHAR(255) NOT NULL,
    asset_code VARCHAR(50) NOT NULL,
    asset_category VARCHAR(100),
    purchase_date DATE NOT NULL,
    cost DECIMAL(19,4) NOT NULL,
    residual_value DECIMAL(19,4) DEFAULT 0,
    useful_life_years INTEGER,
    depreciation_method VARCHAR(10) NOT NULL,
    depreciation_rate DECIMAL(5,4),
    accumulated_depreciation DECIMAL(19,4) DEFAULT 0,
    disposal_date DATE,
    disposal_amount DECIMAL(19,4),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

ALTER TABLE fixed_assets ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON fixed_assets
    USING (tenant_id = current_setting('app.current_tenant_id')::uuid);
```

### 3.2 Implementation Files
- `FixedAssetService.java` — asset management and depreciation
- `FixedAssetController.java` — REST endpoints

---

## 4. Acceptance Criteria

```gherkin
Feature: Fixed Asset Depreciation

  Scenario: SLM depreciation correct
    Given asset "Computer" cost ₹1,00,000, life 5 years, residual ₹10,000
    When I run depreciation for Year 1
    Then depreciation = (₹1,00,000 - ₹10,000) / 5 = ₹18,000
    And journal entry: Dr Depreciation Exp ₹18,000, Cr Accum. Depreciation ₹18,000

  Scenario: Book value cannot go below residual
    Given asset with book value = residual value
    When depreciation runs for next period
    Then depreciation amount = ₹0

  Scenario: Asset disposal generates gain entry
    Given asset with book value ₹40,000 sold for ₹45,000
    When I POST /api/fixed-assets/{id}/dispose with salePrice 45000
    Then journal entry generated: Dr Cash ₹45,000, Cr Fixed Asset ₹40,000, Cr Gain ₹5,000
```

---

## 7. Traceability

| Artifact | Link |
|---------|------|
| BRD | [BR-007](../../business/BRD.md#br-007-fixed-asset-management) |
| FRD | [FR-009, FR-010](../../business/FRD.md#6-fixed-assets) |
| TRD | [TR-005](../../business/TRD.md#6-tr-005-double-entry-validation) |
| RTM | [RTM Row REQ-007](../RTM.md) |
| User Stories | [US-012](../../business/user-stories.md) |
| Agent Owner | [@LedgerExpert](../../../.github/agents/ledger-expert.md) |
| Migration | `V7__reporting_compliance_far.sql` |
