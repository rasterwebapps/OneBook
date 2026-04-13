import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ExpenseVoucherComponent } from './expense-voucher.component';

describe('ExpenseVoucherComponent', () => {
  let component: ExpenseVoucherComponent;
  let fixture: ComponentFixture<ExpenseVoucherComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExpenseVoucherComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ExpenseVoucherComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Create Modal', () => {
    it('should open create modal', () => {
      component.openCreateModal();
      expect(component.showCreateModal()).toBe(true);
    });

    it('should close create modal', () => {
      component.openCreateModal();
      component.closeCreateModal();
      expect(component.showCreateModal()).toBe(false);
    });

    it('should reset form on open', () => {
      component.updateAmount('5000');
      component.openCreateModal();
      expect(component.newExpense().totalAmount).toBe(0);
    });
  });

  describe('Form Updates', () => {
    it('should update amount', () => {
      component.updateAmount('5000');
      expect(component.newExpense().totalAmount).toBe(5000);
    });

    it('should update description', () => {
      component.updateDescription('Test expense');
      expect(component.newExpense().description).toBe('Test expense');
    });

    it('should update expense date', () => {
      component.updateExpenseDate('2026-04-15');
      expect(component.newExpense().expenseDate).toBe('2026-04-15');
    });
  });

  describe('Advance Selection', () => {
    it('should toggle advance selection', () => {
      const advanceId = component.outstandingAdvances()[0].id;
      component.toggleAdvanceSelection(advanceId);
      expect(component.isAdvanceSelected(advanceId)).toBe(true);

      component.toggleAdvanceSelection(advanceId);
      expect(component.isAdvanceSelected(advanceId)).toBe(false);
    });
  });

  describe('Settlement Breakdown', () => {
    it('should calculate zero when no expense', () => {
      const breakdown = component.settlementBreakdown();
      expect(breakdown.fromAdvance).toBe(0);
      expect(breakdown.reimbursement).toBe(0);
    });

    it('should settle from advance when expense <= outstanding', () => {
      const advance = component.outstandingAdvances()[0];
      component.toggleAdvanceSelection(advance.id);
      component.updateAmount('2000');

      const breakdown = component.settlementBreakdown();
      expect(breakdown.fromAdvance).toBe(2000);
      expect(breakdown.reimbursement).toBe(0);
    });

    it('should calculate reimbursement when expense > outstanding', () => {
      const advance = component.outstandingAdvances()[0];
      component.toggleAdvanceSelection(advance.id);
      component.updateAmount('5000'); // More than outstanding 2500

      const breakdown = component.settlementBreakdown();
      expect(breakdown.fromAdvance).toBe(advance.outstandingAmount);
      expect(breakdown.reimbursement).toBe(5000 - advance.outstandingAmount);
    });
  });

  describe('Form Validation', () => {
    it('should be invalid when amount is 0', () => {
      component.updateAmount('0');
      component.updateDescription('Test');
      expect(component.isFormValid()).toBe(false);
    });

    it('should be invalid when description is empty', () => {
      component.updateAmount('5000');
      component.updateDescription('');
      expect(component.isFormValid()).toBe(false);
    });

    it('should be valid with amount and description', () => {
      component.updateAmount('5000');
      component.updateDescription('Test expense');
      expect(component.isFormValid()).toBe(true);
    });
  });

  describe('Submit', () => {
    it('should add new expense voucher', () => {
      const initialCount = component.myExpenseVouchers().length;
      component.updateAmount('1000');
      component.updateDescription('Test expense');
      component.submitExpenseVoucher();
      expect(component.myExpenseVouchers().length).toBe(initialCount + 1);
    });

    it('should close modal after submit', () => {
      component.openCreateModal();
      component.updateAmount('1000');
      component.updateDescription('Test expense');
      component.submitExpenseVoucher();
      expect(component.showCreateModal()).toBe(false);
    });
  });

  describe('Status Labels', () => {
    it('should return correct labels', () => {
      expect(component.statusLabel('SUBMITTED')).toBe('Submitted');
      expect(component.statusLabel('SETTLED')).toBe('Settled');
      expect(component.statusLabel('APPROVED')).toBe('Approved');
    });
  });

  describe('Computed Values', () => {
    it('should calculate total outstanding advance', () => {
      const expected = component.outstandingAdvances().reduce((sum, a) => sum + a.outstandingAmount, 0);
      expect(component.totalOutstandingAdvance()).toBe(expected);
    });
  });
});
