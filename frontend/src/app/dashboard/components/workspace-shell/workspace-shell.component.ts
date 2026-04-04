import {
  Component,
  ChangeDetectionStrategy,
  input,
  computed,
  signal,
  OnInit,
} from '@angular/core';
import {
  RoleDashboardPolicy,
  WorkspaceTemplate,
  UserDashboardPrefs,
} from '../../models/dashboard-layout.models';

/**
 * WorkspaceShellComponent — Runtime (non-draggable) dashboard renderer.
 *
 * Renders the role-based dashboard as a set of locked workspace tabs.
 * Card positions come exclusively from the published WorkspaceTemplate.
 * No drag library is imported here; this component is intentionally static.
 *
 * Spec: docs/ux/role-based-dashboards.md §5
 *
 * TODO (implementation):
 *  - Inject RolePolicyStore and subscribe to policy signal.
 *  - Render actual card components by moduleKey.
 *  - Implement IndexedDB SWR caching and SSE revalidation.
 *  - Wire keyboard navigation for tab strip (ArrowLeft/Right, Home/End).
 */
@Component({
  selector: 'app-workspace-shell',
  standalone: true,
  imports: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <!-- Workspace Tab Strip -->
    <div
      role="tablist"
      aria-label="Dashboard workspaces"
      class="workspace-tab-strip"
    >
      @for (ws of workspaces(); track ws.workspaceId) {
        <button
          role="tab"
          [attr.aria-selected]="activeWorkspaceId() === ws.workspaceId"
          [attr.aria-controls]="'panel-' + ws.workspaceId"
          [attr.id]="'tab-' + ws.workspaceId"
          [attr.tabindex]="activeWorkspaceId() === ws.workspaceId ? 0 : -1"
          class="workspace-tab"
          [class.active]="activeWorkspaceId() === ws.workspaceId"
          (click)="activateWorkspace(ws.workspaceId)"
          (keydown)="onTabKeydown($event, ws.workspaceId)"
        >
          @if (ws.icon) { <span class="tab-icon" aria-hidden="true">{{ ws.icon }}</span> }
          {{ ws.label }}
        </button>
      }
    </div>

    <!-- Workspace Panels -->
    @for (ws of workspaces(); track ws.workspaceId) {
      <div
        role="tabpanel"
        [attr.id]="'panel-' + ws.workspaceId"
        [attr.aria-labelledby]="'tab-' + ws.workspaceId"
        [hidden]="activeWorkspaceId() !== ws.workspaceId"
        class="workspace-panel"
        [attr.aria-busy]="loading()"
      >
        @if (loading()) {
          <div class="workspace-loading" aria-live="polite">Loading workspace&hellip;</div>
        } @else {
          <!-- Locked 12-column grid — positions from WorkspaceTemplate only -->
          <div class="workspace-grid" aria-label="{{ ws.label }} dashboard">
            @for (card of ws.cards; track card.cardId) {
              <div
                class="dashboard-card"
                [style.grid-column]="card.rect.col + ' / span ' + card.rect.w"
                [style.grid-row]="card.rect.row + ' / span ' + card.rect.h"
                [attr.data-module]="card.moduleKey"
                [attr.data-lock]="card.lock"
              >
                <!-- Placeholder: replace with dynamic card component by moduleKey -->
                <div class="card-placeholder">
                  <span class="card-module-key">{{ card.moduleKey }}</span>
                </div>
              </div>
            }
          </div>

          <!-- Add-ons Zone (ACCOUNTANT / CLERK only) -->
          @if (ws.addonsZone) {
            <div
              class="addons-zone"
              [style.grid-column]="ws.addonsZone.rect.col + ' / span ' + ws.addonsZone.rect.w"
              [style.grid-row]="ws.addonsZone.rect.row + ' / span ' + ws.addonsZone.rect.h"
              aria-label="Add-ons zone"
            >
              <!-- Placeholder: add-ons management UI rendered here -->
              <p class="addons-placeholder">Add-ons zone — up to {{ ws.addonsZone.maxCards }} modules</p>
            </div>
          }
        }
      </div>
    }

    @if (error()) {
      <div role="alert" aria-live="assertive" class="workspace-error">
        {{ error() }}
      </div>
    }
  `,
  styles: [`
    :host { display: block; height: 100%; }

    .workspace-tab-strip {
      display: flex;
      gap: 0.25rem;
      border-bottom: 1px solid var(--color-border, #e5e7eb);
      padding: 0 1rem;
    }

    .workspace-tab {
      background: none;
      border: none;
      padding: 0.75rem 1rem;
      cursor: pointer;
      font-size: 0.875rem;
      color: var(--color-text-secondary, #6b7280);
      border-bottom: 2px solid transparent;
      transition: color 0.15s, border-color 0.15s;
    }

    .workspace-tab.active,
    .workspace-tab[aria-selected="true"] {
      color: var(--color-primary, #2563eb);
      border-bottom-color: var(--color-primary, #2563eb);
    }

    .workspace-tab:focus-visible {
      outline: 2px solid var(--color-primary, #2563eb);
      outline-offset: 2px;
      border-radius: 4px;
    }

    .tab-icon { margin-right: 0.375rem; }

    .workspace-panel { padding: 1rem; }

    .workspace-grid {
      display: grid;
      grid-template-columns: repeat(12, 1fr);
      gap: 1rem;
    }

    .dashboard-card {
      background: var(--color-surface, #fff);
      border: 1px solid var(--color-border, #e5e7eb);
      border-radius: 8px;
      padding: 1rem;
      min-height: 120px;
    }

    .card-placeholder {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 100%;
      color: var(--color-text-tertiary, #9ca3af);
      font-size: 0.75rem;
    }

    .card-module-key {
      font-family: monospace;
      background: var(--color-surface-raised, #f3f4f6);
      padding: 0.25rem 0.5rem;
      border-radius: 4px;
    }

    .addons-zone {
      border: 2px dashed var(--color-border, #e5e7eb);
      border-radius: 8px;
      padding: 1rem;
      margin-top: 1rem;
    }

    .addons-placeholder {
      color: var(--color-text-tertiary, #9ca3af);
      font-size: 0.75rem;
      text-align: center;
    }

    .workspace-loading,
    .workspace-error { padding: 2rem; text-align: center; }

    .workspace-error { color: var(--color-error, #ef4444); }
  `],
})
export class WorkspaceShellComponent implements OnInit {
  /** Role dashboard policy loaded by the parent or a store. */
  readonly policy = input<RoleDashboardPolicy | null>(null);

  /** User preferences (last active workspace, density override). */
  readonly userPrefs = input<UserDashboardPrefs | null>(null);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly workspaces = computed<WorkspaceTemplate[]>(() => this.policy()?.workspaces ?? []);

  readonly activeWorkspaceId = signal<string | null>(null);

  ngOnInit(): void {
    const prefs = this.userPrefs();
    const workspaces = this.workspaces();
    const preferred = prefs?.lastWorkspaceId
      ? workspaces.find(w => w.workspaceId === prefs.lastWorkspaceId)
      : null;
    this.activeWorkspaceId.set(preferred?.workspaceId ?? workspaces[0]?.workspaceId ?? null);
  }

  activateWorkspace(workspaceId: string): void {
    this.activeWorkspaceId.set(workspaceId);
  }

  /** ARIA tab strip keyboard navigation (ArrowLeft/Right/Home/End). */
  onTabKeydown(event: KeyboardEvent, currentId: string): void {
    const workspaces = this.workspaces();
    const currentIndex = workspaces.findIndex(w => w.workspaceId === currentId);
    let nextIndex = currentIndex;

    switch (event.key) {
      case 'ArrowRight':
        nextIndex = (currentIndex + 1) % workspaces.length;
        break;
      case 'ArrowLeft':
        nextIndex = (currentIndex - 1 + workspaces.length) % workspaces.length;
        break;
      case 'Home':
        nextIndex = 0;
        break;
      case 'End':
        nextIndex = workspaces.length - 1;
        break;
      default:
        return;
    }

    event.preventDefault();
    this.activateWorkspace(workspaces[nextIndex].workspaceId);
  }
}
