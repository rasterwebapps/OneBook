import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PaymentAdviceListComponent } from './payment-advice-list.component';

describe('PaymentAdviceListComponent', () => {
  let component: PaymentAdviceListComponent;
  let fixture: ComponentFixture<PaymentAdviceListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PaymentAdviceListComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(PaymentAdviceListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Tab Navigation', () => {
    it('should default to pending tab', () => {
      expect(component.activeTab()).toBe('pending');
    });

    it('should switch tabs', () => {
      component.setTab('paid');
      expect(component.activeTab()).toBe('paid');
    });
  });

  describe('Search', () => {
    it('should update search query', () => {
      component.updateSearch('Rajesh');
      expect(component.searchQuery()).toBe('Rajesh');
    });

    it('should filter advices by employee name', () => {
      component.updateSearch('Rajesh');
      const filtered = component.filteredAdvices();
      expect(filtered.every(a => a.employeeName?.toLowerCase().includes('rajesh'))).toBe(true);
    });
  });

  describe('Mark as Paid', () => {
    it('should mark pending advice as paid', () => {
      const pendingAdvice = component.paymentAdvices().find(a => a.status === 'PENDING_PAYMENT');
      if (pendingAdvice) {
        component.markAsPaid(pendingAdvice.id);
        const updated = component.paymentAdvices().find(a => a.id === pendingAdvice.id);
        expect(updated?.status).toBe('PAID');
        expect(updated?.paidBy).toBeTruthy();
        expect(updated?.paidAt).toBeTruthy();
      }
    });
  });

  describe('Computed Values', () => {
    it('should calculate total pending amount', () => {
      const expected = component.paymentAdvices()
        .filter(a => a.status === 'PENDING_PAYMENT')
        .reduce((sum, a) => sum + a.amount, 0);
      expect(component.totalPending()).toBe(expected);
    });

    it('should count pending advices', () => {
      const expected = component.paymentAdvices().filter(a => a.status === 'PENDING_PAYMENT').length;
      expect(component.pendingCount()).toBe(expected);
    });
  });

  describe('Status Labels', () => {
    it('should return correct labels', () => {
      expect(component.statusLabel('PENDING_PAYMENT')).toBe('Pending Payment');
      expect(component.statusLabel('PAID')).toBe('Paid');
    });
  });

  describe('Filtering', () => {
    it('should show only pending advices on pending tab', () => {
      component.setTab('pending');
      const filtered = component.filteredAdvices();
      expect(filtered.every(a => a.status === 'PENDING_PAYMENT')).toBe(true);
    });

    it('should show only paid advices on paid tab', () => {
      component.setTab('paid');
      const filtered = component.filteredAdvices();
      expect(filtered.every(a => a.status === 'PAID')).toBe(true);
    });
  });
});
