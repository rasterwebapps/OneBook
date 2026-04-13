import { Component, ChangeDetectionStrategy, signal, computed } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import {
  NxPageHeaderComponent,
  NxSearchInputComponent,
  NxStatusBadgeComponent,
  NxEmptyStateComponent,
  NxLoadingSpinnerComponent,
  NxStatCardComponent,
} from '../../../shared/components';
import { PaymentAdvice, PaymentAdviceStatus } from '../../models/advance.models';

/**
 * Payment Advice List Component
 *
 * Lists all pending and paid reimbursement payment advices.
 */
@Component({
  selector: 'app-payment-advice-list',
  standalone: true,
  imports: [
    DecimalPipe,
    NxPageHeaderComponent,
    NxSearchInputComponent,
    NxStatusBadgeComponent,
    NxEmptyStateComponent,
    NxLoadingSpinnerComponent,
    NxStatCardComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './payment-advice-list.component.html',
  styleUrl: './payment-advice-list.component.scss',
})
export class PaymentAdviceListComponent {
  readonly loading = signal(false);
  readonly searchQuery = signal('');
  readonly activeTab = signal<'pending' | 'paid'>('pending');

  readonly paymentAdvices = signal<PaymentAdvice[]>([
    {
      id: 201,
      tenantId: 'tenant-001',
      employeeId: 1001,
      employeeName: 'Rajesh Kumar',
      departmentId: 1,
      departmentName: 'Engineering',
      amount: 3000,
      expenseVoucherId: 502,
      status: 'PENDING_PAYMENT',
      createdAt: '2026-04-10T10:00:00Z',
      updatedAt: '2026-04-10T10:00:00Z',
    },
    {
      id: 202,
      tenantId: 'tenant-001',
      employeeId: 2001,
      employeeName: 'Priya Sharma',
      departmentId: 1,
      departmentName: 'Engineering',
      amount: 1500,
      expenseVoucherId: 503,
      status: 'PENDING_PAYMENT',
      createdAt: '2026-04-09T14:00:00Z',
      updatedAt: '2026-04-09T14:00:00Z',
    },
    {
      id: 200,
      tenantId: 'tenant-001',
      employeeId: 2002,
      employeeName: 'Amit Verma',
      departmentId: 1,
      departmentName: 'Engineering',
      amount: 5000,
      expenseVoucherId: 500,
      status: 'PAID',
      paymentVoucherId: 7001,
      paidBy: 'accounts.user',
      paidAt: '2026-04-08T16:00:00Z',
      createdAt: '2026-04-07T10:00:00Z',
      updatedAt: '2026-04-08T16:00:00Z',
    },
  ]);

  readonly filteredAdvices = computed(() => {
    const q = this.searchQuery().toLowerCase();
    const tab = this.activeTab();
    let advices = this.paymentAdvices();

    // Filter by tab
    if (tab === 'pending') {
      advices = advices.filter(a => a.status === 'PENDING_PAYMENT');
    } else {
      advices = advices.filter(a => a.status === 'PAID');
    }

    // Filter by search
    if (q) {
      advices = advices.filter(a =>
        a.employeeName?.toLowerCase().includes(q) ||
        a.departmentName?.toLowerCase().includes(q)
      );
    }

    return advices;
  });

  readonly totalPending = computed(() =>
    this.paymentAdvices()
      .filter(a => a.status === 'PENDING_PAYMENT')
      .reduce((sum, a) => sum + a.amount, 0)
  );

  readonly pendingCount = computed(() =>
    this.paymentAdvices().filter(a => a.status === 'PENDING_PAYMENT').length
  );

  // Actions
  setTab(tab: 'pending' | 'paid'): void {
    this.activeTab.set(tab);
  }

  updateSearch(value: string): void {
    this.searchQuery.set(value);
  }

  markAsPaid(id: number): void {
    this.paymentAdvices.update(advices =>
      advices.map(a => {
        if (a.id !== id) return a;
        return {
          ...a,
          status: 'PAID' as PaymentAdviceStatus,
          paidBy: 'current.user',
          paidAt: new Date().toISOString(),
          paymentVoucherId: Date.now(),
        };
      })
    );
  }

  // Helpers
  statusLabel(status: PaymentAdviceStatus): string {
    return status === 'PENDING_PAYMENT' ? 'Pending Payment' : 'Paid';
  }

  formatDate(dateStr: string | undefined): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  }
}
