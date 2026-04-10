import { Component, ChangeDetectionStrategy, input, computed } from '@angular/core';

/**
 * Pre-mapped accounting status badge.
 * Extends NxBadgeComponent by mapping common accounting statuses to color variants automatically.
 */
@Component({
  selector: 'nx-status-badge',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <span class="nx-badge" [class]="badgeClass()">
      {{ displayLabel() }}
    </span>
  `,
})
export class NxStatusBadgeComponent {
  /** The raw status string, e.g. 'POSTED', 'PENDING', 'DRAFT', etc. */
  readonly status = input('');

  /** Optional override label. If empty, uses formatted status. */
  readonly label = input('');

  private static readonly STATUS_MAP: Record<string, { variant: string; label: string }> = {
    'POSTED': { variant: 'success', label: 'Posted' },
    'APPROVED': { variant: 'success', label: 'Approved' },
    'COMPLETED': { variant: 'success', label: 'Completed' },
    'ACTIVE': { variant: 'success', label: 'Active' },
    'PENDING': { variant: 'warning', label: 'Pending' },
    'PENDING_APPROVAL': { variant: 'warning', label: 'Pending Approval' },
    'IN_PROGRESS': { variant: 'warning', label: 'In Progress' },
    'DRAFT': { variant: 'info', label: 'Draft' },
    'NEW': { variant: 'info', label: 'New' },
    'UNPOSTED': { variant: 'info', label: 'Unposted' },
    'CANCELLED': { variant: 'danger', label: 'Cancelled' },
    'REVERSED': { variant: 'danger', label: 'Reversed' },
    'REJECTED': { variant: 'danger', label: 'Rejected' },
    'FAILED': { variant: 'danger', label: 'Failed' },
    'OVERDUE': { variant: 'danger', label: 'Overdue' },
    'PAYMENT_GENERATED': { variant: 'info', label: 'Payment Generated' },
  };

  readonly badgeClass = computed(() => {
    const entry = NxStatusBadgeComponent.STATUS_MAP[this.status().toUpperCase()];
    const variant = entry?.variant ?? 'neutral';
    return `nx-badge--${variant}`;
  });

  readonly displayLabel = computed(() => {
    if (this.label()) return this.label();
    const entry = NxStatusBadgeComponent.STATUS_MAP[this.status().toUpperCase()];
    return entry?.label ?? this.status();
  });
}
