import { Component, signal, computed, OnInit, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommandPaletteComponent } from './keyboard/components/command-palette/command-palette.component';
import { KeyboardNavigationService } from './keyboard/services/keyboard-navigation.service';
import { CommandBootstrapService } from './keyboard/services/command-bootstrap.service';
import { LanguageSwitcherComponent } from './i18n/components/language-switcher/language-switcher.component';
import { AuthService } from './auth/services/auth.service';

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

const TENANTS: Tenant[] = [
  { id: 'nexus', name: 'Nexus Corp', role: 'Payer' },
  { id: 'alpha', name: 'Alpha Ventures', role: 'Payer' },
  { id: 'beta', name: 'Beta Holdings', role: 'Tenant' },
];

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommandPaletteComponent, LanguageSwitcherComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  private http = inject(HttpClient);
  private keyboardNav = inject(KeyboardNavigationService);
  private commandBootstrap = inject(CommandBootstrapService);
  readonly authService = inject(AuthService);

  title = signal('OneBook');
  backendStatus = signal('Checking...');
  threadInfo = signal('');
  postgresqlStatus = signal('Checking...');
  redisStatus = signal('Checking...');
  sidebarCollapsed = signal(false);

  tenants = signal<Tenant[]>(TENANTS);
  selectedTenant = signal<Tenant>(TENANTS[0]);
  tenantDropdownOpen = signal(false);
  userDropdownOpen = signal(false);

  sectionAccounting = signal(true);
  sectionReports = signal(true);
  sectionManagement = signal(true);
  sectionIntelligence = signal(true);

  statusMessage = computed(() =>
    `${this.title()} — Backend: ${this.backendStatus()}`
  );

  statusClass = computed(() =>
    this.backendStatus() === 'UP' ? 'online' : 'offline'
  );

  ngOnInit(): void {
    this.commandBootstrap.bootstrap();

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
