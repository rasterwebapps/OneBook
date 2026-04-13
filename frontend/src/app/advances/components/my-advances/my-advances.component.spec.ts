import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MyAdvancesComponent } from './my-advances.component';

describe('MyAdvancesComponent', () => {
  let component: MyAdvancesComponent;
  let fixture: ComponentFixture<MyAdvancesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MyAdvancesComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(MyAdvancesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Tab Navigation', () => {
    it('should default to "all" tab', () => {
      expect(component.activeTab()).toBe('all');
    });

    it('should switch tabs', () => {
      component.setTab('pending');
      expect(component.activeTab()).toBe('pending');

      component.setTab('disbursed');
      expect(component.activeTab()).toBe('disbursed');
    });
  });

  describe('Search', () => {
    it('should update search query', () => {
      component.updateSearch('travel');
      expect(component.searchQuery()).toBe('travel');
    });

    it('should filter advances by search query', () => {
      component.updateSearch('Mumbai');
      const filtered = component.filteredAdvances();
      expect(filtered.length).toBeGreaterThan(0);
      expect(filtered.every(a => a.purpose.toLowerCase().includes('mumbai'))).toBe(true);
    });
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
      expect(component.newAdvance().amount).toBe(0);
    });
  });

  describe('Form Validation', () => {
    it('should be invalid when amount is 0', () => {
      component.updateAmount('0');
      component.updatePurpose('Test purpose');
      expect(component.isFormValid()).toBe(false);
    });

    it('should be invalid when purpose is empty', () => {
      component.updateAmount('5000');
      component.updatePurpose('');
      expect(component.isFormValid()).toBe(false);
    });

    it('should be valid when amount > 0 and purpose provided', () => {
      component.updateAmount('5000');
      component.updatePurpose('Office supplies');
      expect(component.isFormValid()).toBe(true);
    });
  });

  describe('Approval Tier', () => {
    it('should return HOD for amount <= 10000', () => {
      expect(component.getApprovalTier(5000)).toBe('HOD (≤₹10k)');
      expect(component.getApprovalTier(10000)).toBe('HOD (≤₹10k)');
    });

    it('should return CEO for amount 10001-20000', () => {
      expect(component.getApprovalTier(15000)).toBe('CEO (₹10k-20k)');
      expect(component.getApprovalTier(20000)).toBe('CEO (₹10k-20k)');
    });

    it('should return MD for amount > 20000', () => {
      expect(component.getApprovalTier(25000)).toBe('MD (>₹20k)');
      expect(component.getApprovalTier(50000)).toBe('MD (>₹20k)');
    });
  });

  describe('Computed Values', () => {
    it('should calculate total outstanding', () => {
      expect(component.totalOutstanding()).toBeGreaterThan(0);
    });

    it('should count pending advances', () => {
      expect(component.pendingCount()).toBeGreaterThanOrEqual(0);
    });
  });

  describe('Status Labels', () => {
    it('should return correct labels', () => {
      expect(component.statusLabel('PENDING_HOD')).toBe('Pending HOD');
      expect(component.statusLabel('DISBURSED')).toBe('Disbursed');
      expect(component.statusLabel('APPROVED')).toBe('Approved');
    });
  });

  describe('Submit Advance', () => {
    it('should add new advance to list', () => {
      const initialCount = component.myAdvances().length;
      component.updateAmount('5000');
      component.updatePurpose('Test advance');
      component.submitAdvanceRequest();
      expect(component.myAdvances().length).toBe(initialCount + 1);
    });

    it('should close modal after submit', () => {
      component.openCreateModal();
      component.updateAmount('5000');
      component.updatePurpose('Test advance');
      component.submitAdvanceRequest();
      expect(component.showCreateModal()).toBe(false);
    });
  });
});
