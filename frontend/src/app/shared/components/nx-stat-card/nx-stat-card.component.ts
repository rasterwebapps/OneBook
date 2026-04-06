import { Component, ChangeDetectionStrategy, input } from '@angular/core';

@Component({
  selector: 'nx-stat-card',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="nx-stat-card" [class]="color()">
      <div class="stat-icon">{{ icon() }}</div>
      <div class="stat-label">{{ label() }}</div>
      <div class="stat-value">{{ value() }}</div>
      @if (trend()) {
        <div class="stat-trend" [class]="trendClass()">
          {{ trendArrow() }} {{ trend() }}
        </div>
      }
    </div>
  `,
})
export class NxStatCardComponent {
  readonly icon = input('📊');
  readonly label = input('');
  readonly value = input('');
  readonly trend = input('');
  readonly trendDirection = input<'up' | 'down' | 'neutral'>('neutral');
  readonly color = input('emerald');

  trendClass(): string { return this.trendDirection(); }
  trendArrow(): string {
    switch (this.trendDirection()) {
      case 'up': return '▲';
      case 'down': return '▼';
      default: return '→';
    }
  }
}
