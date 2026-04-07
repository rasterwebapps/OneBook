import {
  Component, ChangeDetectionStrategy, OnInit,
  inject, signal, computed, HostListener,
} from '@angular/core';
import { DecimalPipe, DatePipe } from '@angular/common';
import { VoucherService } from '../../services/voucher.service';
import { AccountMasterService } from '../../services/account-master.service';
import { Voucher, VoucherCategory, VOUCHER_TYPE_CONFIG } from '../../models/voucher.models';

type ExplorerMode = 'explorer' | 'entry';
type SortField = 'voucherNumber' | 'date' | 'amount' | 'none';
type SortDir = 'asc' | 'desc';

const PAGE_SIZE = 20;

const CATEGORY_LABELS: Record<VoucherCategory, string> = {
  CONTRA:      'Contra',
  PAYMENT:     'Payment',
  RECEIPT:     'Receipt',
  JOURNAL:     'Journal',
  SALES:       'Sales',
  PURCHASE:    'Purchase',
  CREDIT_NOTE: 'Credit Note',
  DEBIT_NOTE:  'Debit Note',
};

const CATEGORY_COLORS: Record<VoucherCategory, string> = {
  CONTRA:      '#6366f1',
  PAYMENT:     '#ef4444',
  RECEIPT:     '#22c55e',
  JOURNAL:     '#f59e0b',
  SALES:       '#3b82f6',
  PURCHASE:    '#8b5cf6',
  CREDIT_NOTE: '#ec4899',
  DEBIT_NOTE:  '#14b8a6',
};

@Component({
  selector: 'app-voucher-explorer',
  standalone: true,
  imports: [DecimalPipe, DatePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './voucher-explorer.component.html',
  styleUrl: './voucher-explorer.component.scss',
})
export class VoucherExplorerComponent implements OnInit {
  private readonly svc = inject(VoucherService);
  private readonly masterSvc = inject(AccountMasterService);

  /* ── Screen mode ── */
  readonly mode = signal<ExplorerMode>('explorer');

  /* ── Edit state ── */
  readonly editing = signal(false);
  readonly editingUuid = signal('');
  readonly editingVoucherNumber = signal('');
  readonly saving = signal(false);
  readonly selectedUuid = signal('');

  /* ── Data from service ── */
  readonly allVouchers = this.svc.vouchers;

  /* ── Filter state ── */
  readonly filterSearch = signal('');
  readonly filterCategory = signal<VoucherCategory | 'ALL'>('ALL');
  readonly filterStatus = signal<'ALL' | 'POSTED' | 'UNPOSTED'>('ALL');
  readonly filterDateFrom = signal('');
  readonly filterDateTo = signal('');

  /* ── Sort state ── */
  readonly sortField = signal<SortField>('date');
  readonly sortDir = signal<SortDir>('desc');

  /* ── Pagination ── */
  readonly currentPage = signal(1);

  /* ── Form fields ── */
  readonly formType = signal<VoucherCategory>('PAYMENT');
  readonly formDate = signal(this.today());
  readonly formDebitAccountId = signal(0);
  readonly formCreditAccountId = signal(0);
  readonly formAmount = signal(0);
  readonly formNarration = signal('');
  readonly formError = signal('');

  /* ── Voucher type config for current form type ── */
  readonly typeConfig = computed(() => VOUCHER_TYPE_CONFIG[this.formType()]);

  /* ── Filtered account lists ── */
  readonly debitAccounts = computed(() => {
    const cfg = this.typeConfig();
    const all = this.masterSvc.accounts().filter(a => a.active);
    if (!cfg.debitGroupIds.length) return all;
    return all.filter(a => cfg.debitGroupIds.includes(a.groupId));
  });

  readonly creditAccounts = computed(() => {
    const cfg = this.typeConfig();
    const all = this.masterSvc.accounts().filter(a => a.active);
    if (!cfg.creditGroupIds.length) return all;
    return all.filter(a => cfg.creditGroupIds.includes(a.groupId));
  });

  /* ── Filtered + sorted vouchers ── */
  readonly filteredVouchers = computed(() => {
    let list = this.allVouchers();

    const search = this.filterSearch().toLowerCase().trim();
    if (search) {
      list = list.filter(v =>
        v.voucherNumber.toLowerCase().includes(search) ||
        v.narration.toLowerCase().includes(search) ||
        v.debitAccountName.toLowerCase().includes(search) ||
        v.creditAccountName.toLowerCase().includes(search)
      );
    }

    const cat = this.filterCategory();
    if (cat !== 'ALL') {
      list = list.filter(v => v.voucherType === cat);
    }

    const status = this.filterStatus();
    if (status === 'POSTED') list = list.filter(v => v.posted);
    else if (status === 'UNPOSTED') list = list.filter(v => !v.posted);

    const from = this.filterDateFrom();
    const to = this.filterDateTo();
    if (from) list = list.filter(v => v.date >= from);
    if (to)   list = list.filter(v => v.date <= to);

    const sf = this.sortField();
    const sd = this.sortDir();
    if (sf !== 'none') {
      list = [...list].sort((a, b) => {
        let cmp = 0;
        if (sf === 'voucherNumber') cmp = a.voucherNumber.localeCompare(b.voucherNumber);
        else if (sf === 'date')     cmp = a.date.localeCompare(b.date);
        else if (sf === 'amount')   cmp = a.amount - b.amount;
        return sd === 'asc' ? cmp : -cmp;
      });
    }

    return list;
  });

  /* ── Paginated slice ── */
  readonly totalPages = computed(() =>
    Math.max(1, Math.ceil(this.filteredVouchers().length / PAGE_SIZE))
  );

  readonly pagedVouchers = computed(() => {
    const page = this.currentPage();
    const start = (page - 1) * PAGE_SIZE;
    return this.filteredVouchers().slice(start, start + PAGE_SIZE);
  });

  /* ── Summary stats ── */
  readonly totalCount = computed(() => this.allVouchers().length);
  readonly postedCount = computed(() => this.allVouchers().filter(v => v.posted).length);
  readonly unpostedCount = computed(() => this.allVouchers().filter(v => !v.posted).length);
  readonly totalAmount = computed(() => this.allVouchers().reduce((s, v) => s + v.amount, 0));

  /* ── Expose constants to template ── */
  readonly categoryLabels = CATEGORY_LABELS;
  readonly categoryColors = CATEGORY_COLORS;
  readonly voucherTypeConfig = VOUCHER_TYPE_CONFIG;
  readonly allCategories = Object.keys(VOUCHER_TYPE_CONFIG) as VoucherCategory[];
  readonly fKeyCategories: VoucherCategory[] = ['CONTRA', 'PAYMENT', 'RECEIPT', 'JOURNAL', 'SALES', 'PURCHASE'];

  ngOnInit(): void {
    this.masterSvc.initialize();
    this.loadAllVouchers();
  }

  /* ── Load all voucher types ── */
  loadAllVouchers(): void {
    // Load one type at a time to merge into a single signal.
    // We iterate all F-key categories sequentially.
    this.fKeyCategories.forEach(cat => this.svc.loadVouchers(cat));
  }

  /* ── Keyboard shortcuts ── */
  @HostListener('window:keydown', ['$event'])
  onKey(e: KeyboardEvent): void {
    // Ctrl+S — save in entry mode
    if (e.ctrlKey && e.key === 's') {
      if (this.mode() === 'entry') {
        e.preventDefault();
        this.save();
      }
      return;
    }

    // Escape — cancel entry form
    if (e.key === 'Escape') {
      if (this.mode() === 'entry') { this.closeEntry(); }
      return;
    }

    // Ignore shortcuts when typing in inputs
    const tag = (e.target as HTMLElement)?.tagName;
    if (tag === 'INPUT' || tag === 'SELECT' || tag === 'TEXTAREA') return;

    if (this.mode() === 'explorer') {
      // 'C' creates a generic voucher (Payment is the most common type, matching Tally default)
      if (e.key === 'c' || e.key === 'C') {
        this.openNewEntry('PAYMENT');
      } else if (e.key === 'F4') { e.preventDefault(); this.openNewEntry('CONTRA'); }
      else if (e.key === 'F5') { e.preventDefault(); this.openNewEntry('PAYMENT'); }
      else if (e.key === 'F6') { e.preventDefault(); this.openNewEntry('RECEIPT'); }
      else if (e.key === 'F7') { e.preventDefault(); this.openNewEntry('JOURNAL'); }
      else if (e.key === 'F8') { e.preventDefault(); this.openNewEntry('SALES'); }
      else if (e.key === 'F9') { e.preventDefault(); this.openNewEntry('PURCHASE'); }
      else if (e.key === 'Enter') {
        if (this.selectedUuid()) {
          const v = this.pagedVouchers().find(x => x.uuid === this.selectedUuid());
          if (v) this.startEdit(v);
        }
      } else if (e.key === 'Delete') {
        if (this.selectedUuid()) {
          this.deleteVoucher(this.selectedUuid());
        }
      } else if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
        e.preventDefault();
        this.navigateList(e.key === 'ArrowDown' ? 1 : -1);
      }
    }
  }

  /* ── Explorer actions ── */
  openNewEntry(type: VoucherCategory): void {
    this.resetForm();
    this.formType.set(type);
    this.editing.set(false);
    this.mode.set('entry');
  }

  startEdit(v: Voucher): void {
    this.formType.set(v.voucherType);
    this.formDate.set(v.date);
    this.formDebitAccountId.set(v.debitAccountId);
    this.formCreditAccountId.set(v.creditAccountId);
    this.formAmount.set(v.amount);
    this.formNarration.set(v.narration);
    this.formError.set('');
    this.editing.set(true);
    this.editingUuid.set(v.uuid);
    this.editingVoucherNumber.set(v.voucherNumber);
    this.mode.set('entry');
  }

  closeEntry(): void {
    this.mode.set('explorer');
  }

  selectVoucher(uuid: string): void {
    this.selectedUuid.set(uuid);
  }

  deleteVoucher(uuid: string): void {
    this.svc.deleteVoucher(uuid).subscribe();
    if (this.selectedUuid() === uuid) this.selectedUuid.set('');
  }

  postVoucher(uuid: string): void {
    const v = this.allVouchers().find(x => x.uuid === uuid);
    if (!v) return;
    this.svc.postTransaction(uuid).subscribe({
      next: () => this.loadAllVouchers(),
      error: (err) => {
        const detail = err?.error?.message || err?.message || 'Please ensure it is balanced.';
        alert(`Failed to post transaction. ${detail}`);
      },
    });
  }

  /* ── Entry form save ── */
  save(): void {
    const err = this.validate();
    if (err) { this.formError.set(err); return; }

    this.saving.set(true);
    this.formError.set('');

    const type = this.formType();
    const date = this.formDate();
    const drId = this.formDebitAccountId();
    const crId = this.formCreditAccountId();
    const amt  = this.formAmount();
    const narr = this.formNarration();

    const obs$ = this.editing()
      ? this.svc.updateVoucher(this.editingUuid(), type, date, drId, crId, amt, narr)
      : this.svc.createVoucher(type, date, drId, crId, amt, narr);

    obs$.subscribe({
      next: () => {
        this.saving.set(false);
        this.mode.set('explorer');
        this.loadAllVouchers();
      },
      error: (err) => {
        this.saving.set(false);
        const msg: string = err?.error?.message || err?.message || '';
        if (msg.toLowerCase().includes('not balanced') || msg.toLowerCase().includes('unbalanced')) {
          this.formError.set('⚠️ Debit and Credit amounts must be equal.');
        } else if (msg.toLowerCase().includes('at least one debit') || msg.toLowerCase().includes('at least one credit')) {
          this.formError.set('⚠️ Transaction must have both debit and credit entries.');
        } else {
          this.formError.set(msg || 'Failed to save. Please try again.');
        }
      },
    });
  }

  /* ── Filter & sort helpers ── */
  toggleSort(field: SortField): void {
    if (this.sortField() === field) {
      this.sortDir.update(d => d === 'asc' ? 'desc' : 'asc');
    } else {
      this.sortField.set(field);
      this.sortDir.set('asc');
    }
    this.currentPage.set(1);
  }

  clearFilters(): void {
    this.filterSearch.set('');
    this.filterCategory.set('ALL');
    this.filterStatus.set('ALL');
    this.filterDateFrom.set('');
    this.filterDateTo.set('');
    this.currentPage.set(1);
  }

  prevPage(): void {
    this.currentPage.update(p => Math.max(1, p - 1));
  }

  nextPage(): void {
    this.currentPage.update(p => Math.min(this.totalPages(), p + 1));
  }

  /** Resolve account ID to display name */
  getAccountName(id: number, side: 'debit' | 'credit'): string {
    const accounts = side === 'debit' ? this.debitAccounts() : this.creditAccounts();
    const account = accounts.find(a => a.id === id);
    return account ? `${account.accountCode} — ${account.accountName}` : `Account #${id}`;
  }

  /* ── Helpers ── */
  private validate(): string {
    if (!this.formDate()) return 'Date is required.';
    if (!this.formDebitAccountId()) return 'Please select a Debit (To) account.';
    if (!this.formCreditAccountId()) return 'Please select a Credit (By) account.';
    if (this.formDebitAccountId() === this.formCreditAccountId())
      return 'Debit and Credit accounts must be different.';
    if (!this.formAmount() || this.formAmount() <= 0)
      return 'Amount must be greater than zero.';
    return '';
  }

  private resetForm(): void {
    this.formDate.set(this.today());
    this.formDebitAccountId.set(0);
    this.formCreditAccountId.set(0);
    this.formAmount.set(0);
    this.formNarration.set('');
    this.formError.set('');
    this.editingUuid.set('');
    this.editingVoucherNumber.set('');
  }

  private today(): string {
    return new Date().toISOString().slice(0, 10);
  }

  private navigateList(dir: number): void {
    const list = this.pagedVouchers();
    if (!list.length) return;
    const idx = list.findIndex(v => v.uuid === this.selectedUuid());
    const next = Math.max(0, Math.min(list.length - 1, idx + dir));
    this.selectedUuid.set(list[next].uuid);
  }
}
