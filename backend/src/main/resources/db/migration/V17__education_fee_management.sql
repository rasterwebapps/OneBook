-- V17: Education Fee Management
-- REQ-015: Education Fee Management module
-- Tables: education_programs, education_courses, fee_types,
--         fee_structures, fee_structure_items,
--         student_enquiries, fee_finalizations
-- RLS: yes — all tables, using current_tenant_id() (defined in V1)
-- Author: @database agent
--
-- Design notes
-- ─────────────────────────────────────────────────────────────────────────────
-- 1. tenant_id is TEXT (not UUID) to match the V1/V2 infrastructure contract.
--    enterprises.id is BIGSERIAL (BIGINT), so "tenant_id UUID REFERENCES
--    enterprises(id)" would produce a FK type-mismatch error at migration time.
--    Every other table in this codebase uses TEXT for tenant_id; we follow suit.
--
-- 2. RLS policies call current_tenant_id() — the TEXT-returning helper
--    established in V1 — rather than casting to ::uuid, which would break
--    on any non-UUID tenant string and is incompatible with a TEXT column.
--
-- 3. Primary keys for all new education tables are UUID (gen_random_uuid()).
--    All cross-table FKs inside this module are UUID → UUID; no type conflicts.
--
-- 4. Monetary columns use NUMERIC(15,2) as specified for this module.
-- ─────────────────────────────────────────────────────────────────────────────

-- ============================================================
-- 1. education_programs — programme master
-- ============================================================
CREATE TABLE education_programs (
    id          UUID        DEFAULT gen_random_uuid() PRIMARY KEY,
    tenant_id   TEXT        NOT NULL DEFAULT current_tenant_id(),
    name        VARCHAR(200) NOT NULL,
    code        VARCHAR(50)  NOT NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_education_programs_tenant_code UNIQUE (tenant_id, code)
);

ALTER TABLE education_programs ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON education_programs
    USING (tenant_id = current_tenant_id());

CREATE INDEX idx_education_programs_tenant ON education_programs (tenant_id);

-- ============================================================
-- 2. education_courses — course master (child of programme)
-- ============================================================
CREATE TABLE education_courses (
    id          UUID        DEFAULT gen_random_uuid() PRIMARY KEY,
    tenant_id   TEXT        NOT NULL DEFAULT current_tenant_id(),
    program_id  UUID        NOT NULL REFERENCES education_programs (id),
    name        VARCHAR(200) NOT NULL,
    code        VARCHAR(50)  NOT NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_education_courses_tenant_code UNIQUE (tenant_id, code)
);

ALTER TABLE education_courses ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON education_courses
    USING (tenant_id = current_tenant_id());

CREATE INDEX idx_education_courses_tenant     ON education_courses (tenant_id);
CREATE INDEX idx_education_courses_program_id ON education_courses (program_id);

-- ============================================================
-- 3. fee_types — fee type master (GENERIC or ADDITIONAL)
-- ============================================================
CREATE TABLE fee_types (
    id               UUID        DEFAULT gen_random_uuid() PRIMARY KEY,
    tenant_id        TEXT        NOT NULL DEFAULT current_tenant_id(),
    name             VARCHAR(200) NOT NULL,
    -- GENERIC: standard tuition / exam fees; ADDITIONAL: hostel, transport, etc.
    category         VARCHAR(20)  NOT NULL
                         CHECK (category IN ('GENERIC', 'ADDITIONAL')),
    -- Populated only when category = 'ADDITIONAL'
    additional_type  VARCHAR(20)
                         CHECK (additional_type IN ('HOSTEL', 'TRANSPORTATION')),
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),

    -- Enforce mutual exclusivity: GENERIC must have no additional_type;
    -- ADDITIONAL must declare which sub-type it is.
    CONSTRAINT chk_fee_types_category_additional_type CHECK (
        (category = 'GENERIC'     AND additional_type IS NULL)
     OR (category = 'ADDITIONAL'  AND additional_type IS NOT NULL)
    )
);

ALTER TABLE fee_types ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON fee_types
    USING (tenant_id = current_tenant_id());

CREATE INDEX idx_fee_types_tenant ON fee_types (tenant_id);

-- ============================================================
-- 4. fee_structures — fee structure header per course × academic year
-- ============================================================
CREATE TABLE fee_structures (
    id             UUID        DEFAULT gen_random_uuid() PRIMARY KEY,
    tenant_id      TEXT        NOT NULL DEFAULT current_tenant_id(),
    course_id      UUID        NOT NULL REFERENCES education_courses (id),
    academic_year  VARCHAR(20)  NOT NULL,
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),

    -- Only one active structure per course per academic year per tenant
    CONSTRAINT uq_fee_structures_tenant_course_year
        UNIQUE (tenant_id, course_id, academic_year)
);

ALTER TABLE fee_structures ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON fee_structures
    USING (tenant_id = current_tenant_id());

CREATE INDEX idx_fee_structures_tenant    ON fee_structures (tenant_id);
CREATE INDEX idx_fee_structures_course_id ON fee_structures (course_id);

-- ============================================================
-- 5. fee_structure_items — individual fee line items in a structure
-- ============================================================
CREATE TABLE fee_structure_items (
    id                UUID        DEFAULT gen_random_uuid() PRIMARY KEY,
    tenant_id         TEXT        NOT NULL DEFAULT current_tenant_id(),
    fee_structure_id  UUID        NOT NULL REFERENCES fee_structures (id),
    fee_type_id       UUID        NOT NULL REFERENCES fee_types (id),
    amount            NUMERIC(15,2) NOT NULL CHECK (amount >= 0),
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),

    -- Each fee type may appear at most once per structure
    CONSTRAINT uq_fee_structure_items_structure_type
        UNIQUE (fee_structure_id, fee_type_id)
);

ALTER TABLE fee_structure_items ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON fee_structure_items
    USING (tenant_id = current_tenant_id());

CREATE INDEX idx_fee_structure_items_tenant           ON fee_structure_items (tenant_id);
CREATE INDEX idx_fee_structure_items_fee_structure_id ON fee_structure_items (fee_structure_id);
CREATE INDEX idx_fee_structure_items_fee_type_id      ON fee_structure_items (fee_type_id);

-- ============================================================
-- 6. student_enquiries — initial fee enquiry record
-- ============================================================
CREATE TABLE student_enquiries (
    id              UUID        DEFAULT gen_random_uuid() PRIMARY KEY,
    tenant_id       TEXT        NOT NULL DEFAULT current_tenant_id(),
    student_name    VARCHAR(200) NOT NULL,
    email           VARCHAR(255),
    phone           VARCHAR(20),
    program_id      UUID        NOT NULL REFERENCES education_programs (id),
    course_id       UUID        NOT NULL REFERENCES education_courses (id),
    -- DAY_SCHOLAR: attends daily; HOSTELER: residential — drives additional fee
    student_type    VARCHAR(20)  NOT NULL
                        CHECK (student_type IN ('DAY_SCHOLAR', 'HOSTELER')),
    generic_total   NUMERIC(15,2) NOT NULL DEFAULT 0,
    additional_fee  NUMERIC(15,2) NOT NULL DEFAULT 0,
    total_fees      NUMERIC(15,2) NOT NULL DEFAULT 0,
    academic_year   VARCHAR(20),
    status          VARCHAR(30)  NOT NULL DEFAULT 'OPEN'
                        CHECK (status IN ('OPEN', 'FINALIZED', 'CANCELLED')),
    enquiry_date    DATE         NOT NULL DEFAULT CURRENT_DATE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

ALTER TABLE student_enquiries ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON student_enquiries
    USING (tenant_id = current_tenant_id());

CREATE INDEX idx_student_enquiries_tenant     ON student_enquiries (tenant_id);
CREATE INDEX idx_student_enquiries_program_id ON student_enquiries (program_id);
CREATE INDEX idx_student_enquiries_course_id  ON student_enquiries (course_id);

-- ============================================================
-- 7. fee_finalizations — one finalization record per enquiry
-- ============================================================
CREATE TABLE fee_finalizations (
    id               UUID        DEFAULT gen_random_uuid() PRIMARY KEY,
    tenant_id        TEXT        NOT NULL DEFAULT current_tenant_id(),
    -- One-to-one: each enquiry can be finalized at most once
    enquiry_id       UUID        NOT NULL UNIQUE REFERENCES student_enquiries (id),
    generic_total    NUMERIC(15,2) NOT NULL,
    additional_fee   NUMERIC(15,2) NOT NULL,
    discount_amount  NUMERIC(15,2) NOT NULL DEFAULT 0 CHECK (discount_amount >= 0),
    final_payable    NUMERIC(15,2) NOT NULL CHECK (final_payable >= 0),
    finalized_by     VARCHAR(200),
    finalized_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),

    -- Accounting integrity: final_payable must equal the computed net amount
    CONSTRAINT chk_fee_finalizations_payable_equation CHECK (
        final_payable = (generic_total + additional_fee - discount_amount)
    )
);

ALTER TABLE fee_finalizations ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON fee_finalizations
    USING (tenant_id = current_tenant_id());

CREATE INDEX idx_fee_finalizations_tenant     ON fee_finalizations (tenant_id);
CREATE INDEX idx_fee_finalizations_enquiry_id ON fee_finalizations (enquiry_id);
