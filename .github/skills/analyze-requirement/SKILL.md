---
name: analyze-requirement
description: >-
  Analyze a new requirement for OneBook using the SDLC methodology. Classify domains,
  assess complexity, assign specialist agents, define implementation phases, and create
  a tracked requirement document.
---

# Analyze Requirement

Analyze and plan implementation of a new requirement using OneBook's SDLC methodology.

## When to Use

- When a user provides a new feature request or requirement
- When @partner needs to orchestrate a multi-domain change
- When classifying and planning any non-trivial work item

## Steps

### 1. Read Project Context

Always start by reading current state:

```
CLAUDE.md                          → Project memory bank entry point
memory-bank/activecontext.md       → What changed recently
memory-bank/progress.md            → What's complete and in progress
```

### 2. Classify the Requirement

**Domain Classification Matrix:**

| Domain Keywords | Primary Agent | Common Collaborators |
|----------------|---------------|---------------------|
| accounting, ledger, journal, API endpoint | @backend | @database, @frontend |
| screen, UI, component, navigation | @frontend | @backend |
| schema, migration, table, RLS | @database | @backend, @security |
| encryption, authentication, audit | @security | @database, @backend |
| Docker, CI/CD, Redis, deployment | @infra | @security |
| documentation, API docs, diagrams | @docs | All |
| testing, quality gates, validation | @quality | All |

**Complexity Assessment:**

| Level | Criteria | Agents | Testing |
|-------|----------|--------|---------|
| LOW | Single domain, < 1 week | 1 | Unit tests only |
| MEDIUM | 2–3 domains, 1–2 weeks | 2–3 | Unit + Integration |
| HIGH | 4+ domains, 3+ weeks | 4+ | Full suite + UAT |
| CRITICAL | System-wide, security impact | All | Full + Security + Perf |

### 3. Create Requirement Document

Save to `docs/requirements/active/REQ-{YYYY}-{NNN}.md`:

```markdown
# REQ-{ID}: {Title}

## Summary
{One-paragraph description}

## Classification
- **Domains**: [Backend, Frontend, Database, Security, Infra]
- **Complexity**: [LOW/MEDIUM/HIGH/CRITICAL]
- **Primary Agent**: @{agent}
- **Collaborating Agents**: [@agent1, @agent2]

## Acceptance Criteria
- [ ] Criterion 1
- [ ] Criterion 2

## Implementation Plan
### Phase 1: Database — @database
- Schema design, migration, RLS policies

### Phase 2: Backend — @backend
- DTOs, repositories, services, controllers, tests

### Phase 3: Frontend — @frontend
- Components, services, routing

### Phase 4: Security — @security
- Encryption, RLS verification, audit trail

### Phase 5: Quality — @quality
- Full quality gate validation

### Phase 6: Documentation — @docs
- API docs, schema docs, memory bank update

## Status Tracker
- [ ] Phase 1: Pending
- [ ] Phase 2: Pending
- [ ] Phase 3: Pending
- [ ] Phase 4: Pending
- [ ] Phase 5: Pending
- [ ] Phase 6: Pending
```

### 4. Define Orchestration Pattern

Choose the appropriate pattern:

- **Sequential** (most common): `@database → @backend → @frontend → @security → @quality → @docs`
- **Parallel**: `@database + @security → @backend → @frontend → @quality → @docs`
- **Iterative**: `@backend ↔ @frontend → @quality → @docs`

### 5. Present Plan to User

Show the plan with:
- Which agents will be involved
- What each agent will do
- Expected deliverables per phase
- Dependencies between phases

**Wait for user approval before executing.**

### 6. Execute with Feedback Loops

After each agent phase:
1. Review output against acceptance criteria
2. If issues → send back to agent with specific feedback
3. If clean → update status tracker → proceed to next phase

## References

- Partner agent: `.github/agents/partner.agent.md`
- Requirement template: `.github/templates/requirement-analysis-template.md`
- Active requirements: `docs/requirements/active/`
