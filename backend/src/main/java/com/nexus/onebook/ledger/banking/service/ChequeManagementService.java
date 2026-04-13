package com.nexus.onebook.ledger.banking.service;

import com.nexus.onebook.ledger.banking.dto.ChequeEntryRequest;
import com.nexus.onebook.ledger.accounts.model.*;
import com.nexus.onebook.ledger.auditor.model.*;
import com.nexus.onebook.ledger.banking.model.*;
import com.nexus.onebook.ledger.clientaccount.model.*;
import com.nexus.onebook.ledger.compliance.model.*;
import com.nexus.onebook.ledger.credit.model.*;
import com.nexus.onebook.ledger.currency.model.*;
import com.nexus.onebook.ledger.entitlement.model.*;
import com.nexus.onebook.ledger.fixedasset.model.*;
import com.nexus.onebook.ledger.foundation.model.*;
import com.nexus.onebook.ledger.intelligence.model.*;
import com.nexus.onebook.ledger.inventory.model.*;
import com.nexus.onebook.ledger.operations.model.*;
import com.nexus.onebook.ledger.payroll.model.*;
import com.nexus.onebook.ledger.reporting.model.*;
import com.nexus.onebook.ledger.tenant.model.*;
import com.nexus.onebook.ledger.banking.repository.ChequeEntryRepository;
import com.nexus.onebook.ledger.accounts.repository.LedgerAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Cheque Management service — integrated cheque register to track
 * issued, cleared, bounced, cancelled, and stale cheques.
 */
@Service
public class ChequeManagementService {

    private final ChequeEntryRepository chequeRepository;
    private final LedgerAccountRepository ledgerAccountRepository;

    public ChequeManagementService(ChequeEntryRepository chequeRepository,
                                    LedgerAccountRepository ledgerAccountRepository) {
        this.chequeRepository = chequeRepository;
        this.ledgerAccountRepository = ledgerAccountRepository;
    }

    @Transactional
    public ChequeEntry issueCheque(ChequeEntryRequest request) {
        LedgerAccount bankAccount = ledgerAccountRepository.findById(request.bankAccountId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Bank account not found: " + request.bankAccountId()));

        chequeRepository.findByTenantIdAndChequeNumberAndBankAccountId(
                        request.tenantId(), request.chequeNumber(), request.bankAccountId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Cheque '" + request.chequeNumber() + "' already registered");
                });

        ChequeEntry cheque = new ChequeEntry(
                request.tenantId(), request.chequeNumber(), bankAccount,
                request.partyName(), request.amount(), request.chequeDate(),
                ChequeType.valueOf(request.chequeType()));

        return chequeRepository.save(cheque);
    }

    @Transactional(readOnly = true)
    public List<ChequeEntry> getChequeRegister(String tenantId) {
        return chequeRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public List<ChequeEntry> getChequesByStatus(String tenantId, ChequeStatus status) {
        return chequeRepository.findByTenantIdAndStatus(tenantId, status);
    }

    @Transactional
    public ChequeEntry clearCheque(Long chequeId, LocalDate clearingDate) {
        ChequeEntry cheque = chequeRepository.findById(chequeId)
                .orElseThrow(() -> new IllegalArgumentException("Cheque not found: " + chequeId));
        cheque.setStatus(ChequeStatus.CLEARED);
        cheque.setClearingDate(clearingDate);
        return chequeRepository.save(cheque);
    }

    @Transactional
    public ChequeEntry bounceCheque(Long chequeId, String reason) {
        ChequeEntry cheque = chequeRepository.findById(chequeId)
                .orElseThrow(() -> new IllegalArgumentException("Cheque not found: " + chequeId));
        cheque.setStatus(ChequeStatus.BOUNCED);
        cheque.setBounceReason(reason);
        return chequeRepository.save(cheque);
    }

    @Transactional
    public ChequeEntry cancelCheque(Long chequeId) {
        ChequeEntry cheque = chequeRepository.findById(chequeId)
                .orElseThrow(() -> new IllegalArgumentException("Cheque not found: " + chequeId));
        cheque.setStatus(ChequeStatus.CANCELLED);
        return chequeRepository.save(cheque);
    }
}
