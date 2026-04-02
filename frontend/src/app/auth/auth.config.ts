import { AuthConfig } from 'angular-oauth2-oidc';

/**
 * OIDC Configuration for OneBook Authentication
 * 
 * Flow: Angular → OIDC → Keycloak → LDAP Federation
 * 
 * Keycloak does NOT store users locally - all users sync from LDAP.
 * Roles (ROLE_ACCOUNTANT, ROLE_ADMIN, etc.) are mapped from LDAP groups.
 */
export const authConfig: AuthConfig = {
  // Keycloak realm issuer URL
  issuer: 'http://localhost:8180/realms/onebook',
  
  // Client ID registered in Keycloak
  clientId: 'onebook-frontend',
  
  // Redirect URI after successful login
  redirectUri: typeof window !== 'undefined' ? window.location.origin : '',
  
  // Post-logout redirect
  postLogoutRedirectUri: typeof window !== 'undefined' ? window.location.origin + '/start' : '',
  
  // Authorization code flow with PKCE (recommended for SPAs)
  responseType: 'code',
  
  // Requested scopes
  scope: 'openid profile email roles',
  
  // Show debug information in development
  showDebugInformation: true,
  
  // Use silent refresh for token renewal
  useSilentRefresh: true,
  silentRefreshRedirectUri: typeof window !== 'undefined' ? window.location.origin + '/silent-refresh.html' : '',
  
  // Session checks
  sessionChecksEnabled: true,
  
  // Require HTTPS in production (disabled for local dev)
  requireHttps: false,
  
  // Strict discovery document validation
  strictDiscoveryDocumentValidation: false,
  
  // Token storage (in-memory for security - never localStorage)
  // This is handled by OAuthStorage override
  
  // Timeouts
  timeoutFactor: 0.75,
  
  // Custom parameters for Keycloak
  customQueryParams: {
    // Force specific identity provider if needed
    // kc_idp_hint: 'ldap'
  }
};
