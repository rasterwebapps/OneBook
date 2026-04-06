import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ClientAccountsComponent, ClientAccount, ClientType } from './client-accounts.component';

describe('ClientAccountsComponent', () => {
  let component: ClientAccountsComponent;
  let fixture: ComponentFixture<ClientAccountsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClientAccountsComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ClientAccountsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should start in list mode', () => {
    expect(component.mode()).toBe('list');
  });

  it('should have sample client accounts', () => {
    expect(component.clientAccounts().length).toBeGreaterThan(0);
  });

  it('should compute account counts', () => {
    const counts = component.accountCounts();
    expect(counts.total).toBe(component.clientAccounts().length);
    expect(counts.active).toBeLessThanOrEqual(counts.total);
    expect(counts.customers).toBeGreaterThan(0);
    expect(counts.vendors).toBeGreaterThan(0);
  });

  it('should compute total credit exposure', () => {
    expect(component.totalCreditExposure()).toBeGreaterThan(0);
  });

  it('should filter accounts by search query', () => {
    component.updateSearch('Sharma');
    const filtered = component.filteredAccounts();
    expect(filtered.length).toBe(1);
    expect(filtered[0].clientName).toContain('Sharma');
  });

  it('should filter accounts by type', () => {
    component.setFilterType('VENDOR');
    const filtered = component.filteredAccounts();
    expect(filtered.every(a => a.clientType === 'VENDOR')).toBeTrue();
  });

  it('should show all when filter is ALL', () => {
    component.setFilterType('ALL');
    const filtered = component.filteredAccounts();
    expect(filtered.length).toBe(component.clientAccounts().length);
  });

  it('should switch to form mode on startCreate', () => {
    component.startCreate();
    expect(component.mode()).toBe('form');
    expect(component.editingAccount()).toBeNull();
  });

  it('should switch to form mode on editAccount', () => {
    const account = component.clientAccounts()[0];
    component.editAccount(account);
    expect(component.mode()).toBe('form');
    expect(component.editingAccount()).toBe(account);
    expect(component.fClientName()).toBe(account.clientName);
  });

  it('should go back to list mode', () => {
    component.startCreate();
    expect(component.mode()).toBe('form');
    component.backToList();
    expect(component.mode()).toBe('list');
    expect(component.editingAccount()).toBeNull();
  });

  it('should validate required fields', () => {
    component.startCreate();
    component.saveAccount();
    expect(component.formError()).toBe('Client Name is required.');
  });

  it('should create a new account', () => {
    const countBefore = component.clientAccounts().length;
    component.startCreate();
    component.updateClientName('Test Client');
    component.updateClientType('CUSTOMER');
    component.saveAccount();
    expect(component.clientAccounts().length).toBe(countBefore + 1);
    expect(component.mode()).toBe('list');
  });

  it('should update an existing account', () => {
    const account = component.clientAccounts()[0];
    component.editAccount(account);
    component.updateClientName('Updated Name');
    component.saveAccount();
    const updated = component.clientAccounts().find(a => a.id === account.id);
    expect(updated?.clientName).toBe('Updated Name');
  });

  it('should deactivate an account', () => {
    const account = component.clientAccounts().find(a => a.active)!;
    component.deactivateAccount(account.id);
    const deactivated = component.clientAccounts().find(a => a.id === account.id);
    expect(deactivated?.active).toBeFalse();
  });

  it('should clear selectedId when deactivating selected account', () => {
    const account = component.clientAccounts()[0];
    component.selectAccount(account.id);
    expect(component.selectedId()).toBe(account.id);
    component.deactivateAccount(account.id);
    expect(component.selectedId()).toBe(0);
  });

  it('should select an account', () => {
    const account = component.clientAccounts()[0];
    component.selectAccount(account.id);
    expect(component.selectedId()).toBe(account.id);
  });

  it('should return correct client type labels', () => {
    expect(component.clientTypeLabel('CUSTOMER')).toBe('Customer');
    expect(component.clientTypeLabel('VENDOR')).toBe('Vendor');
    expect(component.clientTypeLabel('EMPLOYEE')).toBe('Employee');
    expect(component.clientTypeLabel('INTERCOMPANY')).toBe('Intercompany');
  });

  it('should return correct client type icons', () => {
    expect(component.clientTypeIcon('CUSTOMER')).toBe('👤');
    expect(component.clientTypeIcon('VENDOR')).toBe('🏭');
    expect(component.clientTypeIcon('EMPLOYEE')).toBe('💼');
    expect(component.clientTypeIcon('INTERCOMPANY')).toBe('🏢');
  });

  it('should update form fields via setter methods', () => {
    component.updateContactPerson('John');
    expect(component.fContactPerson()).toBe('John');
    component.updateEmail('john@test.com');
    expect(component.fEmail()).toBe('john@test.com');
    component.updatePhone('1234567890');
    expect(component.fPhone()).toBe('1234567890');
    component.updateBillingAddress('123 Main St');
    expect(component.fBillingAddress()).toBe('123 Main St');
    component.updateShippingAddress('456 Ship St');
    expect(component.fShippingAddress()).toBe('456 Ship St');
    component.updateGstin('22AAAAA0000A1Z5');
    expect(component.fGstin()).toBe('22AAAAA0000A1Z5');
    component.updatePan('AAAAA0000A');
    expect(component.fPan()).toBe('AAAAA0000A');
    component.updateCreditLimit(50000);
    expect(component.fCreditLimit()).toBe(50000);
    component.updatePaymentTermsDays(45);
    expect(component.fPaymentTermsDays()).toBe(45);
  });
});
