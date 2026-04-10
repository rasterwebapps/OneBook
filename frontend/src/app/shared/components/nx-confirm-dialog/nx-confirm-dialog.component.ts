import {
  Component, ChangeDetectionStrategy, Injectable,
  signal, inject,
} from '@angular/core';

export interface ConfirmDialogConfig {
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  confirmVariant?: 'danger' | 'emerald' | 'purple' | 'amber';
}

/**
 * Injectable service for showing confirmation dialogs.
 * Returns a Promise<boolean> that resolves true if confirmed, false if cancelled.
 */
@Injectable({ providedIn: 'root' })
export class NxConfirmDialogService {
  readonly visible = signal(false);
  readonly config = signal<ConfirmDialogConfig>({
    title: 'Confirm',
    message: 'Are you sure?',
  });

  private resolveRef?: (value: boolean) => void;

  confirm(config: ConfirmDialogConfig): Promise<boolean> {
    this.config.set(config);
    this.visible.set(true);
    return new Promise<boolean>((resolve) => {
      this.resolveRef = resolve;
    });
  }

  /** Called by the dialog component */
  resolve(value: boolean): void {
    this.visible.set(false);
    if (this.resolveRef) {
      this.resolveRef(value);
      this.resolveRef = undefined;
    }
  }
}

/**
 * Confirm dialog component. Place once in the app shell.
 * Uses the NxConfirmDialogService to show/hide.
 */
@Component({
  selector: 'nx-confirm-dialog',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (dialogService.visible()) {
      <div class="nx-confirm-overlay" (click)="onCancel()" role="dialog"
           aria-modal="true" [attr.aria-label]="dialogService.config().title">
        <div class="nx-confirm-dialog" (click)="$event.stopPropagation()">
          <h3 class="nx-confirm-dialog__title">{{ dialogService.config().title }}</h3>
          <p class="nx-confirm-dialog__message">{{ dialogService.config().message }}</p>
          <div class="nx-confirm-dialog__actions">
            <button class="nx-btn" (click)="onCancel()" type="button">
              {{ dialogService.config().cancelLabel || 'Cancel' }}
            </button>
            <button class="nx-btn" [class]="confirmBtnClass()" (click)="onConfirm()" type="button">
              {{ dialogService.config().confirmLabel || 'Confirm' }}
            </button>
          </div>
        </div>
      </div>
    }
  `,
  styles: [`
    .nx-confirm-overlay {
      position: fixed;
      inset: 0;
      background: rgba(0, 0, 0, 0.45);
      z-index: var(--nx-z-modal, 400);
      display: flex;
      align-items: center;
      justify-content: center;
      animation: nx-overlay-fade 0.15s ease-out;
    }

    .nx-confirm-dialog {
      background: var(--nx-bg-card);
      border: 1px solid var(--nx-border);
      border-radius: var(--nx-radius-xl);
      box-shadow: var(--nx-shadow-xl);
      padding: var(--nx-space-6);
      max-width: 440px;
      width: 90%;
      animation: nx-dialog-scale 0.15s ease-out;
    }

    .nx-confirm-dialog__title {
      margin: 0 0 var(--nx-space-2);
      font-size: var(--nx-text-lg);
      font-weight: var(--nx-font-weight-semibold);
      color: var(--nx-text-primary);
    }

    .nx-confirm-dialog__message {
      margin: 0 0 var(--nx-space-5);
      font-size: var(--nx-text-sm);
      color: var(--nx-text-secondary);
      line-height: 1.5;
    }

    .nx-confirm-dialog__actions {
      display: flex;
      justify-content: flex-end;
      gap: var(--nx-space-2);
    }

    @keyframes nx-overlay-fade {
      from { opacity: 0; }
      to { opacity: 1; }
    }

    @keyframes nx-dialog-scale {
      from {
        opacity: 0;
        transform: scale(0.95);
      }
      to {
        opacity: 1;
        transform: scale(1);
      }
    }
  `],
})
export class NxConfirmDialogComponent {
  readonly dialogService = inject(NxConfirmDialogService);

  confirmBtnClass(): string {
    const variant = this.dialogService.config().confirmVariant ?? 'danger';
    return `nx-btn--${variant}`;
  }

  onConfirm(): void {
    this.dialogService.resolve(true);
  }

  onCancel(): void {
    this.dialogService.resolve(false);
  }
}
