import { Component, ChangeDetectionStrategy } from '@angular/core';
import { NxPageHeaderComponent } from '../../../shared/components';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [NxPageHeaderComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="inventory-shell">
      <nx-page-header title="Inventory" subtitle="Stock management, item tracking, and reorder levels">
        <button class="inv-add-btn hide-mobile">
          <svg viewBox="0 0 24 24" aria-hidden="true"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          Add Item
        </button>
      </nx-page-header>

      <!-- ═══ Glass KPI Cards ═══ -->
      <div class="kpi-row">
        <div class="glass-kpi-card">
          <div class="kpi-icon-wrap kpi-total">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
              <polyline points="3.27 6.96 12 12.01 20.73 6.96"/>
              <line x1="12" y1="22.08" x2="12" y2="12"/>
            </svg>
          </div>
          <span class="kpi-label">Total Items</span>
          <div class="kpi-value">248</div>
          <span class="kpi-sub">Active SKUs in catalogue</span>
        </div>
        <div class="glass-kpi-card">
          <div class="kpi-icon-wrap kpi-low">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
              <line x1="12" y1="9" x2="12" y2="13"/>
              <line x1="12" y1="17" x2="12.01" y2="17"/>
            </svg>
          </div>
          <span class="kpi-label">Low Stock</span>
          <div class="kpi-value kpi-value--amber">12</div>
          <span class="kpi-sub">Below reorder level</span>
        </div>
        <div class="glass-kpi-card">
          <div class="kpi-icon-wrap kpi-out">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <circle cx="12" cy="12" r="10"/>
              <line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/>
            </svg>
          </div>
          <span class="kpi-label">Out of Stock</span>
          <div class="kpi-value kpi-value--red">3</div>
          <span class="kpi-sub">Critical — reorder now</span>
        </div>
      </div>

      <!-- ═══ Filter Bar ═══ -->
      <div class="filter-bar">
        <div class="search-wrap">
          <svg class="search-icon" viewBox="0 0 24 24" aria-hidden="true"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          <input type="search" class="search-field" placeholder="Search by code, name, or category…" />
        </div>
        <div class="filter-controls">
          <div class="select-wrap">
            <svg class="select-icon" viewBox="0 0 24 24" aria-hidden="true"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/></svg>
            <select class="filter-select">
              <option value="">All Categories</option>
              <option>Electronics</option>
              <option>Raw Materials</option>
              <option>Finished Goods</option>
              <option>Consumables</option>
            </select>
          </div>
          <div class="select-wrap">
            <svg class="select-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M22 11.08V12a10 10 0 11-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
            <select class="filter-select">
              <option value="">All Status</option>
              <option>Adequate</option>
              <option>Low</option>
              <option>Out of Stock</option>
            </select>
          </div>
        </div>
      </div>

      <!-- ═══ Items Table ═══ -->
      <div class="table-scroll">
        <table class="inv-table">
          <thead>
            <tr>
              <th>Item Code</th>
              <th>Item Name</th>
              <th>Category</th>
              <th class="num">Stock Qty</th>
              <th class="num">Reorder Lvl</th>
              <th class="num">Unit Cost (₹)</th>
              <th>Status</th>
              <th class="actions-col">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td><span class="item-code">ITM-001</span></td>
              <td>Office Paper A4</td>
              <td><span class="category-badge">Consumables</span></td>
              <td class="num">450</td>
              <td class="num">100</td>
              <td class="num mono">280.00</td>
              <td><span class="status-pill status--adequate"><span class="status-dot"></span>Adequate</span></td>
              <td class="actions-col">
                <div class="row-actions">
                  <button class="row-btn" title="Edit">
                    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                  </button>
                  <button class="row-btn row-btn--danger" title="Delete">
                    <svg viewBox="0 0 24 24" aria-hidden="true"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4h6v2"/></svg>
                  </button>
                </div>
              </td>
            </tr>
            <tr>
              <td><span class="item-code">ITM-002</span></td>
              <td>Printer Ink Black</td>
              <td><span class="category-badge">Consumables</span></td>
              <td class="num">8</td>
              <td class="num">10</td>
              <td class="num mono">650.00</td>
              <td><span class="status-pill status--low"><span class="status-dot"></span>Low</span></td>
              <td class="actions-col">
                <div class="row-actions">
                  <button class="row-btn" title="Edit"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></button>
                  <button class="row-btn row-btn--danger" title="Delete"><svg viewBox="0 0 24 24" aria-hidden="true"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4h6v2"/></svg></button>
                </div>
              </td>
            </tr>
            <tr>
              <td><span class="item-code">ITM-003</span></td>
              <td>USB-C Cables</td>
              <td><span class="category-badge">Electronics</span></td>
              <td class="num">0</td>
              <td class="num">20</td>
              <td class="num mono">120.00</td>
              <td><span class="status-pill status--out"><span class="status-dot"></span>Out of Stock</span></td>
              <td class="actions-col">
                <div class="row-actions">
                  <button class="row-btn" title="Edit"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></button>
                  <button class="row-btn row-btn--danger" title="Delete"><svg viewBox="0 0 24 24" aria-hidden="true"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4h6v2"/></svg></button>
                </div>
              </td>
            </tr>
            <tr>
              <td><span class="item-code">ITM-004</span></td>
              <td>Steel Rods 10mm</td>
              <td><span class="category-badge">Raw Materials</span></td>
              <td class="num">1200</td>
              <td class="num">200</td>
              <td class="num mono">85.50</td>
              <td><span class="status-pill status--adequate"><span class="status-dot"></span>Adequate</span></td>
              <td class="actions-col">
                <div class="row-actions">
                  <button class="row-btn" title="Edit"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></button>
                  <button class="row-btn row-btn--danger" title="Delete"><svg viewBox="0 0 24 24" aria-hidden="true"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4h6v2"/></svg></button>
                </div>
              </td>
            </tr>
            <tr>
              <td><span class="item-code">ITM-005</span></td>
              <td>Packaging Boxes L</td>
              <td><span class="category-badge">Consumables</span></td>
              <td class="num">45</td>
              <td class="num">50</td>
              <td class="num mono">35.00</td>
              <td><span class="status-pill status--low"><span class="status-dot"></span>Low</span></td>
              <td class="actions-col">
                <div class="row-actions">
                  <button class="row-btn" title="Edit"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></button>
                  <button class="row-btn row-btn--danger" title="Delete"><svg viewBox="0 0 24 24" aria-hidden="true"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4h6v2"/></svg></button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Mobile FAB -->
      <button class="fab show-mobile" title="Add Item" aria-label="Add Item">
        <svg viewBox="0 0 24 24" aria-hidden="true"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
      </button>
    </div>
  `,
  styles: [`
    :host svg { fill: none; stroke: currentColor; stroke-linecap: round; stroke-linejoin: round; stroke-width: 2; }

    .inventory-shell { padding: 16px; position: relative; }

    /* ── Add Item button ── */
    .inv-add-btn {
      display: inline-flex; align-items: center; gap: 6px;
      padding: 7px 16px; border: none; border-radius: 7px; cursor: pointer;
      font-size: 0.85rem; font-weight: 600;
      background: var(--nx-emerald, #10b981); color: #fff;
      transition: background 0.15s, box-shadow 0.15s;
      &:hover { background: #059669; box-shadow: 0 2px 8px rgba(16,185,129,0.35); }
      svg { width: 15px; height: 15px; }
    }

    /* ── Glass KPI Cards ── */
    .kpi-row {
      display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; margin-bottom: 18px;
      @media (max-width: 640px) { grid-template-columns: 1fr; }
    }
    .glass-kpi-card {
      display: flex; flex-direction: column; gap: 4px;
      padding: 16px 18px;
      background: var(--nx-glass-bg, rgba(255,255,255,0.04));
      backdrop-filter: blur(8px); -webkit-backdrop-filter: blur(8px);
      border: 1px solid var(--nx-glass-border, rgba(255,255,255,0.1));
      border-radius: 10px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1), 0 4px 12px rgba(0,0,0,0.07);
      transition: box-shadow 0.2s, transform 0.2s;
      &:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.16); transform: translateY(-1px); }
    }
    .kpi-icon-wrap {
      width: 38px; height: 38px; border-radius: 9px; margin-bottom: 6px;
      display: flex; align-items: center; justify-content: center;
      svg { width: 19px; height: 19px; }
    }
    .kpi-total  { background: rgba(14,165,233,0.12); color: #0ea5e9; }
    .kpi-low    { background: rgba(245,158,11,0.12); color: #f59e0b; }
    .kpi-out    { background: rgba(239,68,68,0.12);  color: #ef4444; }
    .kpi-label  { font-size: 0.72rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; color: var(--nx-text-muted); }
    .kpi-value  { font-size: 2.1rem; font-weight: 800; color: var(--nx-text-primary); line-height: 1; }
    .kpi-value--amber { color: #f59e0b; }
    .kpi-value--red   { color: #ef4444; }
    .kpi-sub    { font-size: 0.75rem; color: var(--nx-text-muted); margin-top: 2px; }

    /* ── Filter Bar ── */
    .filter-bar { display: flex; gap: 8px; margin-bottom: 14px; flex-wrap: wrap; align-items: center; }
    .search-wrap { position: relative; flex: 1; min-width: 180px; }
    .search-icon { position: absolute; left: 10px; top: 50%; transform: translateY(-50%); width: 15px; height: 15px; color: var(--nx-text-muted); pointer-events: none; }
    .search-field { width: 100%; padding: 7px 10px 7px 32px; border: 1px solid var(--nx-border); border-radius: 7px; font-size: 0.85rem; background: var(--nx-bg-card); color: var(--nx-text-primary); outline: none; box-sizing: border-box; transition: border-color 0.15s; &:focus { border-color: var(--nx-emerald, #10b981); } }
    .filter-controls { display: flex; gap: 8px; flex-wrap: wrap; }
    .select-wrap { position: relative; }
    .select-icon { position: absolute; left: 9px; top: 50%; transform: translateY(-50%); width: 13px; height: 13px; color: var(--nx-text-muted); pointer-events: none; }
    .filter-select { padding: 7px 10px 7px 28px; border: 1px solid var(--nx-border); border-radius: 7px; font-size: 0.85rem; background: var(--nx-bg-card); color: var(--nx-text-primary); outline: none; appearance: none; cursor: pointer; }

    /* ── Table ── */
    .table-scroll { overflow-x: auto; border-radius: 10px; border: 1px solid var(--nx-border); }
    .inv-table { width: 100%; border-collapse: collapse; min-width: 680px; }
    .inv-table thead tr { position: sticky; top: 0; z-index: 5; }
    .inv-table th { background: var(--nx-bg-surface); padding: 7px 12px; font-weight: 600; font-size: 0.72rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--nx-text-muted); border-bottom: 2px solid var(--nx-border); text-align: left; white-space: nowrap; }
    .inv-table td { padding: 6px 12px; border-bottom: 1px solid var(--nx-border); font-size: 0.85rem; color: var(--nx-text-primary); vertical-align: middle; }
    .inv-table tbody tr:hover { background: var(--nx-bg-card-hover, rgba(255,255,255,0.03)); }
    .inv-table tbody tr:last-child td { border-bottom: none; }
    .inv-table .num { text-align: right; }
    .mono { font-family: var(--nx-font-mono, monospace); font-variant-numeric: tabular-nums; }

    .item-code { font-family: var(--nx-font-mono, monospace); font-size: 0.8rem; color: var(--nx-purple, #8b5cf6); background: rgba(139,92,246,0.08); padding: 2px 7px; border-radius: 4px; }
    .category-badge { font-size: 0.75rem; color: var(--nx-text-muted); background: var(--nx-bg-surface); border: 1px solid var(--nx-border); padding: 2px 7px; border-radius: 4px; white-space: nowrap; }

    /* Status pills */
    .status-pill { display: inline-flex; align-items: center; gap: 5px; font-size: 0.75rem; font-weight: 600; padding: 3px 9px; border-radius: 12px; white-space: nowrap; }
    .status-dot { width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0; }
    .status--adequate { background: rgba(16,185,129,0.1); color: #10b981; .status-dot { background: #10b981; } }
    .status--low      { background: rgba(245,158,11,0.1);  color: #f59e0b; .status-dot { background: #f59e0b; } }
    .status--out      { background: rgba(239,68,68,0.1);   color: #ef4444; .status-dot { background: #ef4444; } }

    /* Row actions */
    .actions-col { width: 80px; text-align: center; }
    .row-actions { display: flex; align-items: center; justify-content: center; gap: 4px; }
    .row-btn { display: inline-flex; align-items: center; justify-content: center; width: 28px; height: 28px; border: none; border-radius: 6px; cursor: pointer; background: var(--nx-bg-surface); color: var(--nx-text-muted); transition: background 0.15s, color 0.15s; svg { width: 13px; height: 13px; } &:hover { background: var(--nx-bg-card-hover); color: var(--nx-text-primary); } }
    .row-btn--danger:hover { background: rgba(239,68,68,0.1); color: #ef4444; }

    /* Mobile FAB */
    .fab {
      display: none; position: fixed; bottom: 24px; right: 24px; z-index: 100;
      width: 52px; height: 52px; border-radius: 50%; border: none; cursor: pointer;
      background: var(--nx-emerald, #10b981); color: #fff;
      box-shadow: 0 4px 16px rgba(16,185,129,0.45);
      align-items: center; justify-content: center;
      transition: transform 0.15s, box-shadow 0.15s;
      &:hover { transform: scale(1.08); box-shadow: 0 6px 20px rgba(16,185,129,0.5); }
      svg { width: 22px; height: 22px; }
    }
    @media (max-width: 640px) {
      .hide-mobile { display: none !important; }
      .show-mobile { display: flex !important; }
    }
  `]
})
export class InventoryComponent {}
