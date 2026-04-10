import {
  Component, ChangeDetectionStrategy, OnInit,
  inject, signal, computed, HostListener,
} from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { AccountMasterService } from '../../../accounting/services/account-master.service';
import { LedgerAccount, AccountGroup, AccountType } from '../../../accounting/models/voucher.models';
import { NxPageHeaderComponent, NxSearchInputComponent, NxEmptyStateComponent } from '../../../shared/components';

type ViewMode = 'list' | 'form';
type TabMode = 'accounts' | 'groups';

/** Map group nature → default AccountType */
const NATURE_TO_TYPE: Record<string, AccountType> = {
  Assets: 'ASSET',
  Liabilities: 'LIABILITY',
  Income: 'REVENUE',
  Expenses: 'EXPENSE',
};

@Component({
  selector: 'app-master',
  standalone: true,
  imports: [DecimalPipe, NxPageHeaderComponent, NxSearchInputComponent, NxEmptyStateComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './master.component.html',
  styleUrl: './master.component.scss',
})
export class MasterComponent implements OnInit {
  private readonly masterSvc = inject(AccountMasterService);

  /* ── View state ── */
  readonly tab = signal<TabMode>('accounts');
  readonly mode = signal<ViewMode>('list');
  readonly selectedId = signal(0);
  readonly searchTerm = signal('');
  readonly filterGroupId = signal(0);

  /* ── Data from service ── */
  readonly accounts = this.masterSvc.accounts;
  readonly groups = this.masterSvc.groups;

  /* ── Filter ── */
  readonly filteredAccounts = computed(() => {
    let list = this.accounts();
    const term = this.searchTerm().toLowerCase();
    const gid = this.filterGroupId();
    if (term) {
      list = list.filter(a =>
        a.accountName.toLowerCase().includes(term) ||
        a.accountCode.toLowerCase().includes(term) ||
        (a.groupName ?? '').toLowerCase().includes(term)
      );
    }
    if (gid) {
      list = list.filter(a => a.groupId === gid);
    }
    return list;
  });

  /* ── Form fields ── */
  readonly editingAccount = signal<LedgerAccount | null>(null);
  readonly fName = signal('');
  readonly fCode = signal('');
  readonly fGroupId = signal(0);
  readonly fType = signal<AccountType | ''>('');
  readonly fBalance = signal(0);
  readonly fBalanceDir = signal('Dr');
  readonly fMailingName = signal('');
  readonly fAddress = signal('');
  readonly formError = signal('');

  ngOnInit(): void {
    this.masterSvc.initialize();
  }

  /* ── Keyboard shortcuts ── */
  @HostListener('window:keydown', ['$event'])
  onKey(e: KeyboardEvent): void {
    if (e.ctrlKey && e.key === 's') {
      if (this.mode() === 'form') { e.preventDefault(); this.saveAccount(); }
      return;
    }
    const tag = (e.target as HTMLElement)?.tagName;
    if (tag === 'INPUT' || tag === 'SELECT' || tag === 'TEXTAREA') return;

    if (e.key === 'c' || e.key === 'C') {
      if (this.mode() === 'list' && this.tab() === 'accounts') this.startCreate();
    } else if (e.key === 'Escape') {
      if (this.mode() === 'form') this.backToList();
    } else if (e.key === 'Enter') {
      if (this.mode() === 'list' && this.selectedId()) {
        const a = this.accounts().find(x => x.id === this.selectedId());
        if (a) this.editAccount(a);
      }
    } else if (e.key === 'Delete') {
      if (this.mode() === 'list' && this.selectedId()) {
        this.deleteAccount(this.selectedId());
      }
    }
  }

  /* ── Actions ── */
  startCreate(): void {
    this.resetForm();
    this.mode.set('form');
  }

  editAccount(a: LedgerAccount): void {
    this.editingAccount.set(a);
    this.fName.set(a.accountName);
    this.fCode.set(a.accountCode);
    this.fGroupId.set(a.groupId);
    this.fType.set(a.accountType);
    this.fBalance.set(Math.abs(a.openingBalance));
    this.fBalanceDir.set(a.openingBalance >= 0 ? 'Dr' : 'Cr');
    this.fMailingName.set(a.mailingName ?? '');
    this.fAddress.set(a.address ?? '');
    this.formError.set('');
    this.mode.set('form');
  }

  deleteAccount(id: number): void {
    this.masterSvc.deleteAccount(id);
    if (this.selectedId() === id) this.selectedId.set(0);
  }

  backToList(): void {
    this.mode.set('list');
    this.editingAccount.set(null);
  }

  onGroupChange(groupId: number): void {
    this.fGroupId.set(groupId);
    const group = this.masterSvc.groupById(groupId);
    if (group) {
      this.fType.set(NATURE_TO_TYPE[group.nature] ?? 'ASSET');
    }
  }

  saveAccount(): void {
    const err = this.validateForm();
    if (err) { this.formError.set(err); return; }

    const balance = this.fBalance() * (this.fBalanceDir() === 'Cr' ? -1 : 1);

    if (this.editingAccount()) {
      this.masterSvc.updateAccount(this.editingAccount()!.id, {
        accountName: this.fName(),
        accountCode: this.fCode(),
        groupId: this.fGroupId(),
        accountType: this.fType() as AccountType,
        openingBalance: balance,
        mailingName: this.fMailingName() || undefined,
        address: this.fAddress() || undefined,
      });
    } else {
      this.masterSvc.createAccount({
        accountName: this.fName(),
        accountCode: this.fCode(),
        accountType: this.fType() as AccountType,
        groupId: this.fGroupId(),
        openingBalance: balance,
        mailingName: this.fMailingName() || undefined,
        address: this.fAddress() || undefined,
        active: true,
      });
    }

    this.backToList();
  }

  updateBalance(val: number): void {
    this.fBalance.set(val);
  }

  /* ── Helpers ── */
  parentName(parentId: number | null): string {
    if (parentId === null) return '— Primary —';
    return this.groups().find(g => g.id === parentId)?.name ?? '';
  }

  accountCountForGroup(groupId: number): number {
    return this.accounts().filter(a => a.groupId === groupId).length;
  }

  abs(n: number): number { return Math.abs(n); }

  private validateForm(): string {
    if (!this.fName().trim()) return 'Account Name is required.';
    if (!this.fCode().trim()) return 'Account Code is required.';
    if (!this.fGroupId()) return 'Please select an Under Group.';

    // Check duplicate code (skip self when editing)
    const existing = this.accounts().find(a =>
      a.accountCode === this.fCode().trim() &&
      a.id !== (this.editingAccount()?.id ?? -1)
    );
    if (existing) return `Account Code "${this.fCode()}" already exists.`;

    return '';
  }

  private resetForm(): void {
    this.editingAccount.set(null);
    this.fName.set('');
    this.fCode.set('');
    this.fGroupId.set(0);
    this.fType.set('');
    this.fBalance.set(0);
    this.fBalanceDir.set('Dr');
    this.fMailingName.set('');
    this.fAddress.set('');
    this.formError.set('');
  }
}
