import { Component, ChangeDetectionStrategy, signal, computed } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { NxPageHeaderComponent, NxSearchInputComponent, NxStatusBadgeComponent, NxEmptyStateComponent } from '../../../shared/components';

export type PaymentMode = 'NEFT' | 'RTGS' | 'IMPS' | 'Cheque' | 'Cash';
export type BatchStatus = 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED' | 'PAYMENT_GENERATED' | 'COMPLETED';

export interface RegisterEntry {
  id: number;
  vendorAccountId: number;
  vendorName: string;
  transactionType: 'PURCHASE' | 'PURCHASE_RETURN' | 'CREDIT_NOTE';
  invoiceNumber: string;
  invoiceDate: string;
  dueDate: string;
  amount: number;
  currency: string;
  paymentMode: string;
  bankAccountNumber: string;
  bankIfscCode: string;
  bankName: string;
  status: string;
  selected: boolean;
}

export interface VendorGroup {
  vendorAccountId: number;
  vendorName: string;
  entries: RegisterEntry[];
  totalPurchases: number;
  totalReturns: number;
  totalCreditNotes: number;
  netOutstanding: number;
  expanded: boolean;
}

export interface PaymentBatch {
  id: number;
  batchNumber: string;
  vendorAccountId: number;
  vendorName: string;
  totalPurchases: number;
  totalReturns: number;
  totalCreditNotes: number;
  netPayable: number;
  bankAccountId: string;
  paymentMode: PaymentMode;
  status: BatchStatus;
  createdBy: string;
  approvedBy?: string;
  rejectedBy?: string;
  rejectionReason?: string;
  vendorBankAccount?: string;
  vendorBankIfsc?: string;
  vendorBankName?: string;
}

@Component({
  selector: 'app-payment-register',
  standalone: true,
  imports: [DecimalPipe, NxPageHeaderComponent, NxSearchInputComponent, NxStatusBadgeComponent, NxEmptyStateComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './payment-register.component.html',
  styleUrl: './payment-register.component.scss',
})
export class PaymentRegisterComponent {
  readonly activeTab = signal<'register' | 'batches'>('register');
  readonly searchQuery = signal('');
  readonly showBatchModal = signal(false);
  readonly batchBankAccount = signal('HDFC-CA-00112233');
  readonly batchPaymentMode = signal<PaymentMode>('NEFT');
  readonly activeVendorId = signal<number | null>(null);
  readonly showApprovalModal = signal(false);
  readonly approvalBatchId = signal<number | null>(null);
  readonly rejectionReason = signal('');
  readonly approvalAction = signal<'APPROVE' | 'REJECT'>('APPROVE');

  readonly vendorGroups = signal<VendorGroup[]>([
    {
      vendorAccountId: 1001,
      vendorName: 'Sharma Traders Pvt. Ltd.',
      expanded: false,
      totalPurchases: 485000,
      totalReturns: 25000,
      totalCreditNotes: 10000,
      netOutstanding: 450000,
      entries: [
        {
          id: 1, vendorAccountId: 1001, vendorName: 'Sharma Traders Pvt. Ltd.',
          transactionType: 'PURCHASE', invoiceNumber: 'INV-ST-4412', invoiceDate: '2026-02-10',
          dueDate: '2026-03-10', amount: 285000, currency: 'INR', paymentMode: 'NEFT',
          bankAccountNumber: '00112233445566', bankIfscCode: 'HDFC0001234', bankName: 'HDFC Bank',
          status: 'AVAILABLE_FOR_PROCESSING', selected: false,
        },
        {
          id: 2, vendorAccountId: 1001, vendorName: 'Sharma Traders Pvt. Ltd.',
          transactionType: 'PURCHASE', invoiceNumber: 'INV-ST-4418', invoiceDate: '2026-02-20',
          dueDate: '2026-03-20', amount: 200000, currency: 'INR', paymentMode: 'NEFT',
          bankAccountNumber: '00112233445566', bankIfscCode: 'HDFC0001234', bankName: 'HDFC Bank',
          status: 'AVAILABLE_FOR_PROCESSING', selected: false,
        },
        {
          id: 3, vendorAccountId: 1001, vendorName: 'Sharma Traders Pvt. Ltd.',
          transactionType: 'PURCHASE_RETURN', invoiceNumber: 'RET-ST-0091', invoiceDate: '2026-02-25',
          dueDate: '2026-03-25', amount: 25000, currency: 'INR', paymentMode: 'NEFT',
          bankAccountNumber: '00112233445566', bankIfscCode: 'HDFC0001234', bankName: 'HDFC Bank',
          status: 'AVAILABLE_FOR_PROCESSING', selected: false,
        },
        {
          id: 4, vendorAccountId: 1001, vendorName: 'Sharma Traders Pvt. Ltd.',
          transactionType: 'CREDIT_NOTE', invoiceNumber: 'CN-ST-0044', invoiceDate: '2026-03-01',
          dueDate: '2026-04-01', amount: 10000, currency: 'INR', paymentMode: 'NEFT',
          bankAccountNumber: '00112233445566', bankIfscCode: 'HDFC0001234', bankName: 'HDFC Bank',
          status: 'AVAILABLE_FOR_PROCESSING', selected: false,
        },
      ],
    },
    {
      vendorAccountId: 1002,
      vendorName: 'Gupta Electricals & Co.',
      expanded: false,
      totalPurchases: 162000,
      totalReturns: 12000,
      totalCreditNotes: 0,
      netOutstanding: 150000,
      entries: [
        {
          id: 5, vendorAccountId: 1002, vendorName: 'Gupta Electricals & Co.',
          transactionType: 'PURCHASE', invoiceNumber: 'INV-GE-8801', invoiceDate: '2026-03-01',
          dueDate: '2026-04-01', amount: 162000, currency: 'INR', paymentMode: 'RTGS',
          bankAccountNumber: '99887766554433', bankIfscCode: 'ICIC0005678', bankName: 'ICICI Bank',
          status: 'AVAILABLE_FOR_PROCESSING', selected: false,
        },
        {
          id: 6, vendorAccountId: 1002, vendorName: 'Gupta Electricals & Co.',
          transactionType: 'PURCHASE_RETURN', invoiceNumber: 'RET-GE-0033', invoiceDate: '2026-03-05',
          dueDate: '2026-04-05', amount: 12000, currency: 'INR', paymentMode: 'RTGS',
          bankAccountNumber: '99887766554433', bankIfscCode: 'ICIC0005678', bankName: 'ICICI Bank',
          status: 'AVAILABLE_FOR_PROCESSING', selected: false,
        },
      ],
    },
    {
      vendorAccountId: 1003,
      vendorName: 'MedSupply India Ltd.',
      expanded: false,
      totalPurchases: 320000,
      totalReturns: 0,
      totalCreditNotes: 20000,
      netOutstanding: 300000,
      entries: [
        {
          id: 7, vendorAccountId: 1003, vendorName: 'MedSupply India Ltd.',
          transactionType: 'PURCHASE', invoiceNumber: 'INV-MS-2201', invoiceDate: '2026-02-28',
          dueDate: '2026-03-28', amount: 180000, currency: 'INR', paymentMode: 'NEFT',
          bankAccountNumber: '11223344556677', bankIfscCode: 'SBIN0009012', bankName: 'SBI',
          status: 'AVAILABLE_FOR_PROCESSING', selected: false,
        },
        {
          id: 8, vendorAccountId: 1003, vendorName: 'MedSupply India Ltd.',
          transactionType: 'PURCHASE', invoiceNumber: 'INV-MS-2208', invoiceDate: '2026-03-05',
          dueDate: '2026-04-05', amount: 140000, currency: 'INR', paymentMode: 'NEFT',
          bankAccountNumber: '11223344556677', bankIfscCode: 'SBIN0009012', bankName: 'SBI',
          status: 'AVAILABLE_FOR_PROCESSING', selected: false,
        },
        {
          id: 9, vendorAccountId: 1003, vendorName: 'MedSupply India Ltd.',
          transactionType: 'CREDIT_NOTE', invoiceNumber: 'CN-MS-0019', invoiceDate: '2026-03-08',
          dueDate: '2026-04-08', amount: 20000, currency: 'INR', paymentMode: 'NEFT',
          bankAccountNumber: '11223344556677', bankIfscCode: 'SBIN0009012', bankName: 'SBI',
          status: 'AVAILABLE_FOR_PROCESSING', selected: false,
        },
      ],
    },
  ]);

  readonly paymentBatches = signal<PaymentBatch[]>([
    {
      id: 101, batchNumber: 'PB-2026-03-001', vendorAccountId: 1001,
      vendorName: 'Sharma Traders Pvt. Ltd.', totalPurchases: 285000, totalReturns: 0,
      totalCreditNotes: 0, netPayable: 285000, bankAccountId: 'HDFC-CA-00112233',
      paymentMode: 'NEFT', status: 'PENDING_APPROVAL', createdBy: 'accounts.user',
      vendorBankAccount: '00112233445566', vendorBankIfsc: 'HDFC0001234', vendorBankName: 'HDFC Bank',
    },
    {
      id: 102, batchNumber: 'PB-2026-03-002', vendorAccountId: 1002,
      vendorName: 'Gupta Electricals & Co.', totalPurchases: 162000, totalReturns: 12000,
      totalCreditNotes: 0, netPayable: 150000, bankAccountId: 'HDFC-CA-00112233',
      paymentMode: 'RTGS', status: 'APPROVED', createdBy: 'accounts.user', approvedBy: 'finance.manager',
      vendorBankAccount: '99887766554433', vendorBankIfsc: 'ICIC0005678', vendorBankName: 'ICICI Bank',
    },
  ]);

  // ── Computed ──────────────────────────────────────────────────────────────

  readonly filteredVendorGroups = computed(() => {
    const q = this.searchQuery().toLowerCase();
    if (!q) return this.vendorGroups();
    return this.vendorGroups().filter(g => g.vendorName.toLowerCase().includes(q));
  });

  readonly totalOutstanding = computed(() =>
    this.vendorGroups().reduce((s, g) => s + g.netOutstanding, 0)
  );

  readonly pendingBatchCount = computed(() =>
    this.paymentBatches().filter(b => b.status === 'PENDING_APPROVAL').length
  );

  readonly selectedEntriesForVendor = computed(() => {
    const vid = this.activeVendorId();
    if (vid === null) return [];
    const group = this.vendorGroups().find(g => g.vendorAccountId === vid);
    return group ? group.entries.filter(e => e.selected) : [];
  });

  readonly selectedNetPayable = computed(() => {
    const entries = this.selectedEntriesForVendor();
    const purchases = entries.filter(e => e.transactionType === 'PURCHASE').reduce((s, e) => s + e.amount, 0);
    const returns = entries.filter(e => e.transactionType === 'PURCHASE_RETURN').reduce((s, e) => s + e.amount, 0);
    const credits = entries.filter(e => e.transactionType === 'CREDIT_NOTE').reduce((s, e) => s + e.amount, 0);
    return purchases - returns - credits;
  });

  // ── Actions — AP Register ────────────────────────────────────────────────

  toggleVendorExpand(vendorAccountId: number): void {
    this.vendorGroups.update(groups =>
      groups.map(g => g.vendorAccountId === vendorAccountId ? { ...g, expanded: !g.expanded } : g)
    );
  }

  toggleEntrySelect(vendorAccountId: number, entryId: number): void {
    this.activeVendorId.set(vendorAccountId);
    this.vendorGroups.update(groups =>
      groups.map(g =>
        g.vendorAccountId === vendorAccountId
          ? { ...g, entries: g.entries.map(e => e.id === entryId ? { ...e, selected: !e.selected } : e) }
          : g
      )
    );
  }

  toggleSelectAllForVendor(vendorAccountId: number): void {
    this.activeVendorId.set(vendorAccountId);
    this.vendorGroups.update(groups =>
      groups.map(g => {
        if (g.vendorAccountId !== vendorAccountId) return g;
        const allSelected = g.entries.every(e => e.selected);
        return { ...g, entries: g.entries.map(e => ({ ...e, selected: !allSelected })) };
      })
    );
  }

  openCreateBatchModal(vendorAccountId: number): void {
    this.activeVendorId.set(vendorAccountId);
    this.showBatchModal.set(true);
  }

  closeBatchModal(): void {
    this.showBatchModal.set(false);
  }

  createBatch(): void {
    const vid = this.activeVendorId();
    if (vid === null) return;

    const group = this.vendorGroups().find(g => g.vendorAccountId === vid);
    if (!group) return;

    const selected = group.entries.filter(e => e.selected);
    if (selected.length === 0) return;

    const purchases = selected.filter(e => e.transactionType === 'PURCHASE').reduce((s, e) => s + e.amount, 0);
    const returns = selected.filter(e => e.transactionType === 'PURCHASE_RETURN').reduce((s, e) => s + e.amount, 0);
    const credits = selected.filter(e => e.transactionType === 'CREDIT_NOTE').reduce((s, e) => s + e.amount, 0);
    const netPayable = purchases - returns - credits;

    const now = new Date();
    const yearMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
    const prefix = `PB-${yearMonth}-`;
    // Use max existing sequence for this month to avoid duplicates
    const existingSeqs = this.paymentBatches()
      .filter(b => b.batchNumber.startsWith(prefix))
      .map(b => parseInt(b.batchNumber.replace(prefix, ''), 10) || 0);
    const nextSeq = (existingSeqs.length > 0 ? Math.max(...existingSeqs) : 0) + 1;
    const batchNumber = `${prefix}${String(nextSeq).padStart(3, '0')}`;

    // Capture vendor bank details from selected entries (first available purchase entry)
    const bankSourceEntry = selected.find(e => e.bankAccountNumber);
    const vendorBankAccount = bankSourceEntry?.bankAccountNumber ?? '';
    const vendorBankIfsc = bankSourceEntry?.bankIfscCode ?? '';
    const vendorBankName = bankSourceEntry?.bankName ?? '';

    const newBatch: PaymentBatch = {
      id: Date.now(),
      batchNumber,
      vendorAccountId: vid,
      vendorName: group.vendorName,
      totalPurchases: purchases,
      totalReturns: returns,
      totalCreditNotes: credits,
      netPayable,
      bankAccountId: this.batchBankAccount(),
      paymentMode: this.batchPaymentMode(),
      status: 'PENDING_APPROVAL',
      createdBy: 'accounts.user',
      vendorBankAccount,
      vendorBankIfsc,
      vendorBankName,
    };

    this.paymentBatches.update(batches => [...batches, newBatch]);

    // Mark selected entries as IN_BATCH
    this.vendorGroups.update(groups =>
      groups.map(g =>
        g.vendorAccountId !== vid ? g : {
          ...g,
          entries: g.entries.map(e =>
            e.selected ? { ...e, status: 'IN_BATCH', selected: false } : e
          ),
        }
      )
    );

    this.showBatchModal.set(false);
    this.activeTab.set('batches');
  }

  // ── Actions — Payment Batches (Maker-Checker) ────────────────────────────

  openApprovalModal(batchId: number, action: 'APPROVE' | 'REJECT'): void {
    this.approvalBatchId.set(batchId);
    this.approvalAction.set(action);
    this.rejectionReason.set('');
    this.showApprovalModal.set(true);
  }

  closeApprovalModal(): void {
    this.showApprovalModal.set(false);
    this.approvalBatchId.set(null);
  }

  confirmApproval(): void {
    const batchId = this.approvalBatchId();
    if (batchId === null) return;

    const action = this.approvalAction();
    this.paymentBatches.update(batches =>
      batches.map(b => {
        if (b.id !== batchId) return b;
        if (action === 'APPROVE') {
          return { ...b, status: 'APPROVED' as BatchStatus, approvedBy: 'finance.manager' };
        } else {
          return { ...b, status: 'REJECTED' as BatchStatus, rejectedBy: 'finance.manager', rejectionReason: this.rejectionReason() };
        }
      })
    );

    this.showApprovalModal.set(false);
    this.approvalBatchId.set(null);
  }

  generatePaymentFile(batchId: number): void {
    const batch = this.paymentBatches().find(b => b.id === batchId);
    if (!batch || batch.status !== 'APPROVED') return;

    const csvHeader = 'Sr No,Vendor Name,Bank Account,IFSC Code,Bank Name,Payment Amount,Payment Reference,Payment Mode\n';
    const csvRow = [
      '1',
      this.csvEscape(batch.vendorName),
      this.csvEscape(batch.vendorBankAccount ?? ''),
      this.csvEscape(batch.vendorBankIfsc ?? ''),
      this.csvEscape(batch.vendorBankName ?? ''),
      batch.netPayable.toString(),
      this.csvEscape(batch.batchNumber),
      this.csvEscape(batch.paymentMode),
    ].join(',') + '\n';

    const blob = new Blob([csvHeader + csvRow], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `payment-${batch.batchNumber}.csv`;
    a.click();
    URL.revokeObjectURL(url);

    this.paymentBatches.update(batches =>
      batches.map(b => b.id === batchId ? { ...b, status: 'PAYMENT_GENERATED' as BatchStatus } : b)
    );
  }

  private csvEscape(value: string): string {
    if (value.includes(',') || value.includes('"') || value.includes('\n')) {
      return '"' + value.replace(/"/g, '""') + '"';
    }
    return value;
  }

  // ── Utilities ─────────────────────────────────────────────────────────────

  setTab(tab: 'register' | 'batches'): void {
    this.activeTab.set(tab);
  }

  updateSearch(value: string): void {
    this.searchQuery.set(value);
  }

  updateBatchBankAccount(value: string): void {
    this.batchBankAccount.set(value);
  }

  updateBatchPaymentMode(value: PaymentMode): void {
    this.batchPaymentMode.set(value);
  }

  updateRejectionReason(value: string): void {
    this.rejectionReason.set(value);
  }

  hasSelectedEntries(vendorAccountId: number): boolean {
    const group = this.vendorGroups().find(g => g.vendorAccountId === vendorAccountId);
    return !!group && group.entries.some(e => e.selected);
  }

  allEntriesSelected(vendorAccountId: number): boolean {
    const group = this.vendorGroups().find(g => g.vendorAccountId === vendorAccountId);
    return !!group && group.entries.length > 0 && group.entries.every(e => e.selected);
  }

  getVendorName(vendorAccountId: number | null): string {
    if (vendorAccountId === null) return '';
    const group = this.vendorGroups().find(g => g.vendorAccountId === vendorAccountId);
    return group ? group.vendorName : '';
  }

  transactionTypeLabel(type: string): string {
    switch (type) {
      case 'PURCHASE': return 'Purchase';
      case 'PURCHASE_RETURN': return 'Return';
      case 'CREDIT_NOTE': return 'Credit Note';
      default: return type;
    }
  }

  transactionTypeColor(type: string): string {
    switch (type) {
      case 'PURCHASE': return 'var(--nx-text-primary)';
      case 'PURCHASE_RETURN': return 'var(--nx-amber)';
      case 'CREDIT_NOTE': return 'var(--nx-purple)';
      default: return 'var(--nx-text-muted)';
    }
  }

  batchStatusLabel(status: BatchStatus): string {
    switch (status) {
      case 'PENDING_APPROVAL': return 'Pending Approval';
      case 'APPROVED': return 'Approved';
      case 'REJECTED': return 'Rejected';
      case 'PAYMENT_GENERATED': return 'Payment Generated';
      case 'COMPLETED': return 'Completed';
      default: return status;
    }
  }

  batchStatusColor(status: BatchStatus): string {
    switch (status) {
      case 'PENDING_APPROVAL': return 'var(--nx-warning)';
      case 'APPROVED': return 'var(--nx-emerald)';
      case 'REJECTED': return 'var(--nx-danger)';
      case 'PAYMENT_GENERATED': return 'var(--nx-purple)';
      case 'COMPLETED': return 'var(--nx-success)';
      default: return 'var(--nx-text-muted)';
    }
  }
}
