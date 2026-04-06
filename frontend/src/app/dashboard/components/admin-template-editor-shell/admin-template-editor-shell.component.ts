import {
  Component,
  ChangeDetectionStrategy,
  input,
  signal,
  computed,
} from '@angular/core';
import {
  RoleDashboardPolicy,
  WorkspaceTemplate,
  CardLayout,
  GridRect,
  PublishConflictDetail,
} from '../../models/dashboard-layout.models';

/**
 * AdminTemplateEditorShellComponent — Admin-only move-only workspace layout editor.
 *
 * Allows administrators to rearrange card positions (move-only, no resize) for a
 * given role's workspace template. Only cards with lock='none' are movable.
 * Runtime drag libraries must NOT be bundled into runtime routes; this component
 * is guarded by adminGuard and lazy-loaded.
 *
 * On publish, sends PUT with If-Match header. Handles 412 conflict with a modal.
 *
 * Spec: docs/ux/role-based-dashboards.md §6
 *
 * TODO (implementation):
 *  - Inject RolePolicyStore to load/publish templates.
 *  - Implement pointer/keyboard drag for move-only grid interactions.
 *  - Implement overlap detection and snap-back on conflict.
 *  - Implement 412 conflict modal with Reload/Compare/Re-apply options.
 *  - Add publish validation (bounds check + overlap check).
 */
@Component({
  selector: 'app-admin-template-editor-shell',
  standalone: true,
  imports: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="editor-shell" role="main" aria-label="Admin Template Editor">

      <!-- Editor Toolbar -->
      <div class="editor-toolbar" role="toolbar" aria-label="Template editor actions">
        <span class="editor-title">
          Template Editor —
          <strong>{{ policy()?.role ?? '' }}</strong>
          @if (activeWorkspace()) {
            / {{ activeWorkspace()!.label }}
          }
        </span>

        <div class="editor-actions">
          <button
            class="btn btn-secondary"
            (click)="discardDraft()"
            [disabled]="!hasDraft()"
            aria-label="Discard unsaved changes"
          >
            Discard
          </button>
          <button
            class="btn btn-primary"
            (click)="publishTemplate()"
            [disabled]="!hasDraft() || publishing()"
            aria-label="Publish workspace template"
          >
            {{ publishing() ? 'Publishing…' : 'Publish' }}
          </button>
        </div>
      </div>

      <!-- Workspace Selector -->
      @if (workspaces().length > 1) {
        <div role="tablist" aria-label="Select workspace to edit" class="workspace-tab-strip">
          @for (ws of workspaces(); track ws.workspaceId) {
            <button
              role="tab"
              [attr.aria-selected]="activeWorkspaceId() === ws.workspaceId"
              [attr.tabindex]="activeWorkspaceId() === ws.workspaceId ? 0 : -1"
              class="workspace-tab"
              [class.active]="activeWorkspaceId() === ws.workspaceId"
              (click)="selectWorkspace(ws.workspaceId)"
              (keydown)="onTabKeydown($event, ws.workspaceId)"
            >
              {{ ws.label }}
            </button>
          }
        </div>
      }

      <!-- Editor Canvas -->
      @if (activeWorkspace(); as ws) {
        <div
          class="editor-canvas"
          aria-label="Card layout editor — move cards to rearrange"
          [attr.aria-busy]="publishing()"
        >
          <div class="editor-grid">
            @for (card of draftCards(); track card.cardId) {
              <div
                class="editor-card"
                [class.movable]="card.lock === 'none'"
                [class.locked]="card.lock === 'locked'"
                [style.grid-column]="card.rect.col + ' / span ' + card.rect.w"
                [style.grid-row]="card.rect.row + ' / span ' + card.rect.h"
                [attr.aria-label]="card.moduleKey + (card.lock === 'locked' ? ' (locked)' : ' (movable)')"
                [attr.tabindex]="card.lock === 'none' ? 0 : -1"
                (keydown)="onCardKeydown($event, card)"
              >
                <div class="card-header">
                  <span class="card-module-key">{{ card.moduleKey }}</span>
                  @if (card.lock === 'locked') {
                    <span class="lock-badge" aria-label="Locked — cannot be moved" title="Locked">🔒</span>
                  }
                </div>
                <div class="card-hint">
                  @if (card.lock === 'none') {
                    <span class="hint-text">Arrow keys to move · Esc to revert</span>
                  } @else {
                    <span class="hint-text locked-hint">Position is fixed by policy</span>
                  }
                </div>
              </div>
            }
          </div>

          @if (validationErrors().length > 0) {
            <div role="alert" aria-live="assertive" class="validation-errors">
              @for (err of validationErrors(); track err) {
                <p class="validation-error">⚠ {{ err }}</p>
              }
            </div>
          }
        </div>
      }

      <!-- Publish Conflict Modal (412 Precondition Failed) -->
      @if (publishConflict()) {
        <div
          role="dialog"
          aria-modal="true"
          aria-labelledby="conflict-dialog-title"
          class="conflict-modal-overlay"
        >
          <div class="conflict-modal">
            <h2 id="conflict-dialog-title" class="conflict-title">
              Template Changed — Conflict Detected
            </h2>
            <p class="conflict-description">
              This template was updated by another admin while you were editing.
            </p>
            <dl class="conflict-details">
              <dt>You started editing at</dt>
              <dd>{{ editingStartedAtIso() }}</dd>
              <dt>Latest template published at</dt>
              <dd>{{ publishConflict()!.serverPublishedAtIso }}</dd>
              <dt>Server policy version</dt>
              <dd>v{{ publishConflict()!.serverPolicyVersion }}</dd>
            </dl>
            <div class="conflict-actions">
              <button class="btn btn-secondary" (click)="reloadLatest()">
                Reload Latest (discard my draft)
              </button>
              <button class="btn btn-secondary" (click)="compareVersions()">
                Compare Changes
              </button>
              <button class="btn btn-primary" (click)="reapplyAddons()">
                Re-apply My Add-ons Changes
              </button>
            </div>
          </div>
        </div>
      }

      @if (publishError()) {
        <div role="alert" aria-live="assertive" class="publish-error">
          {{ publishError() }}
        </div>
      }
    </div>
  `,
  styles: [`
    :host { display: block; }

    .editor-shell {
      display: flex;
      flex-direction: column;
      height: 100%;
      background: var(--color-surface-editor, #f9fafb);
    }

    /* ── Toolbar ── */
    .editor-toolbar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0.75rem 1.5rem;
      background: var(--color-surface, #fff);
      border-bottom: 1px solid var(--color-border, #e5e7eb);
    }

    .editor-title { font-size: 0.9rem; color: var(--color-text, #111827); }
    .editor-actions { display: flex; gap: 0.5rem; }

    /* ── Tabs ── */
    .workspace-tab-strip {
      display: flex;
      gap: 0.25rem;
      padding: 0 1.5rem;
      background: var(--color-surface, #fff);
      border-bottom: 1px solid var(--color-border, #e5e7eb);
    }

    .workspace-tab {
      background: none;
      border: none;
      padding: 0.6rem 1rem;
      cursor: pointer;
      font-size: 0.875rem;
      color: var(--color-text-secondary, #6b7280);
      border-bottom: 2px solid transparent;
    }

    .workspace-tab.active { color: var(--color-primary, #2563eb); border-bottom-color: var(--color-primary, #2563eb); }
    .workspace-tab:focus-visible { outline: 2px solid var(--color-primary, #2563eb); outline-offset: 2px; border-radius: 4px; }

    /* ── Canvas ── */
    .editor-canvas { flex: 1; padding: 1.5rem; overflow: auto; }

    .editor-grid {
      display: grid;
      grid-template-columns: repeat(12, 1fr);
      gap: 0.75rem;
      min-height: 480px;
      background:
        repeating-linear-gradient(
          to right,
          var(--color-grid-line, rgba(99,102,241,0.06)) 0,
          var(--color-grid-line, rgba(99,102,241,0.06)) 1px,
          transparent 1px,
          transparent calc(100% / 12)
        );
    }

    /* ── Cards ── */
    .editor-card {
      background: var(--color-surface, #fff);
      border: 1px solid var(--color-border, #e5e7eb);
      border-radius: 8px;
      padding: 0.75rem;
      min-height: 100px;
      position: relative;
      transition: box-shadow 0.15s;
    }

    .editor-card.movable {
      cursor: grab;
      border-color: var(--color-primary-light, #93c5fd);
    }

    .editor-card.movable:focus-visible {
      outline: 2px solid var(--color-primary, #2563eb);
      outline-offset: 2px;
      box-shadow: 0 0 0 4px rgba(37,99,235,0.12);
    }

    .editor-card.locked { opacity: 0.75; cursor: not-allowed; }

    .card-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 0.5rem; }

    .card-module-key { font-family: monospace; font-size: 0.75rem; color: var(--color-text-secondary, #6b7280); }

    .lock-badge { font-size: 0.9rem; }

    .hint-text { font-size: 0.7rem; color: var(--color-text-tertiary, #9ca3af); }
    .locked-hint { font-style: italic; }

    /* ── Validation errors ── */
    .validation-errors { margin-top: 1rem; }
    .validation-error {
      color: var(--color-error, #ef4444);
      font-size: 0.8rem;
      margin: 0.25rem 0;
    }

    /* ── Conflict modal ── */
    .conflict-modal-overlay {
      position: fixed;
      inset: 0;
      background: rgba(0,0,0,0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
    }

    .conflict-modal {
      background: var(--color-surface, #fff);
      border-radius: 12px;
      padding: 2rem;
      max-width: 480px;
      width: 90%;
      box-shadow: 0 20px 60px rgba(0,0,0,0.3);
    }

    .conflict-title { font-size: 1.1rem; margin: 0 0 0.5rem; color: var(--color-error, #ef4444); }
    .conflict-description { font-size: 0.875rem; color: var(--color-text-secondary, #6b7280); margin: 0 0 1rem; }

    .conflict-details { font-size: 0.8rem; margin: 0 0 1.5rem; }
    .conflict-details dt { font-weight: 600; color: var(--color-text, #111827); margin-top: 0.5rem; }
    .conflict-details dd { margin: 0 0 0.25rem 0; color: var(--color-text-secondary, #6b7280); }

    .conflict-actions { display: flex; flex-direction: column; gap: 0.5rem; }

    /* ── Buttons ── */
    .btn {
      padding: 0.5rem 1rem;
      border-radius: 6px;
      border: none;
      cursor: pointer;
      font-size: 0.875rem;
      transition: opacity 0.15s;
    }

    .btn:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn:focus-visible { outline: 2px solid var(--color-primary, #2563eb); outline-offset: 2px; }

    .btn-primary { background: var(--color-primary, #2563eb); color: #fff; }
    .btn-primary:hover:not(:disabled) { background: var(--color-primary-dark, #1d4ed8); }

    .btn-secondary {
      background: var(--color-surface, #fff);
      color: var(--color-text, #111827);
      border: 1px solid var(--color-border, #e5e7eb);
    }

    .btn-secondary:hover:not(:disabled) { background: var(--color-surface-raised, #f3f4f6); }

    .publish-error {
      margin: 1rem 1.5rem;
      padding: 0.75rem 1rem;
      background: var(--color-error-bg, #fef2f2);
      border: 1px solid var(--color-error-border, #fecaca);
      border-radius: 6px;
      color: var(--color-error, #ef4444);
      font-size: 0.875rem;
    }
  `],
})
export class AdminTemplateEditorShellComponent {
  /** Role dashboard policy loaded by the parent or store. */
  readonly policy = input<RoleDashboardPolicy | null>(null);

  readonly publishing = signal(false);
  readonly publishError = signal<string | null>(null);
  readonly publishConflict = signal<PublishConflictDetail | null>(null);
  readonly validationErrors = signal<string[]>([]);

  /** ISO timestamp captured when editing started (used in conflict modal). */
  readonly editingStartedAtIso = signal<string>(new Date().toISOString());

  readonly activeWorkspaceId = signal<string | null>(null);

  readonly workspaces = computed<WorkspaceTemplate[]>(() => this.policy()?.workspaces ?? []);

  readonly activeWorkspace = computed<WorkspaceTemplate | null>(() => {
    const id = this.activeWorkspaceId();
    return this.workspaces().find(w => w.workspaceId === id) ?? this.workspaces()[0] ?? null;
  });

  /** Draft cards: mutable copy of the active workspace cards for editing. */
  readonly draftCards = signal<CardLayout[]>([]);

  readonly hasDraft = computed(() => this.draftCards().length > 0);

  selectWorkspace(workspaceId: string): void {
    this.activeWorkspaceId.set(workspaceId);
    const ws = this.workspaces().find(w => w.workspaceId === workspaceId);
    this.draftCards.set(ws ? [...ws.cards.map(c => ({ ...c, rect: { ...c.rect } }))] : []);
    this.validationErrors.set([]);
    this.publishError.set(null);
    this.editingStartedAtIso.set(new Date().toISOString());
  }

  /** ARIA tab strip keyboard navigation. */
  onTabKeydown(event: KeyboardEvent, currentId: string): void {
    const workspaces = this.workspaces();
    const idx = workspaces.findIndex(w => w.workspaceId === currentId);
    let nextIdx = idx;

    switch (event.key) {
      case 'ArrowRight': nextIdx = (idx + 1) % workspaces.length; break;
      case 'ArrowLeft': nextIdx = (idx - 1 + workspaces.length) % workspaces.length; break;
      case 'Home': nextIdx = 0; break;
      case 'End': nextIdx = workspaces.length - 1; break;
      default: return;
    }

    event.preventDefault();
    this.selectWorkspace(workspaces[nextIdx].workspaceId);
  }

  /**
   * Keyboard-based card movement.
   * Arrow keys move 1 column/row; Shift+Arrow moves 3.
   * Escape reverts to last published position.
   */
  onCardKeydown(event: KeyboardEvent, card: CardLayout): void {
    if (card.lock === 'locked') return;

    const step = event.shiftKey ? 3 : 1;
    let { col, row } = card.rect;

    switch (event.key) {
      case 'ArrowRight': col = Math.min(col + step, 12 - card.rect.w + 1); break;
      case 'ArrowLeft':  col = Math.max(col - step, 1); break;
      case 'ArrowDown':  row = row + step; break;
      case 'ArrowUp':    row = Math.max(row - step, 1); break;
      case 'Escape':     this.revertCard(card.cardId); return;
      default: return;
    }

    event.preventDefault();
    this.moveCard(card.cardId, { ...card.rect, col, row });
  }

  moveCard(cardId: string, newRect: GridRect): void {
    const current = this.draftCards();
    const targetIndex = current.findIndex(c => c.cardId === cardId);
    if (targetIndex === -1) return;

    const updated = current.map(c => c.cardId === cardId ? { ...c, rect: newRect } : c);

    if (this.hasOverlap(updated)) {
      // Revert: do not apply the move
      return;
    }

    this.draftCards.set(updated);
  }

  revertCard(cardId: string): void {
    const original = this.activeWorkspace()?.cards.find(c => c.cardId === cardId);
    if (!original) return;
    this.draftCards.update(cards =>
      cards.map(c => c.cardId === cardId ? { ...c, rect: { ...original.rect } } : c)
    );
  }

  discardDraft(): void {
    const ws = this.activeWorkspace();
    this.draftCards.set(ws ? [...ws.cards.map(c => ({ ...c, rect: { ...c.rect } }))] : []);
    this.validationErrors.set([]);
    this.publishError.set(null);
  }

  publishTemplate(): void {
    const errors = this.validateDraft();
    if (errors.length > 0) {
      this.validationErrors.set(errors);
      return;
    }
    this.validationErrors.set([]);
    // TODO: call RolePolicyStore.publishTemplate() with If-Match header.
    // Handle 412 by setting publishConflict signal.
    this.publishing.set(true);
    this.publishError.set('Publish not yet wired to RolePolicyStore — see TODO in component.');
    this.publishing.set(false);
  }

  reloadLatest(): void {
    this.publishConflict.set(null);
    this.discardDraft();
    // TODO: trigger RolePolicyStore.loadRolePolicy() revalidation.
  }

  compareVersions(): void {
    // TODO: open diff view showing moved/added/removed cards.
    this.publishConflict.set(null);
  }

  reapplyAddons(): void {
    // TODO: replay add-ons zone mutations onto the latest published template.
    this.publishConflict.set(null);
  }

  // ---------------------------------------------------------------------------
  // Private helpers
  // ---------------------------------------------------------------------------

  private hasOverlap(cards: CardLayout[]): boolean {
    for (let i = 0; i < cards.length; i++) {
      for (let j = i + 1; j < cards.length; j++) {
        if (this.rectsOverlap(cards[i].rect, cards[j].rect)) return true;
      }
    }
    return false;
  }

  private rectsOverlap(a: GridRect, b: GridRect): boolean {
    const aRight  = a.col + a.w;
    const bRight  = b.col + b.w;
    const aBottom = a.row + a.h;
    const bBottom = b.row + b.h;
    return a.col < bRight && aRight > b.col && a.row < bBottom && aBottom > b.row;
  }

  private validateDraft(): string[] {
    const errors: string[] = [];
    const cards = this.draftCards();

    for (const card of cards) {
      const { col, row, w, h } = card.rect;
      if (col < 1 || col + w > 13) {
        errors.push(`Card "${card.moduleKey}" exceeds column bounds (col=${col}, w=${w}).`);
      }
      if (row < 1) {
        errors.push(`Card "${card.moduleKey}" has invalid row (row=${row}).`);
      }
      if (h < 1) {
        errors.push(`Card "${card.moduleKey}" has invalid height (h=${h}).`);
      }
    }

    if (this.hasOverlap(cards)) {
      errors.push('Two or more cards overlap. Resolve overlaps before publishing.');
    }

    return errors;
  }
}
