# REQ-008: TDS/TCS Compliance

**Status:** COMPLETED  
**Priority:** HIGH  
**Owner:** @ComplianceAgent  
**Milestone:** M7  
**Created:** 2026-02-14  
**Last Updated:** 2026-03-18  
**Linked BRD:** [BR-008](../../business/BRD.md#br-008-tdstcs-compliance)  
**Linked FRD:** [FR-011, FR-012](../../business/FRD.md#7-tax-compliance)  
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
Indian enterprises are legally required to deduct Tax Deducted at Source (TDS) on specified payments and collect Tax Collected at Source (TCS) on specified sales. Manual computation is error-prone and non-compliance attracts penalties. OneBook automates TDS/TCS computation, posting, and return generation.

### 1.2 TDS Section Codes Supported
| Section | Nature of Payment | Rate |
|---------|------------------|------|
| 194A | Interest (other than securities) | 10% |
| 194C | Contractor payments | 1%/2% |
| 194H | Commission/Brokerage | 5% |
| 194I | Rent | 10%/2% |
| 194J | Professional/Technical fees | 10% |
| 194Q | Purchase of goods | 0.1% |

### 1.3 Business Rules
- BR-008.1: Auto-compute TDS on applicable payment vouchers
- BR-008.2: Generate TCS on applicable sales transactions
- BR-008.3: Auto-post TDS/TCS ledger entries on voucher posting
- BR-008.4: Generate Form 26Q, 27Q, 27EQ data
- BR-008.5: Generate e-Invoice IRN for B2B transactions above ₹5 lakh

---

## 2. Functional Specification

### 2.1 TDS Computation Flow
```
Payment voucher created with payee PAN + section code
→ TdsTcsService.computeTds(amount, sectionCode, panNumber)
→ Check: amount > section threshold?
→ No → No TDS, proceed to post
→ Yes → Compute TDS = amount × section rate
→ Post journal entries:
     Dr Expense Account = full amount
     Cr TDS Payable = TDS amount
     Cr Accounts Payable = amount - TDS
```

### 2.2 e-Invoice Flow
```
B2B Sales invoice > ₹5L threshold
→ ComplianceService.generateEInvoice(invoiceData)
→ Call GSTN e-Invoice API with IRN request
→ Receive IRN + signed QR code
→ Store on voucher
→ Return IRN to accountant
```

### 2.3 API Endpoints
```
POST   /api/compliance/tds/compute        — Compute TDS on transaction
GET    /api/compliance/tds/register       — TDS deduction register
GET    /api/compliance/tds/26q            — Form 26Q data export
POST   /api/compliance/tcs/compute        — Compute TCS on sale
GET    /api/compliance/gst/r1             — GSTR-1 data
POST   /api/compliance/einvoice/generate  — Generate e-Invoice IRN
GET    /api/compliance/einvoice/{irn}     — Get e-Invoice details
POST   /api/compliance/ewaybill/generate  — Generate e-Way Bill
```

---

## 3. Technical Specification

### 3.1 Implementation Files
- `TdsTcsService.java` — TDS/TCS computation engine
- `TdsTcsController.java` — REST endpoints
- `ComplianceService.java` — e-Invoice, e-Way Bill, GST
- `ComplianceController.java` — Compliance REST endpoints
- `ComplianceCertificationService.java` — Certificate generation
- `V7__reporting_compliance_far.sql` — TDS tables

### 3.2 Data Model
```sql
-- V7__reporting_compliance_far.sql
CREATE TABLE tds_deductions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    journal_transaction_id UUID NOT NULL,
    deductee_pan VARCHAR(10),
    section_code VARCHAR(10) NOT NULL,
    payment_amount DECIMAL(19,4) NOT NULL,
    tds_rate DECIMAL(5,4) NOT NULL,
    tds_amount DECIMAL(19,4) NOT NULL,
    deduction_date DATE NOT NULL,
    challan_number VARCHAR(50),
    deposit_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

---

## 4. Acceptance Criteria

```gherkin
Feature: TDS/TCS Compliance

  Scenario: TDS deducted on professional fees
    Given payment of ₹50,000 to consultant under Section 194J
    When I post the payment voucher
    Then TDS = ₹5,000 (10%)
    And journal entries: Dr Fees ₹50,000, Cr TDS Payable ₹5,000, Cr Payable ₹45,000

  Scenario: No TDS below threshold
    Given Section 194C threshold = ₹30,000
    When I post a contractor payment of ₹15,000
    Then TDS = ₹0
    And full ₹15,000 credited to payable

  Scenario: e-Invoice generated for B2B invoice ≥ ₹5L
    Given B2B invoice of ₹6,00,000 with buyer GSTIN
    When invoice posted
    Then IRN generated and stored with voucher
    And QR code available for printing
```

---

## 7. Traceability

| Artifact | Link |
|---------|------|
| BRD | [BR-008](../../business/BRD.md#br-008-tdstcs-compliance) |
| FRD | [FR-011, FR-012](../../business/FRD.md#7-tax-compliance) |
| TRD | [TR-005](../../business/TRD.md#6-tr-005-double-entry-validation) |
| RTM | [RTM Row REQ-008](../RTM.md) |
| User Stories | [US-008](../../business/user-stories.md) |
| Agent Owner | [@ComplianceAgent](../../../.github/agents/compliance-agent.md) |
| Migration | `V7__reporting_compliance_far.sql` |
