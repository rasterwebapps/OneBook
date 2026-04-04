import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { DashboardService, DashboardSummary } from './dashboard.service';

describe('DashboardService', () => {
  let service: DashboardService;
  let httpMock: HttpTestingController;

  const mockSummary: DashboardSummary = {
    tenantId: 'default-tenant',
    trialBalance: {
      totalDebits: 5000,
      totalCredits: 5000,
      balanced: true,
      accountCount: 3,
    },
    balanceSheet: {
      totalAssets: 10000,
      totalLiabilities: 6000,
      totalEquity: 4000,
      balanced: true,
    },
    profitAndLoss: {
      totalRevenue: 8000,
      totalExpenses: 3000,
      netIncome: 5000,
    },
    cashFlow: {
      netCashFromOperating: 5000,
      netCashFromInvesting: -1000,
      netCashFromFinancing: 500,
      netCashChange: 4500,
    },
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    service = TestBed.inject(DashboardService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should load summary from API', () => {
    service.loadSummary();

    const req = httpMock.expectOne(r =>
      r.url === '/api/dashboard/summary' && r.params.get('tenantId') === 'default-tenant'
    );
    expect(req.request.method).toBe('GET');
    req.flush(mockSummary);

    expect(service.summary()).toEqual(mockSummary);
    expect(service.loading()).toBe(false);
    expect(service.error()).toBeNull();
  });

  it('should set error on API failure', () => {
    service.loadSummary();

    const req = httpMock.expectOne(r => r.url === '/api/dashboard/summary');
    req.error(new ProgressEvent('error'));

    expect(service.summary()).toBeNull();
    expect(service.loading()).toBe(false);
    expect(service.error()).toBe('Failed to load dashboard summary');
  });

  it('should set loading true during request', () => {
    service.loadSummary();
    expect(service.loading()).toBe(true);

    const req = httpMock.expectOne(r => r.url === '/api/dashboard/summary');
    req.flush(mockSummary);
    expect(service.loading()).toBe(false);
  });
});
