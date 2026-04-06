import { Component, ChangeDetectionStrategy, input } from '@angular/core';

@Component({
  selector: 'nx-badge',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <span class="nx-badge" [class]="variantClass()">
      <ng-content></ng-content>
    </span>
  `,
})
export class NxBadgeComponent {
  readonly variant = input<'success' | 'warning' | 'danger' | 'info' | 'neutral'>('neutral');

  variantClass(): string {
    return `nx-badge--${this.variant()}`;
  }
}
