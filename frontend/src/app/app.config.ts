import { ApplicationConfig, provideZoneChangeDetection, APP_INITIALIZER, inject } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideOAuthClient } from 'angular-oauth2-oidc';

import { routes } from './app.routes';
import { provideTranslocoConfig } from './i18n/transloco-config';
import { AuthService } from './auth/services/auth.service';
import { authInterceptor } from './auth/interceptors/auth.interceptor';

/**
 * Initialize authentication on app startup
 */
function initializeAuth(): () => Promise<void> {
  const authService = inject(AuthService);
  return () => authService.initialize();
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideOAuthClient(),
    provideTranslocoConfig(),
    {
      provide: APP_INITIALIZER,
      useFactory: initializeAuth,
      multi: true
    }
  ]
};
