import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ApprovalQueueComponent } from './approval-queue.component';

describe('ApprovalQueueComponent', () => {
  let component: ApprovalQueueComponent;
  let fixture: ComponentFixture<ApprovalQueueComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApprovalQueueComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ApprovalQueueComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Search', () => {
    it('should update search query', () => {
      component.updateSearch('Priya');
      expect(component.searchQuery()).toBe('Priya');
    });

    it('should filter approvals by employee name', () => {
      component.updateSearch('Priya');
      const filtered = component.filteredApprovals();
      expect(filtered.every(a => a.employeeName?.toLowerCase().includes('priya'))).toBe(true);
    });
  });

  describe('Approval Modal', () => {
    it('should open approval modal', () => {
      const advance = component.pendingApprovals()[0];
      component.openApprovalModal(advance, 'APPROVE');
      expect(component.showApprovalModal()).toBe(true);
      expect(component.selectedAdvance()).toBe(advance);
      expect(component.approvalAction()).toBe('APPROVE');
    });

    it('should open rejection modal', () => {
      const advance = component.pendingApprovals()[0];
      component.openApprovalModal(advance, 'REJECT');
      expect(component.showApprovalModal()).toBe(true);
      expect(component.approvalAction()).toBe('REJECT');
    });

    it('should close modal', () => {
      const advance = component.pendingApprovals()[0];
      component.openApprovalModal(advance, 'APPROVE');
      component.closeApprovalModal();
      expect(component.showApprovalModal()).toBe(false);
      expect(component.selectedAdvance()).toBeNull();
    });
  });

  describe('Validation', () => {
    it('should validate approval without override', () => {
      expect(component.isApproveValid()).toBe(true);
    });

    it('should require override reason when override is checked', () => {
      component.toggleOverride();
      expect(component.isApproveValid()).toBe(false);

      component.updateOverrideReason('Urgent business need');
      expect(component.isApproveValid()).toBe(true);
    });

    it('should require rejection reason', () => {
      expect(component.isRejectValid()).toBe(false);

      component.updateRejectionReason('Insufficient documentation');
      expect(component.isRejectValid()).toBe(true);
    });
  });

  describe('Approval Tier', () => {
    it('should return correct tier for amounts', () => {
      expect(component.getApprovalTier(5000)).toBe('HOD (≤₹10k)');
      expect(component.getApprovalTier(15000)).toBe('CEO (₹10k-20k)');
      expect(component.getApprovalTier(25000)).toBe('MD (>₹20k)');
    });
  });

  describe('Urgency Detection', () => {
    it('should detect urgent advances', () => {
      const urgentAdvance = {
        ...component.pendingApprovals()[0],
        requiredBy: new Date(Date.now() + 2 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      };
      expect(component.isUrgent(urgentAdvance)).toBe(true);
    });

    it('should not mark non-urgent advances', () => {
      const nonUrgentAdvance = {
        ...component.pendingApprovals()[0],
        requiredBy: new Date(Date.now() + 10 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      };
      expect(component.isUrgent(nonUrgentAdvance)).toBe(false);
    });
  });

  describe('Computed Values', () => {
    it('should calculate total pending amount', () => {
      const expectedTotal = component.pendingApprovals().reduce((sum, a) => sum + a.amount, 0);
      expect(component.totalPendingAmount()).toBe(expectedTotal);
    });
  });

  describe('Status Labels', () => {
    it('should return correct labels', () => {
      expect(component.statusLabel('PENDING_HOD')).toBe('Pending HOD');
      expect(component.statusLabel('PENDING_CEO')).toBe('Pending CEO');
      expect(component.statusLabel('APPROVED')).toBe('Approved');
    });
  });
});
