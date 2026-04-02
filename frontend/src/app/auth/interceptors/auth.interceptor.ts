import { HttpInterceptorFn, HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

/**
 * Auth Interceptor - Injects Bearer token into API requests
 * 
 * Automatically adds Authorization header to requests targeting the API.
 * Skips injection for authentication endpoints and external URLs.
 */
export const authInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
) => {
  const authService = inject(AuthService);
  
  // Skip auth header for:
  // 1. Requests to external domains
  // 2. Requests to Keycloak/OAuth endpoints
  // 3. Requests to public assets
  if (shouldSkipAuth(req.url)) {
    return next(req);
  }
  
  const token = authService.getAccessToken();
  
  if (token) {
    const authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(authReq);
  }
  
  return next(req);
};

/**
 * Determine if auth header should be skipped for this URL
 */
function shouldSkipAuth(url: string): boolean {
  // Skip external URLs (those starting with http:// or https://)
  if (url.startsWith('http://') || url.startsWith('https://')) {
    // Check if it's our own API (allow localhost and relative URLs)
    const isOwnApi = url.includes('localhost') && url.includes('/api/');
    if (!isOwnApi) {
      return true;
    }
  }
  
  // Skip OAuth/Keycloak endpoints
  const authPaths = [
    '/realms/',
    '/auth/',
    '/oauth/',
    '/.well-known/'
  ];
  
  if (authPaths.some(path => url.includes(path))) {
    return true;
  }
  
  // Skip asset requests
  const assetPaths = ['/assets/', '.svg', '.png', '.jpg', '.json'];
  if (assetPaths.some(path => url.includes(path))) {
    return true;
  }
  
  return false;
}
