import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, map, catchError, of, throwError } from 'rxjs';
import {
  LedgerAccount,
  JournalTransactionRequest,
  JournalTransaction,
  ContraVoucher,
  Voucher,
  VoucherCategory,
  VOUCHER_TYPE_CONFIG,
} from '../models/voucher.models';
import { AccountMasterService } from './account-master.service';

const TENANT_ID = 'default-tenant';

/** Shape returned by GET /api/journal/transactions with nested entries */
interface BackendJournalTransaction {
  id: number;
  tenantId: string;
  transactionUuid: string;
  transactionDate: string;
  description: string;
  posted: boolean;
  metadata: string;
  entries: BackendJournalEntry[];
  createdAt: string;
}

interface BackendJournalEntry {
  id: number;
  tenantId: string;
  entryType: 'DEBIT' | 'CREDIT';
  amount: number;
  description: string;
  account: { id: number; accountName: string; accountCode: string } | null;
}

@Injectable({ providedIn: 'root' })
export class VoucherService {
  private readonly http = inject(HttpClient);
  private readonly masterSvc = inject(AccountMasterService);

  /* ── Ledger accounts — delegate to the master service ── */
  readonly accounts = this.masterSvc.accounts;

  /** Contra uses only Cash-in-Hand (17) + Bank Accounts (16) groups */
  readonly cashBankAccounts = computed(() =>
    this.accounts().filter(a => a.active && (a.groupId === 16 || a.groupId === 17))
  );

  /* ── Generic voucher store ── */
  readonly vouchers = signal<Voucher[]>([]);
  private seqMap: Record<string, number> = {};

  /* ── Legacy contra vouchers — computed from vouchers ── */
  readonly contraVouchers = computed<ContraVoucher[]>(() =>
    this.vouchers().filter(v => v.voucherType === 'CONTRA')
  );

  /* ── Load ledger accounts from backend ── */
  loadAccounts(): Observable<LedgerAccount[]> {
    this.masterSvc.initialize();
    return of(this.accounts());
  }

  /* ── Load vouchers by type from backend ── */
  loadVouchers(type: VoucherCategory): void {
    this.http.get<BackendJournalTransaction[]>('/api/journal/transactions', {
      params: { tenantId: TENANT_ID },
    }).pipe(
      map(txns => txns.filter(t => {
        try { return JSON.parse(t.metadata || '{}').voucherType === type; }
        catch { return false; }
      })),
      map(txns => txns.map(t => this.backendToVoucher(t, type))),
      tap(vs => {
        this.vouchers.set(vs);
        this.seqMap[type] = vs.length;
      }),
      catchError(() => { this.vouchers.set([]); return of([]); }),
    ).subscribe();
  }

  /* ── Legacy: load contra vouchers ── */
  loadContraVouchers(): void {
    this.loadVouchers('CONTRA');
  }

  /* ── Create a voucher (POST to backend) ── */
  createVoucher(
    type: VoucherCategory,
    date: string,
    debitAccountId: number,
    creditAccountId: number,
    amount: number,
    narration: string,
  ): Observable<Voucher> {
    const req: JournalTransactionRequest = {
      tenantId: TENANT_ID,
      transactionDate: date,
      description: narration,
      metadata: JSON.stringify({ voucherType: type }),
      entries: [
        { accountId: debitAccountId, entryType: 'DEBIT', amount },
        { accountId: creditAccountId, entryType: 'CREDIT', amount },
      ],
    };

    return this.http.post<BackendJournalTransaction>('/api/journal/transactions', req).pipe(
      map(txn => this.backendToVoucher(txn, type)),
      tap(v => this.vouchers.update(list => [v, ...list])),
      catchError(() => {
        const v = this.offlineVoucher(type, date, debitAccountId, creditAccountId, amount, narration);
        this.vouchers.update(list => [v, ...list]);
        return of(v);
      }),
    );
  }

  /* ── Legacy create contra ── */
  createContraVoucher(
    date: string, drId: number, crId: number, amount: number, narration: string,
  ): Observable<ContraVoucher> {
    return this.createVoucher('CONTRA', date, drId, crId, amount, narration);
  }

  /* ── Update a voucher (PUT to backend) ── */
  updateVoucher(
    uuid: string,
    type: VoucherCategory,
    date: string,
    debitAccountId: number,
    creditAccountId: number,
    amount: number,
    narration: string,
  ): Observable<Voucher> {
    const req: JournalTransactionRequest = {
      tenantId: TENANT_ID,
      transactionDate: date,
      description: narration,
      metadata: JSON.stringify({ voucherType: type }),
      entries: [
        { accountId: debitAccountId, entryType: 'DEBIT', amount },
        { accountId: creditAccountId, entryType: 'CREDIT', amount },
      ],
    };

    return this.http.put<BackendJournalTransaction>(`/api/journal/transactions/${uuid}`, req).pipe(
      map(txn => this.backendToVoucher(txn, type)),
      tap(v => this.vouchers.update(list => list.map(x => x.uuid === uuid ? v : x))),
      catchError(() => {
        const updated = this.buildVoucher(
          type, uuid, this.voucherNumberFor(uuid, type),
          date, debitAccountId, creditAccountId, amount, narration,
        );
        this.vouchers.update(list => list.map(x => x.uuid === uuid ? updated : x));
        return of(updated);
      }),
    );
  }

  /* ── Legacy update contra ── */
  updateContraVoucher(
    uuid: string, date: string, drId: number, crId: number, amount: number, narration: string,
  ): Observable<ContraVoucher> {
    return this.updateVoucher(uuid, 'CONTRA', date, drId, crId, amount, narration);
  }

  /* ── Delete a voucher (DELETE on backend) ── */
  deleteVoucher(uuid: string): Observable<void> {
    this.vouchers.update(list => list.filter(v => v.uuid !== uuid));
    return this.http.delete<void>(`/api/journal/transactions/${uuid}`).pipe(
      catchError(() => of(undefined)),
    );
  }

  /* ── Post a transaction (marks as posted) ── */
  postTransaction(transactionUuid: string): Observable<void> {
    const voucher = this.vouchers().find(v => v.uuid === transactionUuid);
    if (!voucher) {
      return throwError(() => new Error('Voucher not found'));
    }

    return this.http.post<void>(
      `/api/journal/transactions/${transactionUuid}/post`,
      null,
      { params: { tenantId: TENANT_ID } }
    ).pipe(
      tap(() => {
        this.vouchers.update(list =>
          list.map(v => v.uuid === transactionUuid ? { ...v, posted: true } : v)
        );
      }),
    );
  }

  /* ── Legacy delete contra ── */
  deleteContraVoucher(uuid: string): void {
    this.deleteVoucher(uuid).subscribe();
  }

  /* ── Helpers ── */
  private backendToVoucher(txn: BackendJournalTransaction, type: VoucherCategory): Voucher {
    const dr = txn.entries.find(e => e.entryType === 'DEBIT');
    const cr = txn.entries.find(e => e.entryType === 'CREDIT');
    const accts = this.accounts();
    const drId = dr?.account?.id ?? 0;
    const crId = cr?.account?.id ?? 0;
    return {
      uuid: txn.transactionUuid,
      voucherType: type,
      voucherNumber: this.nextVoucherNumber(type),
      date: txn.transactionDate,
      debitAccountId: drId,
      debitAccountName: dr?.account?.accountName ?? accts.find(a => a.id === drId)?.accountName ?? `Account #${drId}`,
      creditAccountId: crId,
      creditAccountName: cr?.account?.accountName ?? accts.find(a => a.id === crId)?.accountName ?? `Account #${crId}`,
      amount: dr?.amount ?? cr?.amount ?? 0,
      narration: txn.description ?? '',
      posted: txn.posted,
      createdAt: txn.createdAt,
    };
  }

  private offlineVoucher(
    type: VoucherCategory, date: string, drId: number, crId: number, amount: number, narration: string,
  ): Voucher {
    const uuid = crypto.randomUUID();
    return this.buildVoucher(type, uuid, this.nextVoucherNumber(type), date, drId, crId, amount, narration);
  }

  private buildVoucher(
    type: VoucherCategory, uuid: string, vNum: string, date: string,
    drId: number, crId: number, amount: number, narration: string,
  ): Voucher {
    const accts = this.accounts();
    return {
      uuid,
      voucherType: type,
      voucherNumber: vNum,
      date,
      debitAccountId: drId,
      debitAccountName: accts.find(a => a.id === drId)?.accountName ?? `Account #${drId}`,
      creditAccountId: crId,
      creditAccountName: accts.find(a => a.id === crId)?.accountName ?? `Account #${crId}`,
      amount,
      narration,
      posted: false,
      createdAt: new Date().toISOString(),
    };
  }

  private nextVoucherNumber(type: VoucherCategory): string {
    const seq = (this.seqMap[type] ?? 0) + 1;
    this.seqMap[type] = seq;
    const prefix = VOUCHER_TYPE_CONFIG[type]?.prefix ?? 'VCH';
    return `${prefix}-${String(seq).padStart(4, '0')}`;
  }

  private voucherNumberFor(uuid: string, type: VoucherCategory): string {
    return this.vouchers().find(v => v.uuid === uuid)?.voucherNumber ?? this.nextVoucherNumber(type);
  }
}
