# REQ-015: Education Fee Management — Fee Structure, Enquiry & Fee Finalization

## Summary

Implement an Education Fee Management module for OneBook covering three interconnected screens: (1) Fee Structure, where fee types are classified as *Generic* (tuition and all non-hostel/transport fees, forming the base course fee) and *Additional* (hostel fees and transportation fees, excluded from the base total); (2) Enquiry, where program selection filters course options, selecting a course auto-loads a non-editable fee total from the fee structure, and the student's type (day scholar / hosteler) determines which additional fee (transportation or hostel) is appended to the total; and (3) Fee Finalization, which mirrors the enquiry fee logic but allows only discounts — fees may never be increased beyond the loaded amount.

## Classification

- **Domains**: Database, Backend, Frontend
- **Complexity**: HIGH
- **Primary Agent**: @backend
- **Collaborating Agents**: [@database, @frontend, @security, @quality, @docs]

## Business Rules

### Fee Structure Screen
1. Fee types are split into two categories:
   - **Generic**: all fee types EXCEPT hostel fees and transportation fees. The course base total uses only Generic fees.
   - **Additional**: hostel fees and transportation fees only. These are NOT included in the course base total.
2. A fee structure is defined per course (program → course mapping drives the lookup).

### Enquiry Screen
3. Programs and courses are pre-mapped in the database. Selecting a program filters the courses dropdown to show only bound courses.
4. Selecting a course auto-loads the **Generic fee total** into a **non-editable** fees field.
5. A `studentType` field with options `DAY_SCHOLAR` and `HOSTELER` is captured on the enquiry.
6. Based on `studentType`:
   - `DAY_SCHOLAR` → look up the `TRANSPORTATION` fee for the course and add it to the total displayed.
   - `HOSTELER` → look up the `HOSTEL` fee for the course and add it to the total displayed.
7. The final total (Generic + applicable Additional) is stored with the enquiry record.

### Fee Finalization Screen
8. Mirrors enquiry: program → course selection loads the same Generic fee total (non-editable base).
9. `studentType`-based additional fee is added the same way as in enquiry.
10. The only allowed adjustment is a **discount** — a positive `discountAmount` that reduces the payable total.
11. Final payable = (Generic total + applicable Additional fee) − discount. Discount cannot exceed the total; fee cannot be increased.

## Acceptance Criteria

- [ ] Fee Structure: feeTypes rendered in two labelled sections — "Generic Fees" and "Additional Fees"
- [ ] Fee Structure: total course fee = sum of Generic fee amounts only
- [ ] Enquiry: course dropdown filtered to program-bound courses after program selection
- [ ] Enquiry: course selection auto-populates total fees (Generic sum) in a read-only field
- [ ] Enquiry: `studentType` captured; correct additional fee (hostel/transport) appended to displayed total
- [ ] Enquiry: combined total (Generic + Additional) persisted with the enquiry record
- [ ] Fee Finalization: same program → course → fee auto-load flow
- [ ] Fee Finalization: `studentType`-based additional fee added as in enquiry
- [ ] Fee Finalization: only discount field is editable; final payable = total − discount
- [ ] Fee Finalization: backend rejects any finalization where `finalPayable > (genericTotal + additionalFee)`
- [ ] RLS enabled on all new tables with `tenant_id` isolation
- [ ] All new backend endpoints return DTOs (no entity exposure)
- [ ] All monetary values use `BigDecimal`
- [ ] Unit tests cover fee category filtering, total calculation, studentType branching, and discount validation

## Database Schema (Planned)

### Tables
- `education_programs` — program master (id, tenant_id, name, code, is_active)
- `education_courses` — course master (id, tenant_id, program_id FK, name, code, is_active)
- `fee_types` — fee type master (id, tenant_id, name, category ENUM{GENERIC, ADDITIONAL}, additional_type ENUM{HOSTEL, TRANSPORTATION, NULL})
- `fee_structures` — fee structure header (id, tenant_id, course_id FK, academic_year, is_active)
- `fee_structure_items` — line items (id, fee_structure_id FK, fee_type_id FK, amount NUMERIC(15,2))
- `student_enquiries` — enquiry record (id, tenant_id, student_name, program_id FK, course_id FK, student_type ENUM{DAY_SCHOLAR,HOSTELER}, generic_total, additional_fee, total_fees, enquiry_date, status)
- `fee_finalizations` — finalization record (id, tenant_id, enquiry_id FK, generic_total, additional_fee, discount_amount, final_payable, finalized_by, finalized_at)

## Implementation Plan

### Phase 1: Database — @database
- Create Flyway migration `V17__education_fee_management.sql`
- Define all 7 tables with proper types, FKs, constraints
- Enable RLS on all tables; set `tenant_id` on every row-level policy
- Add indexes on `program_id`, `course_id`, `fee_structure_id`, `tenant_id`

### Phase 2: Backend — @backend
- DTOs: `ProgramDto`, `CourseDto`, `FeeTypeDto`, `FeeStructureDto`, `FeeStructureItemDto`, `StudentEnquiryDto`, `FeeFinalizationDto`, `EnquiryFeeBreakdownDto`
- Repositories for all 7 entities (Spring Data JPA, tenant-scoped queries)
- Services:
  - `ProgramService` — CRUD
  - `CourseService` — CRUD, `getCoursesByProgram(programId, tenantId)`
  - `FeeTypeService` — CRUD, `getByCategory(category, tenantId)`
  - `FeeStructureService` — CRUD, `getFeeBreakdownByCourse(courseId, tenantId)` returning Generic total + per-additional-type amounts
  - `StudentEnquiryService` — create/update with fee total computation (Generic + studentType-driven additional)
  - `FeeFinalizationService` — create with discount validation (rejects if `finalPayable > total`)
- Controllers (REST under `/api/education/`): programs, courses, fee-types, fee-structures, enquiries, fee-finalizations
- Unit tests for all service methods

### Phase 3: Frontend — @frontend
- Angular standalone components (Signals, OnPush):
  - `FeeStructureComponent` — two-section layout (Generic / Additional), course-level total derived from Generic items only
  - `StudentEnquiryComponent` — program selector → filtered course selector → auto-loaded read-only fee total → studentType radio → computed total display
  - `FeeFinalizationComponent` — same program/course/studentType flow + editable discount field; payable shown live
- Services: `EducationService` wrapping all backend calls
- Routes under `/education/*`
- Keyboard navigation support (Tab flow across all form fields)
- i18n keys in translation files
- Component spec files with Jasmine tests

### Phase 4: Security — @security
- Verify RLS policies are correctly enforced for all 7 tables
- Confirm no entity objects exposed in REST responses
- Confirm `BigDecimal` used for all monetary fields in DTOs and DB columns

### Phase 5: Quality — @quality
- Run full quality gate suite: `validate-quality-gates.sh`
- Run backend tests: `./gradlew test`
- Run frontend tests: `ng test --watch=false`
- Verify coverage for fee calculation, studentType branching, discount capping

### Phase 6: Documentation — @docs
- Update `memory-bank/activecontext.md`
- Update `memory-bank/progress.md` (add M13)
- Update `docs/technical/api-documentation.md` with new `/api/education/*` endpoints
- Update `docs/technical/sql-schema.md` with the 7 new tables

## Status Tracker

- [ ] Phase 1: Database — Pending
- [ ] Phase 2: Backend — Pending
- [ ] Phase 3: Frontend — Pending
- [ ] Phase 4: Security — Pending
- [ ] Phase 5: Quality — Pending
- [ ] Phase 6: Documentation — Pending
