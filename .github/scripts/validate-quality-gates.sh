#!/usr/bin/env bash
# =============================================================================
# validate-quality-gates.sh — OneBook Automated Quality Gate Enforcement
# =============================================================================
# Catches ALL commonly skipped steps in AI-assisted development:
#   1. Memory bank staleness (test counts, module lists, migration lists)
#   2. RLS policy coverage for tenant-scoped tables
#   3. DTO enforcement (no JPA entities in REST controller return types)
#   4. Test file coverage (every Service must have a Test)
#   5. Flyway migration conventions (tenant_id, RLS, TIMESTAMPTZ)
#   6. Frontend test coverage (every component must have a .spec.ts)
#   7. i18n enforcement (no hardcoded user-facing strings in templates)
#   8. BigDecimal enforcement (no double/float for monetary values)
#
# Uses quality-gate-baselines.conf for regression detection:
#   Pre-existing violations are tracked as baselines.
#   Only NEW violations (count > baseline) cause failures.
#
# Run: ./.github/scripts/validate-quality-gates.sh
# CI: Runs on every PR and push to main
# =============================================================================

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
BASELINES_FILE="$REPO_ROOT/.github/scripts/quality-gate-baselines.conf"
EXIT_CODE=0
WARNINGS=0

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

fail() {
    echo -e "${RED}✗${NC} FAIL: $1"
    EXIT_CODE=1
}

warn() {
    echo -e "${YELLOW}⚠${NC}  WARN: $1"
    WARNINGS=$((WARNINGS + 1))
}

pass() {
    echo -e "${GREEN}✓${NC} $1"
}

info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

# Load baselines
DTO_VIOLATIONS_BASELINE=0
MISSING_BACKEND_TESTS_BASELINE=0
MISSING_FRONTEND_SPECS_BASELINE=0
I18N_VIOLATIONS_BASELINE=0

if [ -f "$BASELINES_FILE" ]; then
    # shellcheck source=/dev/null
    source "$BASELINES_FILE"
    info "Loaded baselines from quality-gate-baselines.conf"
else
    warn "No baselines file found — all violations will be reported"
fi

# Default baselines if not in file
RLS_VIOLATIONS_BASELINE="${RLS_VIOLATIONS_BASELINE:-0}"

# Get actual backend test count (used by multiple gates)
ACTUAL_TEST_COUNT=$(grep -r "@Test" "$REPO_ROOT/backend/src/test" --include="*.java" 2>/dev/null | wc -l | tr -d ' ')

# =============================================================================
# GATE 1: Memory Bank Freshness
# =============================================================================
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🧠 GATE 1: Memory Bank Freshness"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# 1a. Backend test count in techcontext.md
if [ -f "$REPO_ROOT/memory-bank/techcontext.md" ]; then
    TECH_TEST_NUM=$(grep -oP '\d+(?=\+?\s*tests)' "$REPO_ROOT/memory-bank/techcontext.md" | head -1 || true)
    if [ -n "$TECH_TEST_NUM" ] && [ "$TECH_TEST_NUM" -ne "$ACTUAL_TEST_COUNT" ]; then
        fail "techcontext.md says ${TECH_TEST_NUM} tests but actual count is ${ACTUAL_TEST_COUNT}. Run: sync-memory-bank.sh --fix"
    else
        pass "techcontext.md test count matches actual (${ACTUAL_TEST_COUNT})"
    fi
fi

# 1b. Backend test count in progress.md
if [ -f "$REPO_ROOT/memory-bank/progress.md" ]; then
    PROG_TEST_NUM=$(grep -oP '\d+(?= backend)' "$REPO_ROOT/memory-bank/progress.md" | head -1 || true)
    if [ -n "$PROG_TEST_NUM" ] && [ "$PROG_TEST_NUM" -ne "$ACTUAL_TEST_COUNT" ]; then
        fail "progress.md says ${PROG_TEST_NUM} backend tests but actual is ${ACTUAL_TEST_COUNT}. Run: sync-memory-bank.sh --fix"
    else
        pass "progress.md backend test count matches actual (${ACTUAL_TEST_COUNT})"
    fi
fi

# 1c. Quality gate baseline in systempatterns.md
if [ -f "$REPO_ROOT/memory-bank/systempatterns.md" ]; then
    GATE_TEST_NUM=$(grep -oP '\d+(?=\+?\s*backend)' "$REPO_ROOT/memory-bank/systempatterns.md" | head -1 || true)
    if [ -n "$GATE_TEST_NUM" ] && [ "$GATE_TEST_NUM" -ne "$ACTUAL_TEST_COUNT" ]; then
        fail "systempatterns.md baseline says ${GATE_TEST_NUM}+ but actual is ${ACTUAL_TEST_COUNT}. Run: sync-memory-bank.sh --fix"
    else
        pass "systempatterns.md test baseline matches actual (${ACTUAL_TEST_COUNT})"
    fi
fi

# 1d. Frontend module count in activecontext.md
ACTUAL_MODULE_COUNT=$(ls -d "$REPO_ROOT/frontend/src/app"/*/ 2>/dev/null | wc -l | tr -d ' ')
if [ -f "$REPO_ROOT/memory-bank/activecontext.md" ]; then
    DOC_MODULE_COUNT=$(grep -oP '\d+(?= feature modules)' "$REPO_ROOT/memory-bank/activecontext.md" | head -1 || true)
    if [ -n "$DOC_MODULE_COUNT" ] && [ "$DOC_MODULE_COUNT" -ne "$ACTUAL_MODULE_COUNT" ]; then
        fail "activecontext.md says ${DOC_MODULE_COUNT} modules but actual is ${ACTUAL_MODULE_COUNT}. Run: sync-memory-bank.sh --fix"
    else
        pass "activecontext.md module count matches actual (${ACTUAL_MODULE_COUNT})"
    fi
fi

# 1e. All modules listed in techcontext.md
if [ -f "$REPO_ROOT/memory-bank/techcontext.md" ]; then
    MISSING_MODULES=""
    for mod in $(ls -d "$REPO_ROOT/frontend/src/app"/*/ 2>/dev/null | xargs -I{} basename {}); do
        if ! grep -q "${mod}/" "$REPO_ROOT/memory-bank/techcontext.md" 2>/dev/null; then
            MISSING_MODULES="${MISSING_MODULES} ${mod}"
        fi
    done
    if [ -n "$MISSING_MODULES" ]; then
        fail "techcontext.md missing frontend modules:${MISSING_MODULES}. Run: sync-memory-bank.sh --fix"
    else
        pass "techcontext.md lists all frontend modules"
    fi
fi

# 1f. Latest migration referenced
LATEST_MIGRATION=$(ls "$REPO_ROOT/backend/src/main/resources/db/migration"/V*.sql 2>/dev/null | xargs -I{} basename {} | sort -V | tail -1 | grep -oP 'V\d+' || true)
if [ -f "$REPO_ROOT/memory-bank/techcontext.md" ] && [ -n "$LATEST_MIGRATION" ]; then
    if ! grep -q "$LATEST_MIGRATION" "$REPO_ROOT/memory-bank/techcontext.md" 2>/dev/null; then
        fail "techcontext.md does not reference latest migration $LATEST_MIGRATION"
    else
        pass "techcontext.md references latest migration ($LATEST_MIGRATION)"
    fi
fi

# =============================================================================
# GATE 2: RLS Policy Coverage
# =============================================================================
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔒 GATE 2: RLS Policy Coverage"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

RLS_ISSUES=0
for migration in "$REPO_ROOT/backend/src/main/resources/db/migration"/V*.sql; do
    [ -f "$migration" ] || continue
    bn=$(basename "$migration")

    # Get tables created in this migration
    while IFS= read -r line; do
        TABLE_NAME=$(echo "$line" | grep -oP 'CREATE\s+TABLE\s+(IF\s+NOT\s+EXISTS\s+)?(\w+)' | awk '{print $NF}' || true)
        [ -z "$TABLE_NAME" ] && continue

        # Skip system tables
        case "$TABLE_NAME" in
            flyway_*|schema_*|rls_*|audit_config*) continue ;;
        esac

        # Check if tenant_id is in table definition
        HAS_TENANT=$(awk "BEGIN{IGNORECASE=1} /CREATE TABLE.*${TABLE_NAME}/,/\);/" "$migration" | grep -ci "tenant_id" || true)
        if [ "$HAS_TENANT" -gt 0 ]; then
            # Check for RLS in the same migration file
            HAS_RLS=$(grep -ci "ENABLE ROW LEVEL SECURITY" "$migration" || true)
            if [ "$HAS_RLS" -eq 0 ]; then
                RLS_ISSUES=$((RLS_ISSUES + 1))
            fi
        fi
    done < <(grep -iP 'CREATE\s+TABLE' "$migration" || true)
done

if [ "$RLS_ISSUES" -gt "$RLS_VIOLATIONS_BASELINE" ]; then
    NEW_RLS=$((RLS_ISSUES - RLS_VIOLATIONS_BASELINE))
    fail "RLS violations INCREASED: ${RLS_ISSUES} vs ${RLS_VIOLATIONS_BASELINE} baseline (+${NEW_RLS} new tables without RLS)"
elif [ "$RLS_ISSUES" -lt "$RLS_VIOLATIONS_BASELINE" ]; then
    FIXED=$((RLS_VIOLATIONS_BASELINE - RLS_ISSUES))
    pass "RLS violations DECREASED by ${FIXED}! (${RLS_ISSUES} current, baseline was ${RLS_VIOLATIONS_BASELINE})"
else
    pass "RLS coverage at baseline (${RLS_ISSUES}/${RLS_VIOLATIONS_BASELINE} known pre-existing)"
fi

# =============================================================================
# GATE 3: DTO Enforcement (regression detection)
# =============================================================================
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📦 GATE 3: DTO Enforcement"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Collect JPA entity class names
ENTITY_CLASSES=$(grep -rl "@Entity" "$REPO_ROOT/backend/src/main/java" --include="*.java" 2>/dev/null | while read -r f; do
    grep -oP '(?<=class\s)\w+' "$f" 2>/dev/null
done | sort -u)

# Count actual DTO violations
CURRENT_DTO_VIOLATIONS=0
DTO_DETAILS=""
for controller in $(find "$REPO_ROOT/backend/src/main/java" -name "*Controller.java" -type f); do
    cname=$(basename "$controller" .java)
    for entity in $ENTITY_CLASSES; do
        MATCHES=$(grep -cP "ResponseEntity<${entity}>|ResponseEntity<List<${entity}>>|public\s+${entity}\s+\w+\(|public\s+List<${entity}>\s+\w+\(" "$controller" 2>/dev/null || true)
        if [ "$MATCHES" -gt 0 ]; then
            CURRENT_DTO_VIOLATIONS=$((CURRENT_DTO_VIOLATIONS + MATCHES))
            DTO_DETAILS="${DTO_DETAILS}  - ${cname} returns ${entity} (${MATCHES} occurrences)\n"
        fi
    done
done

if [ "$CURRENT_DTO_VIOLATIONS" -gt "$DTO_VIOLATIONS_BASELINE" ]; then
    NEW_VIOLATIONS=$((CURRENT_DTO_VIOLATIONS - DTO_VIOLATIONS_BASELINE))
    fail "DTO violations INCREASED: ${CURRENT_DTO_VIOLATIONS} current vs ${DTO_VIOLATIONS_BASELINE} baseline (+${NEW_VIOLATIONS} new)"
    echo -e "$DTO_DETAILS"
elif [ "$CURRENT_DTO_VIOLATIONS" -lt "$DTO_VIOLATIONS_BASELINE" ]; then
    FIXED=$((DTO_VIOLATIONS_BASELINE - CURRENT_DTO_VIOLATIONS))
    pass "DTO violations DECREASED by ${FIXED}! (${CURRENT_DTO_VIOLATIONS} current, baseline was ${DTO_VIOLATIONS_BASELINE})"
    info "Consider updating baseline in quality-gate-baselines.conf"
else
    pass "DTO violations at baseline (${CURRENT_DTO_VIOLATIONS}/${DTO_VIOLATIONS_BASELINE})"
fi

# =============================================================================
# GATE 4: Backend Test Coverage (regression detection)
# =============================================================================
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🧪 GATE 4: Backend Test Coverage"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

CURRENT_MISSING_TESTS=0
MISSING_TEST_LIST=""
while IFS= read -r service_file; do
    SERVICE_NAME=$(basename "$service_file" .java)
    TEST_EXISTS=$(find "$REPO_ROOT/backend/src/test" -name "${SERVICE_NAME}Test.java" -o -name "${SERVICE_NAME}IntegrationTest.java" -o -name "${SERVICE_NAME}IT.java" 2>/dev/null | head -1)
    if [ -z "$TEST_EXISTS" ]; then
        CURRENT_MISSING_TESTS=$((CURRENT_MISSING_TESTS + 1))
        MISSING_TEST_LIST="${MISSING_TEST_LIST}  - ${SERVICE_NAME}\n"
    fi
done < <(find "$REPO_ROOT/backend/src/main/java" -name "*Service.java" -not -name "*Interface.java" -type f)

if [ "$CURRENT_MISSING_TESTS" -gt "$MISSING_BACKEND_TESTS_BASELINE" ]; then
    NEW_MISSING=$((CURRENT_MISSING_TESTS - MISSING_BACKEND_TESTS_BASELINE))
    fail "Missing test files INCREASED: ${CURRENT_MISSING_TESTS} vs ${MISSING_BACKEND_TESTS_BASELINE} baseline (+${NEW_MISSING} new)"
    echo -e "$MISSING_TEST_LIST"
elif [ "$CURRENT_MISSING_TESTS" -lt "$MISSING_BACKEND_TESTS_BASELINE" ]; then
    FIXED=$((MISSING_BACKEND_TESTS_BASELINE - CURRENT_MISSING_TESTS))
    pass "Missing test files DECREASED by ${FIXED}! (${CURRENT_MISSING_TESTS} current, baseline was ${MISSING_BACKEND_TESTS_BASELINE})"
else
    pass "Missing backend test files at baseline (${CURRENT_MISSING_TESTS}/${MISSING_BACKEND_TESTS_BASELINE})"
fi

SERVICE_COUNT=$(find "$REPO_ROOT/backend/src/main/java" -name "*Service.java" -not -name "*Interface.java" -type f | wc -l | tr -d ' ')
TEST_FILE_COUNT=$(find "$REPO_ROOT/backend/src/test" -name "*Test.java" -type f | wc -l | tr -d ' ')
info "Backend: ${SERVICE_COUNT} services, ${TEST_FILE_COUNT} test files, ${ACTUAL_TEST_COUNT} @Test methods"

# =============================================================================
# GATE 5: Flyway Migration Conventions
# =============================================================================
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🗄️  GATE 5: Flyway Migration Conventions"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

for migration in "$REPO_ROOT/backend/src/main/resources/db/migration"/V*.sql; do
    [ -f "$migration" ] || continue
    bn=$(basename "$migration")

    # Check naming convention
    if ! echo "$bn" | grep -qP '^V\d+__\w+\.sql$'; then
        fail "Migration naming violation: ${bn} (expected V{N}__{description}.sql)"
    fi

    # Check for plain TIMESTAMP (should be TIMESTAMPTZ)
    PLAIN_TS=$(grep -cP '\bTIMESTAMP\b(?!\s*WITH\s*TIME\s*ZONE|\s*TZ)' "$migration" 2>/dev/null || true)
    if [ "$PLAIN_TS" -gt 0 ]; then
        warn "${bn}: uses TIMESTAMP instead of TIMESTAMPTZ ($PLAIN_TS occurrences)"
    fi
done
pass "Migration convention scan complete"

# =============================================================================
# GATE 6: Frontend Test Coverage (regression detection)
# =============================================================================
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🎨 GATE 6: Frontend Test Coverage"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

CURRENT_MISSING_FE_SPECS=0
while IFS= read -r component; do
    [ -z "$component" ] && continue
    SPEC_FILE="${component%.ts}.spec.ts"
    if [ ! -f "$SPEC_FILE" ]; then
        CURRENT_MISSING_FE_SPECS=$((CURRENT_MISSING_FE_SPECS + 1))
    fi
done < <(find "$REPO_ROOT/frontend/src/app" -name "*.component.ts" -not -name "*.spec.ts" -type f 2>/dev/null)

if [ "$CURRENT_MISSING_FE_SPECS" -gt "$MISSING_FRONTEND_SPECS_BASELINE" ]; then
    NEW_MISSING=$((CURRENT_MISSING_FE_SPECS - MISSING_FRONTEND_SPECS_BASELINE))
    fail "Missing frontend spec files INCREASED: ${CURRENT_MISSING_FE_SPECS} vs ${MISSING_FRONTEND_SPECS_BASELINE} baseline (+${NEW_MISSING} new)"
else
    pass "Frontend spec coverage at or below baseline (${CURRENT_MISSING_FE_SPECS}/${MISSING_FRONTEND_SPECS_BASELINE})"
fi

FE_COMPONENTS=$(find "$REPO_ROOT/frontend/src/app" -name "*.component.ts" -not -name "*.spec.ts" -type f 2>/dev/null | wc -l | tr -d ' ')
FE_SPECS=$(find "$REPO_ROOT/frontend/src/app" -name "*.spec.ts" -type f 2>/dev/null | wc -l | tr -d ' ')
info "Frontend: ${FE_COMPONENTS} components, ${FE_SPECS} spec files"

# =============================================================================
# GATE 7: i18n Enforcement (regression detection)
# =============================================================================
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🌐 GATE 7: i18n Enforcement"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

CURRENT_I18N_VIOLATIONS=0
while IFS= read -r template; do
    [ -z "$template" ] && continue
    # Count substantial hardcoded text (3+ words, starts with capital), excluding transloco/i18n patterns
    FILTERED=$(grep -P '>[A-Z][a-z]+(\s+[a-z]+){2,}<' "$template" 2>/dev/null | grep -cv 'transloco\|translate\|i18n\|{{' || true)
    CURRENT_I18N_VIOLATIONS=$((CURRENT_I18N_VIOLATIONS + FILTERED))
done < <(find "$REPO_ROOT/frontend/src/app" -name "*.html" -type f 2>/dev/null)

if [ "$CURRENT_I18N_VIOLATIONS" -gt "$I18N_VIOLATIONS_BASELINE" ]; then
    NEW_VIOLATIONS=$((CURRENT_I18N_VIOLATIONS - I18N_VIOLATIONS_BASELINE))
    fail "i18n violations INCREASED: ${CURRENT_I18N_VIOLATIONS} vs ${I18N_VIOLATIONS_BASELINE} baseline (+${NEW_VIOLATIONS} new)"
else
    pass "i18n violations at or below baseline (${CURRENT_I18N_VIOLATIONS}/${I18N_VIOLATIONS_BASELINE})"
fi

# =============================================================================
# GATE 8: BigDecimal Enforcement
# =============================================================================
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "💰 GATE 8: BigDecimal Enforcement"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

MONETARY_VIOLATIONS=0
while IFS= read -r java_file; do
    [ -z "$java_file" ] && continue
    VIOLATIONS=$(grep -nP '\b(double|float|Double|Float)\s+\w*(amount|price|total|balance|credit|debit|cost|fee|tax|payment|salary|wage|rate)\w*' "$java_file" 2>/dev/null || true)
    if [ -n "$VIOLATIONS" ]; then
        fn=$(basename "$java_file")
        while IFS= read -r line; do
            fail "BigDecimal violation in ${fn}: ${line}"
            MONETARY_VIOLATIONS=$((MONETARY_VIOLATIONS + 1))
        done <<< "$VIOLATIONS"
    fi
done < <(find "$REPO_ROOT/backend/src/main/java" -name "*.java" -type f 2>/dev/null)

[ "$MONETARY_VIOLATIONS" -eq 0 ] && pass "All monetary fields use BigDecimal (no double/float violations)"

# =============================================================================
# SUMMARY
# =============================================================================
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 QUALITY GATE SUMMARY"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ "$EXIT_CODE" -eq 0 ] && [ "$WARNINGS" -eq 0 ]; then
    echo -e "${GREEN}✅ ALL QUALITY GATES PASSED — No failures, no warnings${NC}"
elif [ "$EXIT_CODE" -eq 0 ]; then
    echo -e "${YELLOW}⚠️  QUALITY GATES PASSED WITH ${WARNINGS} WARNING(S)${NC}"
    echo "   Warnings don't block CI but should be addressed."
else
    echo -e "${RED}❌ QUALITY GATES FAILED${NC}"
    echo ""
    echo "📝 To fix:"
    echo "   1. Run ./.github/scripts/sync-memory-bank.sh --fix to auto-fix memory bank staleness"
    echo "   2. Fix any DTO/BigDecimal/RLS violations in your NEW code"
    echo "   3. Add test files for new services"
    echo "   4. Re-run this script to verify"
fi
echo ""

exit $EXIT_CODE
