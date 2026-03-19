import {
  Component, ChangeDetectionStrategy, OnInit, OnDestroy,
  inject, signal, computed, HostListener, effect,
} from '@angular/core';
import { DecimalPipe, LowerCasePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs/operators';
import { VoucherService } from '../../services/voucher.service';
import { AccountMasterService } from '../../services/account-master.service';
import { Voucher, VoucherCategory, VOUCHER_TYPE_CONFIG } from '../../models/voucher.models';

type ViewMode = 'list' | 'form';

/** Map URL slug to VoucherCategory */
const SLUG_TO_CATEGORY: Record<string, VoucherCategory> = {
  contra: 'CONTRA',
  payment: 'PAYMENT',
  receipt: 'RECEIPT',
  journal: 'JOURNAL',
  sales: 'SALES',
  purchase: 'PURCHASE',
};

@Component({
  selector: 'app-voucher-entry',
  standalone: true,
  imports: [DecimalPipe, LowerCasePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './voucher-entry.component.html',
  styleUrl: './voucher-entry.component.scss',
})
export class VoucherEntryComponent implements OnInit, OnDestroy {
  private readonly svc = inject(VoucherService);
  private readonly masterSvc = inject(AccountMasterService);
  private readonly route = inject(ActivatedRoute);

  /* ── Route-driven voucher type ── */
  private readonly routeType = toSignal(
    this.route.paramMap.pipe(map(p => p.get('type') ?? 'contra'))
  );
  readonly voucherCategory = computed<VoucherCategory>(
    () => SLUG_TO_CATEGORY[this.routeType() ?? 'contra'] ?? 'CONTRA'
  );
  readonly typeConfig = computed(() => VOUCHER_TYPE_CONFIG[this.voucherCategory()]);

  /* ── View state ── */
  readonly mode = signal<ViewMode>('list');
  readonly editing = signal(false);
  readonly editingUuid = signal('');
  readonly editingVoucherNumber = signal('');
  readonly saving = signal(false);
  readonly selectedUuid = signal('');

  /* ── Data from service ── */
  readonly vouchers = this.svc.vouchers;

  /** Debit-side accounts filtered by voucher type config */
  readonly debitAccounts = computed(() => {
    const cfg = this.typeConfig();
    const all = this.masterSvc.accounts().filter(a => a.active);
    if (!cfg.debitGroupIds.length) return all;
    return all.filter(a => cfg.debitGroupIds.includes(a.groupId));
  });

  /** Credit-side accounts filtered by voucher type config */
  readonly creditAccounts = computed(() => {
    const cfg = this.typeConfig();
    const all = this.masterSvc.accounts().filter(a => a.active);
    if (!cfg.creditGroupIds.length) return all;
    return all.filter(a => cfg.creditGroupIds.includes(a.groupId));
  });

  readonly totalAmount = computed(() =>
    this.vouchers().reduce((sum, v) => sum + v.amount, 0)
  );

  /* ── Form fields ── */
  readonly formDate = signal(this.today());
  readonly formDebitAccountId = signal(0);
  readonly formCreditAccountId = signal(0);
  readonly formAmount = signal(0);
  readonly formNarration = signal('');
  readonly formError = signal('');

  constructor() {
    // Reactively reload vouchers when the route type changes
    effect(() => {
      const cat = this.voucherCategory();
      this.masterSvc.initialize();
      this.svc.loadVouchers(cat);
      this.mode.set('list');
      this.selectedUuid.set('');
    });
  }

  ngOnInit(): void {
    // Initial load handled by effect above
  }

  ngOnDestroy(): void { /* cleanup if needed */ }

  /* ── Keyboard shortcuts ── */
  @HostListener('window:keydown', ['$event'])
  onKey(e: KeyboardEvent): void {
    if ((e.ctrlKey && e.key === 's') || (e.ctrlKey && e.key === 'Enter')) {
      if (this.mode() === 'form') {
        e.preventDefault();
        this.save();
      }
      return;
    }

    const tag = (e.target as HTMLElement)?.tagName;
    if (tag === 'INPUT' || tag === 'SELECT' || tag === 'TEXTAREA') return;

    if (e.key === 'c' || e.key === 'C') {
      if (this.mode() === 'list') { this.startCreate(); }
    } else if (e.key === 'Escape') {
      if (this.mode() === 'form') { this.backToList(); }
    } else if (e.key === 'Enter') {
      if (this.mode() === 'list' && this.selectedUuid()) {
        const v = this.vouchers().find(x => x.uuid === this.selectedUuid());
        if (v) this.startEdit(v);
      }
    } else if (e.key === 'Delete') {
      if (this.mode() === 'list' && this.selectedUuid()) {
        this.deleteVoucher(this.selectedUuid());
      }
    } else if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
      this.navigateList(e.key === 'ArrowDown' ? 1 : -1);
    }
  }

  /* ── Actions ── */
  startCreate(): void {
    this.resetForm();
    this.editing.set(false);
    this.mode.set('form');
  }

  startEdit(v: Voucher): void {
    this.formDate.set(v.date);
    this.formDebitAccountId.set(v.debitAccountId);
    this.formCreditAccountId.set(v.creditAccountId);
    this.formAmount.set(v.amount);
    this.formNarration.set(v.narration);
    this.formError.set('');
    this.editing.set(true);
    this.editingUuid.set(v.uuid);
    this.editingVoucherNumber.set(v.voucherNumber);
    this.mode.set('form');
  }

  backToList(): void {
    this.mode.set('list');
  }

  selectVoucher(uuid: string): void {
    this.selectedUuid.set(uuid);
  }

  deleteVoucher(uuid: string): void {
    this.svc.deleteVoucher(uuid).subscribe();
    if (this.selectedUuid() === uuid) this.selectedUuid.set('');
  }

  postVoucher(uuid: string): void {
    this.svc.postTransaction(uuid).subscribe({
      next: () => {
        this.svc.loadVouchers(this.voucherCategory());
      },
      error: (err) => {
        console.error('Failed to post transaction:', err);
        const detail = err?.error?.message || err?.message || 'Please ensure it is balanced.';
        alert(`Failed to post transaction. ${detail}`);
      },
    });
  }

  save(): void {
    const err = this.validate();
    if (err) { this.formError.set(err); return; }

    this.saving.set(true);
    this.formError.set('');

    const type = this.voucherCategory();
    const date = this.formDate();
    const drId = this.formDebitAccountId();
    const crId = this.formCreditAccountId();
    const amt = this.formAmount();
    const narr = this.formNarration();

    const obs$ = this.editing()
      ? this.svc.updateVoucher(this.editingUuid(), type, date, drId, crId, amt, narr)
      : this.svc.createVoucher(type, date, drId, crId, amt, narr);

    obs$.subscribe({
      next: () => {
        this.saving.set(false);
        this.backToList();
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
    const list = this.vouchers();
    if (!list.length) return;
    const idx = list.findIndex(v => v.uuid === this.selectedUuid());
    const next = Math.max(0, Math.min(list.length - 1, idx + dir));
    this.selectedUuid.set(list[next].uuid);
  }

  /** Resolve account ID to display name for live preview */
  getAccountName(id: number, side: 'debit' | 'credit'): string {
    const accounts = side === 'debit' ? this.debitAccounts() : this.creditAccounts();
    const account = accounts.find(a => a.id === id);
    return account ? `${account.accountCode} — ${account.accountName}` : `Account #${id}`;
  }
}
