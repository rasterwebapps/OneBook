import { Component, ChangeDetectionStrategy, input } from '@angular/core';

@Component({
  selector: 'nx-skeleton',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <span class="nx-skeleton" [style.width]="width()" [style.height]="height()" [style.display]="'block'"></span>
  `,
})
export class NxSkeletonComponent {
  readonly width = input('100%');
  readonly height = input('16px');
}
