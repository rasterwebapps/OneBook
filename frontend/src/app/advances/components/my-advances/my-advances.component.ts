import { Component, ChangeDetectionStrategy, signal, computed, inject } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import {
  NxPageHeaderComponent,
  NxSearchInputComponent,
  NxStatusBadgeComponent,
  NxEmptyStateComponent,
  NxLoadingSpinnerComponent,
  NxStatCardComponent,
} from '../../../shared/components';
import {
  EmployeeAdvance,
  AdvanceStatus,
  CreateAdvanceRequest,
  AdvanceSummary,
} from '../../models/advance.models';

/**
 * My Advances Component
 *
 * Displays a list of the current employee's advances with ability to
 * create new requests and track approval status.
 */
@Component({
  selector: 'app-my-advances',
  standalone: true,
  imports: [
    DecimalPipe,
    FormsModule,
    NxPageHeaderComponent,
    NxSearchInputComponent,
    NxStatusBadgeComponent,
    NxEmptyStateComponent,
    NxLoadingSpinnerComponent,
    NxStatCardComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './my-advances.component.html',
  styleUrl: './my-advances.component.scss',
})
export class MyAdvancesComponent {
  private readonly router = inject(Router);

  // State
  readonly loading = signal(false);
  readonly searchQuery = signal('');
  readonly showCreateModal = signal(false);
  readonly activeTab = signal<'all' | 'pending' | 'disbursed'>('all');

  // Form state
  readonly newAdvance = signal<CreateAdvanceRequest>({
    employeeId: 1001, // Current user - would come from auth service
    departmentId: 1,
    amount: 0,
    purpose: '',
    requiredBy: '',
  });

  // Mock data - would come from AdvanceService
  readonly myAdvances = signal<EmployeeAdvance[]>([
    {
      id: 1,
      tenantId: 'tenant-001',
      employeeId: 1001,
      employeeName: 'Rajesh Kumar',
      departmentId: 1,
      departmentName: 'Engineering',
      amount: 15000,
      purpose: 'Client visit to Mumbai for project review',
      status: 'DISBURSED',
      requestedDate: '2026-04-01',
      requiredBy: '2026-04-05',
      hodApprovalRequired: true,
      hodApprovedBy: 'Suresh Singh',
      hodApprovedAt: '2026-04-01T10:30:00Z',
      ceoApprovalRequired: true,
      ceoApprovedBy: 'Amit Patel',
      ceoApprovedAt: '2026-04-02T14:00:00Z',
      mdApprovalRequired: false,
      overrideFlag: false,
      settledAmount: 12500,
      outstandingAmount: 2500,
      disbursedAt: '2026-04-03T09:00:00Z',
      journalEntryId: 5001,
      createdBy: 'rajesh.kumar',
      createdAt: '2026-04-01T09:00:00Z',
      updatedAt: '2026-04-03T09:00:00Z',
    },
    {
      id: 2,
      tenantId: 'tenant-001',
      employeeId: 1001,
      employeeName: 'Rajesh Kumar',
      departmentId: 1,
      departmentName: 'Engineering',
      amount: 8000,
      purpose: 'Office supplies procurement',
      status: 'PENDING_HOD',
      requestedDate: '2026-04-10',
      requiredBy: '2026-04-15',
      hodApprovalRequired: true,
      ceoApprovalRequired: false,
      mdApprovalRequired: false,
      overrideFlag: false,
      settledAmount: 0,
      outstandingAmount: 8000,
      createdBy: 'rajesh.kumar',
      createdAt: '2026-04-10T11:00:00Z',
      updatedAt: '2026-04-10T11:00:00Z',
    },
    {
      id: 3,
      tenantId: 'tenant-001',
      employeeId: 1001,
      employeeName: 'Rajesh Kumar',
      departmentId: 1,
      departmentName: 'Engineering',
      amount: 5000,
      purpose: 'Team lunch for project completion',
      status: 'APPROVED',
      requestedDate: '2026-04-08',
      hodApprovalRequired: true,
      hodApprovedBy: 'Suresh Singh',
      hodApprovedAt: '2026-04-08T16:00:00Z',
      ceoApprovalRequired: false,
      mdApprovalRequired: false,
      overrideFlag: false,
      settledAmount: 0,
      outstandingAmount: 5000,
      createdBy: 'rajesh.kumar',
      createdAt: '2026-04-08T14:00:00Z',
      updatedAt: '2026-04-08T16:00:00Z',
    },
  ]);

  readonly summary = signal<AdvanceSummary>({
    totalPending: 8000,
    totalApproved: 5000,
    totalDisbursed: 15000,
    totalOutstanding: 7500,
    pendingApprovalCount: 1,
    overdueCount: 0,
  });

  // Computed
  readonly filteredAdvances = computed(() => {
    const q = this.searchQuery().toLowerCase();
    const tab = this.activeTab();
    let advances = this.myAdvances();

    // Filter by tab
    if (tab === 'pending') {
      advances = advances.filter(a => ['PENDING_HOD', 'PENDING_CEO', 'PENDING_MD', 'APPROVED'].includes(a.status));
    } else if (tab === 'disbursed') {
      advances = advances.filter(a => ['DISBURSED', 'PARTIALLY_SETTLED', 'FULLY_SETTLED'].includes(a.status));
    }

    // Filter by search
    if (q) {
      advances = advances.filter(a =>
        a.purpose.toLowerCase().includes(q) ||
        a.status.toLowerCase().includes(q)
      );
    }

    return advances;
  });

  readonly totalOutstanding = computed(() =>
    this.myAdvances()
      .filter(a => ['DISBURSED', 'PARTIALLY_SETTLED'].includes(a.status))
      .reduce((sum, a) => sum + a.outstandingAmount, 0)
  );

  readonly pendingCount = computed(() =>
    this.myAdvances().filter(a => ['PENDING_HOD', 'PENDING_CEO', 'PENDING_MD'].includes(a.status)).length
  );

  // Actions
  setTab(tab: 'all' | 'pending' | 'disbursed'): void {
    this.activeTab.set(tab);
  }

  updateSearch(value: string): void {
    this.searchQuery.set(value);
  }

  openCreateModal(): void {
    this.newAdvance.set({
      employeeId: 1001,
      departmentId: 1,
      amount: 0,
      purpose: '',
      requiredBy: '',
    });
    this.showCreateModal.set(true);
  }

  closeCreateModal(): void {
    this.showCreateModal.set(false);
  }

  updateAmount(value: string): void {
    const amount = parseFloat(value) || 0;
    this.newAdvance.update(a => ({ ...a, amount }));
  }

  updatePurpose(value: string): void {
    this.newAdvance.update(a => ({ ...a, purpose: value }));
  }

  updateRequiredBy(value: string): void {
    this.newAdvance.update(a => ({ ...a, requiredBy: value }));
  }

  submitAdvanceRequest(): void {
    const request = this.newAdvance();
    if (request.amount <= 0 || !request.purpose.trim()) {
      return;
    }

    // In real implementation, call AdvanceService.createAdvance()
    const now = new Date().toISOString();
    const newAdvance: EmployeeAdvance = {
      id: Date.now(),
      tenantId: 'tenant-001',
      employeeId: request.employeeId,
      employeeName: 'Rajesh Kumar',
      departmentId: request.departmentId,
      departmentName: 'Engineering',
      amount: request.amount,
      purpose: request.purpose,
      status: this.getInitialStatus(request.amount),
      requestedDate: now.split('T')[0],
      requiredBy: request.requiredBy || undefined,
      hodApprovalRequired: true,
      ceoApprovalRequired: request.amount > 10000,
      mdApprovalRequired: request.amount > 20000,
      overrideFlag: false,
      settledAmount: 0,
      outstandingAmount: request.amount,
      createdBy: 'rajesh.kumar',
      createdAt: now,
      updatedAt: now,
    };

    this.myAdvances.update(advances => [newAdvance, ...advances]);
    this.showCreateModal.set(false);
  }

  private getInitialStatus(amount: number): AdvanceStatus {
    return 'PENDING_HOD';
  }

  viewAdvanceDetails(id: number): void {
    // Navigate to advance detail or open modal
    console.log('View advance:', id);
  }

  // Status helpers
  statusLabel(status: AdvanceStatus): string {
    const labels: Record<AdvanceStatus, string> = {
      'DRAFT': 'Draft',
      'PENDING_HOD': 'Pending HOD',
      'PENDING_CEO': 'Pending CEO',
      'PENDING_MD': 'Pending MD',
      'APPROVED': 'Approved',
      'REJECTED': 'Rejected',
      'DISBURSED': 'Disbursed',
      'PARTIALLY_SETTLED': 'Partially Settled',
      'FULLY_SETTLED': 'Fully Settled',
      'CANCELLED': 'Cancelled',
    };
    return labels[status] || status;
  }

  getApprovalTier(amount: number): string {
    if (amount > 20000) return 'MD (>₹20k)';
    if (amount > 10000) return 'CEO (₹10k-20k)';
    return 'HOD (≤₹10k)';
  }

  formatDate(dateStr: string | undefined): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  }

  isFormValid(): boolean {
    const a = this.newAdvance();
    return a.amount > 0 && a.purpose.trim().length > 0;
  }
}
