#!/usr/bin/env node
/**
 * generate-brd.js
 * Auto-generate docs/business/BRD.md from REQ-*.md files.
 * Reads all active requirement files, extracts Business Context sections,
 * groups by priority, and produces a comprehensive BRD.
 */

'use strict';

const fs = require('fs');
const path = require('path');
const { glob } = require('glob');

const ROOT = path.resolve(__dirname, '../..');
const REQ_DIR = path.join(ROOT, 'docs/requirements/active');
const OUTPUT_FILE = path.join(ROOT, 'docs/business/BRD.md');
const PROJECT_BRIEF = path.join(ROOT, 'memory-bank/projectbrief.md');

/**
 * Parse metadata from the top section of a REQ-*.md file.
 * @param {string} content - Raw markdown content
 * @returns {object} metadata object
 */
function parseMetadata(content) {
  const meta = {
    reqId: '',
    title: '',
    status: 'UNKNOWN',
    priority: 'MEDIUM',
    owner: '',
    milestone: '',
    linkedBrd: '',
  };

  const lines = content.split('\n');

  // Extract REQ ID and title from first heading
  const titleMatch = content.match(/^#\s+REQ-(\d+):\s+(.+)$/m);
  if (titleMatch) {
    meta.reqId = `REQ-${titleMatch[1]}`;
    meta.title = titleMatch[2].trim();
  }

  // Extract metadata fields
  for (const line of lines) {
    const statusMatch = line.match(/^\*\*Status:\*\*\s+(.+)$/);
    if (statusMatch) meta.status = statusMatch[1].replace(/[^A-Z_]/g, '').trim();

    const priorityMatch = line.match(/^\*\*Priority:\*\*\s+(CRITICAL|HIGH|MEDIUM|LOW)/);
    if (priorityMatch) meta.priority = priorityMatch[1];

    const ownerMatch = line.match(/^\*\*Owner:\*\*\s+(.+)$/);
    if (ownerMatch) meta.owner = ownerMatch[1].trim();

    const milestoneMatch = line.match(/^\*\*Milestone:\*\*\s+(.+)$/);
    if (milestoneMatch) meta.milestone = milestoneMatch[1].trim();

    const linkedBrdMatch = line.match(/^\*\*Linked BRD:\*\*\s+\[(.+?)\]/);
    if (linkedBrdMatch) meta.linkedBrd = linkedBrdMatch[1];
  }

  return meta;
}

/**
 * Extract a named section from markdown content.
 * @param {string} content - Markdown text
 * @param {string} sectionName - Section header to find (e.g., "Business Context")
 * @returns {string} Section content (excluding the header itself)
 */
function extractSection(content, sectionName) {
  const regex = new RegExp(
    `^##\\s+\\d+\\.\\s+${sectionName}\\s*\n([\\s\\S]*?)(?=^##\\s+|$)`,
    'mi'
  );
  const match = content.match(regex);
  if (!match) {
    // Try without number prefix
    const regex2 = new RegExp(
      `^##\\s+${sectionName}\\s*\n([\\s\\S]*?)(?=^##\\s+|$)`,
      'mi'
    );
    const match2 = content.match(regex2);
    return match2 ? match2[1].trim() : '';
  }
  return match[1].trim();
}

/**
 * Extract business rules from Business Context section.
 */
function extractBusinessRules(content, reqId) {
  const businessSection = extractSection(content, 'Business Context');
  const rules = [];
  const ruleRegex = new RegExp(`- ${reqId}\\.\\d+:.*`, 'g');
  const matches = businessSection.match(ruleRegex) || [];
  return matches;
}

/**
 * Read the project brief for executive summary content.
 */
function getExecutiveSummary() {
  if (fs.existsSync(PROJECT_BRIEF)) {
    const brief = fs.readFileSync(PROJECT_BRIEF, 'utf8');
    // Extract first few paragraphs
    const lines = brief.split('\n');
    const summary = [];
    let inContent = false;
    for (const line of lines) {
      if (line.startsWith('## ') && !inContent) { inContent = true; continue; }
      if (line.startsWith('## ') && inContent) break;
      if (inContent && line.trim()) summary.push(line);
      if (summary.length >= 6) break;
    }
    return summary.join('\n') ||
      'OneBook (Nexus Universal) is a sector-agnostic, zero-trust, high-performance accounting OS.';
  }
  return 'OneBook (Nexus Universal) is a sector-agnostic, zero-trust, high-performance accounting OS.';
}

/**
 * Main generation function.
 */
async function generateBrd() {
  console.log('🔍 Scanning requirement files...');

  const reqFiles = await glob('REQ-*.md', { cwd: REQ_DIR });
  reqFiles.sort();

  if (reqFiles.length === 0) {
    console.warn('⚠️  No REQ-*.md files found in', REQ_DIR);
    process.exit(1);
  }

  console.log(`📄 Found ${reqFiles.length} requirement files`);

  const requirements = [];
  for (const file of reqFiles) {
    const filePath = path.join(REQ_DIR, file);
    const content = fs.readFileSync(filePath, 'utf8');
    const meta = parseMetadata(content);
    const businessContext = extractSection(content, 'Business Context');
    const businessRules = extractBusinessRules(content, meta.reqId);

    requirements.push({
      file,
      meta,
      businessContext,
      businessRules,
    });
    console.log(`  ✓ Parsed ${meta.reqId}: ${meta.title} [${meta.priority}] [${meta.status}]`);
  }

  // Group by priority
  const PRIORITY_ORDER = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW'];
  const byPriority = {};
  for (const p of PRIORITY_ORDER) byPriority[p] = [];
  for (const req of requirements) {
    const p = req.meta.priority in byPriority ? req.meta.priority : 'MEDIUM';
    byPriority[p].push(req);
  }

  const now = new Date().toISOString().split('T')[0];
  const executiveSummary = getExecutiveSummary();

  // Build BRD markdown
  let brd = `# Business Requirements Document (BRD)
## OneBook — Nexus Universal Accounting OS

> **Auto-generated from REQ-*.md files. Version: Living Document.**
> Generated: ${now} by \`docs/automation/generate-brd.js\`

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Business Requirements](#2-business-requirements)
3. [Status Summary](#3-status-summary)
4. [Requirement File Index](#4-requirement-file-index)

---

## 1. Executive Summary

${executiveSummary}

---

## 2. Business Requirements

`;

  // Add requirements grouped by priority
  for (const priority of PRIORITY_ORDER) {
    const reqs = byPriority[priority];
    if (reqs.length === 0) continue;

    brd += `### Priority: ${priority}\n\n`;

    for (const req of reqs) {
      const { meta, businessContext, businessRules } = req;
      brd += `#### ${meta.reqId}: ${meta.title}
**Priority:** ${meta.priority} | **Owner:** ${meta.owner} | **Milestone:** ${meta.milestone} | **Status:** ${meta.status}

`;
      if (businessContext) {
        // Include first problem statement and first few business rules
        const problemMatch = businessContext.match(/###\s+.*Problem Statement\s*\n([\s\S]*?)(?=###|$)/i);
        if (problemMatch) {
          brd += `**Problem Statement:**\n${problemMatch[1].trim()}\n\n`;
        }
      }

      if (businessRules.length > 0) {
        brd += `**Business Rules:**\n`;
        for (const rule of businessRules) {
          brd += `${rule}\n`;
        }
        brd += '\n';
      }

      brd += `**File:** [\`${req.file}\`](../requirements/active/${req.file})\n\n---\n\n`;
    }
  }

  // Status summary
  const statusCounts = {};
  for (const req of requirements) {
    const s = req.meta.status || 'UNKNOWN';
    statusCounts[s] = (statusCounts[s] || 0) + 1;
  }

  brd += `## 3. Status Summary

| Status | Count |
|--------|-------|
`;
  for (const [status, count] of Object.entries(statusCounts)) {
    brd += `| ${status} | ${count} |\n`;
  }
  brd += `| **Total** | **${requirements.length}** |\n`;

  // Requirement file index
  brd += `\n---\n\n## 4. Requirement File Index\n\n`;
  brd += `| Req ID | Title | Status | Priority | Owner | Milestone |\n`;
  brd += `|--------|-------|--------|----------|-------|----------|\n`;
  for (const req of requirements) {
    const { meta } = req;
    brd += `| [${meta.reqId}](../requirements/active/${req.file}) | ${meta.title} | ${meta.status} | ${meta.priority} | ${meta.owner} | ${meta.milestone} |\n`;
  }

  brd += `\n---\n\n*Auto-generated by \`docs/automation/generate-brd.js\` on ${now}. Do not edit manually.*\n`;

  // Write output
  fs.writeFileSync(OUTPUT_FILE, brd, 'utf8');
  console.log(`\n✅ BRD generated: ${OUTPUT_FILE}`);
  console.log(`   Total requirements: ${requirements.length}`);
  console.log(`   Output size: ${(brd.length / 1024).toFixed(1)} KB`);
}

generateBrd().catch((err) => {
  console.error('❌ Failed to generate BRD:', err.message);
  process.exit(1);
});
