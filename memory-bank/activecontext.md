# Active Context — OneBook (Nexus Universal)

> **Current project state, recent changes, and what to focus on next.**  
> This file is updated at the end of every work session.

---

## Current Status

**Date:** 2026-04-04  
**Phase:** Voucher-Receipt-Advance Settlement System  
**All Milestones:** ✅ Complete (M1–M10) + Post-Milestone Enhancements

---

## Recent Changes (Latest Session)

### Voucher-Receipt-Advance Settlement System (2026-04-04)
- **Foundation Entities (9 models + 9 repos):** Department, SubDepartment, Payer, PayerBankAccount, Payee, PayeeBankAccount, Application, Advance, PaymentApprovalLimit + BankAccountType enum
- **Voucher Enums (7):** VoucherStatus, VoucherClosureType, VoucherItemStatus, ReceiptStatus, PaymentAdviceStatus, FileStatus, TwoStepVerificationType
- **Core Entities (9 models + 9 repos):** Voucher, VoucherItem, Receipt, PaymentAdvice, 3 settlement entities, UploadedFile, DepartmentLevelApprovalLimit
- **DTOs (10):** Request/Response records for Voucher, VoucherItem, Receipt, PaymentAdvice, UploadedFile
- **Services (4):** VoucherService, ReceiptService, PaymentAdviceService, UploadedFileService
- **Controllers (4):** /api/vouchers, /api/receipts, /api/payment-advices, /api/uploaded-files
- **Flyway V14:** 18 new tables with CHECK constraints, RLS policies, tenant isolation indexes
- **Unit Tests:** 24 new tests (VoucherServiceTest, ReceiptServiceTest, PaymentAdviceServiceTest, UploadedFileServiceTest)
- **Total Tests:** 489 backend tests passing

---

## Current Project State

### Backend (Spring Boot 3.4+ / Java 21)
- **Package:** `com.nexus.onebook.ledger`
- **Migrations:** V1–V10 (all applied)
- **Tests:** 405+ passing
- **API:** All endpoints functional at `/api/*`

### Frontend (Angular 19+)
- **Modules:** 11 feature modules (all lazy-loaded standalone components)
- **Tests:** 105+ passing (2 pre-existing AppComponent failures known)
- **State:** Signals-based (no RxJS for simple state)

### Infrastructure
- PostgreSQL 17 + Redis 7 via Docker Compose
- GitHub Actions CI validates build, test, and agent ownership on every PR

---

## Active Focus Areas

Authentication module is now complete. All 10 milestones remain complete.

**New authentication capability:**
- `/start` is the public landing page with OIDC login redirect
- All other routes are protected by `authGuard`
- Keycloak + OpenLDAP can be started with `docker compose up keycloak openldap`
- Demo users: `demo-admin`/`admin123!` and `demo-accountant`/`account123!`

**For backend integration (next steps):**
- Add `spring-boot-starter-oauth2-resource-server` to backend
- Configure JWT validation against Keycloak JWKS endpoint
- Extract `tenant_id` from JWT and set RLS context

---

## Next Steps (When New Tasks Arrive)

1. Read `memory-bank/progress.md` to understand what's complete
2. Identify which sub-agent(s) own the affected code
3. Consult the relevant `.github/agents/*.md` file for patterns
4. Implement using conventions in `memory-bank/systempatterns.md`
5. Update this file (`activecontext.md`) before ending the session
6. Run `.github/scripts/validate-agent-ownership.sh` to validate ownership

---

## Open Questions / Decisions Pending

_(None currently — all architectural decisions are documented in `systempatterns.md`)_

---

## How to Update This File

At the end of each session, replace the "Recent Changes" section with a summary of what was done. Add a new date header if multiple sessions occur on different dates. Keep this file concise — 1-2 paragraphs per session maximum.
