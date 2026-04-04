import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, of, tap } from 'rxjs';

const TENANT_ID = 'default-tenant';

/* ── Backend DTOs ── */
export interface TrialBalanceSummary {
  totalDebits: number;
  totalCredits: number;
  balanced: boolean;
  accountCount: number;
}

export interface BalanceSheetSummary {
  totalAssets: number;
  totalLiabilities: number;
  totalEquity: number;
  balanced: boolean;
}

export interface ProfitAndLossSummary {
  totalRevenue: number;
  totalExpenses: number;
  netIncome: number;
}

export interface CashFlowSummary {
  netCashFromOperating: number;
  netCashFromInvesting: number;
  netCashFromFinancing: number;
  netCashChange: number;
}

export interface DashboardSummary {
  tenantId: string;
  trialBalance: TrialBalanceSummary;
  balanceSheet: BalanceSheetSummary;
  profitAndLoss: ProfitAndLossSummary;
  cashFlow: CashFlowSummary;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);

  readonly summary = signal<DashboardSummary | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  loadSummary(): void {
    this.loading.set(true);
    this.error.set(null);
    this.http.get<DashboardSummary>('/api/dashboard/summary', {
      params: { tenantId: TENANT_ID },
    }).pipe(
      tap(s => this.summary.set(s)),
      tap(() => this.loading.set(false)),
      catchError(() => {
        this.loading.set(false);
        this.error.set('Failed to load dashboard summary');
        return of(null);
      }),
    ).subscribe();
  }
}
