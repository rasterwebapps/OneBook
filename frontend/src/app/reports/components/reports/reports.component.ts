import { Component, ChangeDetectionStrategy, inject, computed, effect, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs/operators';
import { DecimalPipe } from '@angular/common';
import { ReportsService } from '../../services/reports.service';
import { NxPageHeaderComponent, NxLoadingSpinnerComponent, NxEmptyStateComponent } from '../../../shared/components';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [DecimalPipe, NxPageHeaderComponent, NxLoadingSpinnerComponent, NxEmptyStateComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="reports-shell">
      <nx-page-header [title]="reportLabel()" subtitle="Financial report for the current period">
        @if (reportType() === 'trial-balance') {
          <div class="date-filter">
            <label>From: <input type="date" [value]="fromDate()" (change)="onFromDate($event)" class="date-input"></label>
            <label>To: <input type="date" [value]="toDate()" (change)="onToDate($event)" class="date-input"></label>
            <button class="nx-btn nx-btn--emerald" (click)="applyDateFilter()">Apply</button>
            <button class="nx-btn nx-btn--ghost" (click)="clearDateFilter()">Clear</button>
            <button class="nx-btn nx-btn--outline" (click)="svc.exportTrialBalance()" title="Export as JSON">⬇ Export</button>
          </div>
        }
        <button class="nx-btn nx-btn--outline" (click)="reload()">🔄 Refresh</button>
      </nx-page-header>

      @if (svc.loading()) {
        <nx-loading-spinner label="Loading report data…" />
      }

      <!-- ═══ TRIAL BALANCE ═══ -->
      @if (reportType() === 'trial-balance') {
        @if (svc.trialBalance(); as tb) {
          @if (tb.lines.length === 0) {
            <nx-empty-state
              icon="📊"
              title="No transactions found"
              description="No posted transactions found for the selected period."
            />
          } @else {
            <table class="report-table">
              <thead>
                <tr><th>Account Code</th><th>Account Name</th><th>Type</th><th class="num">Debit (₹)</th><th class="num">Credit (₹)</th></tr>
              </thead>
              <tbody>
                @for (line of tb.lines; track line.accountId) {
                  <tr>
                    <td>{{ line.accountCode }}</td>
                    <td>{{ line.accountName }}</td>
                    <td>{{ line.accountType }}</td>
                    <td class="num">{{ line.totalDebits | number:'1.2-2' }}</td>
                    <td class="num">{{ line.totalCredits | number:'1.2-2' }}</td>
                  </tr>
                }
              </tbody>
              <tfoot>
                <tr class="total-row">
                  <td colspan="3">Total</td>
                  <td class="num">{{ tb.totalDebits | number:'1.2-2' }}</td>
                  <td class="num">{{ tb.totalCredits | number:'1.2-2' }}</td>
                </tr>
                <tr><td colspan="5" class="balance-status">{{ tb.balanced ? '✅ Balanced' : '⚠️ Unbalanced' }}</td></tr>
              </tfoot>
            </table>
          }
        } @else if (!svc.loading()) {
          <nx-empty-state
            icon="📊"
            title="No trial balance data"
            description="Post some transactions to view the report."
          />
        }
      }

      <!-- ═══ PROFIT & LOSS ═══ -->
      @if (reportType() === 'profit-loss' && svc.profitAndLoss(); as pl) {
        <div class="pl-sections">
          <div class="pl-section">
            <h3>Revenue / Income</h3>
            <table class="report-table">
              <thead><tr><th>Account</th><th class="num">Credit (₹)</th></tr></thead>
              <tbody>
                @for (line of pl.revenueLines; track line.accountId) {
                  <tr><td>{{ line.accountName }}</td><td class="num">{{ line.totalCredits | number:'1.2-2' }}</td></tr>
                }
              </tbody>
              <tfoot><tr class="total-row"><td>Total Revenue</td><td class="num">{{ pl.totalRevenue | number:'1.2-2' }}</td></tr></tfoot>
            </table>
          </div>
          <div class="pl-section">
            <h3>Expenses</h3>
            <table class="report-table">
              <thead><tr><th>Account</th><th class="num">Debit (₹)</th></tr></thead>
              <tbody>
                @for (line of pl.expenseLines; track line.accountId) {
                  <tr><td>{{ line.accountName }}</td><td class="num">{{ line.totalDebits | number:'1.2-2' }}</td></tr>
                }
              </tbody>
              <tfoot><tr class="total-row"><td>Total Expenses</td><td class="num">{{ pl.totalExpenses | number:'1.2-2' }}</td></tr></tfoot>
            </table>
          </div>
          <div class="net-income" [class.profit]="pl.netIncome >= 0" [class.loss]="pl.netIncome < 0">
            <strong>{{ pl.netIncome >= 0 ? 'Net Profit' : 'Net Loss' }}:</strong>
            ₹{{ (pl.netIncome < 0 ? -pl.netIncome : pl.netIncome) | number:'1.2-2' }}
          </div>
        </div>
      }

      <!-- ═══ BALANCE SHEET ═══ -->
      @if (reportType() === 'balance-sheet' && svc.balanceSheet(); as bs) {
        @if (!bs.balanced) {
          <div class="bs-unbalanced-banner" role="alert" aria-live="assertive">
            <svg viewBox="0 0 24 24" class="banner-icon" aria-hidden="true">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
              <line x1="12" y1="9" x2="12" y2="13"/>
              <line x1="12" y1="17" x2="12.01" y2="17"/>
            </svg>
            <div class="banner-body">
              <span class="banner-title">Balance Sheet Unbalanced</span>
              <span class="banner-desc">Assets ({{ bs.totalAssets | number:'1.2-2' }}) ≠ Liabilities + Equity ({{ bs.totalLiabilities + bs.totalEquity | number:'1.2-2' }}). Review your chart of accounts.</span>
            </div>
          </div>
        }
        <div class="bs-grid">
          <!-- Assets card -->
          <div class="bs-card bs-card--assets">
            <div class="bs-card-header">
              <div class="bs-card-icon-wrap bs-card-icon--assets">
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
                  <line x1="8" y1="21" x2="16" y2="21"/>
                  <line x1="12" y1="17" x2="12" y2="21"/>
                </svg>
              </div>
              <div>
                <div class="bs-card-label">Assets</div>
                <div class="bs-card-subtitle">Resources owned or controlled</div>
              </div>
            </div>
            <ul class="bs-list" role="list">
              @for (line of bs.assetLines; track line.accountId) {
                <li class="bs-list-item">
                  <span class="bs-account-name">{{ line.accountName }}</span>
                  <span class="bs-amount">{{ line.totalDebits - line.totalCredits | number:'1.2-2' }}</span>
                </li>
              }
              @if (bs.assetLines.length === 0) {
                <li class="bs-list-empty">No asset accounts recorded</li>
              }
            </ul>
            <div class="bs-total-row">
              <span class="bs-total-label">Total Assets</span>
              <span class="bs-total-amount">{{ bs.totalAssets | number:'1.2-2' }}</span>
            </div>
          </div>

          <!-- Liabilities card -->
          <div class="bs-card bs-card--liabilities">
            <div class="bs-card-header">
              <div class="bs-card-icon-wrap bs-card-icon--liabilities">
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                  <path d="M2 17l10 5 10-5"/>
                  <path d="M2 12l10 5 10-5"/>
                </svg>
              </div>
              <div>
                <div class="bs-card-label">Liabilities</div>
                <div class="bs-card-subtitle">Obligations and debts owed</div>
              </div>
            </div>
            <ul class="bs-list" role="list">
              @for (line of bs.liabilityLines; track line.accountId) {
                <li class="bs-list-item">
                  <span class="bs-account-name">{{ line.accountName }}</span>
                  <span class="bs-amount">{{ line.totalCredits - line.totalDebits | number:'1.2-2' }}</span>
                </li>
              }
              @if (bs.liabilityLines.length === 0) {
                <li class="bs-list-empty">No liability accounts recorded</li>
              }
            </ul>
            <div class="bs-total-row">
              <span class="bs-total-label">Total Liabilities</span>
              <span class="bs-total-amount">{{ bs.totalLiabilities | number:'1.2-2' }}</span>
            </div>
          </div>

          <!-- Equity card -->
          <div class="bs-card bs-card--equity">
            <div class="bs-card-header">
              <div class="bs-card-icon-wrap bs-card-icon--equity">
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <circle cx="12" cy="12" r="10"/>
                  <line x1="12" y1="8" x2="12" y2="16"/>
                  <line x1="8" y1="12" x2="16" y2="12"/>
                </svg>
              </div>
              <div>
                <div class="bs-card-label">Equity</div>
                <div class="bs-card-subtitle">Owner's interest in the business</div>
              </div>
            </div>
            <ul class="bs-list" role="list">
              @for (line of bs.equityLines; track line.accountId) {
                <li class="bs-list-item">
                  <span class="bs-account-name">{{ line.accountName }}</span>
                  <span class="bs-amount">{{ line.totalCredits - line.totalDebits | number:'1.2-2' }}</span>
                </li>
              }
              @if (bs.equityLines.length === 0) {
                <li class="bs-list-empty">No equity accounts recorded</li>
              }
            </ul>
            <div class="bs-total-row">
              <span class="bs-total-label">Total Equity</span>
              <span class="bs-total-amount">{{ bs.totalEquity | number:'1.2-2' }}</span>
            </div>
          </div>
        </div>

        <!-- Accounting equation summary -->
        @if (bs.balanced) {
          <div class="bs-equation">
            <div class="bs-eq-item">
              <span class="bs-eq-label">Total Assets</span>
              <span class="bs-eq-value">{{ bs.totalAssets | number:'1.2-2' }}</span>
            </div>
            <span class="bs-eq-sep">=</span>
            <div class="bs-eq-item">
              <span class="bs-eq-label">Liabilities</span>
              <span class="bs-eq-value">{{ bs.totalLiabilities | number:'1.2-2' }}</span>
            </div>
            <span class="bs-eq-sep">+</span>
            <div class="bs-eq-item">
              <span class="bs-eq-label">Equity</span>
              <span class="bs-eq-value">{{ bs.totalEquity | number:'1.2-2' }}</span>
            </div>
            <div class="bs-eq-balanced">
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
              Balanced
            </div>
          </div>
        }
      }

      <!-- ═══ CASH FLOW ═══ -->
      @if (reportType() === 'cash-flow' && svc.cashFlow(); as cf) {
        <div class="cf-sections">
          <div class="cf-section">
            <h3>Operating Activities</h3>
            <table class="report-table">
              <tbody>
                @for (line of cf.operatingActivities; track line.accountName) {
                  <tr><td>{{ line.accountName }}</td><td class="num">{{ line.amount | number:'1.2-2' }}</td></tr>
                }
              </tbody>
              <tfoot><tr class="total-row"><td>Net Cash from Operating</td><td class="num">{{ cf.netCashFromOperating | number:'1.2-2' }}</td></tr></tfoot>
            </table>
          </div>
          <div class="cf-section">
            <h3>Investing Activities</h3>
            <table class="report-table">
              <tbody>
                @for (line of cf.investingActivities; track line.accountName) {
                  <tr><td>{{ line.accountName }}</td><td class="num">{{ line.amount | number:'1.2-2' }}</td></tr>
                }
              </tbody>
              <tfoot><tr class="total-row"><td>Net Cash from Investing</td><td class="num">{{ cf.netCashFromInvesting | number:'1.2-2' }}</td></tr></tfoot>
            </table>
          </div>
          <div class="cf-section">
            <h3>Financing Activities</h3>
            <table class="report-table">
              <tbody>
                @for (line of cf.financingActivities; track line.accountName) {
                  <tr><td>{{ line.accountName }}</td><td class="num">{{ line.amount | number:'1.2-2' }}</td></tr>
                }
              </tbody>
              <tfoot><tr class="total-row"><td>Net Cash from Financing</td><td class="num">{{ cf.netCashFromFinancing | number:'1.2-2' }}</td></tr></tfoot>
            </table>
          </div>
          <div class="net-income">
            <strong>Net Cash Change:</strong> ₹{{ cf.netCashChange | number:'1.2-2' }}
          </div>
        </div>
      }

      <!-- ═══ DAY BOOK ═══ -->
      @if (reportType() === 'daybook') {
        @if (svc.daybook().length === 0 && !svc.loading()) {
          <nx-empty-state
            icon="📒"
            title="No transactions recorded"
            description="Start creating vouchers to see them in the Day Book."
          />
        } @else {
          <table class="report-table">
            <thead>
              <tr><th>Date</th><th>Type</th><th>Account</th><th>Dr/Cr</th><th class="num">Amount (₹)</th><th>Narration</th></tr>
            </thead>
            <tbody>
              @for (txn of svc.daybook(); track txn.transactionUuid) {
                @for (entry of txn.entries; track $index) {
                  <tr>
                    @if ($index === 0) {
                      <td [attr.rowspan]="txn.entries.length">{{ txn.transactionDate }}</td>
                      <td [attr.rowspan]="txn.entries.length">{{ txn.voucherType }}</td>
                    }
                    <td>{{ entry.accountName }}</td>
                    <td>{{ entry.entryType }}</td>
                    <td class="num">{{ entry.amount | number:'1.2-2' }}</td>
                    @if ($index === 0) {
                      <td [attr.rowspan]="txn.entries.length">{{ txn.description }}</td>
                    }
                  </tr>
                }
              }
            </tbody>
          </table>
        }
      }
    </div>
  `,
  styles: [`
  .reports-shell { padding: 16px; }
  .date-filter { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
  .date-filter label { font-size: var(--nx-text-sm); display: flex; align-items: center; gap: 4px; color: var(--nx-text-secondary); }
  .date-input { padding: 5px 10px; border: 1px solid var(--nx-border); border-radius: var(--nx-radius-sm); font-size: var(--nx-text-sm); background: var(--nx-bg-card); color: var(--nx-text-primary); }

  /* Report table */
  .report-table { width: 100%; border-collapse: collapse; margin-bottom: 16px; }
  .report-table thead tr { position: sticky; top: 0; z-index: 10; }
  .report-table th { background: var(--nx-bg-surface); padding: 8px 12px; font-weight: 600; font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--nx-text-muted); border-bottom: 2px solid var(--nx-border); text-align: left; }
  .report-table td { padding: 8px 12px; border-bottom: 1px solid var(--nx-border); font-size: var(--nx-text-sm); color: var(--nx-text-primary); }
  .report-table tbody tr:hover { background: var(--nx-bg-card-hover); }
  .report-table tbody tr:nth-child(even) { background: var(--nx-bg-surface); }
  .report-table tbody tr:nth-child(even):hover { background: var(--nx-bg-card-hover); }
  .report-table .num { text-align: right; font-family: var(--nx-font-mono, monospace); font-variant-numeric: tabular-nums; }
  .total-row td { font-weight: 700; border-top: 2px solid var(--nx-border); background: var(--nx-bg-surface); }
  .balance-status { text-align: center; padding: 8px; font-weight: 600; }

  /* P&L / BS sections */
  .pl-sections, .cf-sections { display: flex; flex-wrap: wrap; gap: 24px; }
  .pl-section, .cf-section { flex: 1; min-width: 300px; background: var(--nx-bg-card); border: 1px solid var(--nx-border); border-radius: var(--nx-radius-lg, 8px); padding: 16px; box-shadow: var(--nx-shadow-sm); }
  .pl-section h3, .cf-section h3 { margin: 0 0 12px; font-size: 1rem; color: var(--nx-text-primary); padding-bottom: 8px; border-bottom: 1px solid var(--nx-border); }
  .net-income { padding: 14px 18px; border-radius: var(--nx-radius-lg, 8px); font-size: 1.05rem; font-weight: 600; margin-top: 12px; width: 100%; box-sizing: border-box; background: var(--nx-bg-card); border: 1px solid var(--nx-border); }
  .net-income.profit { background: rgba(76,175,80,0.08); color: var(--nx-success, #4caf50); border-color: rgba(76,175,80,0.3); }
  .net-income.loss { background: rgba(239,83,80,0.08); color: var(--nx-danger, #ef5350); border-color: rgba(239,83,80,0.3); }

  /* ── Balance Sheet overhaul ── */
  :host svg { fill: none; stroke: currentColor; stroke-linecap: round; stroke-linejoin: round; }

  .bs-unbalanced-banner {
    display: flex; align-items: flex-start; gap: 14px;
    padding: 14px 18px; margin-bottom: 20px;
    background: rgba(239, 68, 68, 0.08);
    border: 1px solid rgba(239, 68, 68, 0.35);
    border-left: 4px solid #ef4444;
    border-radius: 8px;
    .banner-icon { width: 22px; height: 22px; flex-shrink: 0; margin-top: 1px; stroke: #ef4444; stroke-width: 2; fill: rgba(239,68,68,0.1); }
    .banner-body { display: flex; flex-direction: column; gap: 2px; }
    .banner-title { font-weight: 700; font-size: 0.925rem; color: #ef4444; }
    .banner-desc { font-size: 0.825rem; color: var(--nx-text-secondary); line-height: 1.4; }
  }

  .bs-grid {
    display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 16px;
    @media (max-width: 1024px) { grid-template-columns: repeat(2, 1fr); }
    @media (max-width: 640px) { grid-template-columns: 1fr; }
  }

  .bs-card {
    display: flex; flex-direction: column;
    background: var(--nx-glass-bg, rgba(255,255,255,0.04));
    backdrop-filter: blur(8px); -webkit-backdrop-filter: blur(8px);
    border: 1px solid var(--nx-glass-border, rgba(255,255,255,0.1));
    border-radius: 10px; overflow: hidden;
    box-shadow: 0 1px 3px rgba(0,0,0,0.12), 0 4px 12px rgba(0,0,0,0.08);
    transition: box-shadow 0.2s ease, transform 0.2s ease;
    &:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.18); transform: translateY(-1px); }
  }

  .bs-card-header {
    display: flex; align-items: center; gap: 12px;
    padding: 14px 16px 12px;
    border-bottom: 1px solid var(--nx-border);
    background: var(--nx-bg-surface);
  }

  .bs-card-icon-wrap {
    width: 36px; height: 36px; flex-shrink: 0; border-radius: 8px;
    display: flex; align-items: center; justify-content: center;
    svg { width: 18px; height: 18px; stroke-width: 1.75; }
  }
  .bs-card-icon--assets { background: rgba(14,165,233,0.12); color: #0ea5e9; }
  .bs-card-icon--liabilities { background: rgba(245,158,11,0.12); color: #f59e0b; }
  .bs-card-icon--equity { background: rgba(139,92,246,0.12); color: #8b5cf6; }

  .bs-card-label { font-weight: 700; font-size: 0.9rem; color: var(--nx-text-primary); line-height: 1.2; }
  .bs-card-subtitle { font-size: 0.72rem; color: var(--nx-text-muted); margin-top: 1px; }

  .bs-card--assets .bs-card-label { color: #0ea5e9; }
  .bs-card--liabilities .bs-card-label { color: #f59e0b; }
  .bs-card--equity .bs-card-label { color: #8b5cf6; }

  .bs-list {
    flex: 1; list-style: none; margin: 0; padding: 8px 0;
    overflow-y: auto; max-height: 320px;
  }

  .bs-list-item {
    display: flex; align-items: baseline; justify-content: space-between;
    padding: 7px 16px; gap: 12px;
    transition: background 0.1s ease;
    &:hover { background: var(--nx-bg-card-hover, rgba(255,255,255,0.04)); }
    & + & { border-top: 1px solid var(--nx-border); }
  }

  .bs-account-name { font-size: 0.85rem; color: var(--nx-text-primary); line-height: 1.3; flex: 1; min-width: 0; }
  .bs-amount {
    font-family: var(--nx-font-mono, 'JetBrains Mono', monospace);
    font-size: 0.85rem; font-weight: 500;
    color: var(--nx-text-primary);
    font-variant-numeric: tabular-nums;
    white-space: nowrap; flex-shrink: 0;
  }

  .bs-list-empty { padding: 16px; font-size: 0.825rem; color: var(--nx-text-muted); text-align: center; font-style: italic; }

  .bs-total-row {
    display: flex; align-items: center; justify-content: space-between;
    padding: 11px 16px;
    border-top: 2px solid var(--nx-border);
    background: var(--nx-bg-surface);
    margin-top: auto;
    .bs-total-label { font-size: 0.85rem; font-weight: 700; color: var(--nx-text-primary); }
    .bs-total-amount {
      font-family: var(--nx-font-mono, 'JetBrains Mono', monospace);
      font-size: 1rem; font-weight: 700;
      color: var(--nx-emerald, #10b981);
      font-variant-numeric: tabular-nums;
      letter-spacing: -0.01em;
    }
  }

  .bs-card--liabilities .bs-total-row .bs-total-amount { color: #f59e0b; }
  .bs-card--equity .bs-total-row .bs-total-amount { color: #8b5cf6; }

  .bs-equation {
    display: flex; align-items: center; justify-content: center; flex-wrap: wrap;
    gap: 16px; padding: 16px 20px;
    background: var(--nx-bg-surface);
    border: 1px solid var(--nx-border);
    border-radius: 10px;
    .bs-eq-item { display: flex; flex-direction: column; align-items: center; gap: 2px; }
    .bs-eq-label { font-size: 0.7rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; color: var(--nx-text-muted); }
    .bs-eq-value { font-family: var(--nx-font-mono, monospace); font-size: 1.1rem; font-weight: 700; color: var(--nx-text-primary); font-variant-numeric: tabular-nums; }
    .bs-eq-sep { font-size: 1.4rem; font-weight: 300; color: var(--nx-text-muted); align-self: center; padding-top: 16px; }
    .bs-eq-balanced { display: flex; align-items: center; gap: 6px; padding: 5px 12px; background: rgba(16,185,129,0.1); border: 1px solid rgba(16,185,129,0.3); border-radius: 20px; font-size: 0.82rem; font-weight: 600; color: var(--nx-emerald, #10b981); align-self: center; margin-left: 8px;
      svg { width: 14px; height: 14px; stroke: var(--nx-emerald, #10b981); stroke-width: 2.5; }
    }
  }

  @media print {
    .date-filter { display: none !important; }
    .pl-sections, .bs-sections, .cf-sections { flex-direction: column; }
    .report-table thead tr { position: static; }
  }
`]
})
export class ReportsComponent {
  readonly svc = inject(ReportsService);
  private readonly route = inject(ActivatedRoute);
  private readonly params = toSignal(this.route.paramMap.pipe(map(p => p.get('type') ?? 'unknown')));
  readonly reportType = computed(() => this.params() ?? 'unknown');

  readonly fromDate = signal(this.getFirstDayOfMonth());
  readonly toDate = signal(this.getLastDayOfMonth());

  readonly reportLabel = computed(() => {
    const labels: Record<string, string> = {
      'trial-balance': 'Trial Balance',
      'profit-loss': 'Profit & Loss',
      'balance-sheet': 'Balance Sheet',
      'cash-flow': 'Cash Flow Statement',
      'daybook': 'Day Book',
    };
    return labels[this.reportType()] ?? this.reportType();
  });

  constructor() {
    effect(() => {
      const type = this.reportType();
      this.loadReport(type);
    });
  }

  reload(): void {
    this.loadReport(this.reportType());
  }

  onFromDate(event: Event): void {
    this.fromDate.set((event.target as HTMLInputElement).value);
  }

  onToDate(event: Event): void {
    this.toDate.set((event.target as HTMLInputElement).value);
  }

  applyDateFilter(): void {
    this.svc.loadTrialBalance(this.fromDate() || undefined, this.toDate() || undefined);
  }

  clearDateFilter(): void {
    this.fromDate.set(this.getFirstDayOfMonth());
    this.toDate.set(this.getLastDayOfMonth());
    this.svc.loadTrialBalance(this.fromDate(), this.toDate());
  }

  private loadReport(type: string): void {
    switch (type) {
      case 'trial-balance': this.svc.loadTrialBalance(this.fromDate() || undefined, this.toDate() || undefined); break;
      case 'profit-loss': this.svc.loadProfitAndLoss(); break;
      case 'balance-sheet': this.svc.loadBalanceSheet(); break;
      case 'cash-flow': this.svc.loadCashFlow(); break;
      case 'daybook': this.svc.loadDaybook(); break;
    }
  }

  private getFirstDayOfMonth(): string {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    return `${year}-${month}-01`;
  }

  private getLastDayOfMonth(): string {
    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth() + 1;
    const lastDay = new Date(year, month, 0).getDate();
    return `${year}-${String(month).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`;
  }
}
