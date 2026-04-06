import { Component, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-banking',
  standalone: true,
  imports: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="banking-shell">
      <h1 class="page-title">Banking &amp; Reconciliation</h1>

      <!-- Bank Account Cards -->
      <div class="bank-cards">
        <div class="bank-card">
          <div class="bank-card-header">
            <span class="bank-icon">🏦</span>
            <div>
              <div class="bank-name">HDFC Bank — Current A/c</div>
              <div class="bank-number">****4821</div>
            </div>
            <span class="recon-status reconciled">✓ Reconciled</span>
          </div>
          <div class="bank-balance">₹12,45,832.50</div>
          <div class="bank-meta">Last sync: 2 min ago</div>
        </div>
        <div class="bank-card">
          <div class="bank-card-header">
            <span class="bank-icon">🏛️</span>
            <div>
              <div class="bank-name">SBI — Savings A/c</div>
              <div class="bank-number">****9203</div>
            </div>
            <span class="recon-status pending">⏳ Pending</span>
          </div>
          <div class="bank-balance">₹3,82,100.00</div>
          <div class="bank-meta">Last sync: 1 hour ago</div>
        </div>
        <div class="bank-card">
          <div class="bank-card-header">
            <span class="bank-icon">💳</span>
            <div>
              <div class="bank-name">ICICI — OD Account</div>
              <div class="bank-number">****7651</div>
            </div>
            <span class="recon-status mismatched">⚠ Mismatch</span>
          </div>
          <div class="bank-balance negative">-₹1,20,000.00</div>
          <div class="bank-meta">Last sync: 3 hours ago</div>
        </div>
      </div>

      <!-- Recent Transactions -->
      <div class="section-card">
        <div class="section-header">
          <h3>Recent Transactions</h3>
          <button class="nx-btn">Import Statement</button>
        </div>
        <div class="table-wrapper">
          <table class="nx-data-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Description</th>
                <th>Category</th>
                <th class="num">Debit (₹)</th>
                <th class="num">Credit (₹)</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>06 Apr 2026</td><td>NEFT — Vendor ABC</td><td>Payables</td>
                <td class="num debit">48,500.00</td><td class="num">—</td>
                <td><span class="nx-badge nx-badge--success">Matched</span></td>
              </tr>
              <tr>
                <td>05 Apr 2026</td><td>UPI — Customer XYZ</td><td>Receivables</td>
                <td class="num">—</td><td class="num credit">32,000.00</td>
                <td><span class="nx-badge nx-badge--success">Matched</span></td>
              </tr>
              <tr>
                <td>04 Apr 2026</td><td>Bank Charges</td><td>Expenses</td>
                <td class="num debit">250.00</td><td class="num">—</td>
                <td><span class="nx-badge nx-badge--warning">Unmatched</span></td>
              </tr>
              <tr>
                <td>03 Apr 2026</td><td>Interest Credit</td><td>Income</td>
                <td class="num">—</td><td class="num credit">1,240.00</td>
                <td><span class="nx-badge nx-badge--info">Review</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .banking-shell { padding: 16px; }
    .page-title { margin: 0 0 20px; font-size: 1.5rem; }
    .bank-cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 24px; }
    @media (max-width: 900px) { .bank-cards { grid-template-columns: 1fr; } }
    .bank-card { background: var(--nx-bg-card); border: 1px solid var(--nx-border); border-radius: var(--nx-radius-lg, 8px); padding: 16px 20px; box-shadow: var(--nx-shadow-sm); }
    .bank-card-header { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
    .bank-icon { font-size: 1.5rem; }
    .bank-name { font-weight: 600; font-size: 0.9rem; }
    .bank-number { font-family: var(--nx-font-mono, monospace); font-size: 0.8rem; color: var(--nx-text-muted); }
    .recon-status { margin-left: auto; font-size: 0.75rem; font-weight: 500; padding: 2px 8px; border-radius: var(--nx-radius-sm, 4px); }
    .recon-status.reconciled { background: rgba(76,175,80,0.1); color: var(--nx-success); }
    .recon-status.pending { background: rgba(255,152,0,0.1); color: var(--nx-warning); }
    .recon-status.mismatched { background: rgba(239,83,80,0.1); color: var(--nx-danger); }
    .bank-balance { font-family: var(--nx-font-mono, monospace); font-size: 1.4rem; font-weight: 700; font-variant-numeric: tabular-nums; color: var(--nx-emerald); margin-bottom: 6px; }
    .bank-balance.negative { color: var(--nx-danger); }
    .bank-meta { font-size: 0.75rem; color: var(--nx-text-muted); }
    .section-card { background: var(--nx-bg-card); border: 1px solid var(--nx-border); border-radius: var(--nx-radius-lg, 8px); overflow: hidden; }
    .section-header { padding: 14px 16px; border-bottom: 1px solid var(--nx-border); background: var(--nx-bg-surface); display: flex; justify-content: space-between; align-items: center; }
    .section-header h3 { margin: 0; font-size: 0.95rem; }
    .table-wrapper { overflow-x: auto; }
    .debit { color: var(--nx-danger); font-family: var(--nx-font-mono, monospace); font-variant-numeric: tabular-nums; }
    .credit { color: var(--nx-success); font-family: var(--nx-font-mono, monospace); font-variant-numeric: tabular-nums; }
    .nx-badge--info { background: rgba(66,165,245,0.1); color: var(--nx-info, #42a5f5); }
  `]
})
export class BankingComponent {}
