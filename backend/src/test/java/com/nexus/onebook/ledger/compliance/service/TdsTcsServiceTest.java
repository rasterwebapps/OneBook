package com.nexus.onebook.ledger.compliance.service;

import com.nexus.onebook.ledger.compliance.dto.TdsTcsEntryRequest;
import com.nexus.onebook.ledger.accounts.model.JournalTransaction;
import com.nexus.onebook.ledger.compliance.model.TdsTcsEntry;
import com.nexus.onebook.ledger.compliance.model.TdsTcsStatus;
import com.nexus.onebook.ledger.compliance.model.TdsTcsType;
import com.nexus.onebook.ledger.accounts.repository.JournalTransactionRepository;
import com.nexus.onebook.ledger.compliance.repository.TdsTcsEntryRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TdsTcsServiceTest {

    @Mock
    private TdsTcsEntryRepository tdsTcsEntryRepository;
    @Mock
    private JournalTransactionRepository journalTransactionRepository;

    @InjectMocks
    private TdsTcsService tdsTcsService;

    @Test
    void createEntry_validRequest_calculatesTaxAmount() {
        TdsTcsEntryRequest request = new TdsTcsEntryRequest(
                "tenant-1", "TDS", "194C", "Party A", "ABCDE1234F",
                LocalDate.of(2024, 4, 1), new BigDecimal("100000"), new BigDecimal("2"), 10L);

        JournalTransaction journalTransaction = new JournalTransaction();
        when(journalTransactionRepository.findById(10L)).thenReturn(Optional.of(journalTransaction));
        when(tdsTcsEntryRepository.save(any(TdsTcsEntry.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TdsTcsEntry result = tdsTcsService.createEntry(request);

        assertNotNull(result);
        // 100000 * 2 / 100 = 2000.0000
        assertEquals(new BigDecimal("2000.0000"), result.getTaxAmount());
        verify(tdsTcsEntryRepository).save(any(TdsTcsEntry.class));
    }

    @Test
    void createEntry_withoutJournalTransaction_succeeds() {
        TdsTcsEntryRequest request = new TdsTcsEntryRequest(
                "tenant-1", "TCS", "206C", "Party B", "XYZAB5678G",
                LocalDate.of(2024, 4, 1), new BigDecimal("50000"), new BigDecimal("1"), null);

        when(tdsTcsEntryRepository.save(any(TdsTcsEntry.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TdsTcsEntry result = tdsTcsService.createEntry(request);

        assertNotNull(result);
        verify(journalTransactionRepository, never()).findById(any());
        verify(tdsTcsEntryRepository).save(any(TdsTcsEntry.class));
    }

    @Test
    void getEntriesByType_tds_returnsFilteredList() {
        TdsTcsEntry entry = new TdsTcsEntry(
                "tenant-1", TdsTcsType.TDS, "194C", "Party A",
                LocalDate.of(2024, 4, 1), new BigDecimal("100000"),
                new BigDecimal("2"), new BigDecimal("2000"));

        when(tdsTcsEntryRepository.findByTenantIdAndEntryType("tenant-1", TdsTcsType.TDS))
                .thenReturn(List.of(entry));

        List<TdsTcsEntry> result = tdsTcsService.getEntriesByType("tenant-1", TdsTcsType.TDS);

        assertEquals(1, result.size());
        assertEquals(TdsTcsType.TDS, result.get(0).getEntryType());
    }

    @Test
    void getPendingEntries_returnsPendingOnly() {
        TdsTcsEntry entry = new TdsTcsEntry(
                "tenant-1", TdsTcsType.TDS, "194C", "Party A",
                LocalDate.of(2024, 4, 1), new BigDecimal("100000"),
                new BigDecimal("2"), new BigDecimal("2000"));
        entry.setStatus(TdsTcsStatus.PENDING);

        when(tdsTcsEntryRepository.findByTenantIdAndStatus("tenant-1", TdsTcsStatus.PENDING))
                .thenReturn(List.of(entry));

        List<TdsTcsEntry> result = tdsTcsService.getPendingEntries("tenant-1");

        assertEquals(1, result.size());
    }

    @Test
    void updateStatus_validEntry_updatesSuccessfully() {
        TdsTcsEntry entry = new TdsTcsEntry(
                "tenant-1", TdsTcsType.TDS, "194C", "Party A",
                LocalDate.of(2024, 4, 1), new BigDecimal("100000"),
                new BigDecimal("2"), new BigDecimal("2000"));
        entry.setStatus(TdsTcsStatus.PENDING);

        when(tdsTcsEntryRepository.findById(1L)).thenReturn(Optional.of(entry));
        when(tdsTcsEntryRepository.save(any(TdsTcsEntry.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TdsTcsEntry result = tdsTcsService.updateStatus(1L, TdsTcsStatus.DEPOSITED, "CERT-001");

        assertEquals(TdsTcsStatus.DEPOSITED, result.getStatus());
        assertEquals("CERT-001", result.getCertificateNumber());
    }
}
