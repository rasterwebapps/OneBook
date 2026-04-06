import { Component, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="inventory-shell">
      <!-- Summary Stats -->
      <div class="inventory-stats">
        <div class="nx-stat-card emerald">
          <div class="stat-icon">📦</div>
          <div class="stat-label">Total Items</div>
          <div class="stat-value">248</div>
          <div class="stat-trend neutral">→ Active SKUs</div>
        </div>
        <div class="nx-stat-card amber">
          <div class="stat-icon">⚠️</div>
          <div class="stat-label">Low Stock</div>
          <div class="stat-value">12</div>
          <div class="stat-trend down">▼ Reorder needed</div>
        </div>
        <div class="nx-stat-card danger">
          <div class="stat-icon">🚫</div>
          <div class="stat-label">Out of Stock</div>
          <div class="stat-value">3</div>
          <div class="stat-trend down">▼ Critical</div>
        </div>
      </div>

      <!-- Toolbar -->
      <div class="inventory-toolbar">
        <input type="text" class="search-input" placeholder="Search items..." />
        <select class="category-filter">
          <option value="">All Categories</option>
          <option>Electronics</option>
          <option>Raw Materials</option>
          <option>Finished Goods</option>
          <option>Consumables</option>
        </select>
        <button class="nx-btn nx-btn--emerald">+ Add Item</button>
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
              <td><span class="stock-adequate">● Adequate</span></td>
            </tr>
            <tr>
              <td><code>ITM-002</code></td>
              <td>Printer Ink Black</td>
              <td>Consumables</td>
              <td class="num">8</td>
              <td class="num">10</td>
              <td class="num">650.00</td>
              <td><span class="stock-low">● Low</span></td>
            </tr>
            <tr>
              <td><code>ITM-003</code></td>
              <td>USB-C Cables</td>
              <td>Electronics</td>
              <td class="num">0</td>
              <td class="num">20</td>
              <td class="num">120.00</td>
              <td><span class="stock-critical">● Out of Stock</span></td>
            </tr>
            <tr>
              <td><code>ITM-004</code></td>
              <td>Steel Rods 10mm</td>
              <td>Raw Materials</td>
              <td class="num">1200</td>
              <td class="num">200</td>
              <td class="num">85.50</td>
              <td><span class="stock-adequate">● Adequate</span></td>
            </tr>
            <tr>
              <td><code>ITM-005</code></td>
              <td>Packaging Boxes L</td>
              <td>Consumables</td>
              <td class="num">45</td>
              <td class="num">50</td>
              <td class="num">35.00</td>
              <td><span class="stock-low">● Low</span></td>
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
    .search-input { flex: 1; min-width: 200px; padding: 7px 12px; border: 1px solid var(--nx-border); border-radius: var(--nx-radius-md, 6px); font-size: 0.85rem; background: var(--nx-bg-card); color: var(--nx-text-primary); }
    .category-filter { padding: 7px 10px; border: 1px solid var(--nx-border); border-radius: var(--nx-radius-md, 6px); font-size: 0.85rem; background: var(--nx-bg-card); color: var(--nx-text-primary); }
    .table-wrapper { overflow-x: auto; }
    code { font-family: var(--nx-font-mono, monospace); font-size: 0.8rem; color: var(--nx-purple); }
    .stock-adequate { color: var(--nx-success, #4caf50); font-size: 0.8rem; font-weight: 500; }
    .stock-low { color: var(--nx-warning, #ff9800); font-size: 0.8rem; font-weight: 500; }
    .stock-critical { color: var(--nx-danger, #ef5350); font-size: 0.8rem; font-weight: 600; }
  `]
})
export class InventoryComponent {}
