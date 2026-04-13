# OneBook — UI/UX Agent Guidelines

> **Single source of truth** for all UI/UX decisions in OneBook.
> Read this document before creating any frontend component, page, or screen.
> This covers design tokens, component library, page patterns, keyboard-first UX,
> dark mode, i18n, accessibility, and coding do's/don'ts.

---

## Table of Contents

1. [Design System Overview](#1-design-system-overview)
2. [Component Library Reference](#2-component-library-reference)
3. [Page Structure Patterns](#3-page-structure-patterns)
4. [Accounting-Specific UI Patterns](#4-accounting-specific-ui-patterns)
5. [Keyboard-First UX](#5-keyboard-first-ux)
6. [Navigation & Layout](#6-navigation--layout)
7. [Responsive Breakpoints](#7-responsive-breakpoints)
8. [Dark Mode](#8-dark-mode)
9. [Animation Guidelines](#9-animation-guidelines)
10. [Internationalization (i18n)](#10-internationalization-i18n)
11. [Print Styles](#11-print-styles)
12. [CSS Custom Properties Reference](#12-css-custom-properties-reference)
13. [Accessibility](#13-accessibility)
14. [Do's and Don'ts](#14-dos-and-donts)

---

## 1. Design System Overview

OneBook uses a custom **FinTech-inspired** design system. No third-party component libraries (no Angular Material, no Bootstrap). Every UI element is built from design tokens defined in `frontend/src/styles.scss`.

### Brand Identity

| Role | Token | Value | Usage |
|------|-------|-------|-------|
| **Primary** (Sky Blue) | `--nx-primary` | `#0EA5E9` | CTAs, active states, links, primary actions |
| **Secondary** (Indigo) | `--nx-purple` | `#6366F1` | Secondary actions, chart accents, tags |
| **Warning** (Burnt Orange) | `--nx-amber` | `#F97316` | Warnings, attention indicators |
| **Success** (Mint Green) | `--nx-success` | `#10B981` | Credit amounts, posted status, confirmations |
| **Danger** (Deep Crimson) | `--nx-danger` | `#DC2626` | Debit amounts, errors, destructive actions |
| **Info** | `--nx-info` | `#0EA5E9` | Informational badges, tooltips |
| **Warning** | `--nx-warning` | `#F59E0B` | Pending status, caution messages |

Each accent has three variants for layered depth:

```
--nx-primary:      #0EA5E9     ← Solid (buttons, icons)
--nx-primary-glow: rgba(14, 165, 233, 0.2)  ← Glow (focus rings, highlights)
--nx-primary-dim:  rgba(14, 165, 233, 0.08) ← Dim (icon backgrounds, subtle fills)
```

> **Note:** `--nx-emerald`, `--nx-emerald-glow`, `--nx-emerald-dim` are backward-compatible aliases for `--nx-primary` variants. Prefer `--nx-primary` in new code.

### Typography

| Token | Value | Usage |
|-------|-------|-------|
| `--nx-font-primary` | `'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif` | All UI text |
| `--nx-font-mono` | `'Fira Code', 'JetBrains Mono', 'Consolas', monospace` | Accounting numbers, codes, amounts |

**Fluid Typography Scale (clamp-based):**

| Token | Range | Usage |
|-------|-------|-------|
| `--nx-text-xs` | `clamp(0.625rem, 0.6rem + 0.125vw, 0.6875rem)` | Badges, labels, trends |
| `--nx-text-sm` | `clamp(0.75rem, 0.72rem + 0.15vw, 0.8125rem)` | Table cells, secondary text |
| `--nx-text-base` | `clamp(0.8125rem, 0.78rem + 0.16vw, 0.875rem)` | Body text, card headers |
| `--nx-text-lg` | `clamp(0.9375rem, 0.9rem + 0.2vw, 1rem)` | Section titles, empty-state titles |
| `--nx-text-xl` | `clamp(1rem, 0.95rem + 0.25vw, 1.125rem)` | Page subtitles |
| `--nx-text-2xl` | `clamp(1.25rem, 1.15rem + 0.5vw, 1.375rem)` | Stat card values |
| `--nx-text-3xl` | `clamp(1.5rem, 1.35rem + 0.75vw, 1.75rem)` | Page titles |

**Font Weights:**

| Token | Value |
|-------|-------|
| `--nx-font-weight-normal` | 400 |
| `--nx-font-weight-medium` | 500 |
| `--nx-font-weight-semibold` | 600 |
| `--nx-font-weight-bold` | 700 |

### Spacing Scale (4px base)

| Token | Value | Usage |
|-------|-------|-------|
| `--nx-space-1` | `4px` | Tight inner padding |
| `--nx-space-2` | `8px` | Table cell padding, badge padding |
| `--nx-space-3` | `12px` | Card footer padding, stat margins |
| `--nx-space-4` | `16px` | Card header padding, form group gaps |
| `--nx-space-5` | `20px` | Card body padding, section spacing |
| `--nx-space-6` | `24px` | Empty-state padding |
| `--nx-space-8` | `32px` | Large section gaps |
| `--nx-space-10` | `40px` | Page-level margins |
| `--nx-space-12` | `48px` | Empty-state vertical padding |

**Legacy gaps** (prefer `--nx-space-*` for new code):

| Token | Value |
|-------|-------|
| `--nx-gap-sm` | `8px` |
| `--nx-gap-md` | `16px` |
| `--nx-gap-lg` | `20px` |

### Shadow Hierarchy

| Token | Value | Usage |
|-------|-------|-------|
| `--nx-shadow-sm` | `0 1px 2px rgba(0,0,0,0.04), 0 1px 1px rgba(0,0,0,0.02)` | Cards at rest, badges |
| `--nx-shadow-md` | `0 4px 6px rgba(0,0,0,0.04), 0 2px 4px rgba(0,0,0,0.03)` | Card hover, bento-tile hover |
| `--nx-shadow-lg` | `0 10px 15px rgba(0,0,0,0.06), 0 4px 6px rgba(0,0,0,0.04)` | Dropdowns, elevated elements |
| `--nx-shadow-xl` | `0 20px 25px rgba(0,0,0,0.08), 0 8px 10px rgba(0,0,0,0.04)` | Modals, command palette |

### Border Radius Scale

| Token | Value | Usage |
|-------|-------|-------|
| `--nx-radius-sm` | `6px` | Buttons, badges, inputs |
| `--nx-radius-md` | `8px` | Cards, bento tiles, glass elements |
| `--nx-radius-lg` | `12px` | Larger cards, stat cards, glass-card |
| `--nx-radius-xl` | `16px` | Modal dialogs, command palette |

### Z-Index Scale

| Token | Value | Usage |
|-------|-------|-------|
| `--nx-z-dropdown` | `100` | Dropdown menus, tenant selector |
| `--nx-z-sticky` | `200` | Sticky table headers |
| `--nx-z-modal` | `400` | Modal overlays, command palette |
| `--nx-z-tooltip` | `600` | Tooltips (always on top) |

---

## 2. Component Library Reference

### Shared Angular Components

All shared components live in `frontend/src/app/shared/components/` and are exported from `index.ts`. Import them in any standalone component.

| Component | Selector | Purpose |
|-----------|----------|---------|
| `NxCardComponent` | `<nx-card>` | Card container with header/body/footer slots |
| `NxStatCardComponent` | `<nx-stat-card>` | Dashboard statistic card with icon, value, trend |
| `NxBadgeComponent` | `<nx-badge>` | Status badge with color variants |
| `NxAmountComponent` | `<nx-amount>` | Monetary amount with debit/credit coloring (₹ formatted) |
| `NxSkeletonComponent` | `<nx-skeleton>` | Shimmer loading placeholder |
| `NxEmptyStateComponent` | `<nx-empty-state>` | No-data illustration with optional CTA |
| `NxDataTableComponent` | `<nx-data-table>` | Scrollable table wrapper with sticky headers |
| `NxPageHeaderComponent` | `<nx-page-header>` | Page title + subtitle + projected action buttons |
| `NxSearchInputComponent` | `<nx-search-input>` | Debounced search field with clear button |
| `NxLoadingSpinnerComponent` | `<nx-loading-spinner>` | Full-area loading indicator with label |
| `NxConfirmDialogComponent` | `<nx-confirm-dialog>` | Confirmation dialog (service-driven) |
| `NxStatusBadgeComponent` | `<nx-status-badge>` | Pre-mapped accounting status colors |
| `NxToastComponent` | `<nx-toast>` | Slide-in toast notifications (service-driven) |

#### `<nx-card>` — Card Container

Content projection with named slots:

```html
<nx-card>
  <div card-header>
    <h4>Section Title</h4>
    <button class="nx-btn nx-btn--emerald">Action</button>
  </div>
  <div card-body>
    <!-- Content here -->
  </div>
  <div card-footer>
    <span>Footer info</span>
  </div>
</nx-card>
```

#### `<nx-stat-card>` — Dashboard Statistic

```html
<nx-stat-card
  icon="📊"
  label="Total Revenue"
  value="₹12,45,000"
  trend="+12.5%"
  trendDirection="up"
  color="emerald"
/>
```

Inputs: `icon`, `label`, `value`, `trend`, `trendDirection` (`up`/`down`/`neutral`), `color` (`emerald`/`purple`/`amber`/`danger`).

#### `<nx-badge>` — Status Badge

```html
<nx-badge variant="success">Posted</nx-badge>
<nx-badge variant="warning">Pending</nx-badge>
<nx-badge variant="danger">Cancelled</nx-badge>
<nx-badge variant="info">Draft</nx-badge>
```

Variants: `success`, `warning`, `danger`, `info`, `neutral`.

#### `<nx-amount>` — Monetary Amount

```html
<nx-amount [amount]="transaction.amount" type="auto" />
<nx-amount [amount]="1500.50" type="credit" />
<nx-amount [amount]="-300.00" type="debit" />
```

Inputs: `amount` (string | number), `type` (`debit`/`credit`/`auto`). Auto-detects debit (negative) vs credit (positive). Renders with `₹` prefix and Indian comma formatting.

#### `<nx-skeleton>` — Loading Placeholder

```html
<nx-skeleton width="100%" height="16px" />
<nx-skeleton width="200px" height="32px" />
```

#### `<nx-empty-state>` — No Data

```html
<nx-empty-state
  icon="📭"
  title="No vouchers found"
  description="Create a new voucher to get started"
  actionLabel="Create Voucher"
  (action)="createVoucher()"
/>
```

#### `<nx-data-table>` — Table Wrapper

```html
<nx-data-table>
  <thead>
    <tr><th>Date</th><th>Voucher</th><th class="num">Amount</th><th class="col-actions">Actions</th></tr>
  </thead>
  <tbody>
    @for (row of data(); track row.id) {
      <tr>
        <td>{{ row.date }}</td>
        <td class="nx-number">{{ row.voucherNo }}</td>
        <td class="num"><nx-amount [amount]="row.amount" /></td>
        <td class="col-actions">
          <button class="btn-icon" title="Edit" aria-label="Edit voucher">✏️</button>
          <button class="btn-icon" title="Delete" aria-label="Delete voucher">🗑️</button>
        </td>
      </tr>
    }
  </tbody>
</nx-data-table>
```

### CSS Utility Classes

These are defined in `frontend/src/styles.scss` and available globally:

| Class | Purpose |
|-------|---------|
| `.nx-glass` | Card surface with background, border, shadow |
| `.nx-bento-grid` | Auto-fit responsive grid (`minmax(320px, 1fr)`) |
| `.nx-bento-tile` | Grid tile with hover elevation |
| `.nx-bento-tile--span-2` | Span 2 columns |
| `.nx-bento-tile--span-full` | Span full width |
| `.nx-btn` | Base button style |
| `.nx-btn--emerald` | Primary teal button |
| `.nx-btn--purple` | Secondary indigo button |
| `.nx-btn--amber` | Warning amber button |
| `.nx-btn--danger` | Danger red button |
| `.nx-btn--ghost` | Transparent background button |
| `.nx-btn--outline` | Border-only button |
| `.btn-icon` | Minimal icon-only button (table actions) |
| `.nx-number` | Monospace tabular-nums for codes/IDs |
| `.nx-amount` | Monospace for monetary values (with `.debit`/`.credit`) |
| `.nx-badge` | Inline badge (with `--success/--warning/--danger/--info`) |
| `.nx-card` | Card container (with `.nx-card-header/body/footer`) |
| `.nx-stat-card` | Stat card (with `.stat-icon/label/value/trend`) |
| `.nx-data-table` | Full-width table with sticky headers |
| `.nx-skeleton` | Shimmer loading animation |
| `.nx-empty-state` | Centered no-data layout |
| `.nx-syncing` | Pulsing emerald animation (syncing state) |
| `.nx-locked` | One-shot glow animation (lock/save confirmation) |
| `.col-actions` | Table actions column alignment |
| `.no-print` | Hide element when printing |
| `.sr-only` | Visually hidden, accessible to screen readers |
| `.form-grid` | Responsive two-column form layout |
| `.form-group` | Label + input group |
| `.form-control` | Styled text input / select / textarea |
| `.page-container` | Centered max-width content container |

### Components Added in Recent Update

The following components were previously listed as "Planned" and are now **implemented and available**:

| Component | Selector | Purpose |
|-----------|----------|---------|
| `NxPageHeaderComponent` | `<nx-page-header>` | Consistent page title + subtitle + action buttons bar |
| `NxSearchInputComponent` | `<nx-search-input>` | Reusable debounced search field for list pages |
| `NxLoadingSpinnerComponent` | `<nx-loading-spinner>` | Full-area loading indicator (distinct from skeleton placeholder) |
| `NxConfirmDialogComponent` | `<nx-confirm-dialog>` | Confirmation dialog service for destructive actions |
| `NxStatusBadgeComponent` | `<nx-status-badge>` | Pre-mapped accounting status colors (extends `nx-badge`) |
| `NxToastComponent` | `<nx-toast>` | Slide-in toast notification for success/error/warning feedback |

#### `<nx-page-header>` — Page Header

```html
<nx-page-header title="Voucher Explorer" subtitle="Manage all voucher types">
  <button class="nx-btn nx-btn--emerald" (click)="create()">+ Create</button>
</nx-page-header>
```

Inputs: `title` (string), `subtitle` (string). Content projection for action buttons.

#### `<nx-search-input>` — Debounced Search

```html
<nx-search-input
  placeholder="Search vouchers…"
  [value]="searchQuery()"
  [debounceMs]="300"
  (searchChange)="onSearch($event)"
/>
```

Inputs: `placeholder`, `value`, `debounceMs` (default 300). Output: `searchChange` (debounced string).

#### `<nx-loading-spinner>` — Loading Indicator

```html
<nx-loading-spinner label="Loading report data…" />
```

Inputs: `label` (optional text below spinner). Has `role="status"` and `.sr-only` fallback.

#### `<nx-confirm-dialog>` — Confirmation Dialog

Place once in the app shell. Use the service to trigger:

```typescript
import { NxConfirmDialogService } from '@app/shared/components';

const confirmed = await this.confirmService.confirm({
  title: 'Delete Voucher',
  message: 'This action cannot be undone. Are you sure?',
  confirmLabel: 'Yes, Delete',
  cancelLabel: 'Cancel',
  confirmVariant: 'danger',
});
if (confirmed) { /* proceed */ }
```

#### `<nx-status-badge>` — Accounting Status Badge

```html
<nx-status-badge status="POSTED" />
<nx-status-badge status="PENDING" />
<nx-status-badge status="DRAFT" />
<nx-status-badge status="CANCELLED" />
<nx-status-badge status="APPROVED" label="Custom Label" />
```

Auto-maps standard accounting statuses (POSTED, APPROVED, PENDING, DRAFT, CANCELLED, REJECTED, etc.) to color variants. Accepts optional custom `label`.

#### `<nx-toast>` — Toast Notifications

Place once in the app shell. Use the service to show:

```typescript
import { NxToastService } from '@app/shared/components';

this.toastService.success('Voucher saved successfully');
this.toastService.error('Failed to post transaction');
this.toastService.warning('Unsaved changes will be lost');
this.toastService.info('Report exported');
```

---

## 3. Page Structure Patterns

### Standard List Page

Every list/table page should follow this structure:

```
┌─────────────────────────────────────────────┐
│ Page Header (title + subtitle + actions)    │
├─────────────────────────────────────────────┤
│ Search / Filters Bar                        │
├─────────────────────────────────────────────┤
│ <nx-data-table>                             │
│   thead (sticky)                            │
│   tbody (rows with hover, zebra striping)   │
│   tfoot (totals row)                        │
├─────────────────────────────────────────────┤
│ OR <nx-empty-state> when no data            │
│ OR <nx-skeleton> rows when loading          │
└─────────────────────────────────────────────┘
```

Template structure:

```html
<div class="page-container">
  <!-- Page Header -->
  <div class="page-header" style="display:flex; justify-content:space-between; align-items:center; margin-bottom:var(--nx-space-5);">
    <div>
      <h2 style="margin:0; font-size:var(--nx-text-2xl);">{{ 'feature.title' | transloco }}</h2>
      <p style="margin:var(--nx-space-1) 0 0; color:var(--nx-text-muted); font-size:var(--nx-text-sm);">
        {{ 'feature.subtitle' | transloco }}
      </p>
    </div>
    <div style="display:flex; gap:var(--nx-space-2);">
      <button class="nx-btn nx-btn--emerald" (click)="create()">
        + {{ 'feature.actions.create' | transloco }}
      </button>
    </div>
  </div>

  <!-- Content Card -->
  <nx-card>
    <div card-header>
      <input type="text" class="search-input" placeholder="Search..." />
    </div>
    <div card-body style="padding:0;">
      @if (loading()) {
        <!-- Skeleton rows -->
        @for (i of [1,2,3,4,5]; track i) {
          <div style="padding:var(--nx-space-3) var(--nx-space-4);">
            <nx-skeleton height="20px" />
          </div>
        }
      } @else if (data().length === 0) {
        <nx-empty-state
          title="No records found"
          description="Create your first record to get started"
          actionLabel="Create"
          (action)="create()"
        />
      } @else {
        <nx-data-table>
          <!-- table content -->
        </nx-data-table>
      }
    </div>
  </nx-card>
</div>
```

### Dashboard Page (Bento Grid)

```html
<div class="nx-bento-grid">
  <nx-stat-card icon="💰" label="Revenue" value="₹12,45,000" trend="+12%" trendDirection="up" color="emerald" />
  <nx-stat-card icon="📊" label="Expenses" value="₹8,30,000" trend="-3%" trendDirection="down" color="danger" />
  <nx-stat-card icon="📈" label="Profit" value="₹4,15,000" trend="+22%" trendDirection="up" color="purple" />
  <nx-stat-card icon="🏦" label="Cash" value="₹2,80,000" trendDirection="neutral" color="amber" />

  <div class="nx-bento-tile nx-bento-tile--span-2">
    <!-- Chart or larger widget -->
  </div>
  <div class="nx-bento-tile">
    <!-- Smaller widget -->
  </div>
</div>
```

### Form Page

```html
<div class="page-container">
  <div class="page-header">
    <h2>{{ 'feature.create.title' | transloco }}</h2>
  </div>

  <nx-card>
    <div card-header>
      <h4>{{ 'feature.sections.basic' | transloco }}</h4>
    </div>
    <div card-body>
      <div class="form-grid">
        <div class="form-group">
          <label for="field1">{{ 'feature.fields.name' | transloco }}</label>
          <input id="field1" type="text" class="form-control" />
        </div>
        <div class="form-group">
          <label for="field2">{{ 'feature.fields.code' | transloco }}</label>
          <input id="field2" type="text" class="form-control nx-number" />
        </div>
      </div>
    </div>
    <div card-footer>
      <button class="nx-btn nx-btn--emerald" (click)="save()">
        {{ 'common.save' | transloco }} <kbd>Ctrl+A</kbd>
      </button>
      <button class="nx-btn" (click)="cancel()">
        {{ 'common.cancel' | transloco }} <kbd>Esc</kbd>
      </button>
    </div>
  </nx-card>
</div>
```

---

## 4. Accounting-Specific UI Patterns

### Monetary Amount Display

**Always** use `<nx-amount>` or the `.nx-amount` CSS class for monetary values. Never use Angular `CurrencyPipe`.

```html
<!-- Component approach (preferred) -->
<nx-amount [amount]="entry.debitAmount" type="debit" />
<nx-amount [amount]="entry.creditAmount" type="credit" />

<!-- CSS class approach (inside tables) -->
<td class="nx-amount debit">₹{{ formatAmount(entry.debit) }}</td>
<td class="nx-amount credit">₹{{ formatAmount(entry.credit) }}</td>
```

Color rules:
- **Debit** amounts → `var(--nx-danger)` (deep crimson `#DC2626`)
- **Credit** amounts → `var(--nx-success)` (mint green `#10B981`)
- Always use Indian comma formatting (`en-IN` locale)
- Always show 2 decimal places minimum

### Account Codes and Voucher Numbers

Use `.nx-number` class for any code, ID, or reference number:

```html
<td class="nx-number">V-2026-00142</td>
<td class="nx-number">ACC-001</td>
```

This applies `font-family: var(--nx-font-mono)` and `font-variant-numeric: tabular-nums` for aligned numeric columns.

### Status Badge Mappings

Use `<nx-badge>` with these standard mappings:

| Status | Variant | Example |
|--------|---------|---------|
| `POSTED` | `success` | `<nx-badge variant="success">Posted</nx-badge>` |
| `APPROVED` | `success` | `<nx-badge variant="success">Approved</nx-badge>` |
| `PENDING` | `warning` | `<nx-badge variant="warning">Pending</nx-badge>` |
| `DRAFT` | `info` | `<nx-badge variant="info">Draft</nx-badge>` |
| `CANCELLED` | `danger` | `<nx-badge variant="danger">Cancelled</nx-badge>` |
| `REVERSED` | `danger` | `<nx-badge variant="danger">Reversed</nx-badge>` |
| `REJECTED` | `danger` | `<nx-badge variant="danger">Rejected</nx-badge>` |

### Transaction Type Color Mapping

| Voucher Type | Color Token | Icon |
|-------------|-------------|------|
| Contra | `--nx-purple` | 🔄 |
| Payment | `--nx-danger` | 💳 |
| Receipt | `--nx-success` | 📥 |
| Journal | `--nx-info` | 📝 |
| Sales | `--nx-emerald` | 🛒 |
| Purchase | `--nx-amber` | 📦 |

### Keyboard Shortcut Hints

Display keyboard shortcuts using `<kbd>` tags next to action buttons:

```html
<button class="nx-btn nx-btn--emerald">
  Save <kbd>Ctrl+A</kbd>
</button>
<button class="nx-btn">
  Payment Voucher <kbd>F5</kbd>
</button>
```

The `<kbd>` tag is globally styled with a subtle raised appearance.

### Tenant Context Indicator

The tenant/payer selector in the sidebar shows the active context. Always display the tenant name in any multi-tenant-aware component header.

### Audit Trail Hash Display

For audit trail entries, display hashes using `.nx-number` class with truncation:

```html
<span class="nx-number" title="{{ fullHash }}">{{ hash | slice:0:12 }}…</span>
```

---

## 5. Keyboard-First UX

OneBook implements **"Better-than-Tally" keyboard-first UX**. Every accounting operation must be accessible via keyboard. This is a core differentiator.

### Command Palette (`Ctrl+K`)

The Command Palette is always available via `Ctrl+K` (or `Cmd+K` on macOS). It provides:
- Fuzzy search across all registered commands
- Keyboard shortcut display next to each command
- Category grouping (Vouchers, Masters, Navigation, Reports, Actions)
- Recently used command tracking

Implementation: `CommandPaletteComponent` at `frontend/src/app/keyboard/components/command-palette/`.

### Legacy Tally Shortcuts

Pre-loaded in `KeyBindingRegistryService`:

| Key | Action | Category |
|-----|--------|----------|
| `F4` | Contra Voucher | Vouchers |
| `F5` | Payment Voucher | Vouchers |
| `F6` | Receipt Voucher | Vouchers |
| `F7` | Journal Voucher | Vouchers |
| `F8` | Sales Voucher | Vouchers |
| `F9` | Purchase Voucher | Vouchers |
| `Alt+C` | Create Master | Masters |
| `Alt+A` | Alter Master | Masters |
| `Alt+D` | Display Master | Masters |
| `Ctrl+A` | Save | Actions |
| `Alt+D` | Delete | Actions |
| `Alt+P` | Print | Actions |
| `Alt+E` | Export | Actions |
| `Ctrl+K` | Command Palette | Navigation |
| `Escape` | Close / Go Back | Navigation |
| `Alt+F2` | Day Book | Reports |
| `Alt+F3` | Trial Balance | Reports |
| `Alt+F5` | Profit & Loss | Reports |
| `Alt+F7` | Balance Sheet | Reports |

### Registering New Shortcuts

```typescript
import { KeyBindingRegistryService } from '@app/keyboard/services';

export class MyComponent {
  private registry = inject(KeyBindingRegistryService);

  ngOnInit() {
    this.registry.register({
      id: 'myFeature.action',
      label: 'My Action',
      keys: 'Ctrl+Shift+M',
      category: 'MyFeature',
      description: 'Perform my action',
      enabled: true,
    });
  }
}
```

### Keyboard Architecture

```
KeyboardEvent
  → KeyboardNavigationService (global listener)
    → KeyBindingRegistryService (find matching binding)
    → CommandRegistryService (execute command)
    → KeyboardContextDirective (check active context)
  → CommandPaletteComponent (Ctrl+K UI)
```

Services and components are in `frontend/src/app/keyboard/`.

---

## 6. Navigation & Layout

### App Shell Architecture

```
┌──────────────────────────────────────────────────────────┐
│ Top Header (52px)                                        │
│  [☰] [Logo] OneBook │ Breadcrumbs ›  │ Search │ [Ctrl+K]│
│                                       │ [🌐] [🔔] [👤]  │
├────────────┬─────────────────────────────────────────────┤
│ Sidebar    │ Main Content                                │
│ (Dark)     │                                             │
│ ┌────────┐ │  ┌───────────────────────────────────────┐  │
│ │Tenant  │ │  │ Page Header                           │  │
│ │Selector│ │  ├───────────────────────────────────────┤  │
│ ├────────┤ │  │ Content (cards, tables, forms)         │  │
│ │🏠 Dash │ │  │                                       │  │
│ ├────────┤ │  │                                       │  │
│ │Acctg ▾ │ │  │                                       │  │
│ │ Voucher│ │  │                                       │  │
│ │ Ledger │ │  │                                       │  │
│ ├────────┤ │  └───────────────────────────────────────┘  │
│ │Reports▾│ │                                             │
│ ├────────┤ │                                             │
│ │Mgmt  ▾ │ │                                             │
│ ├────────┤ │                                             │
│ │Intel ▾ │ │                                             │
│ ├────────┤ │                                             │
│ │Status  │ │                                             │
│ └────────┘ │                                             │
└────────────┴─────────────────────────────────────────────┘
```

### Sidebar

- **Theme**: Dark (`--nx-sidebar-bg: #263238`)
- **Collapsible**: Toggle via hamburger menu; collapses to icon-only mode
- **Sections**: Accounting, Reports, Management, Intelligence — each collapsible
- **Tenant selector**: Top of sidebar; dropdown with available tenants
- **Status indicator**: Bottom of sidebar; shows backend connection status

### Top Header

- **Height**: `52px` (`--nx-header-height`)
- **Left**: Hamburger toggle + logo + app title
- **Center**: Dynamic breadcrumbs (route-to-breadcrumb mapping)
- **Right**: Search box, `Ctrl+K` hint, language switcher, apps button, notifications, user menu

### Breadcrumb System

Breadcrumbs are derived from the current route URL via `ROUTE_BREADCRUMBS` mapping in `app.component.ts`. Add new routes to the mapping when creating new pages:

```typescript
const ROUTE_BREADCRUMBS: Record<string, Breadcrumb[]> = {
  '/': [{ label: 'Dashboard' }],
  '/vouchers': [{ label: 'Accounting' }, { label: 'Voucher Explorer' }],
  '/reports/trial-balance': [{ label: 'Reports' }, { label: 'Trial Balance' }],
  // ... add new routes here
};
```

---

## 7. Responsive Breakpoints

| Name | Min Width | Usage |
|------|-----------|-------|
| `sm` | `576px` | Mobile landscape |
| `md` | `768px` | Tablet |
| `lg` | `992px` | Desktop |
| `xl` | `1200px` | Large desktop |

### Responsive Behaviors

- **Sidebar**: On `< md`, sidebar becomes an overlay (hidden by default)
- **Bento grid**: `repeat(auto-fit, minmax(320px, 1fr))` — auto-stacks on narrow screens
- **Data tables**: `<nx-data-table>` wraps in `overflow-x: auto` for horizontal scroll
- **Header**: On mobile, search and some actions collapse into hamburger menu
- **Stat cards**: Stack vertically on narrow viewports via the bento grid

---

## 8. Dark Mode

OneBook supports a full dark mode via the `html.dark-mode` CSS class.

### Toggle Mechanism

Add/remove the class on the `<html>` element:

```typescript
document.documentElement.classList.toggle('dark-mode');
```

### Token Overrides in Dark Mode

All design tokens are overridden in `html.dark-mode`:

| Category | Light Mode | Dark Mode |
|----------|-----------|-----------|
| Background | `#F8FAFC` | `#0F172A` |
| Cards | `#ffffff` | `#1E293B` |
| Card hover | `#F1F5F9` | `#334155` |
| Surface | `#F1F5F9` | `#0F172A` |
| Text primary | `#0F172A` | `#F1F5F9` |
| Text secondary | `#475569` | `#94A3B8` |
| Text muted | `#94A3B8` | `#64748B` |
| Border | `#E2E8F0` | `rgba(148,163,184,0.12)` |
| Glass bg | `rgba(255,255,255,0.7)` | `rgba(30,41,59,0.8)` |
| Shadows | Light, subtle multi-layer | Darker, stronger |

### Rule: Always Use CSS Variables

**Never** hardcode colors. Always use `var(--nx-*)` tokens so dark mode works automatically:

```scss
// ✅ CORRECT
.my-element { color: var(--nx-text-primary); background: var(--nx-bg-card); }

// ❌ WRONG
.my-element { color: #333; background: white; }
```

---

## 9. Animation Guidelines

### Existing Animations

| Class / Keyframes | Usage | Duration |
|-------------------|-------|----------|
| `.nx-syncing` / `nx-pulse-emerald` | Syncing state indicator | `2s` infinite |
| `.nx-locked` / `nx-glow-success` | Save/lock confirmation flash | `0.6s` one-shot |
| `nx-pulse-purple` | Purple accent pulse | `2s` infinite |
| `nx-pulse-amber` | Warning pulse | `2s` infinite |
| `.nx-skeleton` / `nx-shimmer` | Loading placeholder shimmer | `1.6s` infinite |

### Transition Tokens

| Token | Duration | Usage |
|-------|----------|-------|
| `--nx-transition-fast` | `0.15s ease-in-out` | Button hover, row hover |
| `--nx-transition-normal` | `0.2s ease-in-out` | Card hover, border color |
| `--nx-transition-slow` | `0.3s ease-in-out` | Sidebar collapse, page transitions |

### Guidelines

- Use `transition` for interactive state changes (hover, focus, active)
- Use `@keyframes` for continuous state indicators (syncing, loading)
- Prefer transform/opacity for performance: `transform: translateY(-1px)` on hover
- Keep all animations under `0.3s` for interactive feedback
- Always respect `prefers-reduced-motion` media query for accessibility

---

## 10. Internationalization (i18n)

OneBook uses **@jsverse/transloco** for internationalization.

### Configuration

- Config: `frontend/src/app/i18n/transloco-config.ts`
- Loader: `frontend/src/app/i18n/transloco-loader.ts`
- Translation files: `frontend/src/assets/i18n/{lang}.json`
- Language switcher: `LanguageSwitcherComponent` in sidebar header

### Translation Key Convention

```json
{
  "feature": {
    "title": "Feature Title",
    "subtitle": "Feature description",
    "fields": {
      "name": "Name",
      "code": "Code"
    },
    "actions": {
      "create": "Create",
      "edit": "Edit",
      "delete": "Delete"
    },
    "messages": {
      "success": "Record saved successfully",
      "error": "Failed to save record"
    }
  }
}
```

### Rules

- **Never** hardcode user-visible strings — always use `| transloco` pipe
- Use nested keys following the `feature.section.key` pattern
- Import `TranslocoModule` in every standalone component that displays text
- Common keys (Save, Cancel, Delete, etc.) go under a `common` namespace

---

## 11. Print Styles

### Print Utilities

| Class | Effect |
|-------|--------|
| `.no-print` | `display: none !important` in print media |

### Print Overrides

Defined in `@media print` block:

- Table headers become `position: static` (not sticky)
- Body background set to white, text to black
- Non-essential UI elements (sidebar, header) should use `.no-print`

### Print-Friendly Tables

The `<nx-data-table>` component handles de-stickification of headers automatically. For print-ready reports, ensure:

1. Amount columns use `.num` class for right-alignment
2. Footer totals row uses `<tfoot>` for semantic correctness
3. Non-data columns (actions) have `.no-print` class

---

## 12. CSS Custom Properties Reference

Complete token reference from `frontend/src/styles.scss`. All tokens use the `--nx-` prefix.

### Naming Convention

```
--nx-{category}-{modifier}
```

Categories: `bg`, `text`, `border`, `shadow`, `radius`, `space`, `gap`, `font`, `transition`, `z`, `sidebar`, `navbar`, `header`, `page-header`, `primary`, and accent names (`emerald` (alias), `purple`, `amber`).

### Complete Token Table

<details>
<summary>Click to expand full token list</summary>

**Backgrounds:**
`--nx-bg-primary`, `--nx-bg-secondary`, `--nx-bg-card`, `--nx-bg-card-hover`, `--nx-bg-surface`

**Sidebar:**
`--nx-sidebar-bg`, `--nx-sidebar-header-bg`, `--nx-sidebar-text`, `--nx-sidebar-text-hover`, `--nx-sidebar-icon`, `--nx-sidebar-active-bg`, `--nx-sidebar-section-text`, `--nx-sidebar-border`

**Accents:**
`--nx-primary`, `--nx-primary-glow`, `--nx-primary-dim`, `--nx-emerald` (alias), `--nx-emerald-glow` (alias), `--nx-emerald-dim` (alias), `--nx-purple`, `--nx-purple-glow`, `--nx-purple-dim`, `--nx-amber`, `--nx-amber-glow`, `--nx-amber-dim`

**Text:**
`--nx-text-primary`, `--nx-text-secondary`, `--nx-text-muted`

**Borders:**
`--nx-border`, `--nx-border-glow`

**Glass (Glassmorphic surfaces):**
`--nx-glass-bg`, `--nx-glass-border`, `--nx-glass-blur`

> Use the `.glass-card` CSS class for glassmorphic card surfaces with 12px backdrop blur.

**Shadows:**
`--nx-shadow-sm`, `--nx-shadow-md`, `--nx-shadow-lg`, `--nx-shadow-xl`

**Typography:**
`--nx-font-primary`, `--nx-font-mono`, `--nx-text-xs`, `--nx-text-sm`, `--nx-text-base`, `--nx-text-lg`, `--nx-text-xl`, `--nx-text-2xl`, `--nx-text-3xl`, `--nx-font-weight-normal`, `--nx-font-weight-medium`, `--nx-font-weight-semibold`, `--nx-font-weight-bold`

**Transitions:**
`--nx-transition-fast`, `--nx-transition-normal`, `--nx-transition-slow`

**Radius:**
`--nx-radius-sm`, `--nx-radius-md`, `--nx-radius-lg`, `--nx-radius-xl`

**Spacing:**
`--nx-space-1` through `--nx-space-12`, `--nx-gap-sm`, `--nx-gap-md`, `--nx-gap-lg`

**Layout:**
`--nx-header-height`, `--nx-navbar-bg`, `--nx-navbar-border`, `--nx-navbar-height`, `--nx-page-header-bg`

**Status:**
`--nx-success`, `--nx-danger`, `--nx-warning`, `--nx-info`

**Z-Index:**
`--nx-z-dropdown`, `--nx-z-sticky`, `--nx-z-modal`, `--nx-z-tooltip`

</details>

---

## 13. Accessibility

### Focus Indicators

All focusable elements receive a sky-blue focus ring:

```css
*:focus-visible {
  outline: 2px solid var(--nx-primary);
  outline-offset: 2px;
}
```

Never override this to `outline: none` without providing an equivalent visible focus indicator.

### Keyboard Operability

- All interactive elements must be reachable via `Tab`
- All actions must be triggerable via `Enter` or `Space`
- Modal dialogs must trap focus
- The Command Palette demonstrates proper keyboard navigation (arrow keys, Enter to select, Escape to close)

### ARIA Patterns

- **Sidebar toggle**: `aria-label="Toggle sidebar"`
- **Icon buttons**: Always provide `aria-label` for buttons with only icon content
- **Notifications badge**: Use `aria-live="polite"` for count updates
- **Tables**: Use semantic `<thead>`, `<tbody>`, `<tfoot>` elements
- **Command Palette**: Uses `role="dialog"`, `aria-modal="true"`, `role="listbox"` for results
- **Dropdowns**: `aria-expanded` on trigger, `role="menu"` on dropdown

### Color Accessibility

- **Never** use color alone to convey information — always pair with text or icons
  - ✅ `<nx-badge variant="danger">Cancelled</nx-badge>` (color + text)
  - ❌ Red dot with no label
- All text must meet **WCAG 2.1 AA** contrast ratio (4.5:1 for normal text, 3:1 for large text)
- Debit/credit amounts use color + position (debit column vs credit column) for clarity

### Screen Reader Support

Add `.sr-only` class for screen-reader-only text:

```scss
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border-width: 0;
}
```

---

## 14. Do's and Don'ts

### ✅ Do

1. **Use `nx-*` shared components** for cards, badges, amounts, tables, skeletons, empty states
2. **Use CSS custom properties** (`var(--nx-*)`) for all colors, spacing, typography, shadows
3. **Use Angular Signals** (`signal()`, `computed()`, `effect()`) for state management
4. **Use `ChangeDetectionStrategy.OnPush`** on every component
5. **Use `@if` / `@for`** control flow (not `*ngIf` / `*ngFor`)
6. **Use Transloco** (`| transloco` pipe) for all user-visible strings
7. **Use keyboard shortcuts** — register via `KeyBindingRegistryService` for all major actions
8. **Use `standalone: true`** — no NgModules anywhere
9. **Use `<nx-amount>`** for monetary values — never Angular `CurrencyPipe`
10. **Use `.nx-number`** for codes, IDs, voucher numbers
11. **Use lazy loading** (`loadComponent` / `loadChildren`) for all routes
12. **Use `authGuard`** on all protected routes
13. **Use `aria-label`** on all icon-only buttons
14. **Use semantic HTML** (`<table>`, `<thead>`, `<tfoot>`, `<nav>`, `<main>`, `<header>`)
15. **Use `inject()`** function for dependency injection (not constructor parameters)

### ❌ Don't

1. **Don't use Angular Material** or any third-party UI component library
2. **Don't use `CurrencyPipe`** — use `<nx-amount>` or `.nx-amount` class
3. **Don't use `DatePipe`** — use custom formatting with locale support via Transloco
4. **Don't use `*ngIf` / `*ngFor`** — use `@if` / `@for` block syntax
5. **Don't use NgModules** — everything is standalone
6. **Don't hardcode colors** — use `var(--nx-*)` tokens (breaks dark mode)
7. **Don't use `double` or `float`** for amounts — use `BigDecimal` on backend, `number` with proper formatting on frontend
8. **Don't use RxJS** for simple state — use Signals; RxJS only for streams (HTTP, events)
9. **Don't use `localStorage`** for auth tokens — in-memory only (security requirement)
10. **Don't put styles in `::ng-deep`** — use global CSS classes or component-scoped styles
11. **Don't use `z-index` magic numbers** — use `var(--nx-z-*)` tokens
12. **Don't remove `aria-label`** from interactive elements
13. **Don't use inline colors** like `style="color: red"` — use badge/amount components
14. **Don't skip keyboard shortcuts** — every new page should register at least navigation shortcuts
15. **Don't use `@ViewChild` for state** — use Signals and `input()` / `output()`

---

## References

- **Design tokens source**: `frontend/src/styles.scss`
- **Shared components**: `frontend/src/app/shared/components/`
- **Keyboard module**: `frontend/src/app/keyboard/`
- **i18n config**: `frontend/src/app/i18n/`
- **Key binding registry design**: `docs/technical/key-binding-registry.md`
- **Create Angular Component skill**: `.github/skills/create-angular-component/SKILL.md`
- **Frontend agent**: `.github/agents/frontend.agent.md`
