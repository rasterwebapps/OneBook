import { Component, ChangeDetectionStrategy, signal, computed } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import {
  NxPageHeaderComponent,
  NxStatusBadgeComponent,
  NxEmptyStateComponent,
  NxLoadingSpinnerComponent,
  NxStatCardComponent,
} from '../../../shared/components';
import {
  ExpenseVoucher,
  ExpenseVoucherStatus,
  CreateExpenseVoucherRequest,
  EmployeeAdvance,
} from '../../models/advance.models';

/**
 * Expense Voucher Component
 *
 * For employees to submit expense vouchers and settle them against
 * their outstanding advances.
 */
@Component({
  selector: 'app-expense-voucher',
  standalone: true,
  imports: [
    DecimalPipe,
    NxPageHeaderComponent,
    NxStatusBadgeComponent,
    NxEmptyStateComponent,
    NxLoadingSpinnerComponent,
    NxStatCardComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './expense-voucher.component.html',
  styleUrl: './expense-voucher.component.scss',
})
export class ExpenseVoucherComponent {
  // State
  readonly loading = signal(false);
  readonly showCreateModal = signal(false);
  readonly activeTab = signal<'my-expenses' | 'create'>('my-expenses');

  // Form state
  readonly newExpense = signal<CreateExpenseVoucherRequest>({
    employeeId: 1001,
    departmentId: 1,
    totalAmount: 0,
    description: '',
    expenseDate: new Date().toISOString().split('T')[0],
    advanceIdsToSettle: [],
  });

  // Outstanding advances available for settlement
  readonly outstandingAdvances = signal<EmployeeAdvance[]>([
    {
      id: 1,
      tenantId: 'tenant-001',
      employeeId: 1001,
      employeeName: 'Rajesh Kumar',
      departmentId: 1,
      departmentName: 'Engineering',
      amount: 15000,
      purpose: 'Client visit to Mumbai',
      status: 'DISBURSED',
      requestedDate: '2026-04-01',
      hodApprovalRequired: true,
      hodApprovedBy: 'Suresh Singh',
      ceoApprovalRequired: true,
      ceoApprovedBy: 'Amit Patel',
      mdApprovalRequired: false,
      overrideFlag: false,
      settledAmount: 12500,
      outstandingAmount: 2500,
      disbursedAt: '2026-04-03',
      createdBy: 'rajesh.kumar',
      createdAt: '2026-04-01T09:00:00Z',
      updatedAt: '2026-04-03T09:00:00Z',
    },
  ]);

  // Selected advances for settlement
  readonly selectedAdvanceIds = signal<number[]>([]);

  // My expense vouchers
  readonly myExpenseVouchers = signal<ExpenseVoucher[]>([
    {
      id: 501,
      tenantId: 'tenant-001',
      employeeId: 1001,
      employeeName: 'Rajesh Kumar',
      departmentId: 1,
      departmentName: 'Engineering',
      totalAmount: 12500,
      description: 'Mumbai client visit - hotel, meals, local transport',
      expenseDate: '2026-04-06',
      status: 'SETTLED',
      advanceSettlementAmount: 12500,
      reimbursementAmount: 0,
      linkedAdvanceIds: [1],
      createdBy: 'rajesh.kumar',
      createdAt: '2026-04-06T15:00:00Z',
      updatedAt: '2026-04-06T16:00:00Z',
      approvedBy: 'suresh.singh',
      approvedAt: '2026-04-06T16:00:00Z',
    },
    {
      id: 502,
      tenantId: 'tenant-001',
      employeeId: 1001,
      employeeName: 'Rajesh Kumar',
      departmentId: 1,
      departmentName: 'Engineering',
      totalAmount: 5500,
      description: 'Office supplies and stationery',
      expenseDate: '2026-04-10',
      status: 'SUBMITTED',
      advanceSettlementAmount: 2500,
      reimbursementAmount: 3000,
      linkedAdvanceIds: [1],
      paymentAdviceId: 201,
      createdBy: 'rajesh.kumar',
      createdAt: '2026-04-10T10:00:00Z',
      updatedAt: '2026-04-10T10:00:00Z',
    },
  ]);

  // Computed
  readonly totalOutstandingAdvance = computed(() =>
    this.outstandingAdvances().reduce((sum, a) => sum + a.outstandingAmount, 0)
  );

  readonly selectedAdvancesTotal = computed(() => {
    const selectedIds = this.selectedAdvanceIds();
    return this.outstandingAdvances()
      .filter(a => selectedIds.includes(a.id))
      .reduce((sum, a) => sum + a.outstandingAmount, 0);
  });

  readonly settlementBreakdown = computed(() => {
    const expenseAmount = this.newExpense().totalAmount;
    const advanceAvailable = this.selectedAdvancesTotal();

    if (expenseAmount <= 0) {
      return { fromAdvance: 0, reimbursement: 0 };
    }

    const fromAdvance = Math.min(expenseAmount, advanceAvailable);
    const reimbursement = Math.max(0, expenseAmount - advanceAvailable);

    return { fromAdvance, reimbursement };
  });

  // Actions
  setTab(tab: 'my-expenses' | 'create'): void {
    this.activeTab.set(tab);
  }

  openCreateModal(): void {
    this.newExpense.set({
      employeeId: 1001,
      departmentId: 1,
      totalAmount: 0,
      description: '',
      expenseDate: new Date().toISOString().split('T')[0],
      advanceIdsToSettle: [],
    });
    this.selectedAdvanceIds.set([]);
    this.showCreateModal.set(true);
  }

  closeCreateModal(): void {
    this.showCreateModal.set(false);
  }

  updateAmount(value: string): void {
    const amount = parseFloat(value) || 0;
    this.newExpense.update(e => ({ ...e, totalAmount: amount }));
  }

  updateDescription(value: string): void {
    this.newExpense.update(e => ({ ...e, description: value }));
  }

  updateExpenseDate(value: string): void {
    this.newExpense.update(e => ({ ...e, expenseDate: value }));
  }

  toggleAdvanceSelection(advanceId: number): void {
    this.selectedAdvanceIds.update(ids => {
      if (ids.includes(advanceId)) {
        return ids.filter(id => id !== advanceId);
      } else {
        return [...ids, advanceId];
      }
    });
  }

  isAdvanceSelected(advanceId: number): boolean {
    return this.selectedAdvanceIds().includes(advanceId);
  }

  submitExpenseVoucher(): void {
    const expense = this.newExpense();
    if (expense.totalAmount <= 0 || !expense.description.trim()) {
      return;
    }

    const breakdown = this.settlementBreakdown();
    const now = new Date().toISOString();

    const newExpenseVoucher: ExpenseVoucher = {
      id: Date.now(),
      tenantId: 'tenant-001',
      employeeId: expense.employeeId,
      employeeName: 'Rajesh Kumar',
      departmentId: expense.departmentId,
      departmentName: 'Engineering',
      totalAmount: expense.totalAmount,
      description: expense.description,
      expenseDate: expense.expenseDate,
      status: 'SUBMITTED',
      advanceSettlementAmount: breakdown.fromAdvance,
      reimbursementAmount: breakdown.reimbursement,
      linkedAdvanceIds: this.selectedAdvanceIds(),
      paymentAdviceId: breakdown.reimbursement > 0 ? Date.now() + 1 : undefined,
      createdBy: 'rajesh.kumar',
      createdAt: now,
      updatedAt: now,
    };

    this.myExpenseVouchers.update(vouchers => [newExpenseVoucher, ...vouchers]);

    // Update outstanding advances
    const selectedIds = this.selectedAdvanceIds();
    let remainingExpense = expense.totalAmount;

    this.outstandingAdvances.update(advances =>
      advances.map(a => {
        if (!selectedIds.includes(a.id) || remainingExpense <= 0) return a;

        const settle = Math.min(remainingExpense, a.outstandingAmount);
        remainingExpense -= settle;

        return {
          ...a,
          settledAmount: a.settledAmount + settle,
          outstandingAmount: a.outstandingAmount - settle,
          status: a.outstandingAmount - settle <= 0 ? 'FULLY_SETTLED' as const : 'PARTIALLY_SETTLED' as const,
        };
      }).filter(a => a.outstandingAmount > 0)
    );

    this.showCreateModal.set(false);
  }

  isFormValid(): boolean {
    const e = this.newExpense();
    return e.totalAmount > 0 && e.description.trim().length > 0;
  }

  // Helpers
  statusLabel(status: ExpenseVoucherStatus): string {
    const labels: Record<ExpenseVoucherStatus, string> = {
      'DRAFT': 'Draft',
      'SUBMITTED': 'Submitted',
      'APPROVED': 'Approved',
      'REJECTED': 'Rejected',
      'SETTLED': 'Settled',
    };
    return labels[status] || status;
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
