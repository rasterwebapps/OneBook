# REQ-005: Trial Balance Reports

**Status:** COMPLETED  
**Priority:** HIGH  
**Owner:** @LedgerExpert  
**Milestone:** M7  
**Created:** 2026-02-10  
**Last Updated:** 2026-03-18  
**Linked BRD:** [BR-005](../../business/BRD.md#br-005-financial-reports)  
**Linked FRD:** [FR-007, FR-008](../../business/FRD.md#5-reporting)  
**Linked TRD:** [TR-003](../../business/TRD.md#4-tr-003-cache-aside-pattern-redis)

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
Finance managers and auditors need accurate financial statements generated on demand. Reports must handle large datasets (1M+ entries) within acceptable time limits and be served from cache for repeated requests without database load.

### 1.2 Business Rules
- BR-005.1: Generate Trial Balance, P&L, Balance Sheet, Cash Flow statements
- BR-005.2: Generated within 5 seconds for up to 1M journal entries
- BR-005.3: Support date range, cost center, and comparative period filters
- BR-005.4: Cache report data in Redis to serve repeated requests
- BR-005.5: Exportable as PDF and Excel

---

## 2. Functional Specification

### 2.1 Trial Balance
Lists all ledger accounts with opening balance, period movements, and closing balance. Total closing debits must equal total closing credits.

### 2.2 Profit & Loss
Shows revenue, COGS, gross profit, operating expenses, EBIT, and net profit for a period.

### 2.3 Balance Sheet
Shows assets (current/non-current), liabilities (current/non-current), and equity at a specific date.

### 2.4 Cash Flow Statement
Shows cash flows from operating, investing, and financing activities for a period.

### 2.5 API Endpoints
```
GET    /api/reports/trial-balance           — Trial balance
GET    /api/reports/profit-loss             — P&L statement
GET    /api/reports/balance-sheet           — Balance sheet
GET    /api/reports/cash-flow               — Cash flow statement
GET    /api/reports/trial-balance/export    — Export (format=pdf|xlsx)
GET    /api/reports/profit-loss/export      — Export
```

---

## 3. Technical Specification

### 3.1 Cache-Aside Pattern for Reports
```java
// WarmCacheService.java pattern for reports
String cacheKey = "onebook:cache:reports:trial-balance:" + tenantId + ":" + from + ":" + to;
String cached = redisTemplate.opsForValue().get(cacheKey);
if (cached != null) return deserialize(cached);

TrialBalanceResponse report = trialBalanceService.compute(tenantId, from, to);
redisTemplate.opsForValue().set(cacheKey, serialize(report), Duration.ofMinutes(30));
return report;
```

### 3.2 Implementation Files
- `TrialBalanceService.java` — aggregation queries
- `ProfitAndLossService.java` — income statement logic
- `BalanceSheetService.java` — balance sheet logic
- `CashFlowService.java` — cash flow categorization
- `ReportController.java` — REST endpoints
- `WarmCacheService.java` — Redis cache layer

---

## 4. Acceptance Criteria

```gherkin
Feature: Financial Reports

  Scenario: Trial balance totals balance
    Given journal entries have been posted for Q1 2026
    When I GET /api/reports/trial-balance?from=2026-01-01&to=2026-03-31
    Then total closing debits equals total closing credits
    And the report is generated within 5 seconds

  Scenario: Trial balance served from cache on repeat request
    Given trial balance was generated once
    When I request the same trial balance again
    Then response time is < 100ms (Redis hit)

  Scenario: P&L shows correct net profit
    Given revenue ₹10,00,000 and expenses ₹7,50,000 for the period
    When I GET /api/reports/profit-loss?from=2026-01-01&to=2026-12-31
    Then Net Profit = ₹2,50,000

  Scenario: Balance sheet equation holds
    When I GET /api/reports/balance-sheet?asOf=2026-03-31
    Then Total Assets = Total Liabilities + Total Equity
```

---

## 7. Traceability

| Artifact | Link |
|---------|------|
| BRD | [BR-005](../../business/BRD.md#br-005-financial-reports) |
| FRD | [FR-007, FR-008](../../business/FRD.md#5-reporting) |
| TRD | [TR-003](../../business/TRD.md#4-tr-003-cache-aside-pattern-redis) |
| RTM | [RTM Row REQ-005](../RTM.md) |
| User Stories | [US-011, US-013](../../business/user-stories.md) |
| Agent Owner | [@LedgerExpert](../../../.github/agents/ledger-expert.md) |
