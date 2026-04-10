# REQ-013: Payment File Generation

**Status:** COMPLETED  
**Priority:** HIGH  
**Owner:** @LedgerExpert  
**Milestone:** M11  
**Created:** 2026-03-19  
**Last Updated:** 2026-04-07  
**Linked BRD:** [BR-013](../../business/BRD.md#br-013-payment-file-generation)  
**Linked FRD:** [FR-017](../../business/FRD.md#req-013-payment-file-generation)  
**Linked TRD:** [TR-008](../../business/TRD.md#tr-008-payment-processing-pipeline)

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
After batch approval, there is no mechanism to generate bank payment instruction files. Finance managers must manually prepare bank transfer files (for NEFT/RTGS/IMPS), re-entering vendor bank account details and payment amounts — a time-consuming process prone to data entry errors that can cause mis-directed payments.

### 1.2 Business Value
Automated payment file generation eliminates manual re-entry of payment instructions, reduces the risk of mis-directed payments, and accelerates the payment cycle. The generated CSV file can be directly uploaded to the bank portal for bulk NEFT/RTGS processing, saving significant time for Finance teams processing multiple vendor payments.

### 1.3 Stakeholders
| Stakeholder | Need |
|-------------|------|
| Finance Manager | Generate bank-ready payment files from approved batches |
| Accountant | Confirmation that file has been generated and batch status updated |
| CFO | Audit trail of payment file generation events |

### 1.4 Business Rules
- **BR-013.1**: Only `APPROVED` batches can have payment files generated
- **BR-013.2**: File contains: Sr No, Vendor Name, Bank Account, IFSC Code, Bank Name, Payment Amount, Payment Reference, Payment Mode
- **BR-013.3**: After file generation, batch status moves to `PAYMENT_GENERATED` and all register entries move to `PAYMENT_GENERATED`

---

## 2. Functional Specification

### 2.1 CSV File Format
| Column | Description |
|--------|-------------|
| Sr No | Sequential row number |
| Vendor Name | Resolved vendor name from ledger account |
| Bank Account | Vendor's bank account number |
| IFSC Code | Vendor's bank IFSC code |
| Bank Name | Name of vendor's bank |
| Payment Amount | Net payable amount from batch |
| Payment Reference | Batch number (e.g., PB-2026-03-001) |
| Payment Mode | NEFT / RTGS / IMPS |

### 2.2 API Endpoint
```
GET  /api/payment-batches/{batchId}/generate-file?tenantId=T1
```

**Response:**
- `Content-Type: text/csv`
- `Content-Disposition: attachment; filename="payment-PB-2026-03-001.csv"`
- Body: CSV file content

### 2.3 Status Transitions on File Generation
```
Batch:           APPROVED → PAYMENT_GENERATED
Register Items:  APPROVED → PAYMENT_GENERATED
```

---

## 3. Technical Specification

### 3.1 Implementation Files
| File | Package | Description |
|------|---------|-------------|
| `PaymentFileGeneratorService.java` | `ledger/payment/service/` | Generates CSV content, updates batch and register entry statuses |

### 3.2 CSV Generation Logic
```java
// PaymentFileGeneratorService
public byte[] generatePaymentFile(Long batchId, String tenantId) {
    PaymentBatch batch = batchRepository.findByIdAndTenantId(batchId, tenantId)
        .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));
    
    if (batch.getStatus() != PaymentBatchStatus.APPROVED) {
        throw new IllegalStateException("Only APPROVED batches can generate payment files");
    }
    
    // Build CSV rows from batch vendor bank details
    // Update batch status to PAYMENT_GENERATED
    // Update all register entries to PAYMENT_GENERATED
    // Return CSV bytes
}
```

### 3.3 Vendor Bank Details
Vendor bank details (account number, IFSC, bank name) are stored in the ledger account metadata JSONB column (as per the existing account model from REQ-001). The generator resolves these from the `vendorAccountId` on the batch.

---

## 4. Acceptance Criteria

```gherkin
Feature: Payment File Generation

  Scenario: Generate payment file from approved batch
    Given a batch PB-2026-03-001 in APPROVED status
    When user calls GET /api/payment-batches/{id}/generate-file?tenantId=T1
    Then a CSV file is downloaded
    And Content-Disposition header is "attachment; filename=payment-PB-2026-03-001.csv"
    And the CSV contains: Sr No, Vendor Name, Bank Account, IFSC Code, Bank Name, Payment Amount, Payment Reference, Payment Mode
    And batch status moves to PAYMENT_GENERATED
    And all register entries for this batch move to PAYMENT_GENERATED

  Scenario: Cannot generate file from non-approved batch
    Given a batch in PENDING_APPROVAL status
    When user calls GET /api/payment-batches/{id}/generate-file?tenantId=T1
    Then a 400 error is returned
    And error message indicates "only APPROVED batches can generate payment files"

  Scenario: Cannot generate file from already-generated batch
    Given a batch in PAYMENT_GENERATED status
    When user calls GET /api/payment-batches/{id}/generate-file?tenantId=T1
    Then a 400 error is returned
    And error message indicates "payment file already generated for this batch"

  Scenario: File generation is tenant-isolated
    Given batch 999 belongs to tenant T2
    When user calls generate-file with tenantId=T1
    Then a 404 error is returned
```

---

## 7. Traceability

| Artifact | Link |
|---------|------|
| BRD | [BR-013](../../business/BRD.md#br-013-payment-file-generation) |
| FRD | [FR-017](../../business/FRD.md#req-013-payment-file-generation) |
| TRD | [TR-008](../../business/TRD.md#tr-008-payment-processing-pipeline) |
| RTM | [RTM Row REQ-013](../RTM.md) |
| User Stories | [US-024](../../business/user-stories.md) |
| Agent Owner | [@backend](../../../.github/agents/backend.agent.md) |
| Migration | `V11__payment_processing.sql` |
