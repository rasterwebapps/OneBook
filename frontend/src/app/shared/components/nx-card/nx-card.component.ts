import { Component, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'nx-card',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
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
