import { Component, ChangeDetectionStrategy } from '@angular/core';
import { NxPageHeaderComponent, NxStatusBadgeComponent } from '../../../shared/components';

@Component({
  selector: 'app-gst-dashboard',
  standalone: true,
  imports: [NxPageHeaderComponent, NxStatusBadgeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="gst-shell">
      <nx-page-header title="GST & Tax Compliance" subtitle="Return filing status, tax computation, and compliance tracking" />

      <!-- Return Status Cards -->
      <div class="return-cards">
        <div class="return-card filed">
          <div class="return-icon">✅</div>
          <div class="return-info">
            <div class="return-name">GSTR-1</div>
            <div class="return-period">Mar 2026</div>
            <nx-status-badge status="COMPLETED" label="Filed" />
          </div>
          <div class="return-date">Filed: 10 Apr</div>
        </div>
        <div class="return-card pending">
          <div class="return-icon">⏳</div>
          <div class="return-info">
            <div class="return-name">GSTR-3B</div>
            <div class="return-period">Mar 2026</div>
            <nx-status-badge status="PENDING" />
          </div>
          <div class="return-date">Due: 20 Apr</div>
        </div>
        <div class="return-card overdue">
          <div class="return-icon">🚨</div>
          <div class="return-info">
            <div class="return-name">GSTR-9</div>
            <div class="return-period">FY 2024-25</div>
            <nx-status-badge status="OVERDUE" />
          </div>
          <div class="return-date">Due: 31 Dec</div>
        </div>
        <div class="return-card filed">
          <div class="return-icon">✅</div>
          <div class="return-info">
            <div class="return-name">e-Way Bill</div>
            <div class="return-period">Active</div>
            <nx-status-badge status="ACTIVE" />
          </div>
          <div class="return-date">Valid: 30 days</div>
        </div>
      </div>

      <!-- GSTR Summary Table -->
      <div class="section-card">
        <div class="section-header"><h3>GSTR Summary</h3></div>
        <div class="table-wrapper">
          <table class="nx-data-table">
            <thead>
              <tr>
                <th>Return</th>
                <th>Period</th>
                <th class="num">Taxable Value (₹)</th>
                <th class="num">CGST (₹)</th>
                <th class="num">SGST (₹)</th>
                <th class="num">IGST (₹)</th>
                <th class="num">Total Tax (₹)</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>GSTR-1</td><td>Mar 2026</td>
                <td class="num">4,82,500.00</td><td class="num">43,425.00</td>
                <td class="num">43,425.00</td><td class="num">0.00</td>
                <td class="num">86,850.00</td>
                <td><nx-status-badge status="COMPLETED" label="Filed" /></td>
              </tr>
              <tr>
                <td>GSTR-3B</td><td>Mar 2026</td>
                <td class="num">4,82,500.00</td><td class="num">43,425.00</td>
                <td class="num">43,425.00</td><td class="num">0.00</td>
                <td class="num">86,850.00</td>
                <td><nx-status-badge status="PENDING" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Tax Computation Breakdown -->
      <div class="section-card">
        <div class="section-header"><h3>Tax Computation Breakdown</h3></div>
        <div class="tax-breakdown">
          <div class="tax-row">
            <span class="tax-label">Output Tax (Sales)</span>
            <span class="tax-value positive">₹86,850.00</span>
          </div>
          <div class="tax-row">
            <span class="tax-label">Input Tax Credit (Purchases)</span>
            <span class="tax-value negative">-₹32,400.00</span>
          </div>
          <div class="tax-row net">
            <span class="tax-label">Net GST Payable</span>
            <span class="tax-value">₹54,450.00</span>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .gst-shell { padding: 16px; }
    .return-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
    @media (max-width: 900px) { .return-cards { grid-template-columns: repeat(2, 1fr); } }
    @media (max-width: 480px) { .return-cards { grid-template-columns: 1fr; } }
    .return-card { background: var(--nx-bg-card); border: 1px solid var(--nx-border); border-radius: var(--nx-radius-lg, 8px); padding: 16px; box-shadow: var(--nx-shadow-sm); display: flex; flex-direction: column; gap: 8px; }
    .return-card.filed { border-left: 3px solid var(--nx-success, #4caf50); }
    .return-card.pending { border-left: 3px solid var(--nx-warning, #ff9800); }
    .return-card.overdue { border-left: 3px solid var(--nx-danger, #ef5350); }
    .return-icon { font-size: 1.5rem; }
    .return-name { font-weight: 600; font-size: 1rem; }
    .return-period { font-size: 0.8rem; color: var(--nx-text-muted); }
    .return-date { font-size: 0.78rem; color: var(--nx-text-muted); margin-top: auto; }
    .section-card { background: var(--nx-bg-card); border: 1px solid var(--nx-border); border-radius: var(--nx-radius-lg, 8px); margin-bottom: 20px; overflow: hidden; }
    .section-header { padding: 14px 16px; border-bottom: 1px solid var(--nx-border); background: var(--nx-bg-surface); }
    .section-header h3 { margin: 0; font-size: 0.95rem; }
    .table-wrapper { overflow-x: auto; }
    .tax-breakdown { padding: 16px; display: flex; flex-direction: column; gap: 8px; }
    .tax-row { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; border-bottom: 1px solid var(--nx-border); }
    .tax-row.net { font-weight: 700; border-top: 2px solid var(--nx-border); border-bottom: none; }
    .tax-label { color: var(--nx-text-secondary); font-size: 0.85rem; }
    .tax-value { font-family: var(--nx-font-mono, monospace); font-size: 0.9rem; font-variant-numeric: tabular-nums; }
    .tax-value.positive { color: var(--nx-danger); }
    .tax-value.negative { color: var(--nx-success); }
  `]
})
export class GstDashboardComponent {}
