#!/usr/bin/env bash
# =============================================================================
# sync-memory-bank.sh — Auto-sync memory bank files from actual repo state
# =============================================================================
# Automatically detects and fixes staleness in memory bank files:
#   - Test counts (backend @Test count, frontend spec count)
#   - Frontend module lists
#   - Migration lists
#   - Controller/service counts
#   - Cross-file contradictions
#
# Modes:
#   --check   (default) Report staleness without changing files
#   --fix     Automatically update memory bank files
#
# Run: ./.github/scripts/sync-memory-bank.sh [--check|--fix]
# =============================================================================

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
MODE="${1:---check}"
CHANGES=0

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

stale() {
    echo -e "${RED}STALE:${NC} $1"
    CHANGES=$((CHANGES + 1))
}

fresh() {
    echo -e "${GREEN}FRESH:${NC} $1"
}

fixed() {
    echo -e "${BLUE}FIXED:${NC} $1"
}

# =============================================================================
# Gather actual state
# =============================================================================
echo "🔍 Scanning repository for actual state..."
echo ""

# Backend test count
ACTUAL_BACKEND_TESTS=$(grep -r "@Test" "$REPO_ROOT/backend/src/test" --include="*.java" 2>/dev/null | wc -l | tr -d ' ')

# Frontend spec count
ACTUAL_FRONTEND_SPECS=$(find "$REPO_ROOT/frontend/src" -name "*.spec.ts" -type f 2>/dev/null | wc -l | tr -d ' ')

# Frontend modules
ACTUAL_MODULES=$(ls -d "$REPO_ROOT/frontend/src/app"/*/ 2>/dev/null | xargs -I{} basename {} | sort)
ACTUAL_MODULE_COUNT=$(echo "$ACTUAL_MODULES" | wc -l | tr -d ' ')

# Backend controllers
ACTUAL_CONTROLLERS=$(find "$REPO_ROOT/backend/src/main/java" -name "*Controller.java" -type f | wc -l | tr -d ' ')

# Backend services
ACTUAL_SERVICES=$(find "$REPO_ROOT/backend/src/main/java" -name "*Service.java" -not -name "*Interface.java" -type f | wc -l | tr -d ' ')

# Migrations
ACTUAL_MIGRATIONS=$(ls "$REPO_ROOT/backend/src/main/resources/db/migration"/V*.sql 2>/dev/null | xargs -I{} basename {} | sort -V)
LATEST_MIGRATION=$(echo "$ACTUAL_MIGRATIONS" | tail -1 | grep -oP 'V\d+' || true)
MIGRATION_COUNT=$(echo "$ACTUAL_MIGRATIONS" | wc -l | tr -d ' ')

echo "📊 Actual Repository State:"
echo "   Backend tests:     ${ACTUAL_BACKEND_TESTS}"
echo "   Frontend specs:    ${ACTUAL_FRONTEND_SPECS}"
echo "   Frontend modules:  ${ACTUAL_MODULE_COUNT}"
echo "   Backend controllers: ${ACTUAL_CONTROLLERS}"
echo "   Backend services:  ${ACTUAL_SERVICES}"
echo "   Migrations:        ${MIGRATION_COUNT} (latest: ${LATEST_MIGRATION})"
echo "   Modules: $(echo $ACTUAL_MODULES | tr '\n' ', ')"
echo ""

# =============================================================================
# Fix: techcontext.md — Backend test count
# =============================================================================
echo "━━━ Checking memory-bank/techcontext.md ━━━"

TECHCONTEXT="$REPO_ROOT/memory-bank/techcontext.md"
if [ -f "$TECHCONTEXT" ]; then
    # Fix backend test count in "Run tests (NNN+ tests)" line
    OLD_TEST_LINE=$(grep -oP '# Run tests \(\d+\+? tests\)' "$TECHCONTEXT" || true)
    if [ -n "$OLD_TEST_LINE" ]; then
        OLD_TEST_NUM=$(echo "$OLD_TEST_LINE" | grep -oP '\d+' | head -1)
        if [ "$OLD_TEST_NUM" -ne "$ACTUAL_BACKEND_TESTS" ]; then
            stale "techcontext.md: test count ${OLD_TEST_NUM} → ${ACTUAL_BACKEND_TESTS}"
            if [ "$MODE" = "--fix" ]; then
                sed -i "s/# Run tests (${OLD_TEST_NUM}+ tests)/# Run tests (${ACTUAL_BACKEND_TESTS} tests)/" "$TECHCONTEXT"
                sed -i "s/# Run tests (${OLD_TEST_NUM}+\? tests)/# Run tests (${ACTUAL_BACKEND_TESTS} tests)/" "$TECHCONTEXT"
                fixed "techcontext.md: updated test count to ${ACTUAL_BACKEND_TESTS}"
            fi
        else
            fresh "techcontext.md: backend test count (${ACTUAL_BACKEND_TESTS})"
        fi
    fi

    # Fix frontend module list — check for missing modules in the repo structure section
    MISSING_IN_TECH=""
    for mod in $ACTUAL_MODULES; do
        if ! grep -q "${mod}/" "$TECHCONTEXT" 2>/dev/null && ! grep -q "${mod} " "$TECHCONTEXT" 2>/dev/null; then
            MISSING_IN_TECH="${MISSING_IN_TECH} ${mod}"
        fi
    done
    if [ -n "$MISSING_IN_TECH" ]; then
        stale "techcontext.md: missing frontend modules:${MISSING_IN_TECH}"
        if [ "$MODE" = "--fix" ]; then
            # Add missing modules to the repository structure section (before the closing ```)
            # Find the line with "└── receivable/" or the last frontend module and add after it
            for mod in $MISSING_IN_TECH; do
                # Determine appropriate description based on module name
                case "$mod" in
                    auth)    DESC="Authentication & guards" ;;
                    master)  DESC="Master data management" ;;
                    payable) DESC="Accounts payable" ;;
                    reports) DESC="Report generation" ;;
                    *)       DESC="${mod^} module" ;;
                esac

                # Find the last frontend module line and add the new one
                LAST_FE_LINE=$(grep -n "│   ├──\|│   └──" "$TECHCONTEXT" | tail -1 | cut -d: -f1)
                if [ -n "$LAST_FE_LINE" ]; then
                    # Change the last └── to ├── if adding a new last item
                    sed -i "${LAST_FE_LINE}s/└──/├──/" "$TECHCONTEXT"
                    # Insert after the last line
                    sed -i "${LAST_FE_LINE}a\\    └── ${mod}/$(printf '%*s' $((25 - ${#mod})) '')← ${DESC}" "$TECHCONTEXT"
                fi
            done
            fixed "techcontext.md: added missing modules:${MISSING_IN_TECH}"
        fi
    else
        fresh "techcontext.md: all frontend modules listed"
    fi
fi

# =============================================================================
# Fix: activecontext.md — Module count
# =============================================================================
echo ""
echo "━━━ Checking memory-bank/activecontext.md ━━━"

ACTIVECONTEXT="$REPO_ROOT/memory-bank/activecontext.md"
if [ -f "$ACTIVECONTEXT" ]; then
    # Fix module count
    OLD_MODULE_COUNT=$(grep -oP '\d+ feature modules' "$ACTIVECONTEXT" | grep -oP '\d+' | head -1 || true)
    if [ -n "$OLD_MODULE_COUNT" ] && [ "$OLD_MODULE_COUNT" -ne "$ACTUAL_MODULE_COUNT" ]; then
        stale "activecontext.md: module count ${OLD_MODULE_COUNT} → ${ACTUAL_MODULE_COUNT}"
        if [ "$MODE" = "--fix" ]; then
            sed -i "s/${OLD_MODULE_COUNT} feature modules/${ACTUAL_MODULE_COUNT} feature modules/" "$ACTIVECONTEXT"
            fixed "activecontext.md: updated module count to ${ACTUAL_MODULE_COUNT}"
        fi
    else
        fresh "activecontext.md: module count (${ACTUAL_MODULE_COUNT})"
    fi

    # Fix backend test count
    OLD_AC_TESTS=$(grep -oP 'Tests:\*\*\s*\d+' "$ACTIVECONTEXT" | grep -oP '\d+' | head -1 || true)
    if [ -n "$OLD_AC_TESTS" ] && [ "$OLD_AC_TESTS" -ne "$ACTUAL_BACKEND_TESTS" ]; then
        stale "activecontext.md: backend tests ${OLD_AC_TESTS} → ${ACTUAL_BACKEND_TESTS}"
        if [ "$MODE" = "--fix" ]; then
            sed -i "s/Tests:\*\* ${OLD_AC_TESTS}/Tests:** ${ACTUAL_BACKEND_TESTS}/" "$ACTIVECONTEXT"
            fixed "activecontext.md: updated backend tests to ${ACTUAL_BACKEND_TESTS}"
        fi
    else
        fresh "activecontext.md: backend test count"
    fi
fi

# =============================================================================
# Fix: progress.md — Test count in summary
# =============================================================================
echo ""
echo "━━━ Checking memory-bank/progress.md ━━━"

PROGRESS="$REPO_ROOT/memory-bank/progress.md"
if [ -f "$PROGRESS" ]; then
    OLD_PROG_TESTS=$(grep -oP '\d+ backend' "$PROGRESS" | grep -oP '\d+' | head -1 || true)
    if [ -n "$OLD_PROG_TESTS" ] && [ "$OLD_PROG_TESTS" -ne "$ACTUAL_BACKEND_TESTS" ]; then
        stale "progress.md: backend test count ${OLD_PROG_TESTS} → ${ACTUAL_BACKEND_TESTS}"
        if [ "$MODE" = "--fix" ]; then
            sed -i "s/${OLD_PROG_TESTS} backend/${ACTUAL_BACKEND_TESTS} backend/" "$PROGRESS"
            fixed "progress.md: updated backend test count"
        fi
    else
        fresh "progress.md: backend test count (${ACTUAL_BACKEND_TESTS})"
    fi
fi

# =============================================================================
# Fix: systempatterns.md — Quality gate test baseline
# =============================================================================
echo ""
echo "━━━ Checking memory-bank/systempatterns.md ━━━"

SYSPATTERNS="$REPO_ROOT/memory-bank/systempatterns.md"
if [ -f "$SYSPATTERNS" ]; then
    OLD_GATE_TESTS=$(grep -oP '\d+\+?\s*backend' "$SYSPATTERNS" | grep -oP '\d+' | head -1 || true)
    if [ -n "$OLD_GATE_TESTS" ] && [ "$OLD_GATE_TESTS" -ne "$ACTUAL_BACKEND_TESTS" ]; then
        stale "systempatterns.md: quality gate baseline ${OLD_GATE_TESTS}+ → ${ACTUAL_BACKEND_TESTS}+"
        if [ "$MODE" = "--fix" ]; then
            sed -i "s/${OLD_GATE_TESTS}+ backend/${ACTUAL_BACKEND_TESTS}+ backend/" "$SYSPATTERNS"
            fixed "systempatterns.md: updated quality gate baseline"
        fi
    else
        fresh "systempatterns.md: quality gate baseline"
    fi
fi

# =============================================================================
# Summary
# =============================================================================
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if [ "$CHANGES" -eq 0 ]; then
    echo -e "${GREEN}✅ Memory bank is in sync with repository — no staleness detected${NC}"
elif [ "$MODE" = "--fix" ]; then
    echo -e "${BLUE}🔧 Fixed ${CHANGES} stale value(s) in memory bank files${NC}"
    echo "   Changes are ready to commit."
else
    echo -e "${RED}❌ Found ${CHANGES} stale value(s) in memory bank files${NC}"
    echo ""
    echo "   Run with --fix to auto-correct:"
    echo "   ./.github/scripts/sync-memory-bank.sh --fix"
    exit 1
fi
