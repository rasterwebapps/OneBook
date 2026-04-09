---
name: create-angular-component
description: >-
  Create a new Angular standalone component for OneBook with Signals-based state,
  OnPush change detection, Transloco i18n, keyboard navigation support, and
  lazy-loaded routing. Includes design system, page patterns, shared component
  usage, dark mode, and accessibility.
---

# Create Angular Component

Create a new Angular standalone component following OneBook's frontend conventions.

> **📖 Before you start**: Read [`docs/technical/ui-ux-guidelines.md`](../../../docs/technical/ui-ux-guidelines.md) for the comprehensive design system, component library, and UI/UX rules.

## When to Use

- Adding a new screen or page to the application
- Creating a reusable UI component
- Adding a new feature module with routing

## Steps

### 1. Create the Component

Use Angular CLI or create manually. All components MUST be standalone:

```typescript
import { Component, ChangeDetectionStrategy, signal, computed, inject } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { NxCardComponent, NxBadgeComponent, NxAmountComponent,
         NxDataTableComponent, NxSkeletonComponent, NxEmptyStateComponent } from '@app/shared/components';

@Component({
  selector: 'app-{kebab-name}',
  standalone: true,
  imports: [TranslocoModule, NxCardComponent, NxDataTableComponent,
            NxSkeletonComponent, NxEmptyStateComponent, NxAmountComponent, NxBadgeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './{kebab-name}.component.html',
  styleUrl: './{kebab-name}.component.scss'
})
export class {PascalName}Component {
  private readonly service = inject({Feature}Service);

  // Use Signals for state management
  private readonly data = signal<{Type}[]>([]);
  readonly filteredData = computed(() => this.data().filter(d => d.active));
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
}
```

**Rules:**
- ALWAYS `standalone: true` — no NgModules
- ALWAYS `ChangeDetectionStrategy.OnPush`
- ALWAYS use Signals (`signal()`, `computed()`, `effect()`)
- ALWAYS use `inject()` function — not constructor parameters
- NEVER mutate signals directly — use `set()` / `update()`
- NEVER hardcode colors — use CSS custom properties (`var(--nx-emerald)`, `var(--nx-text-primary)`)

### 2. Choose the Right Page Template

Every page should follow one of these standard patterns:

#### List Page (most common)

```html
<div class="page-container">
  <!-- Page Header -->
  <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:var(--nx-space-5);">
    <div>
      <h2 style="margin:0; font-size:var(--nx-text-2xl);">{{ '{feature}.title' | transloco }}</h2>
      <p style="margin:var(--nx-space-1) 0 0; color:var(--nx-text-muted); font-size:var(--nx-text-sm);">
        {{ '{feature}.subtitle' | transloco }}
      </p>
    </div>
    <button class="nx-btn nx-btn--emerald" (click)="create()">
      + {{ '{feature}.actions.create' | transloco }}
    </button>
  </div>

  <nx-card>
    <div card-body style="padding:0;">
      @if (loading()) {
        @for (i of [1,2,3,4,5]; track i) {
          <div style="padding:var(--nx-space-3) var(--nx-space-4);">
            <nx-skeleton height="20px" />
          </div>
        }
      } @else if (data().length === 0) {
        <nx-empty-state
          title="{{ '{feature}.empty.title' | transloco }}"
          description="{{ '{feature}.empty.description' | transloco }}"
          actionLabel="{{ '{feature}.actions.create' | transloco }}"
          (action)="create()"
        />
      } @else {
        <nx-data-table>
          <thead>
            <tr>
              <th>{{ '{feature}.columns.date' | transloco }}</th>
              <th>{{ '{feature}.columns.reference' | transloco }}</th>
              <th class="num">{{ '{feature}.columns.amount' | transloco }}</th>
              <th>{{ '{feature}.columns.status' | transloco }}</th>
              <th class="col-actions no-print">{{ 'common.actions' | transloco }}</th>
            </tr>
          </thead>
          <tbody>
            @for (row of filteredData(); track row.id) {
              <tr>
                <td>{{ row.date }}</td>
                <td class="nx-number">{{ row.referenceNo }}</td>
                <td class="num"><nx-amount [amount]="row.amount" /></td>
                <td><nx-badge [variant]="statusVariant(row.status)">{{ row.status }}</nx-badge></td>
                <td class="col-actions no-print">
                  <button class="btn-icon" title="Edit" aria-label="Edit {{ row.referenceNo }}">✏️</button>
                  <button class="btn-icon" title="Delete" aria-label="Delete {{ row.referenceNo }}">🗑️</button>
                </td>
              </tr>
            }
          </tbody>
        </nx-data-table>
      }
    </div>
  </nx-card>
</div>
```

#### Dashboard Page

Use the bento grid with `<nx-stat-card>` components. See `docs/technical/ui-ux-guidelines.md` §3 for the full pattern.

#### Form Page

Wrap sections in `<nx-card>` with header/body/footer slots. Put save/cancel buttons in the card footer with keyboard shortcut hints (`<kbd>`). See `docs/technical/ui-ux-guidelines.md` §3 for the full pattern.

### 3. Use Shared Components

Always use the `nx-*` shared component library. **Never** build custom versions of these:

| Need | Use This | Import From |
|------|----------|-------------|
| Card container | `<nx-card>` | `@app/shared/components` |
| Monetary amounts | `<nx-amount>` | `@app/shared/components` |
| Status labels | `<nx-badge>` | `@app/shared/components` |
| Data tables | `<nx-data-table>` | `@app/shared/components` |
| Loading placeholders | `<nx-skeleton>` | `@app/shared/components` |
| No-data state | `<nx-empty-state>` | `@app/shared/components` |
| Dashboard stats | `<nx-stat-card>` | `@app/shared/components` |

### 4. Create the Service

```typescript
@Injectable({ providedIn: 'root' })
export class {Feature}Service {
  private readonly http = inject(HttpClient);

  getAll(tenantId: string): Observable<{Type}[]> {
    return this.http.get<{Type}[]>(`/api/{feature}?tenantId=${tenantId}`);
  }
}
```

### 5. Create TypeScript Models

Create interfaces matching backend DTOs exactly:

```typescript
export interface {Type} {
  id: number;
  fieldName: string;
  amount: number;  // BigDecimal maps to number in TypeScript
  createdAt: string;  // ISO 8601 timestamp
}

export interface Create{Type}Request {
  fieldName: string;
  referenceId: number;
  amount: number;
}
```

### 6. Configure Routing

Add lazy-loaded route and update breadcrumbs:

```typescript
// In app routing
{
  path: '{feature}',
  loadComponent: () => import('./{feature}/{feature}.component')
    .then(m => m.{PascalName}Component),
  canActivate: [authGuard]
}

// In app.component.ts — add to ROUTE_BREADCRUMBS
'/{feature}': [{ label: '{Section}' }, { label: '{Feature Title}' }],
```

**Rules:**
- ALWAYS lazy-load with `loadComponent` (single) or `loadChildren` (module)
- ALWAYS protect routes with `authGuard` or `roleGuard`
- ALWAYS add breadcrumb mapping for the new route

### 7. Add Keyboard Shortcuts

Register keyboard shortcuts for the new feature. Every page should have at least navigation shortcuts:

```typescript
import { KeyBindingRegistryService } from '@app/keyboard/services';

private registry = inject(KeyBindingRegistryService);

ngOnInit() {
  this.registry.register({
    id: '{feature}.create',
    label: 'Create {Entity}',
    keys: 'Alt+N',
    category: '{Feature}',
    description: 'Create a new {entity}',
    enabled: true,
  });
}
```

### 8. Add i18n Translations

Add translation keys to `frontend/src/assets/i18n/en.json`:

```json
{
  "{feature}": {
    "title": "{Feature Title}",
    "subtitle": "{Feature description}",
    "columns": {
      "date": "Date",
      "reference": "Reference",
      "amount": "Amount",
      "status": "Status"
    },
    "actions": {
      "create": "Create {Entity}",
      "edit": "Edit {Entity}",
      "delete": "Delete {Entity}"
    },
    "empty": {
      "title": "No {entities} found",
      "description": "Create your first {entity} to get started"
    },
    "messages": {
      "created": "{Entity} created successfully",
      "deleted": "{Entity} deleted",
      "error": "Failed to save {entity}"
    }
  }
}
```

### 9. Accounting-Specific Patterns

When building accounting screens, always follow these patterns:

- **Amounts**: Use `<nx-amount>` with debit/credit coloring — never `CurrencyPipe`
- **Voucher/account codes**: Apply `.nx-number` class for monospace tabular-nums display
- **Status badges**: Map to standard variants (POSTED→success, PENDING→warning, DRAFT→info, CANCELLED→danger)
- **Shortcut hints**: Show `<kbd>` tags on action buttons (e.g., `Save <kbd>Ctrl+A</kbd>`)
- **Table footer totals**: Use `<tfoot>` with `.num` class for right-aligned amounts

### 10. Dark Mode & Styling Checklist

- [ ] All colors use `var(--nx-*)` CSS custom properties — no hardcoded hex values
- [ ] Component SCSS uses only design tokens from `styles.scss`
- [ ] No `::ng-deep` used — prefer global utility classes or component-scoped styles
- [ ] No inline color styles (`style="color: red"`) — use badge/amount components
- [ ] No `z-index` magic numbers — use `var(--nx-z-*)` tokens

### 11. Accessibility Checklist

- [ ] All interactive elements reachable via `Tab` key
- [ ] All icon-only buttons have `aria-label` attribute
- [ ] Color is never used alone to convey meaning — pair with text/icon
- [ ] Semantic HTML used (`<table>`, `<nav>`, `<main>`, `<header>`, `<tfoot>`)
- [ ] Focus indicators not overridden (default `*:focus-visible` with teal outline)
- [ ] Print-hidden elements use `.no-print` class (not `display:none` in component CSS)

### 12. Verify

```bash
cd frontend && npx ng build     # Build check
cd frontend && npx ng test --watch=false --browsers=ChromeHeadless  # Test check
```

## References

- **📖 UI/UX Guidelines**: [`docs/technical/ui-ux-guidelines.md`](../../../docs/technical/ui-ux-guidelines.md) — Full design system, tokens, patterns, do's/don'ts
- Frontend agent: `.github/agents/frontend.agent.md`
- Keyboard shortcuts: `docs/technical/key-binding-registry.md`
- Design tokens: `frontend/src/styles.scss`
- Shared components: `frontend/src/app/shared/components/`
