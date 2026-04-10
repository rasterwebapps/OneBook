import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { AuditorService } from '../../services/auditor.service';
import { NxPageHeaderComponent, NxLoadingSpinnerComponent } from '../../../shared/components';
import {
  AuditSampleRequest,
  AuditComment,
  AuditWorkflow,
  SecurityAuditReport
} from '../../models/auditor.models';

@Component({
  selector: 'app-auditor-dashboard',
  standalone: true,
  imports: [NxPageHeaderComponent, NxLoadingSpinnerComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './auditor-dashboard.component.html',
  styleUrl: './auditor-dashboard.component.scss'
})
export class AuditorDashboardComponent implements OnInit {
  private readonly auditorService = inject(AuditorService);
  private readonly tenantId = 'tenant-1';

  readonly sampleRequests = signal<AuditSampleRequest[]>([]);
  readonly comments = signal<AuditComment[]>([]);
  readonly workflows = signal<AuditWorkflow[]>([]);
  readonly securityReport = signal<SecurityAuditReport | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.loadData();
  }

  private loadData(): void {
    this.loading.set(true);
    this.error.set(null);

    this.auditorService.getSampleRequests(this.tenantId).subscribe({
      next: (data) => this.sampleRequests.set(data),
      error: () => this.error.set('Failed to load audit data')
    });

    this.auditorService.getComments(this.tenantId).subscribe({
      next: (data) => this.comments.set(data),
      error: () => {} // non-critical
    });

    this.auditorService.getWorkflows(this.tenantId).subscribe({
      next: (data) => this.workflows.set(data),
      error: () => {} // non-critical
    });

    this.auditorService.runSecurityAudit(this.tenantId).subscribe({
      next: (data) => {
        this.securityReport.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  statusColor(status: string): string {
    switch (status?.toUpperCase()) {
      case 'APPROVED':
      case 'COMPLETED':
      case 'PASS': return '#22c55e';
      case 'REJECTED':
      case 'FAIL': return '#ef4444';
      case 'PENDING':
      case 'IN_PROGRESS': return '#f59e0b';
      default: return '#6b7280';
    }
  }

  unresolvedCount(): number {
    return this.comments().filter(c => !c.resolved).length;
  }

  pendingWorkflowCount(): number {
    return this.workflows().filter(w => w.status === 'PENDING').length;
  }
}
