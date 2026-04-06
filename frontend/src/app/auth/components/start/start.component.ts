import { Component, ChangeDetectionStrategy, inject, signal, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';

/**
 * StartComponent - Modern Landing Page for OneBook
 *
 * Design: 2026 SaaS aesthetic with glassmorphism and neon emerald accents
 * Layout: Split-screen hero with floating Bento-box branding
 * Action: Login button redirects to Keycloak via OIDC, or shows demo login modal
 */
@Component({
  selector: 'app-start',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './start.component.html',
  styleUrl: './start.component.scss'
})
export class StartComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly isLoading = signal(false);
  readonly showDemoLogin = this.authService.showDemoLogin;
  readonly demoUsers = this.authService.demoUsers;
  readonly authError = this.authService.authError;
  readonly demoMode = this.authService.demoMode;

  // Selected demo user for highlighting
  readonly selectedUser = signal<string | null>(null);

  // Feature highlights for the landing page
  readonly features = [
    {
      icon: '🔐',
      title: 'Zero-Trust Security',
      description: 'AES-256 encryption, HMAC blind indexes, RLS isolation'
    },
    {
      icon: '⚡',
      title: 'Tally-Speed UX',
      description: 'Keyboard-first navigation with sub-100ms response'
    },
    {
      icon: '🤖',
      title: 'AI Intelligence',
      description: 'Smart forecasting, anomaly detection, auto-reconciliation'
    },
    {
      icon: '🌐',
      title: 'Sector Agnostic',
      description: 'Healthcare, Auto, Banking adapters built-in'
    }
  ];

  ngOnInit(): void {
    // If already authenticated, redirect to dashboard
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/']);
    }
  }

  /**
   * Initiate OIDC login flow - redirects to Keycloak or shows demo modal
   */
  async onLogin(): Promise<void> {
    this.isLoading.set(true);
    try {
      await this.authService.login();
    } catch (error) {
      console.error('Login error:', error);
    } finally {
      this.isLoading.set(false);
    }
  }

  /**
   * Select and login with a demo user
   */
  selectDemoUser(username: string): void {
    this.selectedUser.set(username);
    this.authService.demoLogin(username);
  }

  /**
   * Close the demo login modal
   */
  closeDemoModal(): void {
    this.authService.closeDemoLogin();
    this.selectedUser.set(null);
  }

  /**
   * Get role display badge color
   */
  getRoleBadgeClass(role: string): string {
    switch (role) {
      case 'ROLE_ADMIN': return 'badge-admin';
      case 'ROLE_ACCOUNTANT': return 'badge-accountant';
      case 'ROLE_AUDITOR': return 'badge-auditor';
      case 'ROLE_MANAGER': return 'badge-manager';
      default: return 'badge-default';
    }
  }

  /**
   * Format role name for display
   */
  formatRole(role: string): string {
    return role.replace('ROLE_', '').toLowerCase();
  }
}
