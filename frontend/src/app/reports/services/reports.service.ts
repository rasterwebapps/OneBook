import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, of, tap } from 'rxjs';

const TENANT_ID = 'default-tenant';

/* ── Backend DTOs ── */
export interface TrialBalanceLine {
  accountId: number;
  accountCode: string;
  accountName: string;
  accountType: string;
  totalDebits: number;
  totalCredits: number;
}

export interface TrialBalanceReport {
  tenantId: string;
  lines: TrialBalanceLine[];
  totalDebits: number;
  totalCredits: number;
  balanced: boolean;
}

export interface ProfitAndLossReport {
  tenantId: string;
  revenueLines: TrialBalanceLine[];
  expenseLines: TrialBalanceLine[];
  totalRevenue: number;
  totalExpenses: number;
  netIncome: number;
}

export interface BalanceSheetReport {
  tenantId: string;
  assetLines: TrialBalanceLine[];
  liabilityLines: TrialBalanceLine[];
  equityLines: TrialBalanceLine[];
  totalAssets: number;
  totalLiabilities: number;
  totalEquity: number;
  balanced: boolean;
}

export interface CashFlowLine {
  accountName: string;
  amount: number;
}

export interface CashFlowReport {
  tenantId: string;
  operatingActivities: CashFlowLine[];
  investingActivities: CashFlowLine[];
  financingActivities: CashFlowLine[];
  netCashFromOperating: number;
  netCashFromInvesting: number;
  netCashFromFinancing: number;
  netCashChange: number;
}

export interface DaybookEntry {
  transactionUuid: string;
  transactionDate: string;
  description: string;
  voucherType: string;
  entries: { accountName: string; entryType: string; amount: number }[];
}

@Injectable({ providedIn: 'root' })
export class ReportsService {
  private readonly http = inject(HttpClient);

  readonly trialBalance = signal<TrialBalanceReport | null>(null);
  readonly profitAndLoss = signal<ProfitAndLossReport | null>(null);
  readonly balanceSheet = signal<BalanceSheetReport | null>(null);
  readonly cashFlow = signal<CashFlowReport | null>(null);
  readonly daybook = signal<DaybookEntry[]>([]);
  readonly loading = signal(false);

  loadTrialBalance(fromDate?: string, toDate?: string): void {
    this.loading.set(true);
    const params: Record<string, string> = { tenantId: TENANT_ID };
    if (fromDate) params['fromDate'] = fromDate;
    if (toDate) params['toDate'] = toDate;
    this.http.get<TrialBalanceReport>('/api/reports/trial-balance', { params }).pipe(
      tap(r => this.trialBalance.set(r)),
      tap(() => this.loading.set(false)),
      catchError(() => { this.loading.set(false); return of(null); }),
    ).subscribe();
  }

  exportTrialBalance(): void {
    const url = `/api/export/trial-balance?tenantId=${TENANT_ID}`;
    window.open(url, '_blank');
  }

  loadProfitAndLoss(): void {
    this.loading.set(true);
    this.http.get<ProfitAndLossReport>('/api/reports/profit-and-loss', {
      params: { tenantId: TENANT_ID },
    }).pipe(
      tap(r => this.profitAndLoss.set(r)),
      tap(() => this.loading.set(false)),
      catchError(() => { this.loading.set(false); return of(null); }),
    ).subscribe();
  }

  loadBalanceSheet(): void {
    this.loading.set(true);
    this.http.get<BalanceSheetReport>('/api/reports/balance-sheet', {
      params: { tenantId: TENANT_ID },
    }).pipe(
      tap(r => this.balanceSheet.set(r)),
      tap(() => this.loading.set(false)),
      catchError(() => { this.loading.set(false); return of(null); }),
    ).subscribe();
  }

  loadCashFlow(): void {
    this.loading.set(true);
    this.http.get<CashFlowReport>('/api/reports/cash-flow', {
      params: { tenantId: TENANT_ID },
    }).pipe(
      tap(r => this.cashFlow.set(r)),
      tap(() => this.loading.set(false)),
      catchError(() => { this.loading.set(false); return of(null); }),
    ).subscribe();
  }

  loadDaybook(): void {
    this.loading.set(true);
    this.http.get<any[]>('/api/journal/transactions', {
      params: { tenantId: TENANT_ID },
    }).pipe(
      tap(txns => {
        const entries: DaybookEntry[] = txns.map(t => {
          let voucherType = 'JOURNAL';
          try { voucherType = JSON.parse(t.metadata || '{}').voucherType ?? 'JOURNAL'; }
          catch { /* ignore */ }
          return {
            transactionUuid: t.transactionUuid,
            transactionDate: t.transactionDate,
            description: t.description ?? '',
            voucherType,
            entries: (t.entries ?? []).map((e: any) => ({
              accountName: e.account?.accountName ?? `Account #${e.account?.id ?? '?'}`,
              entryType: e.entryType,
              amount: e.amount,
            })),
          };
        });
        this.daybook.set(entries);
      }),
      tap(() => this.loading.set(false)),
      catchError(() => { this.loading.set(false); return of(null); }),
    ).subscribe();
  }
}
