import { Component, ChangeDetectionStrategy } from '@angular/core';
import { NxPageHeaderComponent, NxStatCardComponent, NxSearchInputComponent, NxStatusBadgeComponent } from '../../../shared/components';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [NxPageHeaderComponent, NxStatCardComponent, NxSearchInputComponent, NxStatusBadgeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="inventory-shell">
      <nx-page-header title="Inventory" subtitle="Stock management, item tracking, and reorder levels">
        <button class="nx-btn nx-btn--emerald">+ Add Item</button>
      </nx-page-header>

      <!-- Summary Stats -->
      <div class="inventory-stats">
        <nx-stat-card icon="📦" label="Total Items" value="248" trend="Active SKUs" trendDirection="neutral" color="emerald" />
        <nx-stat-card icon="⚠️" label="Low Stock" value="12" trend="Reorder needed" trendDirection="down" color="amber" />
        <nx-stat-card icon="🚫" label="Out of Stock" value="3" trend="Critical" trendDirection="down" color="danger" />
      </div>

      <!-- Toolbar -->
      <div class="inventory-toolbar">
        <nx-search-input placeholder="Search items..." />
        <select class="category-filter">
          <option value="">All Categories</option>
          <option>Electronics</option>
          <option>Raw Materials</option>
          <option>Finished Goods</option>
          <option>Consumables</option>
        </select>
      </div>

      <!-- Items Table -->
      <div class="table-wrapper">
        <table class="nx-data-table">
          <thead>
            <tr>
              <th>Item Code</th>
              <th>Item Name</th>
              <th>Category</th>
              <th class="num">Stock Qty</th>
              <th class="num">Reorder Lvl</th>
              <th class="num">Unit Cost (₹)</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td><code>ITM-001</code></td>
              <td>Office Paper A4</td>
              <td>Consumables</td>
              <td class="num">450</td>
              <td class="num">100</td>
              <td class="num">280.00</td>
              <td><nx-status-badge status="ACTIVE" label="Adequate" /></td>
            </tr>
            <tr>
              <td><code>ITM-002</code></td>
              <td>Printer Ink Black</td>
              <td>Consumables</td>
              <td class="num">8</td>
              <td class="num">10</td>
              <td class="num">650.00</td>
              <td><nx-status-badge status="PENDING" label="Low" /></td>
            </tr>
            <tr>
              <td><code>ITM-003</code></td>
              <td>USB-C Cables</td>
              <td>Electronics</td>
              <td class="num">0</td>
              <td class="num">20</td>
              <td class="num">120.00</td>
              <td><nx-status-badge status="FAILED" label="Out of Stock" /></td>
            </tr>
            <tr>
              <td><code>ITM-004</code></td>
              <td>Steel Rods 10mm</td>
              <td>Raw Materials</td>
              <td class="num">1200</td>
              <td class="num">200</td>
              <td class="num">85.50</td>
              <td><nx-status-badge status="ACTIVE" label="Adequate" /></td>
            </tr>
            <tr>
              <td><code>ITM-005</code></td>
              <td>Packaging Boxes L</td>
              <td>Consumables</td>
              <td class="num">45</td>
              <td class="num">50</td>
              <td class="num">35.00</td>
              <td><nx-status-badge status="PENDING" label="Low" /></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .inventory-shell { padding: 16px; }
    .inventory-stats { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 20px; }
    @media (max-width: 600px) { .inventory-stats { grid-template-columns: 1fr; } }
    .inventory-toolbar { display: flex; gap: 8px; margin-bottom: 16px; flex-wrap: wrap; align-items: center; }
    .category-filter { padding: 7px 10px; border: 1px solid var(--nx-border); border-radius: var(--nx-radius-md, 6px); font-size: 0.85rem; background: var(--nx-bg-card); color: var(--nx-text-primary); }
    .table-wrapper { overflow-x: auto; }
    code { font-family: var(--nx-font-mono, monospace); font-size: 0.8rem; color: var(--nx-purple); }
  `]
})
export class InventoryComponent {}
