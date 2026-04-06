import { Injectable, signal, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { OAuthService, OAuthEvent } from 'angular-oauth2-oidc';
import { authConfig, AUTH_CONFIG } from '../auth.config';

/**
 * Authentication Service for OneBook
 *
 * Manages OIDC authentication flow with Keycloak.
 * Uses Angular Signals for reactive state management.
 *
 * DEMO MODE: When Keycloak is unavailable, enables local demo authentication
 * for development and UI preview purposes.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly oauthService = inject(OAuthService);
  private readonly router = inject(Router);

  // Demo mode state
  private readonly _demoMode = signal(false);
  private readonly _keycloakAvailable = signal<boolean | null>(null); // null = not checked yet
  private readonly _showDemoLogin = signal(false);

  // Reactive authentication state using Signals
  private readonly _isAuthenticated = signal(false);
  private readonly _userProfile = signal<UserProfile | null>(null);
  private readonly _isLoading = signal(false);
  private readonly _roles = signal<string[]>([]);
  private readonly _authError = signal<string | null>(null);

  // Public readonly signals
  readonly isAuthenticated = this._isAuthenticated.asReadonly();
  readonly userProfile = this._userProfile.asReadonly();
  readonly isLoading = this._isLoading.asReadonly();
  readonly roles = this._roles.asReadonly();
  readonly demoMode = this._demoMode.asReadonly();
  readonly showDemoLogin = this._showDemoLogin.asReadonly();
  readonly authError = this._authError.asReadonly();
  readonly keycloakAvailable = this._keycloakAvailable.asReadonly();

  // Demo users for demo mode
  readonly demoUsers = AUTH_CONFIG.demoUsers;

  // Computed signals
  readonly isAdmin = computed(() => this._roles().includes('ROLE_ADMIN'));
  readonly isAccountant = computed(() => this._roles().includes('ROLE_ACCOUNTANT'));
  readonly isAuditor = computed(() => this._roles().includes('ROLE_AUDITOR'));
  readonly isManager = computed(() => this._roles().includes('ROLE_MANAGER'));

  readonly displayName = computed(() => {
    const profile = this._userProfile();
    return profile?.name || profile?.preferred_username || 'User';
  });

  constructor() {
    this.configureOAuth();
    // Check for Keycloak availability on startup
    this.checkKeycloakAvailability();
  }

  /**
   * Configure OAuth service with Keycloak settings
   */
  private configureOAuth(): void {
    this.oauthService.configure(authConfig);
  }

  /**
   * Check if Keycloak is available
   */
  private async checkKeycloakAvailability(): Promise<boolean> {
    if (!AUTH_CONFIG.demoModeEnabled) {
      this._keycloakAvailable.set(true);
      return true;
    }

    try {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 3000);

      const response = await fetch(`${AUTH_CONFIG.keycloakUrl}/realms/onebook/.well-known/openid-configuration`, {
        signal: controller.signal,
        mode: 'cors'
      });

      clearTimeout(timeoutId);
      const available = response.ok;
      this._keycloakAvailable.set(available);

      if (!available) {
        console.warn('Keycloak not available, demo mode enabled');
        this._demoMode.set(true);
      }

      return available;
    } catch {
      console.warn('Keycloak connection failed, demo mode enabled');
      this._keycloakAvailable.set(false);
      this._demoMode.set(true);
      return false;
    }
  }

  /**
   * Subscribe to OAuth events for state management
   */
  private subscribeToEvents(): void {
    this.oauthService.events.subscribe((event: OAuthEvent) => {
      switch (event.type) {
        case 'token_received':
        case 'token_refreshed':
          this.updateAuthState();
          break;
        case 'logout':
        case 'session_terminated':
          this.clearAuthState();
          break;
        case 'discovery_document_loaded':
          // Discovery document is ready
          break;
      }
    });
  }

  /**
   * Initialize authentication - call on app startup
   */
  async initialize(): Promise<void> {
    this._isLoading.set(true);
    this._authError.set(null);

    try {
      const keycloakAvailable = await this.checkKeycloakAvailability();

      if (keycloakAvailable) {
        this.subscribeToEvents();
        this.oauthService.setupAutomaticSilentRefresh();
        await this.oauthService.loadDiscoveryDocumentAndTryLogin();
        this.updateAuthState();
      } else {
        // In demo mode, check for stored demo session
        this.restoreDemoSession();
      }
    } catch (error) {
      console.error('Auth initialization failed:', error);
      this._demoMode.set(true);
      this.restoreDemoSession();
    } finally {
      this._isLoading.set(false);
    }
  }

  /**
   * Redirect to Keycloak login page or show demo login
   */
  async login(): Promise<void> {
    this._authError.set(null);

    // Check Keycloak availability first
    const keycloakAvailable = await this.checkKeycloakAvailability();

    if (keycloakAvailable) {
      try {
        // Ensure discovery document is loaded before initiating login
        if (!this.oauthService.discoveryDocumentLoaded) {
          await this.oauthService.loadDiscoveryDocument();
        }
        this.oauthService.initCodeFlow();
      } catch (error) {
        console.error('Keycloak login failed, falling back to demo mode:', error);
        this._demoMode.set(true);
        this._showDemoLogin.set(true);
      }
    } else {
      // Show demo login modal
      this._showDemoLogin.set(true);
    }
  }

  /**
   * Demo login - authenticate with a demo user
   */
  demoLogin(username: string): void {
    const demoUser = this.demoUsers.find(u => u.username === username);

    if (!demoUser) {
      this._authError.set('Invalid demo user');
      return;
    }

    // Set demo session
    const profile: UserProfile = {
      sub: `demo-${username}`,
      name: demoUser.name,
      preferred_username: username,
      email: `${username}@onebook.demo`,
      email_verified: true,
      tenant_id: 'demo-tenant'
    };

    this._userProfile.set(profile);
    this._roles.set(demoUser.roles);
    this._isAuthenticated.set(true);
    this._showDemoLogin.set(false);
    this._authError.set(null);

    // Store demo session
    sessionStorage.setItem('onebook_demo_user', username);

    // Navigate to dashboard
    this.router.navigate(['/']);
  }

  /**
   * Close demo login modal
   */
  closeDemoLogin(): void {
    this._showDemoLogin.set(false);
  }

  /**
   * Restore demo session from storage
   */
  private restoreDemoSession(): void {
    const storedUser = sessionStorage.getItem('onebook_demo_user');
    if (storedUser) {
      this.demoLogin(storedUser);
    }
  }

  /**
   * Logout and redirect to start page
   */
  logout(): void {
    if (this._demoMode()) {
      // Demo logout
      sessionStorage.removeItem('onebook_demo_user');
      this.clearAuthState();
      this.router.navigate(['/start']);
    } else {
      // OAuth logout
      this.oauthService.logOut();
      this.clearAuthState();
      this.router.navigate(['/start']);
    }
  }

  /**
   * Get the current access token
   */
  getAccessToken(): string | null {
    if (this._demoMode()) {
      return 'demo-token';
    }
    return this.oauthService.getAccessToken();
  }

  /**
   * Get the current ID token
   */
  getIdToken(): string | null {
    if (this._demoMode()) {
      return 'demo-id-token';
    }
    return this.oauthService.getIdToken();
  }

  /**
   * Check if user has a specific role
   */
  hasRole(role: string): boolean {
    return this._roles().includes(role);
  }

  /**
   * Check if user has any of the specified roles
   */
  hasAnyRole(roles: string[]): boolean {
    return roles.some(role => this._roles().includes(role));
  }

  /**
   * Update authentication state from tokens
   */
  private updateAuthState(): void {
    const hasValidToken = this.oauthService.hasValidAccessToken();
    this._isAuthenticated.set(hasValidToken);

    if (hasValidToken) {
      const claims = this.oauthService.getIdentityClaims() as TokenClaims | null;

      if (claims) {
        this._userProfile.set({
          sub: claims.sub,
          name: claims.name,
          email: claims.email,
          preferred_username: claims.preferred_username,
          tenant_id: claims.tenant_id
        });

        // Extract roles from realm_access or resource_access
        const realmRoles = claims.realm_access?.roles || [];
        const clientRoles = claims.resource_access?.['onebook-frontend']?.roles || [];
        this._roles.set([...realmRoles, ...clientRoles]);
      }
    }
  }

  /**
   * Clear authentication state on logout
   */
  private clearAuthState(): void {
    this._isAuthenticated.set(false);
    this._userProfile.set(null);
    this._roles.set([]);
    this._authError.set(null);
  }
}

/**
 * User profile extracted from ID token
 */
export interface UserProfile {
  sub: string;
  name?: string;
  email?: string;
  email_verified?: boolean;
  preferred_username?: string;
  tenant_id?: string;
  realm_access?: {
    roles: string[];
  };
}

/**
 * JWT token claims structure from Keycloak
 */
interface TokenClaims {
  sub: string;
  name?: string;
  email?: string;
  preferred_username?: string;
  tenant_id?: string;
  realm_access?: {
    roles: string[];
  };
  resource_access?: {
    [clientId: string]: {
      roles: string[];
    };
  };
}
