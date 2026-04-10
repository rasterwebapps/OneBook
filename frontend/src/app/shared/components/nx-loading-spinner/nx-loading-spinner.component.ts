import { Component, ChangeDetectionStrategy, input } from '@angular/core';

@Component({
  selector: 'nx-loading-spinner',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="nx-loading-spinner" [attr.aria-label]="label()" role="status">
      <div class="nx-loading-spinner__ring">
        <svg viewBox="0 0 50 50" aria-hidden="true">
          <circle cx="25" cy="25" r="20" fill="none" stroke-width="4"
                  stroke="var(--nx-border)" />
          <circle cx="25" cy="25" r="20" fill="none" stroke-width="4"
                  stroke="var(--nx-emerald)" stroke-linecap="round"
                  class="nx-loading-spinner__arc" />
        </svg>
      </div>
      @if (label()) {
        <span class="nx-loading-spinner__label">{{ label() }}</span>
      }
      <span class="sr-only">{{ label() || 'Loading' }}</span>
    </div>
  `,
  styles: [`
    .nx-loading-spinner {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: var(--nx-space-8);
      gap: var(--nx-space-3);
    }

    .nx-loading-spinner__ring {
      width: 40px;
      height: 40px;
      animation: nx-spin 1s linear infinite;

      svg {
        width: 100%;
        height: 100%;
      }
    }

    .nx-loading-spinner__arc {
      stroke-dasharray: 90, 150;
      stroke-dashoffset: 0;
    }

    .nx-loading-spinner__label {
      font-size: var(--nx-text-sm);
      color: var(--nx-text-muted);
      font-weight: var(--nx-font-weight-medium);
    }

    @keyframes nx-spin {
      to { transform: rotate(360deg); }
    }
  `],
})
export class NxLoadingSpinnerComponent {
  readonly label = input('');
}
