/**
 * M12 Employee Advances & Settlement — Frontend Models
 * 
 * These models mirror the backend DTOs for employee advance management,
 * expense voucher settlement, and payment advice workflows.
 */

// ═══════════════════════════════════════════════════════════════════════════
// ENUMS
// ═══════════════════════════════════════════════════════════════════════════

export type AdvanceStatus = 
  | 'DRAFT'
  | 'PENDING_HOD'
  | 'PENDING_CEO'
  | 'PENDING_MD'
  | 'APPROVED'
  | 'REJECTED'
  | 'DISBURSED'
  | 'PARTIALLY_SETTLED'
  | 'FULLY_SETTLED'
  | 'CANCELLED';

export type ExpenseVoucherStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'APPROVED'
  | 'REJECTED'
  | 'SETTLED';

export type PaymentAdviceStatus =
  | 'PENDING_PAYMENT'
  | 'PAID';

export type ApproverRole =
  | 'HOD'
  | 'CEO'
  | 'MD';

export type PaymentMode =
  | 'CASH'
  | 'BANK'
  | 'UPI';

// ═══════════════════════════════════════════════════════════════════════════
// EMPLOYEE ADVANCE
// ═══════════════════════════════════════════════════════════════════════════

export interface EmployeeAdvance {
  id: number;
  tenantId: string;
  employeeId: number;
  employeeName?: string;
  departmentId: number;
  departmentName?: string;
  amount: number;
  purpose: string;
  status: AdvanceStatus;
  requestedDate: string;
  requiredBy?: string;
  
  // Approval chain
  hodApprovalRequired: boolean;
  hodApprovedBy?: string;
  hodApprovedAt?: string;
  ceoApprovalRequired: boolean;
  ceoApprovedBy?: string;
  ceoApprovedAt?: string;
  mdApprovalRequired: boolean;
  mdApprovedBy?: string;
  mdApprovedAt?: string;
  
  // Override
  overrideFlag: boolean;
  overrideReason?: string;
  
  // Settlement tracking
  settledAmount: number;
  outstandingAmount: number;
  
  // Disbursement
  disbursedAt?: string;
  journalEntryId?: number;
  
  // Audit
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateAdvanceRequest {
  employeeId: number;
  departmentId: number;
  amount: number;
  purpose: string;
  requiredBy?: string;
}

export interface ApprovalRequest {
  advanceId: number;
  action: 'APPROVE' | 'REJECT';
  reason?: string;
  override?: boolean;
  overrideReason?: string;
}

// ═══════════════════════════════════════════════════════════════════════════
// EXPENSE VOUCHER
// ═══════════════════════════════════════════════════════════════════════════

export interface ExpenseVoucher {
  id: number;
  tenantId: string;
  employeeId: number;
  employeeName?: string;
  departmentId: number;
  departmentName?: string;
  totalAmount: number;
  description: string;
  expenseDate: string;
  status: ExpenseVoucherStatus;
  
  // Settlement
  advanceSettlementAmount: number;
  reimbursementAmount: number;
  
  // Linked advances
  linkedAdvanceIds?: number[];
  
  // Payment advice (if reimbursement needed)
  paymentAdviceId?: number;
  
  // Audit
  approvedBy?: string;
  approvedAt?: string;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateExpenseVoucherRequest {
  employeeId: number;
  departmentId: number;
  totalAmount: number;
  description: string;
  expenseDate: string;
  advanceIdsToSettle?: number[];
}

// ═══════════════════════════════════════════════════════════════════════════
// ADVANCE RECEIPT (Cash Return)
// ═══════════════════════════════════════════════════════════════════════════

export interface AdvanceReceipt {
  id: number;
  tenantId: string;
  employeeId: number;
  employeeName?: string;
  departmentId: number;
  departmentName?: string;
  amount: number;
  paymentMode: PaymentMode;
  receiptDate: string;
  status: string;
  
  // Override
  overrideFlag: boolean;
  overrideReason?: string;
  
  // Journal
  journalEntryId?: number;
  
  // Audit
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateAdvanceReceiptRequest {
  employeeId: number;
  departmentId: number;
  amount: number;
  paymentMode: PaymentMode;
  receiptDate: string;
}

// ═══════════════════════════════════════════════════════════════════════════
// PAYMENT ADVICE (Reimbursement)
// ═══════════════════════════════════════════════════════════════════════════

export interface PaymentAdvice {
  id: number;
  tenantId: string;
  employeeId: number;
  employeeName?: string;
  departmentId: number;
  departmentName?: string;
  amount: number;
  expenseVoucherId: number;
  status: PaymentAdviceStatus;
  
  // Payment
  paymentVoucherId?: number;
  paidBy?: string;
  paidAt?: string;
  
  // Audit
  createdAt: string;
  updatedAt: string;
}

// ═══════════════════════════════════════════════════════════════════════════
// EMPLOYEE ADVANCE CONFIG
// ═══════════════════════════════════════════════════════════════════════════

export interface EmployeeAdvanceConfig {
  id: number;
  tenantId: string;
  employeeId: number;
  employeeName?: string;
  maxAdvanceLimit: number;
  currentOutstanding: number;
  availableLimit: number;
  effectiveFrom: string;
  effectiveTo?: string;
  isActive: boolean;
}

// ═══════════════════════════════════════════════════════════════════════════
// SUMMARY & REPORTS
// ═══════════════════════════════════════════════════════════════════════════

export interface AdvanceSummary {
  totalPending: number;
  totalApproved: number;
  totalDisbursed: number;
  totalOutstanding: number;
  pendingApprovalCount: number;
  overdueCount: number;
}

export interface EmployeeAdvanceBalance {
  employeeId: number;
  employeeName: string;
  departmentName: string;
  outstandingAdvance: number;
  maxLimit: number;
  availableLimit: number;
  oldestOutstandingDate?: string;
}
