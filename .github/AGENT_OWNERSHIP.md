# Adding New Code? Update Agent Ownership! 📝

When you add new files, modules, services, or controllers to OneBook, you **MUST** update the agent ownership declarations.

## Quick Start

**1. Add your code**
```bash
# Create your new service, controller, or module
```

**2. Update agent ownership**
```bash
# Identify which agent owns your code
# See: .github/agents/MAINTENANCE.md for ownership rules

# Update the appropriate agent file in .github/agents/
# Add your new component to the "Files Owned" section
```

**3. Validate**
```bash
# Run the validation script
./.github/scripts/validate-agent-ownership.sh

# Should output: ✓ All modules, services, and controllers are documented
```

**4. Commit everything together**
```bash
git add .
git commit -m "Your changes"
```

## Why?

The OneBook sub-agent architecture requires clear ownership boundaries. When agents know which files they own, they can:
- Enforce domain-specific conventions
- Maintain consistent patterns
- Collaborate effectively on cross-cutting concerns
- Keep the codebase maintainable at scale

## Ownership Quick Reference

| What You Added | Which Agent Owns It | File to Update |
|----------------|---------------------|----------------|
| Core accounting service | @backend | `backend.agent.md` |
| Financial report | @backend | `backend.agent.md` |
| Security/encryption service | @security | `security.agent.md` |
| Cache-related service | @infra | `infra.agent.md` |
| Integration adapter | @backend | `backend.agent.md` |
| Inventory/payroll service | @backend | `backend.agent.md` |
| AI/forecasting service | @backend | `backend.agent.md` |
| Tax/compliance service | @backend | `backend.agent.md` |
| Audit/observability service | @security | `security.agent.md` |
| Frontend module (UI) | @frontend | `frontend.agent.md` |
| Database migration | @database | `database.agent.md` |

## Detailed Guide

For comprehensive ownership rules, examples, and troubleshooting, see:

📖 **[Agent Ownership Maintenance Guide](agents/MAINTENANCE.md)**

## CI Validation

The validation script runs automatically on all PRs. If you forget to update agent ownership, the CI build will fail with a clear message telling you what's missing.

---

**Questions?** See [agents/MAINTENANCE.md](agents/MAINTENANCE.md) or [agents/README.md](agents/README.md)
