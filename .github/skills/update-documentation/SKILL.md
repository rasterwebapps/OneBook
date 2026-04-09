---
name: update-documentation
description: >-
  Update OneBook documentation following the Selective Documentation Update Protocol.
  Uses the Documentation Impact Matrix to determine which files need updating based
  on the type of change made.
---

# Update Documentation

Update project documentation following OneBook's Selective Documentation Update Protocol.

## When to Use

- After any code implementation phase completes
- When @partner delegates documentation to @docs
- After new API endpoints, database migrations, UI components, or security changes
- At the end of every work session (memory bank update)

## Steps

### 1. Identify the Change Scope

Determine what type of change was made:
- Bug fix (no API/schema change)
- New API endpoint
- New database table/migration
- New UI screen/component
- New keyboard shortcut
- New module/package
- Security change
- Infrastructure change
- Full new feature (DB + API + UI)
- Documentation-only change
- Refactor (no behavior change)

### 2. Consult the Documentation Impact Matrix

| Change Type | ALWAYS Update | CONDITIONALLY Update | NEVER Touch |
|---|---|---|---|
| Bug fix | `activecontext.md`, `troubleshooting.md` | — | User manual, API docs |
| New API endpoint | `activecontext.md`, `progress.md` | `api-documentation.md`, `api-contracts.md` | Keyboard shortcuts, architecture |
| New DB table | `activecontext.md`, `progress.md` | `sql-schema.md`, `data-dictionary.md` | User manual, keyboard shortcuts |
| New UI component | `activecontext.md`, `progress.md` | `user-manual.md`, `feature-catalog.md` | SQL schema, API docs |
| New keyboard shortcut | `activecontext.md` | `keyboard-shortcuts.md`, `key-binding-registry.md` | SQL schema, API docs |
| New module/package | `activecontext.md`, `progress.md` | Agent files, `architecture-diagram.md` | User manual, keyboard shortcuts |
| Security change | `activecontext.md`, `systempatterns.md` | `operational-runbook.md` | User manual, feature catalog |
| Infrastructure change | `activecontext.md` | `operational-runbook.md`, `developer-guide.md` | User manual, API docs |
| Full new feature | `activecontext.md`, `progress.md` | All docs in scope | Docs for unrelated features |

### 3. Update Memory Bank (ALWAYS Required)

**`memory-bank/activecontext.md`** — Record what changed:
```markdown
### {Feature Name} ({Date})
- **What changed**: Brief description
- **Files**: Key files created/modified
- **Tests**: Test count delta
- **Next**: What should happen next
```

**`memory-bank/progress.md`** — If milestone items completed, update checkboxes.

**`memory-bank/systempatterns.md`** — If new architectural patterns were established.

**`memory-bank/troubleshooting.md`** — If bugs were found and fixed.

### 4. Update Affected Documentation

Based on Step 2, update ONLY the affected docs:

- **API docs**: `docs/technical/api-documentation.md`, `docs/technical/api-contracts.md`
- **Schema docs**: `docs/technical/sql-schema.md`, `docs/technical/data-dictionary.md`
- **Architecture**: `docs/technical/architecture-diagram.md`
- **User docs**: `docs/user/user-manual.md`, `docs/user/feature-catalog.md`
- **Keyboard**: `docs/user/keyboard-shortcuts.md`, `docs/technical/key-binding-registry.md`

### 5. Run Documentation Validation

```bash
cd docs/automation && npm run validate
```

### 6. Run Memory Bank Sync

```bash
./.github/scripts/sync-memory-bank.sh --check
```

If stale values detected:
```bash
./.github/scripts/sync-memory-bank.sh --fix
```

## References

- Documentation agent: `.github/agents/docs.agent.md`
- Impact Matrix: `CLAUDE.md` → "Selective Documentation Update Protocol"
- Doc automation: `docs/automation/`
