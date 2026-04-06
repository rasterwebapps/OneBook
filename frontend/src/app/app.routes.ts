import { Routes } from '@angular/router';
import { authGuard, publicGuard, auditorGuard } from './auth/guards/auth.guard';

export const routes: Routes = [
  // Start / Landing Page (Public)
  { 
    path: 'start', 
    loadComponent: () => import('./auth/components/start/start.component').then(m => m.StartComponent),
    canActivate: [publicGuard]
  },

  // Home / Dashboard (Omni-Command Center) - Protected
  { 
    path: '', 
    loadComponent: () => import('./dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [authGuard]
  },

  // Voucher Entry (F4-F9 shortcuts) - Protected
  { 
    path: 'voucher/:type', 
    loadComponent: () => import('./accounting/components/voucher-entry/voucher-entry.component').then(m => m.VoucherEntryComponent),
    canActivate: [authGuard]
  },

  // Ledger - Protected
  { 
    path: 'ledger', 
    loadComponent: () => import('./accounting/components/ledger/ledger.component').then(m => m.LedgerComponent),
    canActivate: [authGuard]
  },
  { 
    path: 'ledger/:name', 
    loadComponent: () => import('./accounting/components/ledger/ledger.component').then(m => m.LedgerComponent),
    canActivate: [authGuard]
  },

  // Universal Accounts Receivable - Protected
  { 
    path: 'receivable', 
    loadComponent: () => import('./receivable/components/accounts-receivable/accounts-receivable.component').then(m => m.AccountsReceivableComponent),
    canActivate: [authGuard]
  },

  // Reports (Alt+F2, F3, F5, F7) - Protected
  { 
    path: 'reports/:type', 
    loadComponent: () => import('./reports/components/reports/reports.component').then(m => m.ReportsComponent),
    canActivate: [authGuard]
  },

  // Inventory - Protected
  { 
    path: 'inventory', 
    loadComponent: () => import('./inventory/components/inventory/inventory.component').then(m => m.InventoryComponent),
    canActivate: [authGuard]
  },

  // GST & Compliance - Protected
  { 
    path: 'gst', 
    loadComponent: () => import('./gst/components/gst-dashboard/gst-dashboard.component').then(m => m.GstDashboardComponent),
    canActivate: [authGuard]
  },

  // Masters (Alt+C, Alt+A, Alt+D) - Protected
  { 
    path: 'master/:mode', 
    loadComponent: () => import('./master/components/master/master.component').then(m => m.MasterComponent),
    canActivate: [authGuard]
  },

  // Payment Register (AP Payables + Maker-Checker Batch Approval) - Protected
  {
    path: 'payable-register',
    loadComponent: () => import('./payable/components/payment-register/payment-register.component').then(m => m.PaymentRegisterComponent),
    canActivate: [authGuard]
  },

  // Client Accounts (Customers, Vendors, Employees, Intercompany) - Protected
  {
    path: 'client-accounts',
    loadComponent: () => import('./client-accounts/components/client-accounts/client-accounts.component').then(m => m.ClientAccountsComponent),
    canActivate: [authGuard]
  },

  // Banking & Reconciliation - Protected
  { 
    path: 'banking', 
    loadComponent: () => import('./banking/components/banking/banking.component').then(m => m.BankingComponent),
    canActivate: [authGuard]
  },

  // AI Insights - Protected
  { 
    path: 'ai', 
    loadComponent: () => import('./ai/components/ai-dashboard/ai-dashboard.component').then(m => m.AiDashboardComponent),
    canActivate: [authGuard]
  },

  // Share Market Valuation Center - Protected
  { 
    path: 'market', 
    loadComponent: () => import('./market/components/market-valuation/market-valuation.component').then(m => m.MarketValuationComponent),
    canActivate: [authGuard]
  },

  // Auditor Portal - Protected (Auditor Role)
  { 
    path: 'auditor', 
    loadComponent: () => import('./auditor/components/auditor-dashboard/auditor-dashboard.component').then(m => m.AuditorDashboardComponent),
    canActivate: [auditorGuard]
  },

  // Catch-all redirect to start page
  { path: '**', redirectTo: 'start' },
];
