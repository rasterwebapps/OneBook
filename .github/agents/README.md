# OneBook Agent System

This directory contains two tiers of agent files for the OneBook SDLC automation system.

## Memory Bank Integration

**⚡ Before starting any task, read [`CLAUDE.md`](../../CLAUDE.md)** — the AI memory bank entry point.  
It provides accumulated project intelligence, current state, and navigation to all memory files.

**At the end of every task, update:**
1. `memory-bank/activecontext.md` — what changed this session
2. `memory-bank/progress.md` — if milestone items were completed
3. `memory-bank/systempatterns.md` — if new patterns were established
4. `memory-bank/troubleshooting.md` — if bugs were found and fixed

---

## Tier 1: Invocable Copilot Agents (`.agent.md`)

These are real GitHub Copilot agents that can be invoked via `@` in chat. Users should ONLY invoke `@partner`.

| Agent | File | Role | SDLC Mapping |
|-------|------|------|-------------|
| 🤝 **@partner** | `partner.agent.md` | **Head Orchestrator** — the ONLY agent users invoke | Business Analyst + Project Manager + Team Lead |
| 📒 @backend | `backend.agent.md` | Backend development (Java/Spring Boot) | Backend Dev Team |
| 🎹 @frontend | `frontend.agent.md` | Frontend development (Angular) | Frontend Dev Team |
| 🗄️ @database | `database.agent.md` | Database design (PostgreSQL/Flyway) | DB Design Team |
| 🔐 @security | `security.agent.md` | Security (encryption, RLS, audit) | Security Review Team |
| 🏗️ @infra | `infra.agent.md` | Infrastructure (Docker, CI/CD, Redis) | DevOps Team |
| 📝 @docs | `docs.agent.md` | Documentation & memory bank | BA Documentation Phase |
| ✅ @quality | `quality.agent.md` | Testing & quality gates | Testing Team |

### SDLC Workflow (Agile with Feedback Loops)

```
User → @partner (analyzes requirement)
  │
  ├── Phase 1: @database (schema design)
  ├── Phase 2: @backend (services, controllers, DTOs)
  ├── Phase 3: @frontend (components, routes)
  ├── Phase 4: @security (encryption, RLS, auth)
  │
  ├── FEEDBACK LOOP: @partner validates each phase output
  │   └── Issues? → Route back to responsible agent → Fix → Re-validate
  │
  ├── Phase 5: @quality (testing, quality gates)
  │   └── Failures? → Route back to responsible agent → Fix → Re-test
  │
  └── Phase 6: @docs (documentation, memory bank update)
      └── Final status report to user
```

### Global Instructions

`.github/copilot-instructions.md` — Cross-cutting rules injected into ALL Copilot interactions.

---

## How It Works: SDLC with Agile Feedback Loops

### Traditional SDLC → Agent Mapping

| Step | Traditional Role | Agent |
|------|-----------------|-------|
| 1 | Client explains requirement | User types `@partner` |
| 2 | BA studies project, creates document | @partner reads memory bank, creates REQ doc |
| 3 | PM discusses with Team Lead | @partner plans phases and agent assignments |
| 4.1 | DB team designs schema | @partner delegates to @database |
| 4.2 | Backend team implements | @partner delegates to @backend |
| 4.3 | Frontend team implements | @partner delegates to @frontend |
| 5 | Team updates PM | Each agent reports completion to @partner |
| 6 | BA documents status | @partner tracks in requirement document |
| 7 | Testing team tests + reports | @partner invokes @quality → feedback loop |
| 8 | Business team gets delivery | @partner presents final status report |

### Feedback Loop Protocol (Agile Sprint)

```
Agent completes phase
  → @partner reviews output
  → Issues found? 
    → YES: @partner sends specific feedback → Agent fixes → @partner re-reviews
    → NO: Phase approved → Next phase begins
  → After all phases: @quality runs full test suite
  → Test failures? → Route back to responsible agent → Fix → Re-test
  → All green: @docs updates documentation → Delivery
```

### Inter-Agent Communication

Agents communicate through:
1. **@partner orchestration** — @partner delegates and collects results
2. **Shared memory bank** — All agents read/write `memory-bank/` files
3. **CLAUDE.md** — Common project knowledge base read by all agents
4. **copilot-instructions.md** — Global rules injected into every interaction

## Master Coordinator: @partner

**@partner** is the entry point for ALL work on OneBook. It receives, classifies, and orchestrates every requirement through the complete SDLC lifecycle.

**How it works with other agents:**
1. @partner **classifies** each requirement using the Domain Classification Matrix
2. It **assigns** a Primary Agent and Collaborating Agents based on the classification
3. It **orchestrates** the implementation phases (sequential, parallel, or iterative)
4. It **validates** quality gates before advancing to the next phase
5. It **coordinates** @security sign-off before production for HIGH/CRITICAL requirements
6. It **closes** the requirement after acceptance criteria are validated

For any new requirement, start by creating a requirement document using `.github/templates/requirement-analysis-template.md`.

---

## Usage

When working on the OneBook codebase:
1. **Read `CLAUDE.md`** — start every session here to load project context
2. Identify which domain your task belongs to
3. Review the relevant agent instruction file
4. Follow the patterns, conventions, and standards defined
5. If working across multiple domains, consult the Sub-Agent Interaction Matrix in `sub-agents.md`
6. **For new requirements** — use @partner to classify and orchestrate
7. **Update memory bank** at end of session — keep context alive for next session

## Updates

These instruction files should be kept in sync with:
- `CLAUDE.md` — AI memory bank entry point
- `memory-bank/` — Persistent session memory files
- `sub-agents.md` - Overall sub-agent architecture and interaction matrix
- `docs/developer-guide.md` - General developer onboarding guide
- `CONTRIBUTING.md` - Contribution guidelines
- Code examples in the repository

When adding new patterns or conventions, update the relevant agent instruction file(s) AND the memory bank.

## Maintaining Agent Ownership

**⚠️ IMPORTANT**: When you add new files, modules, services, controllers, or migrations to the codebase, you MUST update the appropriate agent instruction files to declare ownership.

See **[MAINTENANCE.md](MAINTENANCE.md)** for:
- Step-by-step guide on updating agent ownership
- Ownership rules for different component types
- Validation script usage (`.github/scripts/validate-agent-ownership.sh`)
- Examples and troubleshooting

**Quick validation**: Run `./.github/scripts/validate-agent-ownership.sh` to check for missing ownership declarations.
