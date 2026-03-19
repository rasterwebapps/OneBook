#!/usr/bin/env node
/**
 * generate-data-dictionary.js
 * Generate docs/technical/data-dictionary.md from REQ-*.md files.
 * Extracts data model sections (SQL schemas) and compiles them into a dictionary.
 */

'use strict';

const fs = require('fs');
const path = require('path');
const { glob } = require('glob');

const ROOT = path.resolve(__dirname, '../..');
const REQ_DIR = path.join(ROOT, 'docs/requirements/active');
const OUTPUT_FILE = path.join(ROOT, 'docs/technical/data-dictionary.md');

/**
 * Parse metadata from a REQ file.
 */
function parseMetadata(content) {
  const meta = { reqId: '', title: '', milestone: '' };
  const titleMatch = content.match(/^#\s+REQ-(\d+):\s+(.+)$/m);
  if (titleMatch) {
    meta.reqId = `REQ-${titleMatch[1]}`;
    meta.title = titleMatch[2].trim();
  }
  const ms = content.match(/^\*\*Milestone:\*\*\s+(.+)$/m);
  if (ms) meta.milestone = ms[1].trim();
  return meta;
}

/**
 * Extract SQL code blocks from content.
 */
function extractSqlBlocks(content) {
  const sqlBlocks = [];
  const regex = /```sql\n([\s\S]*?)```/g;
  let match;
  while ((match = regex.exec(content)) !== null) {
    sqlBlocks.push(match[1].trim());
  }
  return sqlBlocks;
}

/**
 * Extract table names from a SQL block.
 */
function extractTableNames(sql) {
  const tables = [];
  const createMatch = sql.match(/CREATE TABLE (\w+)/gi) || [];
  const alterMatch = sql.match(/ALTER TABLE (\w+)/gi) || [];
  for (const m of [...createMatch, ...alterMatch]) {
    const name = m.replace(/^(CREATE TABLE|ALTER TABLE)\s+/i, '').trim();
    if (name && !tables.includes(name)) tables.push(name);
  }
  return tables;
}

/**
 * Parse CREATE TABLE columns.
 */
function parseColumns(createTableSql) {
  const columns = [];
  const tableMatch = createTableSql.match(/CREATE TABLE (\w+)\s*\(([^;]+)\)/si);
  if (!tableMatch) return columns;

  const columnDefs = tableMatch[2].split(',\n');
  for (const def of columnDefs) {
    const trimmed = def.trim();
    if (!trimmed || trimmed.startsWith('--') || trimmed.startsWith('CONSTRAINT') ||
        trimmed.startsWith('PRIMARY KEY') || trimmed.startsWith('UNIQUE') ||
        trimmed.startsWith('CHECK') || trimmed.startsWith('FOREIGN KEY')) continue;

    const colMatch = trimmed.match(/^(\w+)\s+(\w+(?:\([^)]+\))?)\s*(.*)/);
    if (colMatch) {
      const name = colMatch[1];
      const type = colMatch[2];
      const rest = colMatch[3] || '';
      const nullable = !rest.includes('NOT NULL');
      const defaultVal = (rest.match(/DEFAULT\s+(\S+)/i) || [])[1] || '';
      const encrypted = name.includes('_encrypted');
      columns.push({ name, type, nullable, defaultVal, encrypted });
    }
  }
  return columns;
}

/**
 * Main data dictionary generation.
 */
async function generateDataDictionary() {
  console.log('🔍 Scanning requirement files for data dictionary...');

  const reqFiles = await glob('REQ-*.md', { cwd: REQ_DIR });
  reqFiles.sort();

  if (reqFiles.length === 0) {
    console.warn('⚠️  No REQ-*.md files found.');
    process.exit(1);
  }

  console.log(`📄 Found ${reqFiles.length} requirement files`);

  const allTables = [];

  for (const file of reqFiles) {
    const content = fs.readFileSync(path.join(REQ_DIR, file), 'utf8');
    const meta = parseMetadata(content);
    const sqlBlocks = extractSqlBlocks(content);

    for (const sql of sqlBlocks) {
      const tableNames = extractTableNames(sql);
      for (const tableName of tableNames) {
        if (tableName.toLowerCase().includes('policy') || tableName.toLowerCase().includes('function')) continue;
        const columns = parseColumns(sql);
        if (columns.length > 0) {
          allTables.push({
            tableName,
            reqId: meta.reqId,
            reqTitle: meta.title,
            milestone: meta.milestone,
            columns,
            fullSql: sql,
          });
          console.log(`  ✓ Table: ${tableName} (${columns.length} columns) from ${meta.reqId}`);
        }
      }
    }
  }

  const now = new Date().toISOString().split('T')[0];

  let dict = `# Data Dictionary (Generated)
## OneBook — Nexus Universal Accounting OS

> **Auto-generated from REQ-*.md files SQL sections.**
> Generated: ${now} by \`docs/automation/generate-data-dictionary.js\`
> For the curated, human-authored version see \`docs/technical/data-dictionary.md\`

---

## Discovered Tables (${allTables.length})

`;

  // TOC
  for (const table of allTables) {
    dict += `- [\`${table.tableName}\`](#${table.tableName.toLowerCase().replace(/_/g, '-')})\n`;
  }
  dict += '\n---\n\n';

  // Table definitions
  for (const table of allTables) {
    dict += `## \`${table.tableName}\`

**Source:** ${table.reqId} — ${table.reqTitle} | **Milestone:** ${table.milestone}

`;
    if (table.columns.length > 0) {
      dict += `| Column | Type | Nullable | Default | Encrypted |\n`;
      dict += `|--------|------|----------|---------|----------|\n`;
      for (const col of table.columns) {
        dict += `| \`${col.name}\` | ${col.type} | ${col.nullable ? 'Yes' : 'No'} | ${col.defaultVal || '—'} | ${col.encrypted ? '✅ AES-256-GCM' : 'No'} |\n`;
      }
      dict += '\n';
    }

    dict += `**SQL Definition:**\n\`\`\`sql\n${table.fullSql}\n\`\`\`\n\n---\n\n`;
  }

  dict += `*Auto-generated by \`docs/automation/generate-data-dictionary.js\` on ${now}.*\n`;

  fs.writeFileSync(OUTPUT_FILE, dict, 'utf8');
  console.log(`\n✅ Data dictionary generated: ${OUTPUT_FILE}`);
  console.log(`   Tables discovered: ${allTables.length}`);
  console.log(`   Output size: ${(dict.length / 1024).toFixed(1)} KB`);
}

generateDataDictionary().catch((err) => {
  console.error('❌ Failed to generate data dictionary:', err.message);
  process.exit(1);
});
