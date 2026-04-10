import { Component, signal, computed, OnInit, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router, RouterOutlet, RouterLink, RouterLinkActive, NavigationEnd } from '@angular/router';
import { CommandPaletteComponent } from './keyboard/components/command-palette/command-palette.component';
import { KeyboardNavigationService } from './keyboard/services/keyboard-navigation.service';
import { CommandBootstrapService } from './keyboard/services/command-bootstrap.service';
import { LanguageSwitcherComponent } from './i18n/components/language-switcher/language-switcher.component';
import { AuthService } from './auth/services/auth.service';
import { NxToastComponent } from './shared/components/nx-toast/nx-toast.component';
import { NxConfirmDialogComponent } from './shared/components/nx-confirm-dialog/nx-confirm-dialog.component';
import { filter } from 'rxjs/operators';

interface HealthResponse {
  status: string;
  service: string;
  thread: string;
  components: {
    postgresql: string;
    redis: string;
  };
}

interface Tenant {
  id: string;
  name: string;
  role: string;
}

interface Breadcrumb {
  label: string;
  url?: string;
}

const TENANTS: Tenant[] = [
  { id: 'nexus', name: 'Nexus Corp', role: 'Payer' },
  { id: 'alpha', name: 'Alpha Ventures', role: 'Payer' },
  { id: 'beta', name: 'Beta Holdings', role: 'Tenant' },
];

// Route to breadcrumb mapping
const ROUTE_BREADCRUMBS: Record<string, Breadcrumb[]> = {
  '/': [{ label: 'Dashboard' }],
  '/vouchers': [{ label: 'Accounting' }, { label: 'Voucher Explorer' }],
  '/ledger': [{ label: 'Accounting' }, { label: 'Ledger' }],
  '/receivable': [{ label: 'Accounting' }, { label: 'Receivables' }],
  '/payable': [{ label: 'Accounting' }, { label: 'Payables' }],
  '/reports/trial-balance': [{ label: 'Reports' }, { label: 'Trial Balance' }],
  '/reports/profit-loss': [{ label: 'Reports' }, { label: 'Profit & Loss' }],
  '/reports/balance-sheet': [{ label: 'Reports' }, { label: 'Balance Sheet' }],
  '/reports/cash-flow': [{ label: 'Reports' }, { label: 'Cash Flow' }],
  '/reports/daybook': [{ label: 'Reports' }, { label: 'Day Book' }],
  '/inventory': [{ label: 'Management' }, { label: 'Inventory' }],
  '/gst': [{ label: 'Management' }, { label: 'GST & Tax' }],
  '/banking': [{ label: 'Management' }, { label: 'Banking' }],
  '/master/create': [{ label: 'Management' }, { label: 'Masters' }],
  '/ai': [{ label: 'Intelligence' }, { label: 'AI Insights' }],
  '/market': [{ label: 'Intelligence' }, { label: 'Market Valuation' }],
  '/auditor': [{ label: 'Intelligence' }, { label: 'Auditor Portal' }],
};

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommandPaletteComponent, LanguageSwitcherComponent, NxToastComponent, NxConfirmDialogComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  private http = inject(HttpClient);
  private router = inject(Router);
  private keyboardNav = inject(KeyboardNavigationService);
  private commandBootstrap = inject(CommandBootstrapService);
  readonly authService = inject(AuthService);

  title = signal('OneBook');
  backendStatus = signal('Checking...');
  threadInfo = signal('');
  postgresqlStatus = signal('Checking...');
  redisStatus = signal('Checking...');
  sidebarCollapsed = signal(false);

  // Track if we're on a public page (start page)
  isPublicPage = signal(false);

  // Dynamic breadcrumbs
  breadcrumbs = signal<Breadcrumb[]>([{ label: 'Dashboard' }]);

  tenants = signal<Tenant[]>(TENANTS);
  selectedTenant = signal<Tenant>(TENANTS[0]);
  tenantDropdownOpen = signal(false);
  userDropdownOpen = signal(false);

  sectionAccounting = signal(true);
  sectionReports = signal(true);
  sectionManagement = signal(true);
  sectionIntelligence = signal(true);

  // Computed: show app shell only when NOT on public page
  // When unauthenticated, user should be on /start anyway, but we also check isPublicPage
  showAppShell = computed(() => !this.isPublicPage());

  statusMessage = computed(() =>
    `${this.title()} — Backend: ${this.backendStatus()}`
  );

  statusClass = computed(() =>
    this.backendStatus() === 'UP' ? 'online' : 'offline'
  );

  ngOnInit(): void {
    this.commandBootstrap.bootstrap();

    // Subscribe to route changes for breadcrumbs and public page detection
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      const url = event.urlAfterRedirects || event.url;
      this.updateBreadcrumbs(url);
      this.checkPublicPage(url);
    });

    // Set initial state
    this.updateBreadcrumbs(this.router.url);
    this.checkPublicPage(this.router.url);

    this.http.get<HealthResponse>('/api/health')
      .subscribe({
        next: (res) => {
          this.backendStatus.set(res.status);
          this.threadInfo.set(res.thread);
          if (res.components) {
            this.postgresqlStatus.set(res.components.postgresql);
            this.redisStatus.set(res.components.redis);
          }
        },
        error: () => {
          this.backendStatus.set('Offline');
          this.postgresqlStatus.set('Offline');
          this.redisStatus.set('Offline');
        }
      });
  }

  private checkPublicPage(url: string): void {
    const path = url.split('?')[0];
    // Public pages that should NOT show the app shell
    this.isPublicPage.set(path === '/start' || path.startsWith('/start'));
  }

  private updateBreadcrumbs(url: string): void {
    // Remove query params
    const path = url.split('?')[0];

    // Find matching breadcrumbs; fall back to generic /voucher entry for /voucher/:type
    let crumbs = ROUTE_BREADCRUMBS[path];
    if (!crumbs && path.startsWith('/voucher/')) {
      crumbs = [{ label: 'Accounting' }, { label: 'Voucher Entry' }];
    }
    this.breadcrumbs.set(crumbs || [{ label: 'Dashboard' }]);
  }

  toggleSidebar(): void {
    this.sidebarCollapsed.update(v => !v);
  }

  toggleTenantDropdown(): void {
    this.tenantDropdownOpen.update(v => !v);
  }

  selectTenant(tenant: Tenant): void {
    this.selectedTenant.set(tenant);
    this.tenantDropdownOpen.set(false);
  }

  toggleSection(section: 'accounting' | 'reports' | 'management' | 'intelligence'): void {
    switch (section) {
      case 'accounting': this.sectionAccounting.update(v => !v); break;
      case 'reports': this.sectionReports.update(v => !v); break;
      case 'management': this.sectionManagement.update(v => !v); break;
      case 'intelligence': this.sectionIntelligence.update(v => !v); break;
    }
  }

  toggleUserDropdown(): void {
    this.userDropdownOpen.update(v => !v);
  }

  logout(): void {
    this.userDropdownOpen.set(false);
    this.authService.logout();
  }
}
