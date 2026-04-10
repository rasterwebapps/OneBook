import { Component, ChangeDetectionStrategy, input } from '@angular/core';

@Component({
  selector: 'nx-page-header',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="nx-page-header">
      <div class="nx-page-header__left">
        <h2 class="nx-page-header__title">{{ title() }}</h2>
        @if (subtitle()) {
          <p class="nx-page-header__subtitle">{{ subtitle() }}</p>
        }
      </div>
      <div class="nx-page-header__actions">
        <ng-content></ng-content>
      </div>
    </div>
  `,
  styles: [`
    .nx-page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: var(--nx-space-5);
      flex-wrap: wrap;
      gap: var(--nx-space-3);
    }

    .nx-page-header__left {
      min-width: 0;
    }

    .nx-page-header__title {
      margin: 0;
      font-size: var(--nx-text-2xl);
      font-weight: var(--nx-font-weight-bold);
      color: var(--nx-text-primary);
      line-height: 1.3;
    }

    .nx-page-header__subtitle {
      margin: var(--nx-space-1) 0 0;
      color: var(--nx-text-muted);
      font-size: var(--nx-text-sm);
    }

    .nx-page-header__actions {
      display: flex;
      gap: var(--nx-space-2);
      align-items: center;
      flex-shrink: 0;
    }
  `],
})
export class NxPageHeaderComponent {
  readonly title = input('');
  readonly subtitle = input('');
}
