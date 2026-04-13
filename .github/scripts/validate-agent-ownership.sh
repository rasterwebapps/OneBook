#!/bin/bash
# Script to validate that all modules, services, and controllers are documented in agent instruction files
# Usage: ./validate-agent-ownership.sh

set -e

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
AGENTS_DIR="$REPO_ROOT/.github/agents"
EXIT_CODE=0

echo "🔍 Validating agent ownership documentation..."
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to check if a path is mentioned in agent files
check_ownership() {
    local path=$1
    local type=$2
    local found=0
    
    # Remove leading ./
    path=${path#./}
    
    # Search in agent instruction files (.agent.md files)
    for agent_file in "$AGENTS_DIR"/*.agent.md; do
        if [ -f "$agent_file" ]; then
            if grep -q "$path" "$agent_file" 2>/dev/null; then
                found=1
                break
            fi
        fi
    done
    
    if [ $found -eq 0 ]; then
        echo -e "${RED}✗${NC} Missing: $type - $path"
        EXIT_CODE=1
    fi
}

# Check Frontend Modules
echo "📁 Checking Frontend Modules..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

cd "$REPO_ROOT/frontend/src/app"
for dir in */; do
    dir=${dir%/}
    # Skip common non-business directories
    if [[ "$dir" != "shared" && "$dir" != "core" && "$dir" != "utils" ]]; then
        check_ownership "frontend/src/app/$dir" "Frontend Module"
    fi
done

# Check Backend Services
echo ""
echo "⚙️  Checking Backend Services..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

ONEBOOK_PKG="$REPO_ROOT/backend/src/main/java/com/nexus/onebook"
find "$ONEBOOK_PKG" -name "*Service.java" | while read -r service_file; do
    file=$(basename "$service_file")
    service_name="${file%.java}"
    found=0
    for agent_file in "$AGENTS_DIR"/*.agent.md; do
        if [ -f "$agent_file" ]; then
            if grep -q "$service_name" "$agent_file" 2>/dev/null; then
                found=1
                break
            fi
        fi
    done
    if [ $found -eq 0 ]; then
        echo -e "${RED}✗${NC} Missing: Backend Service - $service_name"
        EXIT_CODE=1
    fi
done

# Check Backend Controllers
echo ""
echo "🎮 Checking Backend Controllers..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

find "$ONEBOOK_PKG" -name "*Controller.java" | while read -r ctrl_file; do
    file=$(basename "$ctrl_file")
    controller_name="${file%.java}"
    found=0
    for agent_file in "$AGENTS_DIR"/*.agent.md; do
        if [ -f "$agent_file" ]; then
            if grep -q "$controller_name" "$agent_file" 2>/dev/null; then
                found=1
                break
            fi
        fi
    done
    if [ $found -eq 0 ]; then
        echo -e "${RED}✗${NC} Missing: Backend Controller - $controller_name"
        EXIT_CODE=1
    fi
done

# Check Backend Packages
echo ""
echo "📦 Checking Backend Packages..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

cd "$ONEBOOK_PKG"
for dir in */; do
    dir=${dir%/}
    # Skip non-domain entries at the onebook root level
    if [[ "$dir" != "config" ]]; then
        check_ownership "$dir" "Backend Package"
    fi
done

# Check Database Migrations
echo ""
echo "🗄️  Checking Database Migrations..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

cd "$REPO_ROOT/backend/src/main/resources/db/migration"
for file in V*.sql; do
    if [ -f "$file" ]; then
        # Check if migration file is mentioned in agent files
        if ! grep -r --include="*.md" -q "$file" "$AGENTS_DIR" 2>/dev/null; then
            echo -e "${YELLOW}⚠${NC}  Warning: Migration not documented - $file"
            # Don't fail for migrations, just warn
        fi
    fi
done

echo ""
echo "🎯 Checking Orchestrator & Orchestration..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check that the @partner agent file exists
if [ ! -f "$AGENTS_DIR/partner.agent.md" ]; then
    echo -e "${RED}✗${NC} Missing: @partner agent file - .github/agents/partner.agent.md"
    EXIT_CODE=1
else
    echo -e "${GREEN}✓${NC} @partner agent file exists"
fi

# Check that the requirement analysis template exists
if [ ! -f "$REPO_ROOT/.github/templates/requirement-analysis-template.md" ]; then
    echo -e "${RED}✗${NC} Missing: Requirement template - .github/templates/requirement-analysis-template.md"
    EXIT_CODE=1
else
    echo -e "${GREEN}✓${NC} Requirement analysis template exists"
fi

# Check that partner.agent.md references the domain classification matrix (orchestration responsibilities)
if ! grep -q "Domain Classification Matrix" "$AGENTS_DIR/partner.agent.md" 2>/dev/null; then
    echo -e "${RED}✗${NC} Missing: Domain Classification Matrix in partner.agent.md"
    EXIT_CODE=1
else
    echo -e "${GREEN}✓${NC} Orchestration responsibilities documented (Domain Classification Matrix present)"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if [ $EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✓${NC} All modules, services, and controllers are documented in agent files"
else
    echo -e "${RED}✗${NC} Some components are missing from agent documentation"
    echo ""
    echo "📝 To fix this:"
    echo "   1. Identify which agent should own the missing component"
    echo "   2. Update the appropriate agent .md file in .github/agents/"
    echo "   3. Add the component to the 'Files Owned' section"
    echo "   4. Update INDEX.md if needed for cross-references"
    echo ""
    echo "📖 For detailed guidance, see:"
    echo "   .github/agents/MAINTENANCE.md - Complete ownership rules and examples"
    echo "   .github/AGENT_OWNERSHIP.md - Quick reference guide"
fi
echo ""

exit $EXIT_CODE
