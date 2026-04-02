import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PaymentRegisterComponent } from './payment-register.component';

describe('PaymentRegisterComponent', () => {
  let component: PaymentRegisterComponent;
  let fixture: ComponentFixture<PaymentRegisterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PaymentRegisterComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(PaymentRegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should start on the register tab', () => {
    expect(component.activeTab()).toBe('register');
  });

  it('should have vendor groups with entries', () => {
    expect(component.vendorGroups().length).toBeGreaterThan(0);
    expect(component.vendorGroups()[0].entries.length).toBeGreaterThan(0);
  });

  it('should compute total outstanding', () => {
    expect(component.totalOutstanding()).toBeGreaterThan(0);
  });

  it('should compute pending batch count', () => {
    const pending = component.paymentBatches().filter(b => b.status === 'PENDING_APPROVAL').length;
    expect(component.pendingBatchCount()).toBe(pending);
  });

  it('should toggle vendor group expand', () => {
    const vid = component.vendorGroups()[0].vendorAccountId;
    expect(component.vendorGroups()[0].expanded).toBeFalse();
    component.toggleVendorExpand(vid);
    expect(component.vendorGroups().find(g => g.vendorAccountId === vid)?.expanded).toBeTrue();
  });

  it('should toggle entry selection', () => {
    const group = component.vendorGroups()[0];
    const vid = group.vendorAccountId;
    const eid = group.entries[0].id;
    expect(group.entries[0].selected).toBeFalse();
    component.toggleEntrySelect(vid, eid);
    const updated = component.vendorGroups().find(g => g.vendorAccountId === vid)!;
    expect(updated.entries.find(e => e.id === eid)?.selected).toBeTrue();
  });

  it('should toggle select all entries for a vendor', () => {
    const vid = component.vendorGroups()[0].vendorAccountId;
    component.toggleSelectAllForVendor(vid);
    const group = component.vendorGroups().find(g => g.vendorAccountId === vid)!;
    expect(group.entries.every(e => e.selected)).toBeTrue();
  });

  it('should detect hasSelectedEntries after selecting', () => {
    const group = component.vendorGroups()[0];
    const vid = group.vendorAccountId;
    expect(component.hasSelectedEntries(vid)).toBeFalse();
    component.toggleEntrySelect(vid, group.entries[0].id);
    expect(component.hasSelectedEntries(vid)).toBeTrue();
  });

  it('should open and close batch modal', () => {
    const vid = component.vendorGroups()[0].vendorAccountId;
    component.openCreateBatchModal(vid);
    expect(component.showBatchModal()).toBeTrue();
    component.closeBatchModal();
    expect(component.showBatchModal()).toBeFalse();
  });

  it('should open approval modal for approve action', () => {
    const batchId = component.paymentBatches()[0].id;
    component.openApprovalModal(batchId, 'APPROVE');
    expect(component.showApprovalModal()).toBeTrue();
    expect(component.approvalAction()).toBe('APPROVE');
  });

  it('should open approval modal for reject action', () => {
    const batchId = component.paymentBatches()[0].id;
    component.openApprovalModal(batchId, 'REJECT');
    expect(component.showApprovalModal()).toBeTrue();
    expect(component.approvalAction()).toBe('REJECT');
  });

  it('should close approval modal', () => {
    component.openApprovalModal(component.paymentBatches()[0].id, 'APPROVE');
    component.closeApprovalModal();
    expect(component.showApprovalModal()).toBeFalse();
    expect(component.approvalBatchId()).toBeNull();
  });

  it('should approve a pending batch', () => {
    const batch = component.paymentBatches().find(b => b.status === 'PENDING_APPROVAL')!;
    component.openApprovalModal(batch.id, 'APPROVE');
    component.confirmApproval();
    const updated = component.paymentBatches().find(b => b.id === batch.id)!;
    expect(updated.status).toBe('APPROVED');
    expect(updated.approvedBy).toBe('finance.manager');
  });

  it('should reject a pending batch with a reason', () => {
    const batch = component.paymentBatches().find(b => b.status === 'PENDING_APPROVAL')!;
    component.openApprovalModal(batch.id, 'REJECT');
    component.updateRejectionReason('Duplicate payment');
    component.confirmApproval();
    const updated = component.paymentBatches().find(b => b.id === batch.id)!;
    expect(updated.status).toBe('REJECTED');
    expect(updated.rejectionReason).toBe('Duplicate payment');
  });

  it('should create a new batch from selected entries', () => {
    const group = component.vendorGroups()[0];
    const vid = group.vendorAccountId;
    // Select only purchase entries
    group.entries.filter(e => e.status === 'AVAILABLE_FOR_PROCESSING').forEach(e => {
      component.toggleEntrySelect(vid, e.id);
    });
    const batchCountBefore = component.paymentBatches().length;
    component.openCreateBatchModal(vid);
    component.createBatch();
    expect(component.paymentBatches().length).toBe(batchCountBefore + 1);
    expect(component.activeTab()).toBe('batches');
  });

  it('should switch tabs', () => {
    component.setTab('batches');
    expect(component.activeTab()).toBe('batches');
    component.setTab('register');
    expect(component.activeTab()).toBe('register');
  });

  it('should filter vendor groups by search query', () => {
    component.updateSearch('Gupta');
    const filtered = component.filteredVendorGroups();
    expect(filtered.length).toBe(1);
    expect(filtered[0].vendorName).toContain('Gupta');
  });

  it('should update batch payment mode', () => {
    component.updateBatchPaymentMode('RTGS');
    expect(component.batchPaymentMode()).toBe('RTGS');
  });

  it('should update batch bank account', () => {
    component.updateBatchBankAccount('SBI-CA-11223344');
    expect(component.batchBankAccount()).toBe('SBI-CA-11223344');
  });

  it('should return correct transaction type labels', () => {
    expect(component.transactionTypeLabel('PURCHASE')).toBe('Purchase');
    expect(component.transactionTypeLabel('PURCHASE_RETURN')).toBe('Return');
    expect(component.transactionTypeLabel('CREDIT_NOTE')).toBe('Credit Note');
  });

  it('should return correct batch status labels', () => {
    expect(component.batchStatusLabel('PENDING_APPROVAL')).toBe('Pending Approval');
    expect(component.batchStatusLabel('APPROVED')).toBe('Approved');
    expect(component.batchStatusLabel('REJECTED')).toBe('Rejected');
    expect(component.batchStatusLabel('PAYMENT_GENERATED')).toBe('Payment Generated');
  });

  it('should return vendor name by id', () => {
    const group = component.vendorGroups()[0];
    expect(component.getVendorName(group.vendorAccountId)).toBe(group.vendorName);
    expect(component.getVendorName(null)).toBe('');
  });

  it('should generate a unique batch number based on existing max sequence', () => {
    const group = component.vendorGroups()[0];
    const vid = group.vendorAccountId;
    group.entries.filter(e => e.status === 'AVAILABLE_FOR_PROCESSING').forEach(e => {
      component.toggleEntrySelect(vid, e.id);
    });
    component.openCreateBatchModal(vid);
    component.createBatch();
    const batches = component.paymentBatches();
    const last = batches[batches.length - 1];
    // Ensure no duplicates
    const batchNumbers = batches.map(b => b.batchNumber);
    expect(new Set(batchNumbers).size).toBe(batchNumbers.length);
    expect(last.batchNumber).toMatch(/^PB-\d{4}-\d{2}-\d{3}$/);
  });

  it('should compute selected net payable correctly', () => {
    const group = component.vendorGroups()[0];
    const vid = group.vendorAccountId;
    // Select all entries
    component.toggleSelectAllForVendor(vid);
    // Net = purchases - returns - credits
    const net = component.selectedNetPayable();
    expect(net).toBe(group.netOutstanding);
  });
});
