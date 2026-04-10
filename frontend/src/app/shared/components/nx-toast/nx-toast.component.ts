import {
  Component, ChangeDetectionStrategy, Injectable,
  signal, inject, OnDestroy,
} from '@angular/core';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface ToastMessage {
  id: number;
  message: string;
  type: ToastType;
  duration: number;
}

/**
 * Injectable service for showing toast notifications.
 * Inject this service and call show(), success(), error(), warning(), or info().
 */
@Injectable({ providedIn: 'root' })
export class NxToastService {
  private nextId = 0;
  readonly toasts = signal<ToastMessage[]>([]);

  show(message: string, type: ToastType = 'info', duration = 4000): void {
    const id = this.nextId++;
    this.toasts.update(t => [...t, { id, message, type, duration }]);

    if (duration > 0) {
      setTimeout(() => this.dismiss(id), duration);
    }
  }

  success(message: string, duration = 4000): void {
    this.show(message, 'success', duration);
  }

  error(message: string, duration = 6000): void {
    this.show(message, 'error', duration);
  }

  warning(message: string, duration = 5000): void {
    this.show(message, 'warning', duration);
  }

  info(message: string, duration = 4000): void {
    this.show(message, 'info', duration);
  }

  dismiss(id: number): void {
    this.toasts.update(t => t.filter(toast => toast.id !== id));
  }
}

/**
 * Toast container component. Place once in the app shell (e.g., app.component.html).
 * Renders all active toast notifications as slide-in cards.
 */
@Component({
  selector: 'nx-toast',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="nx-toast-container" aria-live="polite" aria-atomic="false">
      @for (toast of toastService.toasts(); track toast.id) {
        <div class="nx-toast" [class]="'nx-toast--' + toast.type" role="alert">
          <span class="nx-toast__icon">{{ iconFor(toast.type) }}</span>
          <span class="nx-toast__message">{{ toast.message }}</span>
          <button class="nx-toast__close" (click)="toastService.dismiss(toast.id)"
                  aria-label="Dismiss notification" type="button">✕</button>
        </div>
      }
    </div>
  `,
  styles: [`
    .nx-toast-container {
      position: fixed;
      top: calc(var(--nx-header-height, 52px) + var(--nx-space-3));
      right: var(--nx-space-4);
      z-index: 700;
      display: flex;
      flex-direction: column;
      gap: var(--nx-space-2);
      max-width: 400px;
      pointer-events: none;
    }

    .nx-toast {
      display: flex;
      align-items: flex-start;
      gap: var(--nx-space-2);
      padding: var(--nx-space-3) var(--nx-space-4);
      background: var(--nx-bg-card);
      border: 1px solid var(--nx-border);
      border-radius: var(--nx-radius-md);
      box-shadow: var(--nx-shadow-lg);
      animation: nx-toast-slide-in 0.25s ease-out;
      pointer-events: auto;
      border-left: 3px solid var(--nx-info);
    }

    .nx-toast--success { border-left-color: var(--nx-success); }
    .nx-toast--error { border-left-color: var(--nx-danger); }
    .nx-toast--warning { border-left-color: var(--nx-warning); }
    .nx-toast--info { border-left-color: var(--nx-info); }

    .nx-toast__icon {
      flex-shrink: 0;
      font-size: 1rem;
      line-height: 1.3;
    }

    .nx-toast__message {
      flex: 1;
      font-size: var(--nx-text-sm);
      color: var(--nx-text-primary);
      line-height: 1.4;
    }

    .nx-toast__close {
      flex-shrink: 0;
      background: none;
      border: none;
      cursor: pointer;
      color: var(--nx-text-muted);
      font-size: var(--nx-text-xs);
      padding: 2px 4px;
      border-radius: var(--nx-radius-sm);
      transition: color var(--nx-transition-fast);

      &:hover {
        color: var(--nx-text-primary);
      }
    }

    @keyframes nx-toast-slide-in {
      from {
        opacity: 0;
        transform: translateX(20px);
      }
      to {
        opacity: 1;
        transform: translateX(0);
      }
    }
  `],
})
export class NxToastComponent {
  readonly toastService = inject(NxToastService);

  iconFor(type: ToastType): string {
    switch (type) {
      case 'success': return '✓';
      case 'error': return '✕';
      case 'warning': return '⚠';
      case 'info': return 'ℹ';
    }
  }
}
