import { Component, ChangeDetectionStrategy, inject, computed, signal, effect } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { map, catchError, tap } from 'rxjs/operators';
import { DecimalPipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { of } from 'rxjs';
import { AccountMasterService } from '../../services/account-master.service';
import { NxPageHeaderComponent, NxSearchInputComponent, NxLoadingSpinnerComponent, NxEmptyStateComponent } from '../../../shared/components';

const TENANT_ID = 'default-tenant';

interface LedgerEntry {
  date: string;
  voucherType: string;
  particulars: string;
  debit: number;
  credit: number;
  balance: number;
}

@Component({
  selector: 'app-ledger',
  standalone: true,
  imports: [DecimalPipe, NxPageHeaderComponent, NxSearchInputComponent, NxLoadingSpinnerComponent, NxEmptyStateComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="ledger-shell">
      <nx-page-header title="Ledger" subtitle="View account-level transaction history">
        @if (selectedAccountId()) {
          <button class="nx-btn nx-btn--ghost no-print" (click)="clearSelection()">← Back to Accounts</button>
        }
      </nx-page-header>

      <!-- Account picker -->
      @if (!selectedAccountId()) {
        <div class="account-picker">
          <h3>Select a Ledger Account</h3>
          <nx-search-input placeholder="Search accounts…" [value]="searchTerm()" (searchChange)="searchTerm.set($event)" />
          <div class="account-list">
            @for (a of filteredAccounts(); track a.id) {
              <div class="account-item" (click)="selectAccount(a.id, a.accountName)">
                <span class="account-code">{{ a.accountCode }}</span>
                <span class="account-name">{{ a.accountName }}</span>
                <span class="account-type">{{ a.accountType }}</span>
              </div>
            } @empty {
              <nx-empty-state icon="📖" title="No accounts found" description="Try a different search term." />
            }
          </div>
        </div>
      }

      <!-- Ledger view for selected account -->
      @if (selectedAccountId()) {
        <div class="ledger-view">
          <div class="ledger-title-bar">
            <h2>{{ selectedAccountName() }}</h2>
          </div>

          @if (loading()) {
            <nx-loading-spinner label="Loading transactions…" />
          }

          @if (!loading() && ledgerEntries().length === 0) {
            <nx-empty-state icon="📖" title="No transactions" description="No transactions found for this account." />
          }

          @if (ledgerEntries().length > 0) {
            <div class="table-wrapper">
            <table class="ledger-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Voucher Type</th>
                  <th>Particulars</th>
                  <th class="num">Debit (₹)</th>
                  <th class="num">Credit (₹)</th>
                  <th class="num">Balance (₹)</th>
                </tr>
              </thead>
              <tbody>
                @for (entry of ledgerEntries(); track $index) {
                  <tr>
                    <td>{{ entry.date }}</td>
                    <td>{{ entry.voucherType }}</td>
                    <td>{{ entry.particulars }}</td>
                    <td class="num">{{ entry.debit ? (entry.debit | number:'1.2-2') : '' }}</td>
                    <td class="num">{{ entry.credit ? (entry.credit | number:'1.2-2') : '' }}</td>
                    <td class="num" [class.balance-positive]="entry.balance >= 0" [class.balance-negative]="entry.balance < 0">
                      {{ (entry.balance < 0 ? -entry.balance : entry.balance) | number:'1.2-2' }}
                      {{ entry.balance >= 0 ? 'Dr' : 'Cr' }}
                    </td>
                  </tr>
                }
              </tbody>
            </table>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
  .ledger-shell { padding: 16px; }
  .ledger-header { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
  .ledger-header h1 { margin: 0; font-size: 1.5rem; }

  /* Date filter bar */
  .date-filter-bar {
    display: flex; align-items: center; gap: 8px; flex-wrap: wrap;
    padding: 10px 16px; background: var(--nx-bg-surface); border-radius: var(--nx-radius-md, 6px);
    border: 1px solid var(--nx-border); margin-bottom: 16px;
  }
  .date-filter-bar label { font-size: 0.8rem; color: var(--nx-text-secondary); }
  .date-input { padding: 5px 10px; border: 1px solid var(--nx-border); border-radius: var(--nx-radius-sm, 4px); font-size: 0.82rem; background: var(--nx-bg-card); color: var(--nx-text-primary); }

  /* Account picker */
  .account-picker { max-width: 600px; }
  .account-picker h3 { margin: 0 0 12px; font-size: 1.1rem; }
  .search-input { width: 100%; padding: 8px 12px; border: 1px solid var(--nx-border); border-radius: var(--nx-radius-md, 6px); margin-bottom: 12px; font-size: 0.9rem; background: var(--nx-bg-card); color: var(--nx-text-primary); }
  .account-list { max-height: 400px; overflow-y: auto; border: 1px solid var(--nx-border); border-radius: var(--nx-radius-md, 6px); }
  .account-item { display: flex; gap: 12px; padding: 10px 14px; cursor: pointer; border-bottom: 1px solid var(--nx-border); transition: background 0.15s; }
  .account-item:hover { background: var(--nx-bg-card-hover); }
  .account-code { font-weight: 600; min-width: 80px; color: var(--nx-text-secondary); font-family: var(--nx-font-mono, monospace); font-size: 0.82rem; }
  .account-name { flex: 1; }
  .account-type { font-size: 0.75rem; color: var(--nx-text-muted); text-transform: uppercase; }

  /* Ledger view */
  .ledger-title-bar { display: flex; align-items: center; gap: 16px; margin-bottom: 16px; }
  .ledger-title-bar h2 { margin: 0; font-size: 1.2rem; }
  .btn { padding: 6px 14px; border-radius: var(--nx-radius-sm, 4px); cursor: pointer; border: 1px solid var(--nx-border); background: var(--nx-bg-card); color: var(--nx-text-primary); font-size: 0.82rem; transition: background 0.15s; }
  .btn-secondary:hover { background: var(--nx-bg-card-hover); }
  .input { padding: 8px 12px; border: 1px solid var(--nx-border); border-radius: var(--nx-radius-sm, 4px); }

  /* Loading + empty */
  .loading { padding: 24px; text-align: center; color: var(--nx-text-muted); }
  .empty-state { padding: 48px; text-align: center; color: var(--nx-text-muted); }

  /* Ledger table */
  .table-wrapper { overflow-x: auto; }
  .ledger-table { width: 100%; border-collapse: collapse; }
  .ledger-table thead tr { position: sticky; top: 0; z-index: 10; }
  .ledger-table th { background: var(--nx-bg-surface); padding: 8px 12px; text-align: left; font-size: 0.75rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; color: var(--nx-text-muted); border-bottom: 2px solid var(--nx-border); white-space: nowrap; }
  .ledger-table td { padding: 8px 12px; border-bottom: 1px solid var(--nx-border); font-size: 0.82rem; color: var(--nx-text-primary); }
  .ledger-table tbody tr { transition: background 0.12s; }
  .ledger-table tbody tr:hover { background: var(--nx-bg-card-hover); }
  .ledger-table tbody tr:nth-child(even) { background: var(--nx-bg-surface); }
  .ledger-table tbody tr:nth-child(even):hover { background: var(--nx-bg-card-hover); }
  .ledger-table tfoot tr td { font-weight: 700; border-top: 2px solid var(--nx-border); background: var(--nx-bg-surface); }
  .ledger-table .num { text-align: right; font-family: var(--nx-font-mono, monospace); font-variant-numeric: tabular-nums; }
  .balance-positive { color: var(--nx-success, #4caf50); font-weight: 600; }
  .balance-negative { color: var(--nx-danger, #ef5350); font-weight: 600; }
  @media print {
    .no-print { display: none !important; }
    .ledger-table thead tr { position: static; }
  }
`]
})
export class LedgerComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly http = inject(HttpClient);
  private readonly masterSvc = inject(AccountMasterService);

  private readonly params = toSignal(this.route.paramMap.pipe(map(p => p.get('name'))));

  readonly searchTerm = signal('');
  readonly selectedAccountId = signal<number>(0);
  readonly selectedAccountName = signal('');
  readonly ledgerEntries = signal<LedgerEntry[]>([]);
  readonly loading = signal(false);

  readonly filteredAccounts = computed(() => {
    const term = this.searchTerm().toLowerCase();
    const all = this.masterSvc.accounts().filter(a => a.active);
    if (!term) return all;
    return all.filter(a =>
      a.accountName.toLowerCase().includes(term) ||
      a.accountCode.toLowerCase().includes(term)
    );
  });

  constructor() {
    this.masterSvc.initialize();

    // If route has :name param, try to find and select that account
    effect(() => {
      const name = this.params();
      if (name) {
        const accounts = this.masterSvc.accounts();
        const found = accounts.find(a =>
          a.accountName.toLowerCase() === name.toLowerCase() ||
          a.accountCode.toLowerCase() === name.toLowerCase()
        );
        if (found) {
          this.selectAccount(found.id, found.accountName);
        }
      }
    });
  }

  selectAccount(id: number, name: string): void {
    this.selectedAccountId.set(id);
    this.selectedAccountName.set(name);
    this.loadLedger(id);
  }

  clearSelection(): void {
    this.selectedAccountId.set(0);
    this.selectedAccountName.set('');
    this.ledgerEntries.set([]);
  }

  private loadLedger(accountId: number): void {
    this.loading.set(true);
    this.http.get<any[]>('/api/journal/transactions', {
      params: { tenantId: TENANT_ID },
    }).pipe(
      map(txns => {
        const entries: LedgerEntry[] = [];
        let balance = 0;

        // Get opening balance from account
        const account = this.masterSvc.accounts().find(a => a.id === accountId);
        if (account?.openingBalance) {
          balance = account.openingBalance;
          entries.push({
            date: 'Opening',
            voucherType: '',
            particulars: 'Opening Balance',
            debit: balance > 0 ? balance : 0,
            credit: balance < 0 ? -balance : 0,
            balance,
          });
        }

        // Sort by date
        const sorted = txns.sort((a: any, b: any) =>
          (a.transactionDate ?? '').localeCompare(b.transactionDate ?? '')
        );

        for (const txn of sorted) {
          const relevantEntries = (txn.entries ?? []).filter(
            (e: any) => e.account?.id === accountId
          );
          if (relevantEntries.length === 0) continue;

          let voucherType = 'JOURNAL';
          try { voucherType = JSON.parse(txn.metadata || '{}').voucherType ?? 'JOURNAL'; }
          catch { /* ignore */ }

          // Find the counterpart account names for "Particulars"
          const otherAccounts = (txn.entries ?? [])
            .filter((e: any) => e.account?.id !== accountId)
            .map((e: any) => e.account?.accountName ?? 'Unknown')
            .join(', ');

          for (const entry of relevantEntries) {
            const dr = entry.entryType === 'DEBIT' ? entry.amount : 0;
            const cr = entry.entryType === 'CREDIT' ? entry.amount : 0;
            balance += dr - cr;

            entries.push({
              date: txn.transactionDate,
              voucherType,
              particulars: otherAccounts || txn.description || '',
              debit: dr,
              credit: cr,
              balance,
            });
          }
        }

        return entries;
      }),
      tap(entries => {
        this.ledgerEntries.set(entries);
        this.loading.set(false);
      }),
      catchError(() => {
        this.loading.set(false);
        return of([]);
      }),
    ).subscribe();
  }
}
