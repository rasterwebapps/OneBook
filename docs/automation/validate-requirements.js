#!/usr/bin/env node
/**
 * validate-requirements.js
 * Validate requirement file structure and content quality.
 * Checks for required sections, valid Status/Priority values, and acceptance criteria.
 * Exits with code 1 if any validation fails.
 */

'use strict';

const fs = require('fs');
const path = require('path');
const { glob } = require('glob');

const ROOT = path.resolve(__dirname, '../..');
const REQ_DIR = path.join(ROOT, 'docs/requirements/active');

const VALID_STATUSES = ['COMPLETED', 'IN_PROGRESS', 'DRAFT', 'REJECTED', 'APPROVED'];
const VALID_PRIORITIES = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW'];

const REQUIRED_SECTIONS = [
  'Business Context',
  'Functional Specification',
  'Technical Specification',
  'Acceptance Criteria',
  'Traceability',
];

const REQUIRED_METADATA = [
  { field: 'Status', pattern: /^\*\*Status:\*\*\s+(\S+)/ },
  { field: 'Priority', pattern: /^\*\*Priority:\*\*\s+(CRITICAL|HIGH|MEDIUM|LOW)/ },
  { field: 'Owner', pattern: /^\*\*Owner:\*\*\s+@\w+/ },
  { field: 'Milestone', pattern: /^\*\*Milestone:\*\*\s+M\d+/ },
];

/**
 * Check if a section heading exists in content.
 */
function hasSection(content, sectionName) {
  const regex = new RegExp(`^##\\s+(?:\\d+\\.\\s+)?${sectionName}`, 'mi');
  return regex.test(content);
}

/**
 * Check if acceptance criteria contains at least one Gherkin scenario.
 */
function hasGherkinScenario(content) {
  return /Scenario:/.test(content);
}

/**
 * Check if traceability section has required links.
 */
function hasTraceabilityLinks(content) {
  return /BRD/.test(content) && /FRD/.test(content);
}

/**
 * Validate a single requirement file.
 * @returns {Array<string>} List of validation errors (empty = valid)
 */
function validateRequirement(content, filename) {
  const errors = [];
  const warnings = [];

  // Check title format
  if (!/^#\s+REQ-\d+:\s+.+$/m.test(content)) {
    errors.push('Missing or malformed title (expected: # REQ-NNN: Title)');
  }

  // Check metadata fields
  const lines = content.split('\n');
  for (const { field, pattern } of REQUIRED_METADATA) {
    const found = lines.some(line => pattern.test(line));
    if (!found) {
      errors.push(`Missing or invalid metadata: **${field}:**`);
    }
  }

  // Validate Status value
  const statusMatch = content.match(/^\*\*Status:\*\*\s+(\S+)/m);
  if (statusMatch) {
    const status = statusMatch[1].replace(/[^A-Z_]/g, '');
    if (!VALID_STATUSES.some(s => status.includes(s))) {
      warnings.push(`Status "${statusMatch[1]}" is not a standard value. Expected: ${VALID_STATUSES.join(', ')}`);
    }
  }

  // Validate Priority value
  const priorityMatch = content.match(/^\*\*Priority:\*\*\s+(\w+)/m);
  if (priorityMatch && !VALID_PRIORITIES.includes(priorityMatch[1])) {
    errors.push(`Invalid Priority "${priorityMatch[1]}". Must be one of: ${VALID_PRIORITIES.join(', ')}`);
  }

  // Check required sections
  for (const section of REQUIRED_SECTIONS) {
    if (!hasSection(content, section)) {
      // Some files may use abbreviated section names
      if (section === 'Functional Specification' && !hasSection(content, 'Functional Spec')) {
        warnings.push(`Missing section: "${section}"`);
      } else if (section !== 'Functional Specification') {
        warnings.push(`Missing section: "${section}"`);
      }
    }
  }

  // Check for Gherkin acceptance criteria
  if (!hasGherkinScenario(content)) {
    warnings.push('No Gherkin Scenario found in Acceptance Criteria');
  }

  // Check for traceability links
  if (!hasTraceabilityLinks(content)) {
    warnings.push('Traceability section is missing BRD or FRD links');
  }

  // Check Quality Gate Checklist exists
  if (!content.includes('Quality Gate Checklist')) {
    warnings.push('Missing Quality Gate Checklist');
  }

  return { errors, warnings };
}

/**
 * Main validation runner.
 */
async function validateRequirements() {
  console.log('🔍 Validating requirement files...\n');

  const reqFiles = await glob('REQ-*.md', { cwd: REQ_DIR });
  reqFiles.sort();

  if (reqFiles.length === 0) {
    console.warn('⚠️  No REQ-*.md files found in', REQ_DIR);
    process.exit(1);
  }

  let totalErrors = 0;
  let totalWarnings = 0;
  const results = [];

  for (const file of reqFiles) {
    const content = fs.readFileSync(path.join(REQ_DIR, file), 'utf8');
    const { errors, warnings } = validateRequirement(content, file);

    totalErrors += errors.length;
    totalWarnings += warnings.length;

    const status = errors.length > 0 ? '❌' : warnings.length > 0 ? '⚠️ ' : '✅';
    results.push({ file, errors, warnings, status });

    console.log(`${status} ${file}`);
    for (const error of errors) {
      console.log(`   ❌ ERROR: ${error}`);
    }
    for (const warning of warnings) {
      console.log(`   ⚠️  WARN:  ${warning}`);
    }
  }

  // Summary
  console.log('\n' + '─'.repeat(60));
  console.log(`📊 Validation Summary`);
  console.log(`   Files checked:   ${reqFiles.length}`);
  console.log(`   Files valid:     ${results.filter(r => r.errors.length === 0 && r.warnings.length === 0).length}`);
  console.log(`   Files with warns:${results.filter(r => r.warnings.length > 0).length}`);
  console.log(`   Files with errors:${results.filter(r => r.errors.length > 0).length}`);
  console.log(`   Total errors:    ${totalErrors}`);
  console.log(`   Total warnings:  ${totalWarnings}`);

  if (totalErrors > 0) {
    console.log('\n❌ Validation FAILED — please fix errors above.');
    process.exit(1);
  } else if (totalWarnings > 0) {
    console.log('\n⚠️  Validation PASSED with warnings.');
    process.exit(0);
  } else {
    console.log('\n✅ All requirement files are valid!');
    process.exit(0);
  }
}

validateRequirements().catch((err) => {
  console.error('❌ Validation script error:', err.message);
  process.exit(1);
});
