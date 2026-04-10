import { Component, ChangeDetectionStrategy, OnInit, signal, inject, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { DashboardService } from './services/dashboard.service';
import { NxPageHeaderComponent } from '../shared/components';

/**
 * Modern Fintech-Style Accounting Dashboard
 *
 * Features:
 * - 4 KPI cards: Cash, Bank, Pending Validations, Failed PANs (shadow-sm, trend badges)
 * - 2-column grid: Cashflow chart placeholder (bg-slate-50) + Recent Activity (5 vouchers)
 * - Faint borders (border-slate-100), clean white background
 * - Tabular-nums for amounts, right-aligned
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, DecimalPipe, NxPageHeaderComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  private readonly dashboardService = inject(DashboardService);

  /* ── Live data from cross-domain DashboardService ── */
  readonly summary = this.dashboardService.summary;
  readonly loading = this.dashboardService.loading;
  readonly error = this.dashboardService.error;

  /* ── AI Cash Flow Summary (computed from API or fallback to demo data) ── */
  readonly cashFlowSummary = computed(() => {
    const s = this.summary();
    if (s) {
      return {
        currentBalance: s.cashFlow.netCashFromOperating,
        inflow30d: s.cashFlow.netCashFromOperating > 0 ? s.cashFlow.netCashFromOperating : 0,
        outflow30d: Math.abs(s.cashFlow.netCashFromInvesting) + Math.abs(s.cashFlow.netCashFromFinancing),
        netChange: s.cashFlow.netCashChange,
        trend: s.cashFlow.netCashChange >= 0 ? 'up' as const : 'down' as const,
        sparkline: [42, 55, 48, 62, 58, 71, 65, 78, 72, 85, 80, 92],
      };
    }
    // Fallback demo data for UI development
    return {
      currentBalance: 1247500,
      inflow30d: 856000,
      outflow30d: 423000,
      netChange: 433000,
      trend: 'up' as const,
      sparkline: [42, 55, 48, 62, 58, 71, 65, 78, 72, 85, 80, 92],
    };
  });

  /* ── KPI: Bank Balance ── */
  readonly bankBalance = computed(() => {
    const s = this.summary();
    if (s) {
      // Sum of bank-related cash (operating + financing)
      return s.cashFlow.netCashFromOperating + s.cashFlow.netCashFromFinancing;
    }
    return 3584200; // Demo fallback
  });

  /* ── KPI: Pending Validations ── */
  readonly pendingValidations = signal(7); // Would come from backend in production

  /* ── KPI: Failed PANs (PAN verification failures) ── */
  readonly failedPANs = signal(2); // Would come from compliance service in production

  /* ── Recent Vouchers (last 5) ── */
  readonly recentVouchers = signal([
    { id: 'INV-2487', description: 'Invoice #INV-2487 created', time: '2 min ago', amount: 45000, type: 'sale' },
    { id: 'PMT-892', description: 'Payment PMT-892 approved', time: '8 min ago', amount: -12500, type: 'payment' },
    { id: 'PO-1024', description: 'Purchase PO-1024 received', time: '15 min ago', amount: -78200, type: 'purchase' },
    { id: 'RCT-445', description: 'Receipt RCT-445 posted', time: '22 min ago', amount: 32000, type: 'receipt' },
    { id: 'JV-1024', description: 'Journal JV-1024 posted', time: '34 min ago', amount: 0, type: 'journal' },
  ]);

  /* ── Universal Ingestion Status ── */
  readonly ingestionSources = signal([
    { name: 'Healthcare HL7', icon: '🏥', status: 'active' as const, lastSync: '< 1 min', records: 12847 },
    { name: 'Auto DMS', icon: '🚗', status: 'active' as const, lastSync: '3 min', records: 8921 },
    { name: 'ISO 20022', icon: '🏦', status: 'active' as const, lastSync: '1 min', records: 34521 },
    { name: 'Webhook API', icon: '🔌', status: 'syncing' as const, lastSync: 'syncing...', records: 5643 },
  ]);

  /* ── Quick Actions ── */
  readonly shortcuts = [
    { key: 'F4', label: 'Contra', route: '/voucher/contra' },
    { key: 'F5', label: 'Payment', route: '/voucher/payment' },
    { key: 'F6', label: 'Receipt', route: '/voucher/receipt' },
    { key: 'F7', label: 'Journal', route: '/voucher/journal' },
    { key: 'F8', label: 'Sales', route: '/voucher/sales' },
    { key: 'F9', label: 'Purchase', route: '/voucher/purchase' },
  ];

  ngOnInit(): void {
    this.dashboardService.loadSummary();
  }
}
