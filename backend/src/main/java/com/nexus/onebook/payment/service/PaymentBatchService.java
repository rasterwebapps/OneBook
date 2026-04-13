package com.nexus.onebook.payment.service;

import com.nexus.onebook.cache.WarmCacheService;
import com.nexus.onebook.accounts.dto.JournalEntryRequest;
import com.nexus.onebook.accounts.dto.JournalTransactionRequest;



import com.nexus.onebook.payment.dto.BatchApprovalRequest;
import com.nexus.onebook.payment.dto.CreateBatchRequest;
import com.nexus.onebook.payment.dto.PaymentBatchResponse;
import com.nexus.onebook.payment.model.*;
import com.nexus.onebook.payment.repository.PaymentBatchItemRepository;
import com.nexus.onebook.payment.repository.PaymentBatchRepository;
import com.nexus.onebook.payment.repository.PaymentRegisterRepository;
import com.nexus.onebook.security.AuditLogService;
import com.nexus.onebook.accounts.service.JournalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentBatchService {

    private final PaymentRegisterRepository registerRepository;
    private final PaymentBatchRepository batchRepository;
    private final PaymentBatchItemRepository batchItemRepository;
    private final JournalService journalService;
    private final WarmCacheService warmCacheService;
    private final AuditLogService auditLogService;
    private final PaymentFileGeneratorService fileGeneratorService;

    public PaymentBatchService(
            PaymentRegisterRepository registerRepository,
            PaymentBatchRepository batchRepository,
            PaymentBatchItemRepository batchItemRepository,
            JournalService journalService,
            WarmCacheService warmCacheService,
            AuditLogService auditLogService,
            PaymentFileGeneratorService fileGeneratorService) {
        this.registerRepository = registerRepository;
        this.batchRepository = batchRepository;
        this.batchItemRepository = batchItemRepository;
        this.journalService = journalService;
        this.warmCacheService = warmCacheService;
        this.auditLogService = auditLogService;
        this.fileGeneratorService = fileGeneratorService;
    }

    @Transactional
    public PaymentBatchResponse createBatch(String tenantId, CreateBatchRequest request, String createdBy) {
        List<PaymentRegisterEntry> entries = registerRepository
            .findByIdInAndTenantId(request.registerEntryIds(), tenantId);

        if (entries.size() != request.registerEntryIds().size()) {
            throw new IllegalArgumentException("Some register entries not found or do not belong to tenant");
        }

        for (PaymentRegisterEntry entry : entries) {
            if (entry.getStatus() != PaymentRegisterStatus.AVAILABLE_FOR_PROCESSING) {
                throw new IllegalStateException("Entry " + entry.getId() + " is not available for processing");
            }
        }

        boolean allSameVendor = entries.stream()
            .allMatch(e -> request.vendorAccountId().equals(e.getVendorAccountId()));
        if (!allSameVendor) {
            throw new IllegalArgumentException("All register entries must belong to the same vendor");
        }

        BigDecimal totalPurchases = entries.stream()
            .filter(e -> "PURCHASE".equals(e.getTransactionType()))
            .map(PaymentRegisterEntry::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalReturns = entries.stream()
            .filter(e -> "PURCHASE_RETURN".equals(e.getTransactionType()))
            .map(PaymentRegisterEntry::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCreditNotes = entries.stream()
            .filter(e -> "CREDIT_NOTE".equals(e.getTransactionType()))
            .map(PaymentRegisterEntry::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netPayable = totalPurchases.subtract(totalReturns).subtract(totalCreditNotes);

        String batchNumber = generateBatchNumber(tenantId);
        String vendorName = entries.get(0).getVendorName();

        PaymentBatch batch = new PaymentBatch();
        batch.setTenantId(tenantId);
        batch.setBatchNumber(batchNumber);
        batch.setVendorAccountId(request.vendorAccountId());
        batch.setVendorName(vendorName);
        batch.setTotalPurchases(totalPurchases);
        batch.setTotalReturns(totalReturns);
        batch.setTotalCreditNotes(totalCreditNotes);
        batch.setNetPayable(netPayable);
        batch.setBankAccountId(request.bankAccountId());
        batch.setPaymentMode(request.paymentMode());
        batch.setStatus(PaymentBatchStatus.PENDING_APPROVAL);
        batch.setCreatedBy(createdBy);

        PaymentBatch savedBatch = batchRepository.save(batch);

        List<PaymentBatchItem> items = new ArrayList<>();
        for (PaymentRegisterEntry entry : entries) {
            PaymentBatchItem item = new PaymentBatchItem();
            item.setTenantId(tenantId);
            item.setBatch(savedBatch);
            item.setRegisterEntry(entry);
            item.setTransactionType(entry.getTransactionType());
            item.setAmount(entry.getAmount());
            items.add(item);

            entry.setStatus(PaymentRegisterStatus.IN_BATCH);
            entry.setBatchId(savedBatch.getId());
        }

        batchItemRepository.saveAll(items);
        registerRepository.saveAll(entries);

        auditLogService.logInsert(tenantId, "payment_batches", savedBatch.getId(),
            "batchNumber=" + batchNumber + ", vendorAccountId=" + request.vendorAccountId() + ", netPayable=" + netPayable);

        return PaymentBatchResponse.from(savedBatch, items);
    }

    @Transactional
    public PaymentBatchResponse approveBatch(String tenantId, Long batchId, String approvedBy) {
        PaymentBatch batch = batchRepository.findById(batchId)
            .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchId));

        if (!tenantId.equals(batch.getTenantId())) {
            throw new IllegalArgumentException("Batch does not belong to tenant");
        }

        if (batch.getStatus() != PaymentBatchStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Batch " + batchId + " is not pending approval");
        }

        batch.setStatus(PaymentBatchStatus.APPROVED);
        batch.setApprovedBy(approvedBy);
        batch.setApprovedAt(Instant.now());

        if (batch.getBankAccountId() != null && batch.getVendorAccountId() != null) {
            // Double-entry: Dr Vendor AP (reduces liability owed to vendor) / Cr Bank (reduces cash balance)
            List<JournalEntryRequest> journalEntries = List.of(
                new JournalEntryRequest(batch.getVendorAccountId(), "DEBIT", batch.getNetPayable(),
                    "Payment batch " + batch.getBatchNumber(), null),
                new JournalEntryRequest(batch.getBankAccountId(), "CREDIT", batch.getNetPayable(),
                    "Payment batch " + batch.getBatchNumber(), null)
            );

            JournalTransactionRequest journalRequest = new JournalTransactionRequest(
                tenantId,
                LocalDate.now(),
                "Payment batch " + batch.getBatchNumber() + " approved",
                null,
                journalEntries
            );

            var journalTx = journalService.createTransaction(journalRequest);
            batch.setPaymentJournalId(journalTx.getId());
        }

        List<PaymentBatchItem> batchItems = batchItemRepository.findByBatchId(batchId);
        List<Long> registerEntryIds = batchItems.stream()
            .map(item -> item.getRegisterEntry().getId())
            .collect(Collectors.toList());

        List<PaymentRegisterEntry> entries = registerRepository.findByIdInAndTenantId(registerEntryIds, tenantId);
        for (PaymentRegisterEntry entry : entries) {
            entry.setStatus(PaymentRegisterStatus.POSTED);
        }
        registerRepository.saveAll(entries);


        PaymentBatch savedBatch = batchRepository.save(batch);

        warmCacheService.evictTrialBalance(tenantId);

        auditLogService.logUpdate(tenantId, "payment_batches", batchId,
            "status=PENDING_APPROVAL",
            "status=APPROVED, approvedBy=" + approvedBy);

        return PaymentBatchResponse.from(savedBatch, batchItems);
    }

    @Transactional
    public PaymentBatchResponse rejectBatch(String tenantId, Long batchId, String rejectedBy, String reason) {
        PaymentBatch batch = batchRepository.findById(batchId)
            .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchId));

        if (!tenantId.equals(batch.getTenantId())) {
            throw new IllegalArgumentException("Batch does not belong to tenant");
        }

        if (batch.getStatus() != PaymentBatchStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Batch " + batchId + " is not pending approval");
        }

        batch.setStatus(PaymentBatchStatus.REJECTED);
        batch.setRejectedBy(rejectedBy);
        batch.setRejectedAt(Instant.now());
        batch.setRejectionReason(reason);

        List<PaymentBatchItem> batchItems = batchItemRepository.findByBatchId(batchId);
        List<Long> registerEntryIds = batchItems.stream()
            .map(item -> item.getRegisterEntry().getId())
            .collect(Collectors.toList());

        List<PaymentRegisterEntry> entries = registerRepository.findByIdInAndTenantId(registerEntryIds, tenantId);
        for (PaymentRegisterEntry entry : entries) {
            entry.setStatus(PaymentRegisterStatus.AVAILABLE_FOR_PROCESSING);
            entry.setBatchId(null);
        }
        registerRepository.saveAll(entries);

        PaymentBatch savedBatch = batchRepository.save(batch);

        auditLogService.logUpdate(tenantId, "payment_batches", batchId,
            "status=PENDING_APPROVAL",
            "status=REJECTED, rejectedBy=" + rejectedBy + ", reason=" + reason);

        return PaymentBatchResponse.from(savedBatch, batchItems);
    }

    @Transactional
    public byte[] generatePaymentFile(String tenantId, Long batchId) {
        PaymentBatch batch = batchRepository.findById(batchId)
            .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchId));

        if (!tenantId.equals(batch.getTenantId())) {
            throw new IllegalArgumentException("Batch does not belong to tenant");
        }

        if (batch.getStatus() != PaymentBatchStatus.APPROVED) {
            throw new IllegalStateException("Batch " + batchId + " is not approved for payment file generation");
        }

        List<PaymentBatchItem> batchItems = batchItemRepository.findByBatchId(batchId);
        byte[] csvBytes = fileGeneratorService.generateCsv(batch, batchItems);

        batch.setStatus(PaymentBatchStatus.PAYMENT_GENERATED);
        batch.setPaymentFileGenerated(true);

        List<Long> registerEntryIds = batchItems.stream()
            .map(item -> item.getRegisterEntry().getId())
            .collect(Collectors.toList());
        List<PaymentRegisterEntry> entries = registerRepository.findByIdInAndTenantId(registerEntryIds, tenantId);
        for (PaymentRegisterEntry entry : entries) {
            entry.setStatus(PaymentRegisterStatus.PAYMENT_GENERATED);
        }
        registerRepository.saveAll(entries);
        batchRepository.save(batch);

        auditLogService.logUpdate(tenantId, "payment_batches", batchId,
            "status=APPROVED",
            "status=PAYMENT_GENERATED, paymentFileGenerated=true");

        return csvBytes;
    }

    @Transactional(readOnly = true)
    public List<PaymentBatchResponse> getPendingBatches(String tenantId) {
        return batchRepository.findByTenantIdAndStatus(tenantId, PaymentBatchStatus.PENDING_APPROVAL)
            .stream()
            .map(b -> PaymentBatchResponse.from(b, batchItemRepository.findByBatchId(b.getId())))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentBatchResponse> getApprovedBatches(String tenantId) {
        return batchRepository.findByTenantIdAndStatus(tenantId, PaymentBatchStatus.APPROVED)
            .stream()
            .map(b -> PaymentBatchResponse.from(b, batchItemRepository.findByBatchId(b.getId())))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PaymentBatchResponse getBatchById(String tenantId, Long batchId) {
        PaymentBatch batch = batchRepository.findById(batchId)
            .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchId));
        if (!tenantId.equals(batch.getTenantId())) {
            throw new IllegalArgumentException("Batch does not belong to tenant");
        }
        List<PaymentBatchItem> items = batchItemRepository.findByBatchId(batchId);
        return PaymentBatchResponse.from(batch, items);
    }

    private String generateBatchNumber(String tenantId) {
        LocalDate now = LocalDate.now();
        String prefix = "PB-" + now.format(DateTimeFormatter.ofPattern("yyyy-MM")) + "-";
        long count = batchRepository.countByTenantIdAndBatchNumberStartingWith(tenantId, prefix);
        return prefix + String.format("%03d", count + 1);
    }
}
