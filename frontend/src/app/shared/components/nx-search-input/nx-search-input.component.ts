import {
  Component, ChangeDetectionStrategy, input, output,
  signal, effect, OnDestroy,
} from '@angular/core';

@Component({
  selector: 'nx-search-input',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="nx-search-input" [class.focused]="focused()">
      <svg class="nx-search-input__icon" viewBox="0 0 24 24" fill="none"
           stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
           aria-hidden="true">
        <circle cx="11" cy="11" r="8" />
        <line x1="21" y1="21" x2="16.65" y2="16.65" />
      </svg>
      <input
        type="search"
        class="nx-search-input__field"
        [placeholder]="placeholder()"
        [value]="value()"
        [attr.aria-label]="placeholder()"
        (input)="onInput($event)"
        (focus)="focused.set(true)"
        (blur)="focused.set(false)"
      />
      @if (value()) {
        <button class="nx-search-input__clear" (click)="clear()"
                aria-label="Clear search" type="button">✕</button>
      }
    </div>
  `,
  styles: [`
    .nx-search-input {
      display: flex;
      align-items: center;
      gap: var(--nx-space-2);
      background: var(--nx-bg-card);
      border: 1px solid var(--nx-border);
      border-radius: var(--nx-radius-sm);
      padding: 0 var(--nx-space-3);
      height: 36px;
      transition: border-color var(--nx-transition-fast), box-shadow var(--nx-transition-fast);
      min-width: 200px;

      &.focused {
        border-color: var(--nx-emerald);
        box-shadow: 0 0 0 3px var(--nx-emerald-glow);
      }
    }

    .nx-search-input__icon {
      width: 14px;
      height: 14px;
      color: var(--nx-text-muted);
      flex-shrink: 0;
    }

    .nx-search-input__field {
      flex: 1;
      border: none;
      background: transparent;
      font-size: var(--nx-text-sm);
      font-family: var(--nx-font-primary);
      color: var(--nx-text-primary);
      outline: none;
      width: 100%;
      min-width: 0;

      &::placeholder {
        color: var(--nx-text-muted);
      }

      &::-webkit-search-cancel-button {
        display: none;
      }
    }

    .nx-search-input__clear {
      background: none;
      border: none;
      cursor: pointer;
      color: var(--nx-text-muted);
      font-size: var(--nx-text-xs);
      padding: 2px 4px;
      border-radius: var(--nx-radius-sm);
      transition: color var(--nx-transition-fast), background var(--nx-transition-fast);
      flex-shrink: 0;

      &:hover {
        color: var(--nx-text-primary);
        background: rgba(0, 0, 0, 0.06);
      }
    }
  `],
})
export class NxSearchInputComponent implements OnDestroy {
  readonly placeholder = input('Search…');
  readonly value = input('');
  readonly debounceMs = input(300);
  readonly searchChange = output<string>();

  readonly focused = signal(false);

  private debounceTimer: ReturnType<typeof setTimeout> | null = null;

  constructor() {
    // Emit initial value changes from parent
    effect(() => {
      const v = this.value();
      // value input changes are not debounced
    });
  }

  onInput(event: Event): void {
    const val = (event.target as HTMLInputElement).value;
    if (this.debounceTimer) {
      clearTimeout(this.debounceTimer);
    }
    this.debounceTimer = setTimeout(() => {
      this.searchChange.emit(val);
    }, this.debounceMs());
  }

  clear(): void {
    if (this.debounceTimer) {
      clearTimeout(this.debounceTimer);
    }
    this.searchChange.emit('');
  }

  ngOnDestroy(): void {
    if (this.debounceTimer) {
      clearTimeout(this.debounceTimer);
    }
  }
}
