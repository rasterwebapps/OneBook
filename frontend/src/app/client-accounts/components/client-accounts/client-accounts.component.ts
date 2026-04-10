import { Component, ChangeDetectionStrategy, signal, computed, HostListener } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { NxPageHeaderComponent, NxSearchInputComponent, NxEmptyStateComponent } from '../../../shared/components';

export type ClientType = 'CUSTOMER' | 'VENDOR' | 'EMPLOYEE' | 'INTERCOMPANY';
export type ViewMode = 'list' | 'form';

export interface ClientAccount {
  id: number;
  tenantId: string;
  ledgerAccountId: number;
  ledgerAccountName: string;
  clientType: ClientType;
  clientName: string;
  contactPerson: string;
  email: string;
  phone: string;
  billingAddress: string;
  shippingAddress: string;
  gstin: string;
  pan: string;
  creditLimit: number;
  paymentTermsDays: number;
  active: boolean;
}

const CLIENT_TYPE_LABELS: Record<ClientType, string> = {
  CUSTOMER: 'Customer',
  VENDOR: 'Vendor',
  EMPLOYEE: 'Employee',
  INTERCOMPANY: 'Intercompany',
};

const CLIENT_TYPE_ICONS: Record<ClientType, string> = {
  CUSTOMER: '👤',
  VENDOR: '🏭',
  EMPLOYEE: '💼',
  INTERCOMPANY: '🏢',
};

@Component({
  selector: 'app-client-accounts',
  standalone: true,
  imports: [DecimalPipe, NxPageHeaderComponent, NxSearchInputComponent, NxEmptyStateComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './client-accounts.component.html',
  styleUrl: './client-accounts.component.scss',
})
export class ClientAccountsComponent {

  /* ── View State ── */
  readonly mode = signal<ViewMode>('list');
  readonly searchQuery = signal('');
  readonly filterType = signal<ClientType | 'ALL'>('ALL');
  readonly selectedId = signal(0);

  /* ── Form State ── */
  readonly editingAccount = signal<ClientAccount | null>(null);
  readonly fClientName = signal('');
  readonly fClientType = signal<ClientType>('CUSTOMER');
  readonly fContactPerson = signal('');
  readonly fEmail = signal('');
  readonly fPhone = signal('');
  readonly fBillingAddress = signal('');
  readonly fShippingAddress = signal('');
  readonly fGstin = signal('');
  readonly fPan = signal('');
  readonly fCreditLimit = signal(0);
  readonly fPaymentTermsDays = signal(30);
  readonly formError = signal('');

  /* ── Data ── */
  readonly clientAccounts = signal<ClientAccount[]>([
    {
      id: 1, tenantId: 't1', ledgerAccountId: 101, ledgerAccountName: 'Sundry Debtors',
      clientType: 'CUSTOMER', clientName: 'Sharma Traders Pvt. Ltd.',
      contactPerson: 'Rajesh Sharma', email: 'rajesh@sharmatraders.com', phone: '9876543210',
      billingAddress: '45 MG Road, Mumbai 400001', shippingAddress: '45 MG Road, Mumbai 400001',
      gstin: '27AADCS1234P1Z5', pan: 'AADCS1234P', creditLimit: 500000, paymentTermsDays: 30, active: true,
    },
    {
      id: 2, tenantId: 't1', ledgerAccountId: 102, ledgerAccountName: 'Sundry Creditors',
      clientType: 'VENDOR', clientName: 'Gupta Electricals & Co.',
      contactPerson: 'Amit Gupta', email: 'amit@guptaelec.com', phone: '9988776655',
      billingAddress: '12 Nehru Place, New Delhi 110019', shippingAddress: '12 Nehru Place, New Delhi 110019',
      gstin: '07AADCG5678Q1Z3', pan: 'AADCG5678Q', creditLimit: 250000, paymentTermsDays: 45, active: true,
    },
    {
      id: 3, tenantId: 't1', ledgerAccountId: 103, ledgerAccountName: 'Salary Payable',
      clientType: 'EMPLOYEE', clientName: 'Priya Desai',
      contactPerson: 'Priya Desai', email: 'priya.desai@company.com', phone: '9112233445',
      billingAddress: '', shippingAddress: '',
      gstin: '', pan: 'BFGPD1234R', creditLimit: 0, paymentTermsDays: 0, active: true,
    },
    {
      id: 4, tenantId: 't1', ledgerAccountId: 104, ledgerAccountName: 'Intercompany Receivables',
      clientType: 'INTERCOMPANY', clientName: 'Nexus Subsidiary Ltd.',
      contactPerson: 'Finance Dept', email: 'finance@nexussub.com', phone: '1122334455',
      billingAddress: '100 Business Park, Bangalore 560001', shippingAddress: '',
      gstin: '29AADCN9876L1Z8', pan: 'AADCN9876L', creditLimit: 1000000, paymentTermsDays: 60, active: true,
    },
    {
      id: 5, tenantId: 't1', ledgerAccountId: 105, ledgerAccountName: 'Sundry Debtors',
      clientType: 'CUSTOMER', clientName: 'MedSupply India Ltd.',
      contactPerson: 'Dr. Kapoor', email: 'kapoor@medsupply.in', phone: '9001234567',
      billingAddress: '78 Health Lane, Pune 411001', shippingAddress: '78 Health Lane, Pune 411001',
      gstin: '27AADCM2345R1Z7', pan: 'AADCM2345R', creditLimit: 300000, paymentTermsDays: 30, active: false,
    },
  ]);

  /* ── Computed ── */
  readonly filteredAccounts = computed(() => {
    const query = this.searchQuery().toLowerCase();
    const type = this.filterType();
    return this.clientAccounts().filter(a => {
      if (!a.active && this.mode() === 'list') {
        // Show inactive only if specifically filtering
      }
      if (type !== 'ALL' && a.clientType !== type) return false;
      if (query && !a.clientName.toLowerCase().includes(query)
          && !a.contactPerson.toLowerCase().includes(query)
          && !a.email.toLowerCase().includes(query)) return false;
      return true;
    });
  });

  readonly accountCounts = computed(() => {
    const accounts = this.clientAccounts();
    return {
      total: accounts.length,
      active: accounts.filter(a => a.active).length,
      customers: accounts.filter(a => a.clientType === 'CUSTOMER').length,
      vendors: accounts.filter(a => a.clientType === 'VENDOR').length,
      employees: accounts.filter(a => a.clientType === 'EMPLOYEE').length,
      intercompany: accounts.filter(a => a.clientType === 'INTERCOMPANY').length,
    };
  });

  readonly totalCreditExposure = computed(() =>
    this.clientAccounts().filter(a => a.active).reduce((sum, a) => sum + a.creditLimit, 0)
  );

  /* ── Keyboard Shortcuts ── */
  @HostListener('window:keydown', ['$event'])
  onKey(e: KeyboardEvent): void {
    if (e.ctrlKey && e.key === 's') {
      if (this.mode() === 'form') { e.preventDefault(); this.saveAccount(); }
      return;
    }
    const tag = (e.target as HTMLElement)?.tagName;
    if (tag === 'INPUT' || tag === 'SELECT' || tag === 'TEXTAREA') return;

    if (e.key === 'c' || e.key === 'C') {
      if (this.mode() === 'list') this.startCreate();
    } else if (e.key === 'Escape') {
      if (this.mode() === 'form') this.backToList();
    } else if (e.key === 'Enter') {
      if (this.mode() === 'list' && this.selectedId()) {
        const a = this.clientAccounts().find(x => x.id === this.selectedId());
        if (a) this.editAccount(a);
      }
    }
  }

  /* ── Actions ── */
  startCreate(): void {
    this.resetForm();
    this.mode.set('form');
  }

  editAccount(a: ClientAccount): void {
    this.editingAccount.set(a);
    this.fClientName.set(a.clientName);
    this.fClientType.set(a.clientType);
    this.fContactPerson.set(a.contactPerson);
    this.fEmail.set(a.email);
    this.fPhone.set(a.phone);
    this.fBillingAddress.set(a.billingAddress);
    this.fShippingAddress.set(a.shippingAddress);
    this.fGstin.set(a.gstin);
    this.fPan.set(a.pan);
    this.fCreditLimit.set(a.creditLimit);
    this.fPaymentTermsDays.set(a.paymentTermsDays);
    this.formError.set('');
    this.mode.set('form');
  }

  saveAccount(): void {
    const err = this.validateForm();
    if (err) { this.formError.set(err); return; }

    if (this.editingAccount()) {
      this.clientAccounts.update(accounts =>
        accounts.map(a => a.id === this.editingAccount()!.id ? {
          ...a,
          clientName: this.fClientName(),
          clientType: this.fClientType(),
          contactPerson: this.fContactPerson(),
          email: this.fEmail(),
          phone: this.fPhone(),
          billingAddress: this.fBillingAddress(),
          shippingAddress: this.fShippingAddress(),
          gstin: this.fGstin(),
          pan: this.fPan(),
          creditLimit: this.fCreditLimit(),
          paymentTermsDays: this.fPaymentTermsDays(),
        } : a)
      );
    } else {
      const maxId = this.clientAccounts().reduce((m, a) => Math.max(m, a.id), 0);
      const newAccount: ClientAccount = {
        id: maxId + 1,
        tenantId: 't1',
        ledgerAccountId: 0,
        ledgerAccountName: 'Unlinked',
        clientType: this.fClientType(),
        clientName: this.fClientName(),
        contactPerson: this.fContactPerson(),
        email: this.fEmail(),
        phone: this.fPhone(),
        billingAddress: this.fBillingAddress(),
        shippingAddress: this.fShippingAddress(),
        gstin: this.fGstin(),
        pan: this.fPan(),
        creditLimit: this.fCreditLimit(),
        paymentTermsDays: this.fPaymentTermsDays(),
        active: true,
      };
      this.clientAccounts.update(accounts => [...accounts, newAccount]);
    }

    this.backToList();
  }

  deactivateAccount(id: number): void {
    this.clientAccounts.update(accounts =>
      accounts.map(a => a.id === id ? { ...a, active: false } : a)
    );
    if (this.selectedId() === id) this.selectedId.set(0);
  }

  backToList(): void {
    this.mode.set('list');
    this.editingAccount.set(null);
  }

  /* ── Utility Methods ── */
  updateSearch(value: string): void {
    this.searchQuery.set(value);
  }

  setFilterType(type: ClientType | 'ALL'): void {
    this.filterType.set(type);
  }

  selectAccount(id: number): void {
    this.selectedId.set(id);
  }

  clientTypeLabel(type: ClientType): string {
    return CLIENT_TYPE_LABELS[type] ?? type;
  }

  clientTypeIcon(type: ClientType): string {
    return CLIENT_TYPE_ICONS[type] ?? '📋';
  }

  updateClientName(value: string): void { this.fClientName.set(value); }
  updateClientType(value: ClientType): void { this.fClientType.set(value); }
  updateContactPerson(value: string): void { this.fContactPerson.set(value); }
  updateEmail(value: string): void { this.fEmail.set(value); }
  updatePhone(value: string): void { this.fPhone.set(value); }
  updateBillingAddress(value: string): void { this.fBillingAddress.set(value); }
  updateShippingAddress(value: string): void { this.fShippingAddress.set(value); }
  updateGstin(value: string): void { this.fGstin.set(value); }
  updatePan(value: string): void { this.fPan.set(value); }
  updateCreditLimit(value: number): void { this.fCreditLimit.set(value); }
  updatePaymentTermsDays(value: number): void { this.fPaymentTermsDays.set(value); }

  /* ── Private ── */
  private validateForm(): string {
    if (!this.fClientName().trim()) return 'Client Name is required.';
    if (!this.fClientType()) return 'Client Type is required.';
    return '';
  }

  private resetForm(): void {
    this.editingAccount.set(null);
    this.fClientName.set('');
    this.fClientType.set('CUSTOMER');
    this.fContactPerson.set('');
    this.fEmail.set('');
    this.fPhone.set('');
    this.fBillingAddress.set('');
    this.fShippingAddress.set('');
    this.fGstin.set('');
    this.fPan.set('');
    this.fCreditLimit.set(0);
    this.fPaymentTermsDays.set(30);
    this.formError.set('');
  }
}
