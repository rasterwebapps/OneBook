# Requirement Traceability Matrix (RTM)
## OneBook — Nexus Universal Accounting OS

> **Auto-generated from REQ-*.md files.**
> Generated: 2026-04-04 by `docs/automation/generate-rtm.js`

---

## Traceability Matrix

| Req ID | Title | BRD | FRD | TRD | Key Code Files | Tests | User Stories | Status |
|--------|-------|-----|-----|-----|----------------|-------|-------------|--------|
| [REQ-001](active/REQ-001-multi-tenant-ledger.md) | Multi-Tenant Ledger | BR-001 | FR-001, FR-002 | TR-001 | LedgerAccount.java<br>LedgerAccountService.java<br>LedgerController.java<br>V1__rls_infrastructure.sql | — | US-001, US-003, US-004 | ✅ COMPLETED |
| [REQ-002](active/REQ-002-zero-knowledge-encryption.md) | Zero-Knowledge Encryption | BR-002 | FR-003, FR-004 | TR-002, TR-007 | FieldEncryptionService.java<br>BlindIndexService.java<br>KeyManagementService.java<br>V5__blind_dba_infrastructure.sql | — | US-006, US-007 | ✅ COMPLETED |
| [REQ-003](active/REQ-003-external-app-ingestion.md) | External App Ingestion | BR-003 | FR-005, FR-006 | TR-006 | ExternalAppIngestionService.java<br>IngestionController.java<br>FinancialEventRepository.java<br>V6__ingestion_layer.sql | — | US-016 | ✅ COMPLETED |
| [REQ-004](active/REQ-004-voucher-posting.md) | Voucher Posting | BR-004 | FR-002 | TR-005 | JournalService.java<br>JournalController.java<br>VoucherTypeService.java<br>V3__ledger_and_journal.sql | — | US-002, US-005 | ✅ COMPLETED |
| [REQ-005](active/REQ-005-trial-balance-reports.md) | Trial Balance Reports | BR-005 | FR-007, FR-008 | TR-003 | TrialBalanceService.java<br>ProfitAndLossService.java<br>BalanceSheetService.java | — | US-011, US-013 | ✅ COMPLETED |
| [REQ-006](active/REQ-006-cost-center-management.md) | Cost Center & Branch Management | BR-006 | FR-001, FR-008 | TR-001 | CostCenterRepository.java<br>BranchRepository.java<br>IntercompanyService.java<br>V2__organizational_hierarchy.sql | — | US-004, US-018 | ✅ COMPLETED |
| [REQ-007](active/REQ-007-fixed-asset-management.md) | Fixed Asset Management | BR-007 | FR-009, FR-010 | TR-005 | FixedAssetService.java<br>FixedAssetController.java<br>V7__reporting_compliance_far.sql | — | US-012 | ✅ COMPLETED |
| [REQ-008](active/REQ-008-tds-tcs-compliance.md) | TDS/TCS Compliance | BR-008 | FR-011, FR-012 | TR-005 | TdsTcsService.java<br>TdsTcsController.java<br>ComplianceService.java<br>V7__reporting_compliance_far.sql | — | US-008 | ✅ COMPLETED |
| [REQ-009](active/REQ-009-bank-reconciliation.md) | Bank Reconciliation | BR-009 | FR-013 | TR-005 | BankReconciliationService.java<br>ReconciliationController.java<br>BankFeedTransactionRepository.java<br>V7__reporting_compliance_far.sql | — | US-009 | ✅ COMPLETED |
| [REQ-010](active/REQ-010-maker-checker-workflow.md) | Maker-Checker-Approver Workflow | BR-010 | FR-014 | TR-007 | AuditorPortalService.java<br>AuditorPortalController.java<br>AuditWorkflowRepository.java<br>V9__hardening_audit_production.sql | — | US-010, US-019 | ✅ COMPLETED |
| [REQ-011](active/REQ-011-payment-register.md) | Payment Register | BR-011 | FR-015 | TR-008 | PaymentRegisterEntry.java<br>PaymentRegisterStatus.java<br>PaymentRegisterRepository.java<br>PaymentRegisterService.java<br>PaymentRegisterController.java<br>V11__payment_processing.sql | PaymentRegisterServiceTest<br>PaymentRegisterControllerTest | US-021 | ✅ COMPLETED |
| [REQ-012](active/REQ-012-payment-batch-processing.md) | Payment Batch Processing | BR-012 | FR-016 | TR-008 | PaymentBatch.java<br>PaymentBatchItem.java<br>PaymentBatchStatus.java<br>PaymentBatchService.java<br>PaymentBatchController.java<br>V11__payment_processing.sql | PaymentBatchServiceTest<br>PaymentBatchControllerTest | US-022, US-023 | ✅ COMPLETED |
| [REQ-013](active/REQ-013-payment-generation.md) | Payment File Generation | BR-013 | FR-017 | TR-008 | PaymentFileGeneratorService.java<br>V11__payment_processing.sql | PaymentFileGeneratorServiceTest | US-024 | ✅ COMPLETED |
| [REQ-014](active/REQ-014-employee-advances-and-settlement.md) | Employee Advances, Expense Settlement, Advance Receipt & Payment Advice | BR-014 | FR-018 | TR-009 | EmployeeAdvance.java<br>ExpenseVoucher.java<br>AdvanceReceipt.java<br>V15__employee_advances_settlement.sql | — | US-025, US-026, US-027 | 📝 DRAFT |

---

## Detailed Traceability

### REQ-001: Multi-Tenant Ledger

| Artifact | Details |
|---------|--------|
| Status | ✅ COMPLETED |
| Priority | CRITICAL |
| Owner | @LedgerExpert |
| Milestone | M1/M2 |
| BRD | BR-001 |
| FRD | FR-001, FR-002 |
| TRD | TR-001 |
| Java Files | LedgerAccount.java, LedgerAccountService.java, LedgerController.java, LedgerAccountRepository.java, TenantContextFilter.java |
| Migrations | V1__rls_infrastructure.sql, V2__organizational_hierarchy.sql, V3__ledger_and_journal.sql |
| User Stories | US-001, US-003, US-004 |

### REQ-002: Zero-Knowledge Encryption

| Artifact | Details |
|---------|--------|
| Status | ✅ COMPLETED |
| Priority | CRITICAL |
| Owner | @SecurityWarden |
| Milestone | M3 |
| BRD | BR-002 |
| FRD | FR-003, FR-004 |
| TRD | TR-002, TR-007 |
| Java Files | FieldEncryptionService.java, BlindIndexService.java, KeyManagementService.java, AuditLogService.java, EncryptedStringConverter.java, AuditLog.java, AuditLogRepository.java |
| Migrations | V5__blind_dba_infrastructure.sql |
| User Stories | US-006, US-007 |

### REQ-003: External App Ingestion

| Artifact | Details |
|---------|--------|
| Status | ✅ COMPLETED |
| Priority | HIGH |
| Owner | @IntegrationBot |
| Milestone | M6 |
| BRD | BR-003 |
| FRD | FR-005, FR-006 |
| TRD | TR-006 |
| Java Files | ExternalAppIngestionService.java, IngestionController.java, FinancialEventRepository.java, OcrInvoiceService.java, ThreeWayMatchingService.java, CorporateCardService.java |
| Migrations | V6__ingestion_layer.sql |
| User Stories | US-016 |

### REQ-004: Voucher Posting

| Artifact | Details |
|---------|--------|
| Status | ✅ COMPLETED |
| Priority | CRITICAL |
| Owner | @LedgerExpert |
| Milestone | M2 |
| BRD | BR-004 |
| FRD | FR-002 |
| TRD | TR-005 |
| Java Files | JournalService.java, JournalController.java, VoucherTypeService.java, VoucherTypeController.java, JournalTransaction.java, JournalEntry.java, UnbalancedTransactionException.java |
| Migrations | V3__ledger_and_journal.sql |
| User Stories | US-002, US-005 |

### REQ-005: Trial Balance Reports

| Artifact | Details |
|---------|--------|
| Status | ✅ COMPLETED |
| Priority | HIGH |
| Owner | @LedgerExpert |
| Milestone | M7 |
| BRD | BR-005 |
| FRD | FR-007, FR-008 |
| TRD | TR-003 |
| Java Files | TrialBalanceService.java, ProfitAndLossService.java, BalanceSheetService.java, CashFlowService.java, ReportController.java, WarmCacheService.java |
| User Stories | US-011, US-013 |

### REQ-006: Cost Center & Branch Management

| Artifact | Details |
|---------|--------|
| Status | ✅ COMPLETED |
| Priority | HIGH |
| Owner | @LedgerExpert |
| Milestone | M2 |
| BRD | BR-006 |
| FRD | FR-001, FR-008 |
| TRD | TR-001 |
| Java Files | CostCenterRepository.java, BranchRepository.java, IntercompanyService.java, ConsolidationController.java |
| Migrations | V2__organizational_hierarchy.sql |
| User Stories | US-004, US-018 |

### REQ-007: Fixed Asset Management

| Artifact | Details |
|---------|--------|
| Status | ✅ COMPLETED |
| Priority | HIGH |
| Owner | @LedgerExpert |
| Milestone | M7 |
| BRD | BR-007 |
| FRD | FR-009, FR-010 |
| TRD | TR-005 |
| Java Files | FixedAssetService.java, FixedAssetController.java |
| Migrations | V7__reporting_compliance_far.sql |
| User Stories | US-012 |

### REQ-008: TDS/TCS Compliance

| Artifact | Details |
|---------|--------|
| Status | ✅ COMPLETED |
| Priority | HIGH |
| Owner | @ComplianceAgent |
| Milestone | M7 |
| BRD | BR-008 |
| FRD | FR-011, FR-012 |
| TRD | TR-005 |
| Java Files | TdsTcsService.java, TdsTcsController.java, ComplianceService.java, ComplianceController.java, ComplianceCertificationService.java |
| Migrations | V7__reporting_compliance_far.sql |
| User Stories | US-008 |

### REQ-009: Bank Reconciliation

| Artifact | Details |
|---------|--------|
| Status | ✅ COMPLETED |
| Priority | HIGH |
| Owner | @LedgerExpert |
| Milestone | M7 |
| BRD | BR-009 |
| FRD | FR-013 |
| TRD | TR-005 |
| Java Files | BankReconciliationService.java, ReconciliationController.java, BankFeedTransactionRepository.java |
| Migrations | V7__reporting_compliance_far.sql |
| User Stories | US-009 |

### REQ-010: Maker-Checker-Approver Workflow

| Artifact | Details |
|---------|--------|
| Status | ✅ COMPLETED |
| Priority | HIGH |
| Owner | @AuditAgent |
| Milestone | M10 |
| BRD | BR-010 |
| FRD | FR-014 |
| TRD | TR-007 |
| Java Files | AuditorPortalService.java, AuditorPortalController.java, AuditWorkflowRepository.java, SecurityAuditService.java |
| Migrations | V9__hardening_audit_production.sql |
| User Stories | US-010, US-019 |

### REQ-011: Payment Register

| Artifact | Details |
|---------|--------|
| Status | 🔄 IN_PROGRESS |
| Priority | HIGH |
| Owner | @LedgerExpert |
| Milestone | M11 |
| BRD | BR-011 |
| FRD | FR-015 |
| TRD | TR-008 |
| Java Files | PaymentRegisterEntry.java, PaymentRegisterStatus.java, PaymentRegisterRepository.java, PaymentRegisterService.java, PaymentRegisterController.java |
| Migrations | V11__payment_processing.sql |
| User Stories | US-021 |

### REQ-012: Payment Batch Processing

| Artifact | Details |
|---------|--------|
| Status | 🔄 IN_PROGRESS |
| Priority | HIGH |
| Owner | @LedgerExpert |
| Milestone | M11 |
| BRD | BR-012 |
| FRD | FR-016 |
| TRD | TR-008 |
| Java Files | PaymentBatch.java, PaymentBatchItem.java, PaymentBatchStatus.java, PaymentBatchRepository.java, PaymentBatchItemRepository.java, CreateBatchRequest.java, PaymentBatchResponse.java, BatchApprovalRequest.java, PaymentBatchService.java, PaymentBatchController.java |
| Migrations | V11__payment_processing.sql |
| User Stories | US-022, US-023 |

### REQ-013: Payment File Generation

| Artifact | Details |
|---------|--------|
| Status | 🔄 IN_PROGRESS |
| Priority | HIGH |
| Owner | @LedgerExpert |
| Milestone | M11 |
| BRD | BR-013 |
| FRD | FR-017 |
| TRD | TR-008 |
| Java Files | PaymentFileGeneratorService.java |
| Migrations | V11__payment_processing.sql |
| User Stories | US-024 |

### REQ-014: Employee Advances, Expense Settlement, Advance Receipt & Payment Advice

| Artifact | Details |
|---------|--------|
| Status | 📝 DRAFT |
| Priority | HIGH |
| Owner | @LedgerExpert |
| Milestone | M12 |
| BRD | BR-014 |
| FRD | FR-018 |
| TRD | TR-009 |
| Java Files | EmployeeAdvance.java, ExpenseVoucher.java, AdvanceReceipt.java, PaymentAdvice.java, EmployeeAdvanceConfig.java, EmployeeAdvanceBalance.java, EmployeeAdvanceDto.java, ExpenseVoucherDto.java, AdvanceReceiptDto.java, PaymentAdviceDto.java, AdvanceApprovalRequest.java, SettlementResult.java, EmployeeAdvanceRepository.java, ExpenseVoucherRepository.java, AdvanceReceiptRepository.java, PaymentAdviceRepository.java, EmployeeAdvanceConfigRepository.java, EmployeeAdvanceBalanceRepository.java, AdvanceLimitCheckService.java, ApprovalTierResolver.java, AdvanceSettlementService.java, EmployeeAdvanceService.java, ExpenseVoucherService.java, AdvanceReceiptService.java, PaymentAdviceService.java, AdvanceReportService.java, EmployeeAdvanceController.java, ExpenseVoucherController.java, AdvanceReceiptController.java, PaymentAdviceController.java, AdvanceReportController.java, AdvanceLimitExceededException.java, VoucherType.java, DepartmentAccessPolicy.java |
| Migrations | V15__employee_advances_settlement.sql |
| User Stories | US-025, US-026, US-027, US-028 |

---

## Coverage Summary

| Category | Total | Completed | Coverage |
|----------|-------|-----------|----------|
| Requirements | 14 | 10 | 71% |
| Requirements with Tests | 14 | 0 | 0% |
| Requirements with Code | 14 | 14 | 100% |
| Requirements with DB Migration | 14 | 13 | 93% |

---

*Auto-generated by `docs/automation/generate-rtm.js` on 2026-04-04. Do not edit manually.*
