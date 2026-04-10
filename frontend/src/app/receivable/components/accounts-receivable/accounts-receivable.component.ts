import { Component, ChangeDetectionStrategy, signal, computed } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { NxPageHeaderComponent, NxSearchInputComponent, NxStatusBadgeComponent } from '../../../shared/components';

export interface ReceivableRecord {
  id: string;
  customerName: string;
  invoiceNumber: string;
  invoiceDate: string;
  dueDate: string;
  amount: number;
  paidAmount: number;
  status: 'overdue' | 'pending' | 'partial' | 'paid';
  sector: 'healthcare' | 'auto' | 'general';
  sectorIcon: string;
  expanded: boolean;
  /** JSONB nested details */
  details: Record<string, string>;
  paymentPrediction?: string;
  blocked: boolean;
}

@Component({
  selector: 'app-accounts-receivable',
  standalone: true,
  imports: [DecimalPipe, NxPageHeaderComponent, NxSearchInputComponent, NxStatusBadgeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './accounts-receivable.component.html',
  styleUrl: './accounts-receivable.component.scss',
})
export class AccountsReceivableComponent {
  readonly searchQuery = signal('');
  readonly filterStatus = signal<string>('all');

  readonly receivables = signal<ReceivableRecord[]>([
    {
      id: 'AR-001', customerName: 'MedCare Hospital Group', invoiceNumber: 'INV-2487',
      invoiceDate: '2026-02-15', dueDate: '2026-03-15', amount: 245000, paidAmount: 100000,
      status: 'partial', sector: 'healthcare', sectorIcon: '🏥', expanded: false,
      details: { 'Patient ID': 'PAT-88421', 'Dept': 'Radiology', 'PO Number': 'PO-HC-2201', 'Insurance Ref': 'INSREF-442' },
      blocked: false,
    },
    {
      id: 'AR-002', customerName: 'AutoMax Dealership', invoiceNumber: 'INV-2491',
      invoiceDate: '2026-02-20', dueDate: '2026-03-20', amount: 578000, paidAmount: 0,
      status: 'pending', sector: 'auto', sectorIcon: '🚗', expanded: false,
      details: { 'VIN': '1HGBH41JXMN109186', 'Model': '2026 Sedan Pro', 'Lot #': 'LOT-A42', 'Sales Rep': 'R. Kumar' },
      blocked: false,
    },
    {
      id: 'AR-003', customerName: 'City General Clinic', invoiceNumber: 'INV-2465',
      invoiceDate: '2026-01-28', dueDate: '2026-02-28', amount: 89500, paidAmount: 0,
      status: 'overdue', sector: 'healthcare', sectorIcon: '🏥', expanded: false,
      details: { 'Patient ID': 'PAT-77203', 'Dept': 'Pharmacy', 'PO Number': 'PO-HC-2188', 'Insurance Ref': 'INSREF-398' },
      blocked: false,
    },
    {
      id: 'AR-004', customerName: 'Premier Auto Parts', invoiceNumber: 'INV-2499',
      invoiceDate: '2026-03-01', dueDate: '2026-04-01', amount: 192000, paidAmount: 192000,
      status: 'paid', sector: 'auto', sectorIcon: '🚗', expanded: false,
      details: { 'VIN': '5YJSA1E11HF123456', 'Model': 'EV Crossover', 'Lot #': 'LOT-B17', 'Sales Rep': 'S. Patel' },
      blocked: false,
    },
    {
      id: 'AR-005', customerName: 'Global Trading Corp', invoiceNumber: 'INV-2502',
      invoiceDate: '2026-03-05', dueDate: '2026-04-05', amount: 445000, paidAmount: 200000,
      status: 'partial', sector: 'general', sectorIcon: '🏢', expanded: false,
      details: { 'Contract ID': 'CTR-9921', 'Region': 'APAC', 'Terms': 'Net 30', 'PO Number': 'PO-GT-1105' },
      blocked: true,
    },
  ]);

  readonly filteredReceivables = computed(() => {
    const status = this.filterStatus();
    const query = this.searchQuery().toLowerCase();
    return this.receivables().filter(r => {
      if (status !== 'all' && r.status !== status) return false;
      if (query && !r.customerName.toLowerCase().includes(query) && !r.invoiceNumber.toLowerCase().includes(query)) return false;
      return true;
    });
  });

  readonly totalOutstanding = computed(() =>
    this.receivables().reduce((sum, r) => sum + (r.amount - r.paidAmount), 0)
  );

  readonly totalOverdue = computed(() =>
    this.receivables().filter(r => r.status === 'overdue').reduce((sum, r) => sum + r.amount, 0)
  );

  toggleExpand(id: string): void {
    this.receivables.update(list =>
      list.map(r => r.id === id ? { ...r, expanded: !r.expanded } : r)
    );
  }

  generatePrediction(id: string): void {
    this.receivables.update(list =>
      list.map(r => r.id === id ? { ...r, paymentPrediction: 'AI predicts payment within 7-12 days based on historical patterns. Confidence: 87%.' } : r)
    );
  }

  toggleBlock(id: string): void {
    this.receivables.update(list =>
      list.map(r => r.id === id ? { ...r, blocked: !r.blocked } : r)
    );
  }

  statusColor(status: string): string {
    switch (status) {
      case 'overdue': return 'var(--nx-amber)';
      case 'pending': return 'var(--nx-text-muted)';
      case 'partial': return 'var(--nx-purple)';
      case 'paid': return 'var(--nx-emerald)';
      default: return 'var(--nx-text-muted)';
    }
  }

  detailEntries(details: Record<string, string>): [string, string][] {
    return Object.entries(details);
  }

  setFilter(status: string): void {
    this.filterStatus.set(status);
  }

  updateSearch(value: string): void {
    this.searchQuery.set(value);
  }
}
