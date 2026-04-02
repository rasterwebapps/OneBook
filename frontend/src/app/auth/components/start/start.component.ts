import { Component, ChangeDetectionStrategy, inject, signal, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

/**
 * StartComponent - Modern Landing Page for OneBook
 * 
 * Design: 2026 SaaS aesthetic with glassmorphism and neon emerald accents
 * Layout: Split-screen hero with floating Bento-box branding
 * Action: Login button redirects to Keycloak via OIDC
 */
@Component({
  selector: 'app-start',
  standalone: true,
  imports: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './start.component.html',
  styleUrl: './start.component.scss'
})
export class StartComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  
  readonly isLoading = signal(false);
  
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
   * Initiate OIDC login flow - redirects to Keycloak
   */
  onLogin(): void {
    this.isLoading.set(true);
    this.authService.login();
  }
}
