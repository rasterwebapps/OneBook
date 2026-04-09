---
name: create-angular-component
description: >-
  Create a new Angular standalone component for OneBook with Signals-based state,
  OnPush change detection, Transloco i18n, keyboard navigation support, and
  lazy-loaded routing.
---

# Create Angular Component

Create a new Angular standalone component following OneBook's frontend conventions.

## When to Use

- Adding a new screen or page to the application
- Creating a reusable UI component
- Adding a new feature module with routing

## Steps

### 1. Create the Component

Use Angular CLI or create manually. All components MUST be standalone:

```typescript
import { Component, ChangeDetectionStrategy, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslocoModule } from '@jsverse/transloco';

@Component({
  selector: 'app-{kebab-name}',
  standalone: true,
  imports: [CommonModule, TranslocoModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="container">
      <h1>{{ '{feature}.title' | transloco }}</h1>
      <!-- Component template -->
    </div>
  `
})
export class {PascalName}Component {
  // Use Signals for state management
  private readonly data = signal<{Type}[]>([]);
  readonly filteredData = computed(() => this.data().filter(d => d.active));
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  constructor(private readonly service: {Feature}Service) {}
}
```

**Rules:**
- ALWAYS `standalone: true` — no NgModules
- ALWAYS `ChangeDetectionStrategy.OnPush`
- ALWAYS use Signals (`signal()`, `computed()`, `effect()`)
- NEVER mutate signals directly — use `set()` / `update()`
- NEVER hardcode colors — use CSS custom properties (`--nx-emerald`, `--nx-purple`, `--nx-amber`)

### 2. Create the Service

```typescript
@Injectable({ providedIn: 'root' })
export class {Feature}Service {
  private readonly http = inject(HttpClient);

  getAll(tenantId: string): Observable<{Type}[]> {
    return this.http.get<{Type}[]>(`/api/{feature}?tenantId=${tenantId}`);
  }
}
```

### 3. Create TypeScript Models

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

### 4. Configure Routing

Add lazy-loaded route in the app routing:

```typescript
{
  path: '{feature}',
  loadComponent: () => import('./{feature}/{feature}.component')
    .then(m => m.{PascalName}Component),
  canActivate: [authGuard]
}
```

**Rules:**
- ALWAYS lazy-load with `loadComponent` (single) or `loadChildren` (module)
- ALWAYS protect routes with `authGuard` or `roleGuard`

### 5. Add Keyboard Shortcuts (if applicable)

Register keyboard shortcuts for the new feature:

```typescript
this.keyBindingRegistry.register({
  key: '{shortcut}',
  context: '{feature}',
  action: () => this.performAction(),
  description: '{Action description}'
});
```

### 6. Add i18n Translations

Add translation keys to `frontend/src/assets/i18n/en.json`:

```json
{
  "{feature}": {
    "title": "{Feature Title}",
    "actions": {
      "create": "Create {Entity}",
      "edit": "Edit {Entity}",
      "delete": "Delete {Entity}"
    }
  }
}
```

### 7. Verify

```bash
cd frontend && npx ng build     # Build check
```

## References

- Frontend agent: `.github/agents/frontend.agent.md`
- Keyboard shortcuts: `docs/technical/key-binding-registry.md`
- Design tokens: `frontend/src/styles.scss`
