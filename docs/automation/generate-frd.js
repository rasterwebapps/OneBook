#!/usr/bin/env node
/**
 * generate-frd.js
 * Auto-generate docs/business/FRD.md from REQ-*.md files.
 * Extracts Functional Specification sections from each requirement file.
 */

'use strict';

const fs = require('fs');
const path = require('path');
const { glob } = require('glob');

const ROOT = path.resolve(__dirname, '../..');
const REQ_DIR = path.join(ROOT, 'docs/requirements/active');
const OUTPUT_FILE = path.join(ROOT, 'docs/business/FRD.md');

/**
 * Parse metadata from a REQ file.
 */
function parseMetadata(content) {
  const meta = { reqId: '', title: '', status: 'UNKNOWN', priority: 'MEDIUM', owner: '', milestone: '' };
  const titleMatch = content.match(/^#\s+REQ-(\d+):\s+(.+)$/m);
  if (titleMatch) {
    meta.reqId = `REQ-${titleMatch[1]}`;
    meta.title = titleMatch[2].trim();
  }
  const lines = content.split('\n');
  for (const line of lines) {
    const m = line.match(/^\*\*Status:\*\*\s+(\S+)/);  if (m) meta.status = m[1];
    const p = line.match(/^\*\*Priority:\*\*\s+(CRITICAL|HIGH|MEDIUM|LOW)/); if (p) meta.priority = p[1];
    const o = line.match(/^\*\*Owner:\*\*\s+(.+)$/); if (o) meta.owner = o[1].trim();
    const ms = line.match(/^\*\*Milestone:\*\*\s+(.+)$/); if (ms) meta.milestone = ms[1].trim();
  }
  return meta;
}

/**
 * Extract a numbered section from markdown.
 */
function extractSection(content, ...sectionNames) {
  for (const name of sectionNames) {
    const regex = new RegExp(
      `^##\\s+\\d+\\.\\s+${name}\\s*$([\\s\\S]*?)(?=^##\\s+\\d+\\.|^---$|$)`,
      'mi'
    );
    const match = content.match(regex);
    if (match) return match[1].trim();

    // Try without number
    const regex2 = new RegExp(`^##\\s+${name}\\s*\n([\\s\\S]*?)(?=^##\\s+|^---$|$)`, 'mi');
    const match2 = content.match(regex2);
    if (match2) return match2[1].trim();
  }
  return '';
}

/**
 * Extract subsections from a section content block.
 * Returns an object mapping subsection name → content.
 */
function extractSubsections(sectionContent) {
  const subsections = {};
  const parts = sectionContent.split(/^###\s+/m);
  for (const part of parts.slice(1)) {
    const lines = part.split('\n');
    const name = lines[0].trim().replace(/^\d+\.\d+\s+/, '');
    const body = lines.slice(1).join('\n').trim();
    subsections[name] = body;
  }
  return subsections;
}

/**
 * Main FRD generation.
 */
async function generateFrd() {
  console.log('🔍 Scanning requirement files for FRD generation...');

  const reqFiles = await glob('REQ-*.md', { cwd: REQ_DIR });
  reqFiles.sort();

  if (reqFiles.length === 0) {
    console.warn('⚠️  No REQ-*.md files found.');
    process.exit(1);
  }

  console.log(`📄 Found ${reqFiles.length} requirement files`);

  const requirements = [];
  for (const file of reqFiles) {
    const content = fs.readFileSync(path.join(REQ_DIR, file), 'utf8');
    const meta = parseMetadata(content);
    const functionalSpec = extractSection(content, 'Functional Specification', 'Functional Spec');
    const acceptanceCriteria = extractSection(content, 'Acceptance Criteria');

    requirements.push({ file, meta, functionalSpec, acceptanceCriteria });
    console.log(`  ✓ ${meta.reqId}: ${meta.title}`);
  }

  const now = new Date().toISOString().split('T')[0];

  let frd = `# Functional Requirements Document (FRD)
## OneBook — Nexus Universal Accounting OS

> **Auto-generated from REQ-*.md files. Version: Living Document.**
> Generated: ${now} by \`docs/automation/generate-frd.js\`

---

## Table of Contents

`;

  // TOC
  for (let i = 0; i < requirements.length; i++) {
    const { meta } = requirements[i];
    const anchor = `${meta.reqId.toLowerCase()}-${meta.title.toLowerCase().replace(/[^a-z0-9]+/g, '-')}`;
    frd += `${i + 1}. [${meta.reqId}: ${meta.title}](#${anchor})\n`;
  }
  frd += `\n---\n\n`;

  // Functional specifications
  for (const req of requirements) {
    const { meta, functionalSpec, acceptanceCriteria } = req;
    const anchor = `${meta.reqId.toLowerCase()}-${meta.title.toLowerCase().replace(/[^a-z0-9]+/g, '-')}`;

    frd += `## ${meta.reqId}: ${meta.title} {#${anchor}}

**Priority:** ${meta.priority} | **Owner:** ${meta.owner} | **Milestone:** ${meta.milestone} | **Status:** ${meta.status}

`;

    if (functionalSpec) {
      frd += `### Functional Specification\n\n${functionalSpec}\n\n`;
    } else {
      frd += `### Functional Specification\n\n*See [${req.file}](../requirements/active/${req.file}) for full specification.*\n\n`;
    }

    if (acceptanceCriteria) {
      frd += `### Acceptance Criteria\n\n${acceptanceCriteria}\n\n`;
    }

    frd += `**Full Requirement:** [${req.file}](../requirements/active/${req.file})\n\n---\n\n`;
  }

  // Summary table
  frd += `## Implementation Status Summary\n\n`;
  frd += `| Req ID | Title | Priority | Milestone | Status |\n`;
  frd += `|--------|-------|----------|-----------|--------|\n`;
  for (const req of requirements) {
    const { meta } = req;
    frd += `| [${meta.reqId}](../requirements/active/${req.file}) | ${meta.title} | ${meta.priority} | ${meta.milestone} | ${meta.status} |\n`;
  }

  frd += `\n---\n\n*Auto-generated by \`docs/automation/generate-frd.js\` on ${now}. Do not edit manually.*\n`;

  fs.writeFileSync(OUTPUT_FILE, frd, 'utf8');
  console.log(`\n✅ FRD generated: ${OUTPUT_FILE}`);
  console.log(`   Total requirements: ${requirements.length}`);
  console.log(`   Output size: ${(frd.length / 1024).toFixed(1)} KB`);
}

generateFrd().catch((err) => {
  console.error('❌ Failed to generate FRD:', err.message);
  process.exit(1);
});
