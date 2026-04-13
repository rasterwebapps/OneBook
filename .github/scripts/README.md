# Validation & Automation Scripts

This directory contains validation and automation scripts to ensure code quality, consistency, and memory bank freshness across the OneBook codebase.

## Available Scripts

### validate-agent-ownership.sh

**Purpose**: Validates that all modules, services, controllers, and packages in the codebase are properly documented in agent instruction files.

**Usage**:
```bash
./.github/scripts/validate-agent-ownership.sh
```

**What it checks**:
- ✅ All frontend modules in `frontend/src/app/` are documented
- ✅ All backend services in `backend/src/main/java/.../service/` are documented
- ✅ All backend controllers in `backend/src/main/java/.../controller/` are documented
- ✅ All backend packages in `backend/src/main/java/com/nexus/onebook/` are documented
- ⚠️ Database migrations (warnings only, doesn't fail)

**Exit codes**:
- `0` - All components are documented ✓
- `1` - Missing ownership declarations found ✗

---

### validate-quality-gates.sh

**Purpose**: Comprehensive quality gate enforcement that catches ALL commonly skipped steps in AI-assisted development.

**Usage**:
```bash
./.github/scripts/validate-quality-gates.sh
```

**What it checks (8 gates)**:
1. 🧠 **Memory Bank Freshness** — Test counts, module lists, migration lists match actual repo
2. 🔒 **RLS Policy Coverage** — Every tenant-scoped table has Row-Level Security
3. 📦 **DTO Enforcement** — No JPA entities returned directly from controllers
4. 🧪 **Backend Test Coverage** — Every Service class has a corresponding Test file
5. 🗄️ **Flyway Migration Conventions** — Naming, tenant_id, TIMESTAMPTZ usage
6. 🎨 **Frontend Test Coverage** — Every component has a .spec.ts file
7. 🌐 **i18n Enforcement** — No hardcoded English strings in templates
8. 💰 **BigDecimal Enforcement** — No double/float for monetary fields

**Exit codes**:
- `0` - All gates passed (may have warnings)
- `1` - One or more gates failed ✗

---

### sync-memory-bank.sh

**Purpose**: Auto-detects and fixes staleness in memory bank files by comparing documented values against actual repository state.

**Usage**:
```bash
# Check mode (default) — report staleness without changing files
./.github/scripts/sync-memory-bank.sh --check

# Fix mode — automatically update stale values in memory bank files
./.github/scripts/sync-memory-bank.sh --fix
```

**What it syncs**:
- Backend test count (actual @Test annotations vs documented count)
- Frontend module list and count
- Migration list and latest version
- Cross-file consistency (techcontext.md, activecontext.md, progress.md, systempatterns.md)

**CI integration**: On push to `main`, if staleness is detected, the fix is auto-committed.

**Exit codes**:
- `0` - Memory bank is in sync / fixes applied
- `1` - Staleness detected (check mode only)

---

## CI/CD Integration

All three scripts run automatically in the CI pipeline (`.github/workflows/ci.yml`):

| Job | Trigger | Script | Behavior |
|-----|---------|--------|----------|
| `validate-ownership` | Every PR & push | `validate-agent-ownership.sh` | Fails PR if undocumented components |
| `validate-quality-gates` | Every PR & push | `validate-quality-gates.sh` | Fails PR if quality violations found |
| `sync-memory-bank` | Push to `main` only | `sync-memory-bank.sh --fix` | Auto-commits memory bank fixes |

## When to Run Locally

- **Before submitting a PR**: Run all three scripts
- **After adding new code**: `validate-agent-ownership.sh` + `validate-quality-gates.sh`
- **After adding tests**: `sync-memory-bank.sh --fix` to update test counts
- **Start of session**: `sync-memory-bank.sh --check` to verify context

## Related Documentation

- [Agent Ownership Maintenance Guide](../agents/MAINTENANCE.md) - How to update agent files
- [Agent Instructions README](../agents/README.md) - Overview of agent system
- [Design Requirements Index](../agents/INDEX.md) - Quick reference by category

---

**Last Updated:** 2026-04-04  
**Maintained By:** @Architect
