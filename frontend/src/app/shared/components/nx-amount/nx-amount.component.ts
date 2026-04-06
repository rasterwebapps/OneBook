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
  /** Accept string (from backend BigDecimal serialization) or number. */
  readonly amount = input<string | number>(0);
  readonly type = input<'debit' | 'credit' | 'auto'>('auto');

  private numericValue = computed(() => {
    const v = this.amount();
    return typeof v === 'string' ? parseFloat(v) : v;
  });

  isDebit = computed(() => this.type() === 'debit' || (this.type() === 'auto' && this.numericValue() < 0));
  isCredit = computed(() => this.type() === 'credit' || (this.type() === 'auto' && this.numericValue() > 0));
  formatted = computed(() => {
    const abs = Math.abs(this.numericValue());
    return abs.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  });
}
