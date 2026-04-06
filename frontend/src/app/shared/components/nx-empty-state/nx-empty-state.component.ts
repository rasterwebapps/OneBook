import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';

@Component({
  selector: 'nx-empty-state',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="nx-empty-state">
      <div class="empty-icon">{{ icon() }}</div>
      <div class="empty-title">{{ title() }}</div>
      @if (description()) {
        <p class="empty-desc">{{ description() }}</p>
      }
      @if (actionLabel()) {
        <button class="nx-btn nx-btn--emerald" (click)="action.emit()">{{ actionLabel() }}</button>
      }
    </div>
  `,
})
export class NxEmptyStateComponent {
  readonly icon = input('📭');
  readonly title = input('No data found');
  readonly description = input('');
  readonly actionLabel = input('');
  readonly action = output<void>();
}
