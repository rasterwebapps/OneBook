import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { AiService } from '../../../ai/services/ai.service';
import { MarketValuation, HoldingValuation, MarketSentiment } from '../../../ai/models/ai.models';
import { NxPageHeaderComponent, NxLoadingSpinnerComponent } from '../../../shared/components';

@Component({
  selector: 'app-market-valuation',
  standalone: true,
  imports: [CurrencyPipe, DecimalPipe, NxPageHeaderComponent, NxLoadingSpinnerComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './market-valuation.component.html',
  styleUrl: './market-valuation.component.scss'
})
export class MarketValuationComponent implements OnInit {
  private readonly aiService = inject(AiService);
  private readonly tenantId = 'tenant-1';

  readonly valuation = signal<MarketValuation | null>(null);
  readonly sentiments = signal<MarketSentiment[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  /* ── Demo MTM adjustments ── */
  readonly mtmAdjustments = signal([
    { symbol: 'RELIANCE', costBasis: 245000, marketValue: 268000, adjustment: 23000, type: 'gain' as const },
    { symbol: 'TCS', costBasis: 180000, marketValue: 172500, adjustment: -7500, type: 'loss' as const },
    { symbol: 'INFY', costBasis: 320000, marketValue: 348000, adjustment: 28000, type: 'gain' as const },
    { symbol: 'HDFC', costBasis: 150000, marketValue: 162000, adjustment: 12000, type: 'gain' as const },
  ]);

  /* ── Demo AI suggestions ── */
  readonly aiSuggestions = signal([
    { symbol: 'WIPRO', action: 'BUY' as const, confidence: 82, reason: 'Strong Q4 earnings forecast, undervalued P/E ratio' },
    { symbol: 'TCS', action: 'HOLD' as const, confidence: 71, reason: 'Market correction expected, maintain position' },
    { symbol: 'RELIANCE', action: 'BUY' as const, confidence: 89, reason: 'New energy division catalyst, bullish momentum' },
    { symbol: 'ZOMATO', action: 'SELL' as const, confidence: 76, reason: 'Overvalued at current levels, take profits' },
  ]);

  /* ── Demo chart data (simulated price points) ── */
  readonly chartData = signal([
    32, 35, 33, 38, 42, 40, 45, 43, 48, 52, 50, 55,
    53, 58, 56, 62, 60, 65, 63, 68, 72, 70, 75, 78,
  ]);

  ngOnInit(): void {
    this.loadData();
  }

  private loadData(): void {
    this.loading.set(true);
    this.error.set(null);

    this.aiService.getPortfolioValuation(this.tenantId).subscribe({
      next: (data) => {
        this.valuation.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Failed to load market data');
      }
    });

    this.aiService.getMarketSentiment(this.tenantId).subscribe({
      next: (data) => this.sentiments.set(data),
      error: (err) => console.warn('Failed to load market sentiment:', err)
    });
  }

  actionColor(action: string): string {
    switch (action) {
      case 'BUY': return 'var(--nx-emerald)';
      case 'SELL': return '#ef4444';
      case 'HOLD': return 'var(--nx-amber)';
      default: return 'var(--nx-text-muted)';
    }
  }

  sentimentColor(score: string): string {
    switch (score?.toUpperCase()) {
      case 'POSITIVE': return 'var(--nx-emerald)';
      case 'NEGATIVE': return '#ef4444';
      default: return 'var(--nx-amber)';
    }
  }

  /** Generate SVG polyline points from chart data */
  chartDataPoints(): string {
    const data = this.chartData();
    const max = Math.max(...data);
    return data
      .map((v, i) => `${(i / (data.length - 1)) * 240},${80 - (v / max) * 72}`)
      .join(' ');
  }
}
