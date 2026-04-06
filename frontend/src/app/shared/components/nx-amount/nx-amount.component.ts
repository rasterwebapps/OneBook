import { Component, ChangeDetectionStrategy, input, computed } from '@angular/core';

@Component({
  selector: 'nx-amount',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <span class="nx-amount" [class.debit]="isDebit()" [class.credit]="isCredit()">
      ₹{{ formatted() }}
    </span>
  `,
})
export class NxAmountComponent {
  readonly amount = input(0);
  readonly type = input<'debit' | 'credit' | 'auto'>('auto');

  isDebit = computed(() => this.type() === 'debit' || (this.type() === 'auto' && this.amount() < 0));
  isCredit = computed(() => this.type() === 'credit' || (this.type() === 'auto' && this.amount() > 0));
  formatted = computed(() => {
    const abs = Math.abs(this.amount());
    return abs.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  });
}
