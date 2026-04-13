package com.nexus.onebook.compliance.service;

import com.nexus.onebook.compliance.dto.TdsTcsEntryRequest;
import com.nexus.onebook.accounts.model.*;
import com.nexus.onebook.auditor.model.*;
import com.nexus.onebook.banking.model.*;
import com.nexus.onebook.clientaccount.model.*;
import com.nexus.onebook.compliance.model.*;
import com.nexus.onebook.credit.model.*;
import com.nexus.onebook.currency.model.*;
import com.nexus.onebook.entitlement.model.*;
import com.nexus.onebook.fixedasset.model.*;
import com.nexus.onebook.foundation.model.*;
import com.nexus.onebook.intelligence.model.*;
import com.nexus.onebook.inventory.model.*;
import com.nexus.onebook.operations.model.*;
import com.nexus.onebook.payroll.model.*;
import com.nexus.onebook.reporting.model.*;
import com.nexus.onebook.tenant.model.*;
import com.nexus.onebook.accounts.repository.JournalTransactionRepository;
import com.nexus.onebook.compliance.repository.TdsTcsEntryRepository;
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
