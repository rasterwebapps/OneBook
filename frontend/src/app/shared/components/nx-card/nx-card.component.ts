import { Component, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'nx-card',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  // Slot order: [card-header], [card-body], unslotted fallback content, [card-footer]
  template: `
    <div class="nx-card">
      <ng-content select="[card-header]"></ng-content>
      <ng-content select="[card-body]"></ng-content>
      <ng-content></ng-content>
      <ng-content select="[card-footer]"></ng-content>
    </div>
  `,
})
export class NxCardComponent {}
