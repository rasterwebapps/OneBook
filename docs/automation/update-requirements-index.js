#!/usr/bin/env node
/**
 * update-requirements-index.js
 * Auto-update docs/requirements/requirements-index.md from all REQ-*.md files.
 * Reads metadata from each REQ file and rebuilds the master index table.
 */

'use strict';

const fs = require('fs');
const path = require('path');
const { glob } = require('glob');

const ROOT = path.resolve(__dirname, '../..');
const REQ_DIR = path.join(ROOT, 'docs/requirements/active');
const OUTPUT_FILE = path.join(ROOT, 'docs/requirements/requirements-index.md');

/**
 * Parse metadata from a REQ file.
 */
function parseMetadata(content, filename) {
  const meta = {
    filename,
    reqId: '',
    title: '',
    status: 'UNKNOWN',
    priority: 'MEDIUM',
    owner: '',
    milestone: '',
    linkedBrd: '',
    linkedFrd: '',
  };

  const titleMatch = content.match(/^#\s+REQ-(\d+):\s+(.+)$/m);
  if (titleMatch) {
    meta.reqId = `REQ-${titleMatch[1]}`;
    meta.title = titleMatch[2].trim();
  }

  for (const line of content.split('\n')) {
    const s = line.match(/^\*\*Status:\*\*\s+(\S+)/); if (s) meta.status = s[1];
    const p = line.match(/^\*\*Priority:\*\*\s+(CRITICAL|HIGH|MEDIUM|LOW)/); if (p) meta.priority = p[1];
    const o = line.match(/^\*\*Owner:\*\*\s+(.+)$/); if (o) meta.owner = o[1].trim();
    const m = line.match(/^\*\*Milestone:\*\*\s+(.+)$/); if (m) meta.milestone = m[1].trim();
    const brd = line.match(/^\*\*Linked BRD:\*\*\s+\[(.+?)\]/); if (brd) meta.linkedBrd = brd[1];
    const frd = line.match(/^\*\*Linked FRD:\*\*\s+\[(.+?)\]/); if (frd) meta.linkedFrd = frd[1];
  }

  return meta;
}

/**
 * Status icon helper.
 */
function statusIcon(status) {
  if (status.includes('COMPLETE')) return '✅';
  if (status.includes('PROGRESS')) return '🔄';
  if (status.includes('DRAFT')) return '📝';
  if (status.includes('REJECT')) return '❌';
  return '❓';
}

/**
 * Main index update.
 */
async function updateIndex() {
  console.log('🔍 Scanning requirement files to update index...');

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
    const meta = parseMetadata(content, file);
    requirements.push(meta);
    console.log(`  ✓ ${meta.reqId}: ${meta.title} [${meta.priority}] [${meta.status}]`);
  }

  const now = new Date().toISOString().split('T')[0];

  // Count statuses
  const statusCounts = {};
  const priorityCounts = {};
  const ownerMap = {};

  for (const req of requirements) {
    const s = req.status;
    statusCounts[s] = (statusCounts[s] || 0) + 1;
    priorityCounts[req.priority] = (priorityCounts[req.priority] || 0) + 1;
    if (!ownerMap[req.owner]) ownerMap[req.owner] = [];
    ownerMap[req.owner].push(req.reqId);
  }

  let index = `# Requirements Index
## OneBook — Nexus Universal Accounting OS

> **Master index of all active requirements.**
> Auto-updated by \`docs/automation/update-requirements-index.js\`.
> Last Updated: ${now}

---

## All Requirements

| Req ID | Title | Status | Priority | Owner | Milestone | BRD Link | FRD Link | File |
|--------|-------|--------|----------|-------|-----------|----------|----------|------|
`;

  for (const req of requirements) {
    const icon = statusIcon(req.status);
    const brdLink = req.linkedBrd ? `[${req.linkedBrd}](../business/BRD.md)` : '—';
    const frdLink = req.linkedFrd ? `[${req.linkedFrd}](../business/FRD.md)` : '—';
    index += `| [${req.reqId}](active/${req.filename}) | ${req.title} | ${icon} ${req.status} | ${req.priority} | ${req.owner} | ${req.milestone} | ${brdLink} | ${frdLink} | [${req.filename}](active/${req.filename}) |\n`;
  }

  index += `\n---\n\n## Status Summary\n\n`;
  index += `| Status | Count | Percentage |\n|--------|-------|------------|\n`;

  const total = requirements.length;
  for (const [status, count] of Object.entries(statusCounts)) {
    index += `| ${statusIcon(status)} ${status} | ${count} | ${Math.round((count / total) * 100)}% |\n`;
  }
  index += `| **Total** | **${total}** | **100%** |\n`;

  index += `\n## Priority Breakdown\n\n`;
  index += `| Priority | Count | Requirements |\n|----------|-------|-------------|\n`;
  for (const priority of ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW']) {
    const count = priorityCounts[priority] || 0;
    if (count === 0) continue;
    const reqs = requirements.filter(r => r.priority === priority).map(r => r.reqId).join(', ');
    index += `| ${priority} | ${count} | ${reqs} |\n`;
  }

  index += `\n## Owner Assignments\n\n`;
  index += `| Owner Agent | Requirements |\n|-------------|-------------|\n`;
  for (const [owner, reqs] of Object.entries(ownerMap)) {
    index += `| ${owner} | ${reqs.join(', ')} |\n`;
  }

  // Milestone mapping
  const milestoneMap = {};
  for (const req of requirements) {
    const ms = req.milestone;
    if (!milestoneMap[ms]) milestoneMap[ms] = [];
    milestoneMap[ms].push(req.reqId);
  }

  index += `\n## Milestone Mapping\n\n`;
  index += `| Milestone | Requirements |\n|-----------|-------------|\n`;
  for (const [ms, reqs] of Object.entries(milestoneMap).sort()) {
    index += `| ${ms} | ${reqs.join(', ')} |\n`;
  }

  index += `\n---\n\n*Auto-generated by \`docs/automation/update-requirements-index.js\` on ${now}. Do not edit manually.*\n`;

  fs.writeFileSync(OUTPUT_FILE, index, 'utf8');
  console.log(`\n✅ Requirements index updated: ${OUTPUT_FILE}`);
  console.log(`   Total requirements: ${total}`);
  console.log(`   Output size: ${(index.length / 1024).toFixed(1)} KB`);
}

updateIndex().catch((err) => {
  console.error('❌ Failed to update requirements index:', err.message);
  process.exit(1);
});
