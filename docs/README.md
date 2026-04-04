# OneBook Documentation

> Organized documentation for the Nexus Universal Accounting OS.

## Directory Structure

```
docs/
├── README.md                          ← You are here
├── architecture.md                    ← High-level Mermaid system diagram
├── milestones.md                      ← Project milestone specifications (M1–M12)
├── sub-agents.md                      ← Sub-agent architecture & delegation rules
│
├── business/                          ← Business documentation
│   ├── BRD.md                         ← Business Requirements Document (auto-generated)
│   ├── FRD.md                         ← Functional Requirements Document (auto-generated)
│   ├── TRD.md                         ← Technical Requirements Document (auto-generated)
│   ├── glossary.md                    ← Domain glossary
│   ├── user-stories.md                ← User stories by milestone
│   └── tally-features.md              ← Tally feature parity reference
│
├── technical/                         ← Technical documentation
│   ├── api-documentation.md           ← REST API reference for all endpoints
│   ├── architecture-diagram.md        ← Detailed Mermaid.js diagrams (data flow, security, cache, deployment)
│   ├── developer-guide.md             ← Developer onboarding, setup, coding standards
│   ├── key-binding-registry.md        ← Keyboard navigation technical design
│   ├── operational-runbook.md         ← Deployment, monitoring, troubleshooting, backup
│   ├── sql-schema.md                  ← Universal Secured Ledger schema documentation
│   ├── api-contracts.md               ← Complete API specifications by module
│   ├── data-dictionary.md             ← All entities, fields, validations
│   └── workflow-diagrams.md           ← Mermaid process flow diagrams
│
├── user/                              ← User-facing documentation
│   ├── user-manual.md                 ← Complete user guide
│   ├── feature-catalog.md             ← All features with status and endpoints
│   └── keyboard-shortcuts.md          ← Complete shortcut reference
│
├── requirements/                      ← Requirements tracking
│   ├── RTM.md                         ← Requirements Traceability Matrix
│   ├── requirements-index.md          ← Master requirements index
│   ├── requirement-template.md        ← Template for new requirements
│   └── active/                        ← Active requirement specs (REQ-001 through REQ-014)
│
└── automation/                        ← Documentation generation scripts
    ├── package.json                   ← npm scripts for doc generation
    ├── generate-brd.js                ← BRD generator
    ├── generate-frd.js                ← FRD generator
    ├── generate-trd.js                ← TRD generator
    ├── generate-data-dictionary.js    ← Data dictionary generator
    ├── generate-rtm.js                ← RTM generator
    ├── update-requirements-index.js   ← Requirements index updater
    └── validate-requirements.js       ← Requirements validator
```

## Quick Links

| Category | Key Documents |
|----------|--------------|
| **Architecture** | [High-Level Diagram](architecture.md) · [Detailed Diagrams](technical/architecture-diagram.md) |
| **API** | [API Reference](technical/api-documentation.md) · [API Contracts](technical/api-contracts.md) |
| **Database** | [SQL Schema](technical/sql-schema.md) · [Data Dictionary](technical/data-dictionary.md) |
| **Getting Started** | [Developer Guide](technical/developer-guide.md) · [Operational Runbook](technical/operational-runbook.md) |
| **Keyboard** | [Key-Binding Registry](technical/key-binding-registry.md) · [Keyboard Shortcuts](user/keyboard-shortcuts.md) |
| **Business** | [BRD](business/BRD.md) · [FRD](business/FRD.md) · [TRD](business/TRD.md) · [Tally Features](business/tally-features.md) |
| **User** | [User Manual](user/user-manual.md) · [Feature Catalog](user/feature-catalog.md) |
| **Requirements** | [RTM](requirements/RTM.md) · [Requirements Index](requirements/requirements-index.md) |
| **Milestones** | [Project Milestones](milestones.md) |
