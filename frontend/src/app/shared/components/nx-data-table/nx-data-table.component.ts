import { Component, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'nx-data-table',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="nx-data-table-wrapper" style="overflow-x:auto;">
      <table class="nx-data-table">
        <ng-content></ng-content>
      </table>
    </div>
  `,
})
export class NxDataTableComponent {}
