import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  EmployeeAdvance,
  CreateAdvanceRequest,
  ApprovalRequest,
  ExpenseVoucher,
  CreateExpenseVoucherRequest,
  AdvanceReceipt,
  CreateAdvanceReceiptRequest,
  PaymentAdvice,
  AdvanceSummary,
  EmployeeAdvanceBalance,
} from '../models/advance.models';

/**
 * M12 Employee Advances & Settlement — API Service
 *
 * Handles all HTTP requests for advance management, expense settlement,
 * and payment advice workflows.
 */
@Injectable({ providedIn: 'root' })
export class AdvanceService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api';

  // ═══════════════════════════════════════════════════════════════════════════
  // EMPLOYEE ADVANCES
  // ═══════════════════════════════════════════════════════════════════════════

  getAdvances(): Observable<EmployeeAdvance[]> {
    return this.http.get<EmployeeAdvance[]>(`${this.baseUrl}/advances`);
  }

  getAdvanceById(id: number): Observable<EmployeeAdvance> {
    return this.http.get<EmployeeAdvance>(`${this.baseUrl}/advances/${id}`);
  }

  getMyAdvances(): Observable<EmployeeAdvance[]> {
    return this.http.get<EmployeeAdvance[]>(`${this.baseUrl}/advances/my`);
  }

  getAdvancesByEmployee(employeeId: number): Observable<EmployeeAdvance[]> {
    return this.http.get<EmployeeAdvance[]>(`${this.baseUrl}/advances/employee/${employeeId}`);
  }

  getAdvancesByDepartment(departmentId: number): Observable<EmployeeAdvance[]> {
    return this.http.get<EmployeeAdvance[]>(`${this.baseUrl}/advances/department/${departmentId}`);
  }

  getPendingApprovals(): Observable<EmployeeAdvance[]> {
    return this.http.get<EmployeeAdvance[]>(`${this.baseUrl}/advances/pending-approvals`);
  }

  createAdvance(request: CreateAdvanceRequest): Observable<EmployeeAdvance> {
    return this.http.post<EmployeeAdvance>(`${this.baseUrl}/advances`, request);
  }

  approveAdvance(id: number, request: ApprovalRequest): Observable<EmployeeAdvance> {
    return this.http.post<EmployeeAdvance>(`${this.baseUrl}/advances/${id}/approve`, request);
  }

  rejectAdvance(id: number, request: ApprovalRequest): Observable<EmployeeAdvance> {
    return this.http.post<EmployeeAdvance>(`${this.baseUrl}/advances/${id}/reject`, request);
  }

  disburseAdvance(id: number): Observable<EmployeeAdvance> {
    return this.http.post<EmployeeAdvance>(`${this.baseUrl}/advances/${id}/disburse`, {});
  }

  getAdvanceSummary(): Observable<AdvanceSummary> {
    return this.http.get<AdvanceSummary>(`${this.baseUrl}/advances/summary`);
  }

  getEmployeeBalances(): Observable<EmployeeAdvanceBalance[]> {
    return this.http.get<EmployeeAdvanceBalance[]>(`${this.baseUrl}/advances/balances`);
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // EXPENSE VOUCHERS
  // ═══════════════════════════════════════════════════════════════════════════

  getExpenseVouchers(): Observable<ExpenseVoucher[]> {
    return this.http.get<ExpenseVoucher[]>(`${this.baseUrl}/expense-vouchers`);
  }

  getExpenseVoucherById(id: number): Observable<ExpenseVoucher> {
    return this.http.get<ExpenseVoucher>(`${this.baseUrl}/expense-vouchers/${id}`);
  }

  getMyExpenseVouchers(): Observable<ExpenseVoucher[]> {
    return this.http.get<ExpenseVoucher[]>(`${this.baseUrl}/expense-vouchers/my`);
  }

  createExpenseVoucher(request: CreateExpenseVoucherRequest): Observable<ExpenseVoucher> {
    return this.http.post<ExpenseVoucher>(`${this.baseUrl}/expense-vouchers`, request);
  }

  approveExpenseVoucher(id: number): Observable<ExpenseVoucher> {
    return this.http.post<ExpenseVoucher>(`${this.baseUrl}/expense-vouchers/${id}/approve`, {});
  }

  rejectExpenseVoucher(id: number, reason: string): Observable<ExpenseVoucher> {
    return this.http.post<ExpenseVoucher>(`${this.baseUrl}/expense-vouchers/${id}/reject`, { reason });
  }

  settleExpenseVoucher(id: number): Observable<ExpenseVoucher> {
    return this.http.post<ExpenseVoucher>(`${this.baseUrl}/expense-vouchers/${id}/settle`, {});
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // ADVANCE RECEIPTS (Cash Return)
  // ═══════════════════════════════════════════════════════════════════════════

  getAdvanceReceipts(): Observable<AdvanceReceipt[]> {
    return this.http.get<AdvanceReceipt[]>(`${this.baseUrl}/advance-receipts`);
  }

  getAdvanceReceiptById(id: number): Observable<AdvanceReceipt> {
    return this.http.get<AdvanceReceipt>(`${this.baseUrl}/advance-receipts/${id}`);
  }

  createAdvanceReceipt(request: CreateAdvanceReceiptRequest): Observable<AdvanceReceipt> {
    return this.http.post<AdvanceReceipt>(`${this.baseUrl}/advance-receipts`, request);
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // PAYMENT ADVICES (Reimbursement)
  // ═══════════════════════════════════════════════════════════════════════════

  getPaymentAdvices(): Observable<PaymentAdvice[]> {
    return this.http.get<PaymentAdvice[]>(`${this.baseUrl}/payment-advices`);
  }

  getPaymentAdviceById(id: number): Observable<PaymentAdvice> {
    return this.http.get<PaymentAdvice>(`${this.baseUrl}/payment-advices/${id}`);
  }

  getPendingPaymentAdvices(): Observable<PaymentAdvice[]> {
    return this.http.get<PaymentAdvice[]>(`${this.baseUrl}/payment-advices/pending`);
  }

  markPaymentAdvicePaid(id: number, paymentVoucherId: number): Observable<PaymentAdvice> {
    return this.http.post<PaymentAdvice>(`${this.baseUrl}/payment-advices/${id}/pay`, { paymentVoucherId });
  }
}
