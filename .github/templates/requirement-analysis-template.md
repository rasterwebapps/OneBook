# Requirement Analysis: [REQ-ID]

> **Template Version:** 1.0  
> **Owner:** @RequirementsAnalyzer  
> **Instructions:** Replace all `[placeholder]` values. Remove this instruction block before submitting. Save the completed analysis as `.github/requirements/REQ-YYYY-NNN.md` (e.g., `REQ-2026-001.md`). Create the `.github/requirements/` directory if it does not yet exist.

---

## Original Requirement

[Raw business requirement as received — paste the issue, user story, or business request verbatim]

---

## Standardized Specification

**Summary:** [One-line summary of what this requirement delivers]  
**Business Value:** [Why this is needed — the problem it solves or the opportunity it captures]  
**Acceptance Criteria:**
- [ ] [Specific, measurable criterion 1]
- [ ] [Specific, measurable criterion 2]
- [ ] [Specific, measurable criterion N]

---

## Classification Results

**Primary Domain:** [INTEGRATION | SECURITY | BUSINESS_LOGIC | USER_INTERFACE | PERFORMANCE | INTELLIGENCE | COMPLIANCE | INFRASTRUCTURE | DOCUMENTATION | AUDIT]  
**Secondary Domains:** [List of additional triggered domains, or "None"]  
**Complexity Level:** [LOW | MEDIUM | HIGH | CRITICAL]  
**Estimated Effort:** [N person-weeks]  
**Classification Rationale:** [Why this complexity and domain were chosen — list the triggering keywords]

---

## Agent Assignment

**Primary Agent:** @[AgentName]  
**Collaborating Agents:** [@Agent1, @Agent2, ...]  
**Coordination Pattern:** [Sequential | Parallel | Iterative]  
**Pattern Rationale:** [Why this coordination pattern was chosen]

---

## Implementation Plan

| Phase | Agent | Deliverable | Pattern | Dependency |
|-------|-------|-------------|---------|------------|
| 1 | @[Agent] | [Deliverable description] | [Sequential/Parallel] | None |
| 2 | @[Agent] | [Deliverable description] | [Sequential/Parallel] | Phase 1 |
| N | @[Agent] | [Deliverable description] | [Sequential/Parallel] | Phase N-1 |

### Detailed Phase Breakdown

#### Phase 1: @[Agent] — [Deliverable]
- [ ] [Specific task 1]
- [ ] [Specific task 2]

#### Phase 2: @[Agent] — [Deliverable]
- [ ] [Specific task 1]
- [ ] [Specific task 2]

---

## Dependencies & Risks

**External Dependencies:**
- [System or service name] — [How it is used]
- [None if not applicable]

**Technical Risks:**

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|-----------|
| [Risk description] | [Low/Med/High] | [Low/Med/High] | [Mitigation plan] |

**Business Risks:**

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|-----------|
| [Risk description] | [Low/Med/High] | [Low/Med/High] | [Mitigation plan] |

---

## Quality Gates

### Gate 1 — Classification Approved
- [ ] Domain(s) correctly identified
- [ ] Complexity level justified
- [ ] Agent assignment appropriate
- [ ] Orchestration pattern chosen

### Gate 2 — Implementation Ready
- [ ] Acceptance criteria are specific and measurable
- [ ] Technical dependencies identified and available
- [ ] Database migration required? (if yes, flag @Architect + @SecurityWarden)
- [ ] External system involved? (if yes, flag @IntegrationBot)

### Gate 3 — Implementation Complete
- [ ] All phases delivered required artifacts
- [ ] Code reviewed and merged

### Gate 4 — Testing Complete
- [ ] Unit tests passing
- [ ] Integration tests passing (MEDIUM+)
- [ ] UAT passed (HIGH+)
- [ ] Security audit passed (CRITICAL or where @SecurityWarden involved)
- [ ] Performance testing passed (CRITICAL or where @PerfEngineer involved)
- [ ] Test count has not decreased (405+ backend, 105+ frontend)

### Gate 5 — Documentation Complete
- [ ] API docs updated (if endpoints changed)
- [ ] SQL schema docs updated (if migrations added)
- [ ] Architecture diagrams updated (if topology changed)
- [ ] Memory bank `activecontext.md` updated

### Gate 6 — Deployment Approved
- [ ] @AuditAgent sign-off (HIGH and CRITICAL)
- [ ] Agent ownership validation script passes
- [ ] CI pipeline green
- [ ] Human reviewer approval (CRITICAL)

---

## Progress Tracking

- [ ] Classification Complete
- [ ] Agent Assignment Complete
- [ ] Implementation Started
- [ ] Implementation Complete
- [ ] Testing Complete
- [ ] Documentation Complete
- [ ] Deployed
- [ ] Validated
- [ ] Closed

---

## Lifecycle Log

| Date | Phase | Agent | Action | Notes |
|------|-------|-------|--------|-------|
| [YYYY-MM-DD] | Classification | @RequirementsAnalyzer | Requirement received and classified | — |
| [YYYY-MM-DD] | Assignment | @RequirementsAnalyzer | Agents assigned | — |

---

**Created:** [YYYY-MM-DD]  
**Last Updated:** [YYYY-MM-DD]  
**Status:** [OPEN | IN_PROGRESS | BLOCKED | DEPLOYED | CLOSED]
