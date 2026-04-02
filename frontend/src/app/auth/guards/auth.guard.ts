import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Auth Guard - Protects routes requiring authentication
 * 
 * Usage in routes:
 * { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] }
 */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  if (authService.isAuthenticated()) {
    return true;
  }
  
  // Not authenticated - redirect to start page
  router.navigate(['/start']);
  return false;
};

/**
 * Role Guard Factory - Protects routes requiring specific roles
 * 
 * Usage in routes:
 * { path: 'admin', component: AdminComponent, canActivate: [roleGuard(['ROLE_ADMIN'])] }
 */
export const roleGuard = (requiredRoles: string[]): CanActivateFn => {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);
    
    if (!authService.isAuthenticated()) {
      router.navigate(['/start']);
      return false;
    }
    
    if (authService.hasAnyRole(requiredRoles)) {
      return true;
    }
    
    // Authenticated but lacks required roles - redirect to dashboard with error
    router.navigate(['/'], { 
      queryParams: { error: 'access_denied' } 
    });
    return false;
  };
};

/**
 * Admin Guard - Shortcut for admin-only routes
 */
export const adminGuard: CanActivateFn = roleGuard(['ROLE_ADMIN']);

/**
 * Accountant Guard - Routes for accountants
 */
export const accountantGuard: CanActivateFn = roleGuard(['ROLE_ACCOUNTANT', 'ROLE_ADMIN']);

/**
 * Auditor Guard - Routes for auditors (read-only access)
 */
export const auditorGuard: CanActivateFn = roleGuard(['ROLE_AUDITOR', 'ROLE_ADMIN']);

/**
 * Public Guard - Ensures user is NOT authenticated (for login page)
 * Redirects to dashboard if already logged in
 */
export const publicGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  if (authService.isAuthenticated()) {
    // Already logged in - redirect to dashboard
    router.navigate(['/']);
    return false;
  }
  
  return true;
};
