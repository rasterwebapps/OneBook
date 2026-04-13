package com.nexus.onebook.ledger.compliance.service;

import com.nexus.onebook.ledger.compliance.dto.TdsTcsEntryRequest;
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
import com.nexus.onebook.ledger.accounts.repository.JournalTransactionRepository;
import com.nexus.onebook.ledger.compliance.repository.TdsTcsEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * TDS/TCS service — automated deduction and collection of tax at source.
 * Supports section-wise rates and status tracking (PENDING → DEDUCTED → DEPOSITED → FILED).
 */
@Service
public class TdsTcsService {

    private final TdsTcsEntryRepository tdsTcsRepository;
    private final JournalTransactionRepository journalTransactionRepository;

    public TdsTcsService(TdsTcsEntryRepository tdsTcsRepository,
                          JournalTransactionRepository journalTransactionRepository) {
        this.tdsTcsRepository = tdsTcsRepository;
        this.journalTransactionRepository = journalTransactionRepository;
    }

    @Transactional
    public TdsTcsEntry createEntry(TdsTcsEntryRequest request) {
        BigDecimal taxAmount = request.taxableAmount()
                .multiply(request.taxRate())
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        TdsTcsEntry entry = new TdsTcsEntry(
                request.tenantId(),
                TdsTcsType.valueOf(request.entryType()),
                request.sectionCode(),
                request.partyName(),
                request.transactionDate(),
                request.taxableAmount(),
                request.taxRate(),
                taxAmount);

        if (request.partyPan() != null) entry.setPartyPan(request.partyPan());
        if (request.journalTransactionId() != null) {
            JournalTransaction jt = journalTransactionRepository.findById(request.journalTransactionId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Journal transaction not found: " + request.journalTransactionId()));
            entry.setJournalTransaction(jt);
        }

        return tdsTcsRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<TdsTcsEntry> getEntries(String tenantId) {
        return tdsTcsRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public List<TdsTcsEntry> getEntriesByType(String tenantId, TdsTcsType type) {
        return tdsTcsRepository.findByTenantIdAndEntryType(tenantId, type);
    }

    @Transactional(readOnly = true)
    public List<TdsTcsEntry> getPendingEntries(String tenantId) {
        return tdsTcsRepository.findByTenantIdAndStatus(tenantId, TdsTcsStatus.PENDING);
    }

    @Transactional
    public TdsTcsEntry updateStatus(Long entryId, TdsTcsStatus newStatus, String certificateNumber) {
        TdsTcsEntry entry = tdsTcsRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("TDS/TCS entry not found: " + entryId));
        entry.setStatus(newStatus);
        if (certificateNumber != null) entry.setCertificateNumber(certificateNumber);
        return tdsTcsRepository.save(entry);
    }
}
