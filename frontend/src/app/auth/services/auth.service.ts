import { Injectable, signal, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { OAuthService, OAuthEvent } from 'angular-oauth2-oidc';
import { authConfig } from '../auth.config';

/**
 * Authentication Service for OneBook
 * 
 * Manages OIDC authentication flow with Keycloak.
 * Uses Angular Signals for reactive state management.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly oauthService = inject(OAuthService);
  private readonly router = inject(Router);
  
  // Reactive authentication state using Signals
  private readonly _isAuthenticated = signal(false);
  private readonly _userProfile = signal<UserProfile | null>(null);
  private readonly _isLoading = signal(true);
  private readonly _roles = signal<string[]>([]);
  
  // Public readonly signals
  readonly isAuthenticated = this._isAuthenticated.asReadonly();
  readonly userProfile = this._userProfile.asReadonly();
  readonly isLoading = this._isLoading.asReadonly();
  readonly roles = this._roles.asReadonly();
  
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
    this.subscribeToEvents();
  }
  
  /**
   * Configure OAuth service with Keycloak settings
   */
  private configureOAuth(): void {
    this.oauthService.configure(authConfig);
    this.oauthService.setupAutomaticSilentRefresh();
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
    
    try {
      // Load discovery document and try to login
      await this.oauthService.loadDiscoveryDocumentAndTryLogin();
      this.updateAuthState();
    } catch (error) {
      console.error('Auth initialization failed:', error);
      this.clearAuthState();
    } finally {
      this._isLoading.set(false);
    }
  }
  
  /**
   * Redirect to Keycloak login page
   */
  login(): void {
    this.oauthService.initCodeFlow();
  }
  
  /**
   * Logout and redirect to start page
   */
  logout(): void {
    this.oauthService.logOut();
    this.clearAuthState();
    this.router.navigate(['/start']);
  }
  
  /**
   * Get the current access token
   */
  getAccessToken(): string | null {
    return this.oauthService.getAccessToken();
  }
  
  /**
   * Get the current ID token
   */
  getIdToken(): string | null {
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
  }
}

/**
 * User profile extracted from ID token
 */
export interface UserProfile {
  sub: string;
  name?: string;
  email?: string;
  preferred_username?: string;
  tenant_id?: string;
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
