import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { AiService } from '../../services/ai.service';
import { ForecastChartComponent } from '../forecast-chart/forecast-chart.component';
import { AnomalyAlertComponent } from '../anomaly-alert/anomaly-alert.component';
import { NxPageHeaderComponent, NxLoadingSpinnerComponent } from '../../../shared/components';
import {
  CashFlowForecast,
  TransactionAnomaly,
  MarketSentiment,
  MarketValuation
} from '../../models/ai.models';

@Component({
  selector: 'app-ai-dashboard',
  standalone: true,
  imports: [CurrencyPipe, DecimalPipe, ForecastChartComponent, AnomalyAlertComponent, NxPageHeaderComponent, NxLoadingSpinnerComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './ai-dashboard.component.html',
  styleUrl: './ai-dashboard.component.scss'
})
export class AiDashboardComponent implements OnInit {
  private readonly aiService = inject(AiService);
  private readonly tenantId = 'tenant-1';

  readonly forecast = signal<CashFlowForecast | null>(null);
  readonly anomalies = signal<TransactionAnomaly[]>([]);
  readonly sentiments = signal<MarketSentiment[]>([]);
  readonly valuation = signal<MarketValuation | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.loadData();
  }

  private loadData(): void {
    this.loading.set(true);
    this.error.set(null);

    this.aiService.getForecast(this.tenantId).subscribe({
      next: (data) => this.forecast.set(data),
      error: () => this.error.set('Failed to load forecast data')
    });

    this.aiService.getAnomalies(this.tenantId).subscribe({
      next: (data) => this.anomalies.set(data),
      error: () => {} // non-critical
    });

    this.aiService.getMarketSentiment(this.tenantId).subscribe({
      next: (data) => this.sentiments.set(data),
      error: () => {} // non-critical
    });

    this.aiService.getPortfolioValuation(this.tenantId).subscribe({
      next: (data) => {
        this.valuation.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  sentimentColor(score: string): string {
    switch (score?.toUpperCase()) {
      case 'POSITIVE': return '#22c55e';
      case 'NEGATIVE': return '#ef4444';
      default: return '#f59e0b';
    }
  }
}
