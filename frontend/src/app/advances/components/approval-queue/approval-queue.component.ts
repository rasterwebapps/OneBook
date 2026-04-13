import { Component, ChangeDetectionStrategy, signal, computed } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import {
  NxPageHeaderComponent,
  NxSearchInputComponent,
  NxEmptyStateComponent,
  NxLoadingSpinnerComponent,
  NxStatCardComponent,
} from '../../../shared/components';
import {
  EmployeeAdvance,
  AdvanceStatus,
  ApprovalRequest,
} from '../../models/advance.models';

/**
 * Approval Queue Component
 *
 * For HODs, CEOs, and MDs to view and approve/reject advance requests
 * based on their approval authority level.
 */
@Component({
  selector: 'app-approval-queue',
  standalone: true,
  imports: [
    DecimalPipe,
    NxPageHeaderComponent,
    NxSearchInputComponent,
    NxEmptyStateComponent,
    NxLoadingSpinnerComponent,
    NxStatCardComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './approval-queue.component.html',
  styleUrl: './approval-queue.component.scss',
})
export class ApprovalQueueComponent {
  // State
  readonly loading = signal(false);
  readonly searchQuery = signal('');
  readonly showApprovalModal = signal(false);
  readonly selectedAdvance = signal<EmployeeAdvance | null>(null);
  readonly approvalAction = signal<'APPROVE' | 'REJECT'>('APPROVE');
  readonly rejectionReason = signal('');
  readonly overrideFlag = signal(false);
  readonly overrideReason = signal('');

  // Current user's role (would come from auth service)
  readonly currentUserRole = signal<'HOD' | 'CEO' | 'MD'>('HOD');
  readonly currentUserDepartmentId = signal<number>(1);

  // Mock data - pending approvals visible to this user
  readonly pendingApprovals = signal<EmployeeAdvance[]>([
    {
      id: 101,
      tenantId: 'tenant-001',
      employeeId: 2001,
      employeeName: 'Priya Sharma',
      departmentId: 1,
      departmentName: 'Engineering',
      amount: 8000,
      purpose: 'Client meeting expenses for Bangalore trip',
      status: 'PENDING_HOD',
      requestedDate: '2026-04-10',
      requiredBy: '2026-04-15',
      hodApprovalRequired: true,
      ceoApprovalRequired: false,
      mdApprovalRequired: false,
      overrideFlag: false,
      settledAmount: 0,
      outstandingAmount: 8000,
      createdBy: 'priya.sharma',
      createdAt: '2026-04-10T09:00:00Z',
      updatedAt: '2026-04-10T09:00:00Z',
    },
    {
      id: 102,
      tenantId: 'tenant-001',
      employeeId: 2002,
      employeeName: 'Amit Verma',
      departmentId: 1,
      departmentName: 'Engineering',
      amount: 15000,
      purpose: 'Conference registration and travel - Tech Summit 2026',
      status: 'PENDING_HOD',
      requestedDate: '2026-04-08',
      requiredBy: '2026-04-20',
      hodApprovalRequired: true,
      ceoApprovalRequired: true,
      mdApprovalRequired: false,
      overrideFlag: false,
      settledAmount: 0,
      outstandingAmount: 15000,
      createdBy: 'amit.verma',
      createdAt: '2026-04-08T14:00:00Z',
      updatedAt: '2026-04-08T14:00:00Z',
    },
    {
      id: 103,
      tenantId: 'tenant-001',
      employeeId: 2003,
      employeeName: 'Sneha Patel',
      departmentId: 1,
      departmentName: 'Engineering',
      amount: 25000,
      purpose: 'Equipment procurement for new project setup',
      status: 'PENDING_HOD',
      requestedDate: '2026-04-05',
      requiredBy: '2026-04-12',
      hodApprovalRequired: true,
      ceoApprovalRequired: true,
      mdApprovalRequired: true,
      overrideFlag: false,
      settledAmount: 0,
      outstandingAmount: 25000,
      createdBy: 'sneha.patel',
      createdAt: '2026-04-05T11:00:00Z',
      updatedAt: '2026-04-05T11:00:00Z',
    },
  ]);

  // Computed
  readonly filteredApprovals = computed(() => {
    const q = this.searchQuery().toLowerCase();
    let advances = this.pendingApprovals();

    if (q) {
      advances = advances.filter(a =>
        a.employeeName?.toLowerCase().includes(q) ||
        a.purpose.toLowerCase().includes(q) ||
        a.departmentName?.toLowerCase().includes(q)
      );
    }

    return advances;
  });

  readonly totalPendingAmount = computed(() =>
    this.pendingApprovals().reduce((sum, a) => sum + a.amount, 0)
  );

  readonly urgentCount = computed(() =>
    this.pendingApprovals().filter(a => {
      if (!a.requiredBy) return false;
      const requiredDate = new Date(a.requiredBy);
      const today = new Date();
      const daysUntil = Math.ceil((requiredDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
      return daysUntil <= 3;
    }).length
  );

  // Actions
  updateSearch(value: string): void {
    this.searchQuery.set(value);
  }

  openApprovalModal(advance: EmployeeAdvance, action: 'APPROVE' | 'REJECT'): void {
    this.selectedAdvance.set(advance);
    this.approvalAction.set(action);
    this.rejectionReason.set('');
    this.overrideFlag.set(false);
    this.overrideReason.set('');
    this.showApprovalModal.set(true);
  }

  closeApprovalModal(): void {
    this.showApprovalModal.set(false);
    this.selectedAdvance.set(null);
  }

  updateRejectionReason(value: string): void {
    this.rejectionReason.set(value);
  }

  toggleOverride(): void {
    this.overrideFlag.update(v => !v);
    if (!this.overrideFlag()) {
      this.overrideReason.set('');
    }
  }

  updateOverrideReason(value: string): void {
    this.overrideReason.set(value);
  }

  confirmApproval(): void {
    const advance = this.selectedAdvance();
    if (!advance) return;

    const action = this.approvalAction();
    const role = this.currentUserRole();

    // In real implementation, call AdvanceService
    if (action === 'APPROVE') {
      this.pendingApprovals.update(advances =>
        advances.map(a => {
          if (a.id !== advance.id) return a;

          // Update approval chain based on role
          const updated = { ...a };
          if (role === 'HOD') {
            updated.hodApprovedBy = 'current.user';
            updated.hodApprovedAt = new Date().toISOString();
            // Determine next status
            if (a.ceoApprovalRequired) {
              updated.status = 'PENDING_CEO';
            } else {
              updated.status = 'APPROVED';
            }
          } else if (role === 'CEO') {
            updated.ceoApprovedBy = 'current.user';
            updated.ceoApprovedAt = new Date().toISOString();
            if (a.mdApprovalRequired) {
              updated.status = 'PENDING_MD';
            } else {
              updated.status = 'APPROVED';
            }
          } else if (role === 'MD') {
            updated.mdApprovedBy = 'current.user';
            updated.mdApprovedAt = new Date().toISOString();
            updated.status = 'APPROVED';
          }

          if (this.overrideFlag()) {
            updated.overrideFlag = true;
            updated.overrideReason = this.overrideReason();
          }

          return updated;
        }).filter(a => a.status.startsWith('PENDING_') && this.canApprove(a))
      );
    } else {
      // Reject
      this.pendingApprovals.update(advances =>
        advances.filter(a => a.id !== advance.id)
      );
    }

    this.closeApprovalModal();
  }

  canApprove(advance: EmployeeAdvance): boolean {
    const role = this.currentUserRole();
    const deptId = this.currentUserDepartmentId();

    // HOD can only see their department
    if (role === 'HOD' && advance.departmentId !== deptId) {
      return false;
    }

    // Check if current role matches pending status
    if (role === 'HOD' && advance.status === 'PENDING_HOD') return true;
    if (role === 'CEO' && advance.status === 'PENDING_CEO') return true;
    if (role === 'MD' && advance.status === 'PENDING_MD') return true;

    return false;
  }

  isApproveValid(): boolean {
    if (this.overrideFlag() && !this.overrideReason().trim()) {
      return false;
    }
    return true;
  }

  isRejectValid(): boolean {
    return this.rejectionReason().trim().length > 0;
  }

  // Helpers
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

  isUrgent(advance: EmployeeAdvance): boolean {
    if (!advance.requiredBy) return false;
    const requiredDate = new Date(advance.requiredBy);
    const today = new Date();
    const daysUntil = Math.ceil((requiredDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
    return daysUntil <= 3;
  }

  getDaysUntilDue(advance: EmployeeAdvance): number {
    if (!advance.requiredBy) return 999;
    const requiredDate = new Date(advance.requiredBy);
    const today = new Date();
    return Math.ceil((requiredDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
  }
}
