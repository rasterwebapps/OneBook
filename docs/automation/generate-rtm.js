#!/usr/bin/env node
/**
 * generate-rtm.js
 * Auto-generate docs/requirements/RTM.md (Requirement Traceability Matrix).
 * Reads all REQ-*.md files, extracts traceability info, generates the matrix.
 */

'use strict';

const fs = require('fs');
const path = require('path');
const { glob } = require('glob');

const ROOT = path.resolve(__dirname, '../..');
const REQ_DIR = path.join(ROOT, 'docs/requirements/active');
const OUTPUT_FILE = path.join(ROOT, 'docs/requirements/RTM.md');

/**
 * Parse metadata and traceability from a REQ file.
 */
function parseRequirement(content, filename) {
  const req = {
    filename,
    reqId: '',
    title: '',
    status: 'UNKNOWN',
    priority: 'MEDIUM',
    owner: '',
    milestone: '',
    linkedBrd: '',
    linkedFrd: '',
    linkedTrd: '',
    javaFiles: [],
    sqlFiles: [],
    testFiles: [],
    userStories: [],
  };

  // Title
  const titleMatch = content.match(/^#\s+REQ-(\d+):\s+(.+)$/m);
  if (titleMatch) {
    req.reqId = `REQ-${titleMatch[1]}`;
    req.title = titleMatch[2].trim();
  }

  // Metadata
  for (const line of content.split('\n')) {
    const s = line.match(/^\*\*Status:\*\*\s+(\S+)/); if (s) req.status = s[1];
    const p = line.match(/^\*\*Priority:\*\*\s+(CRITICAL|HIGH|MEDIUM|LOW)/); if (p) req.priority = p[1];
    const o = line.match(/^\*\*Owner:\*\*\s+(.+)$/); if (o) req.owner = o[1].trim();
    const m = line.match(/^\*\*Milestone:\*\*\s+(.+)$/); if (m) req.milestone = m[1].trim();
    const brd = line.match(/^\*\*Linked BRD:\*\*\s+\[(.+?)\]/); if (brd) req.linkedBrd = brd[1];
    const frd = line.match(/^\*\*Linked FRD:\*\*\s+\[(.+?)\]/); if (frd) req.linkedFrd = frd[1];
    const trd = line.match(/^\*\*Linked TRD:\*\*\s+\[(.+?)\]/); if (trd) req.linkedTrd = trd[1];
  }

  // Extract Java files
  const javaMatches = content.match(/`([\w]+\.java)`/g) || [];
  req.javaFiles = [...new Set(javaMatches.map(m => m.replace(/`/g, '')))];

  // Extract SQL migration files
  const sqlMatches = content.match(/`(V\d+__[\w]+\.sql)`/g) || [];
  req.sqlFiles = [...new Set(sqlMatches.map(m => m.replace(/`/g, '')))];

  // Extract test file references
  const testMatches = content.match(/`([\w]+Test\.java)`/g) || [];
  req.testFiles = [...new Set(testMatches.map(m => m.replace(/`/g, '')))];

  // Extract user story references
  const storyMatches = content.match(/US-\d+/g) || [];
  req.userStories = [...new Set(storyMatches)];

  return req;
}

/**
 * Generate status emoji.
 */
function statusIcon(status) {
  if (status.includes('COMPLETE')) return '✅';
  if (status.includes('PROGRESS')) return '🔄';
  if (status.includes('DRAFT')) return '📋';
  if (status.includes('REJECT')) return '❌';
  return '❓';
}

/**
 * Main RTM generation.
 */
async function generateRtm() {
  console.log('🔍 Scanning requirement files for RTM generation...');

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
    const req = parseRequirement(content, file);
    requirements.push(req);
    console.log(`  ✓ ${req.reqId}: ${req.title} [${req.status}]`);
  }

  const now = new Date().toISOString().split('T')[0];

  let rtm = `# Requirement Traceability Matrix (RTM)
## OneBook — Nexus Universal Accounting OS

> **Auto-generated from REQ-*.md files.**
> Generated: ${now} by \`docs/automation/generate-rtm.js\`

---

## Traceability Matrix

| Req ID | Title | BRD | FRD | TRD | Key Code Files | Tests | User Stories | Status |
|--------|-------|-----|-----|-----|----------------|-------|-------------|--------|
`;

  for (const req of requirements) {
    const codeFiles = [...req.javaFiles.slice(0, 3), ...req.sqlFiles.slice(0, 1)].join('<br>');
    const tests = req.testFiles.slice(0, 2).join('<br>');
    const stories = req.userStories.slice(0, 3).join(', ');
    const icon = statusIcon(req.status);

    rtm += `| [${req.reqId}](active/${req.filename}) | ${req.title} | ${req.linkedBrd || '—'} | ${req.linkedFrd || '—'} | ${req.linkedTrd || '—'} | ${codeFiles || '—'} | ${tests || '—'} | ${stories || '—'} | ${icon} ${req.status} |\n`;
  }

  rtm += `\n---\n\n`;

  // Detailed traceability per requirement
  rtm += `## Detailed Traceability\n\n`;

  for (const req of requirements) {
    rtm += `### ${req.reqId}: ${req.title}\n\n`;
    rtm += `| Artifact | Details |\n|---------|--------|\n`;
    rtm += `| Status | ${statusIcon(req.status)} ${req.status} |\n`;
    rtm += `| Priority | ${req.priority} |\n`;
    rtm += `| Owner | ${req.owner} |\n`;
    rtm += `| Milestone | ${req.milestone} |\n`;
    if (req.linkedBrd) rtm += `| BRD | ${req.linkedBrd} |\n`;
    if (req.linkedFrd) rtm += `| FRD | ${req.linkedFrd} |\n`;
    if (req.linkedTrd) rtm += `| TRD | ${req.linkedTrd} |\n`;
    if (req.javaFiles.length > 0) rtm += `| Java Files | ${req.javaFiles.join(', ')} |\n`;
    if (req.sqlFiles.length > 0) rtm += `| Migrations | ${req.sqlFiles.join(', ')} |\n`;
    if (req.testFiles.length > 0) rtm += `| Tests | ${req.testFiles.join(', ')} |\n`;
    if (req.userStories.length > 0) rtm += `| User Stories | ${req.userStories.join(', ')} |\n`;
    rtm += `\n`;
  }

  // Coverage summary
  const completed = requirements.filter(r => r.status.includes('COMPLETE')).length;
  const total = requirements.length;

  rtm += `---\n\n## Coverage Summary\n\n`;
  rtm += `| Category | Total | Completed | Coverage |\n`;
  rtm += `|----------|-------|-----------|----------|\n`;
  rtm += `| Requirements | ${total} | ${completed} | ${Math.round((completed / total) * 100)}% |\n`;

  const withTests = requirements.filter(r => r.testFiles.length > 0).length;
  rtm += `| Requirements with Tests | ${total} | ${withTests} | ${Math.round((withTests / total) * 100)}% |\n`;

  const withCode = requirements.filter(r => r.javaFiles.length > 0).length;
  rtm += `| Requirements with Code | ${total} | ${withCode} | ${Math.round((withCode / total) * 100)}% |\n`;

  const withMigration = requirements.filter(r => r.sqlFiles.length > 0).length;
  rtm += `| Requirements with DB Migration | ${total} | ${withMigration} | ${Math.round((withMigration / total) * 100)}% |\n`;

  rtm += `\n---\n\n*Auto-generated by \`docs/automation/generate-rtm.js\` on ${now}. Do not edit manually.*\n`;

  fs.writeFileSync(OUTPUT_FILE, rtm, 'utf8');
  console.log(`\n✅ RTM generated: ${OUTPUT_FILE}`);
  console.log(`   Total requirements: ${total}`);
  console.log(`   Completed: ${completed}/${total} (${Math.round((completed / total) * 100)}%)`);
  console.log(`   Output size: ${(rtm.length / 1024).toFixed(1)} KB`);
}

generateRtm().catch((err) => {
  console.error('❌ Failed to generate RTM:', err.message);
  process.exit(1);
});
