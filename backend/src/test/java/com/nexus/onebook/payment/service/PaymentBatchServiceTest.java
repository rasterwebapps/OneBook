package com.nexus.onebook.payment.service;

import com.nexus.onebook.cache.WarmCacheService;
import com.nexus.onebook.accounts.dto.JournalTransactionRequest;
import com.nexus.onebook.accounts.model.JournalTransaction;
import com.nexus.onebook.payment.dto.CreateBatchRequest;
import com.nexus.onebook.payment.model.*;
import com.nexus.onebook.payment.repository.PaymentBatchItemRepository;
import com.nexus.onebook.payment.repository.PaymentBatchRepository;
import com.nexus.onebook.payment.repository.PaymentRegisterRepository;
import com.nexus.onebook.security.AuditLogService;
import com.nexus.onebook.accounts.service.JournalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentBatchServiceTest {

    @Mock private PaymentRegisterRepository registerRepository;
    @Mock private PaymentBatchRepository batchRepository;
    @Mock private PaymentBatchItemRepository batchItemRepository;
    @Mock private JournalService journalService;
    @Mock private WarmCacheService warmCacheService;
    @Mock private AuditLogService auditLogService;
    @Mock private PaymentFileGeneratorService fileGeneratorService;

    @InjectMocks
    private PaymentBatchService batchService;

    private static final String TENANT = "tenant-1";

    private PaymentRegisterEntry buildEntry(Long id, String type, BigDecimal amount) {
        PaymentRegisterEntry entry = new PaymentRegisterEntry();
        entry.setId(id);
        entry.setTenantId(TENANT);
        entry.setVendorAccountId(10L);
        entry.setVendorName("Vendor A");
        entry.setTransactionType(type);
        entry.setAmount(amount);
        entry.setStatus(PaymentRegisterStatus.AVAILABLE_FOR_PROCESSING);
        entry.setDueDate(LocalDate.now().plusDays(30));
        return entry;
    }

    private PaymentBatch buildSavedBatch(Long id) {
        PaymentBatch batch = new PaymentBatch();
        batch.setId(id);
        batch.setTenantId(TENANT);
        batch.setBatchNumber("PB-2026-01-001");
        batch.setVendorAccountId(10L);
        batch.setVendorName("Vendor A");
        batch.setTotalPurchases(new BigDecimal("1000.00"));
        batch.setTotalReturns(new BigDecimal("100.00"));
        batch.setTotalCreditNotes(new BigDecimal("50.00"));
        batch.setNetPayable(new BigDecimal("850.00"));
        batch.setBankAccountId(20L);
        batch.setStatus(PaymentBatchStatus.PENDING_APPROVAL);
        batch.setCreatedBy("user1");
        return batch;
    }

    @Test
    void createBatch_validEntries_returnsBatchWithNetPayable() {
        var entry1 = buildEntry(1L, "PURCHASE", new BigDecimal("1000.00"));
        var entry2 = buildEntry(2L, "PURCHASE_RETURN", new BigDecimal("100.00"));
        var entry3 = buildEntry(3L, "CREDIT_NOTE", new BigDecimal("50.00"));

        CreateBatchRequest request = new CreateBatchRequest(10L, List.of(1L, 2L, 3L), 20L, "NEFT");

        when(registerRepository.findByIdInAndTenantId(List.of(1L, 2L, 3L), TENANT))
            .thenReturn(List.of(entry1, entry2, entry3));
        when(batchRepository.countByTenantIdAndBatchNumberStartingWith(eq(TENANT), anyString())).thenReturn(0L);

        PaymentBatch savedBatch = buildSavedBatch(1L);
        when(batchRepository.save(any())).thenReturn(savedBatch);
        when(batchItemRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(registerRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = batchService.createBatch(TENANT, request, "user1");

        assertNotNull(response);
        assertEquals(new BigDecimal("850.00"), response.netPayable());
        verify(batchRepository).save(any());
        verify(batchItemRepository).saveAll(any());
    }

    @Test
    void createBatch_entriesFromDifferentVendors_throwsException() {
        var entry1 = buildEntry(1L, "PURCHASE", new BigDecimal("500.00"));
        var entry2 = buildEntry(2L, "PURCHASE", new BigDecimal("300.00"));
        entry2.setVendorAccountId(99L);

        CreateBatchRequest request = new CreateBatchRequest(10L, List.of(1L, 2L), 20L, "NEFT");

        when(registerRepository.findByIdInAndTenantId(List.of(1L, 2L), TENANT))
            .thenReturn(List.of(entry1, entry2));

        assertThrows(IllegalArgumentException.class,
            () -> batchService.createBatch(TENANT, request, "user1"));
    }

    @Test
    void createBatch_entryNotAvailable_throwsException() {
        var entry1 = buildEntry(1L, "PURCHASE", new BigDecimal("500.00"));
        entry1.setStatus(PaymentRegisterStatus.IN_BATCH);

        CreateBatchRequest request = new CreateBatchRequest(10L, List.of(1L), 20L, "NEFT");

        when(registerRepository.findByIdInAndTenantId(List.of(1L), TENANT))
            .thenReturn(List.of(entry1));

        assertThrows(IllegalStateException.class,
            () -> batchService.createBatch(TENANT, request, "user1"));
    }

    @Test
    void approveBatch_postsPaymentJournal() {
        PaymentBatch batch = buildSavedBatch(1L);
        batch.setStatus(PaymentBatchStatus.PENDING_APPROVAL);

        when(batchRepository.findById(1L)).thenReturn(Optional.of(batch));
        when(batchRepository.save(any())).thenReturn(batch);

        var entry = buildEntry(1L, "PURCHASE", new BigDecimal("850.00"));
        var batchItem = new PaymentBatchItem();
        batchItem.setId(1L);
        batchItem.setTenantId(TENANT);
        batchItem.setBatch(batch);
        batchItem.setRegisterEntry(entry);
        batchItem.setTransactionType("PURCHASE");
        batchItem.setAmount(new BigDecimal("850.00"));

        when(batchItemRepository.findByBatchId(1L)).thenReturn(List.of(batchItem));
        when(registerRepository.findByIdInAndTenantId(any(), eq(TENANT))).thenReturn(List.of(entry));
        when(registerRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        JournalTransaction mockTx = new JournalTransaction();
        mockTx.setId(100L);
        when(journalService.createTransaction(any(JournalTransactionRequest.class))).thenReturn(mockTx);

        var response = batchService.approveBatch(TENANT, 1L, "approver1");

        assertNotNull(response);
        verify(journalService).createTransaction(any(JournalTransactionRequest.class));
        verify(warmCacheService).evictTrialBalance(TENANT);
    }

    @Test
    void rejectBatch_releasesEntriesToAvailable() {
        PaymentBatch batch = buildSavedBatch(1L);
        batch.setStatus(PaymentBatchStatus.PENDING_APPROVAL);

        when(batchRepository.findById(1L)).thenReturn(Optional.of(batch));
        when(batchRepository.save(any())).thenReturn(batch);

        var entry = buildEntry(1L, "PURCHASE", new BigDecimal("850.00"));
        entry.setStatus(PaymentRegisterStatus.IN_BATCH);

        var batchItem = new PaymentBatchItem();
        batchItem.setId(1L);
        batchItem.setTenantId(TENANT);
        batchItem.setBatch(batch);
        batchItem.setRegisterEntry(entry);
        batchItem.setTransactionType("PURCHASE");
        batchItem.setAmount(new BigDecimal("850.00"));

        when(batchItemRepository.findByBatchId(1L)).thenReturn(List.of(batchItem));
        when(registerRepository.findByIdInAndTenantId(any(), eq(TENANT))).thenReturn(List.of(entry));
        when(registerRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        batchService.rejectBatch(TENANT, 1L, "approver1", "Duplicate batch");

        assertEquals(PaymentRegisterStatus.AVAILABLE_FOR_PROCESSING, entry.getStatus());
        assertNull(entry.getBatchId());
        verify(registerRepository).saveAll(any());
    }
}
