package com.nexus.onebook.ingestion.connector;

import com.nexus.onebook.ingestion.dto.CardTransactionRequest;
import com.nexus.onebook.ingestion.model.CardTransaction;
import com.nexus.onebook.ingestion.repository.CardTransactionRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CorporateCardServiceTest {

    @Mock
    private CardTransactionRepository cardTransactionRepository;

    @InjectMocks
    private CorporateCardService corporateCardService;

    @Test
    void syncTransaction_validRequest_createsTransaction() {
        when(cardTransactionRepository.findByTenantIdAndExternalId("tenant-1", "EXT-001"))
                .thenReturn(Optional.empty());
        when(cardTransactionRepository.save(any(CardTransaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CardTransactionRequest request = new CardTransactionRequest(
                "tenant-1", "EXT-001", "1234", "Coffee Shop",
                new BigDecimal("15.50"), "USD",
                LocalDate.of(2026, 3, 10), "Food & Drink",
                "Morning coffee", null
        );

        CardTransaction result = corporateCardService.syncTransaction(request);

        assertNotNull(result);
        assertEquals("EXT-001", result.getExternalId());
        assertEquals("Coffee Shop", result.getMerchantName());
        assertEquals(new BigDecimal("15.50"), result.getAmount());
        assertFalse(result.isPosted());
    }

    @Test
    void syncTransaction_duplicate_throws() {
        when(cardTransactionRepository.findByTenantIdAndExternalId("tenant-1", "EXT-001"))
                .thenReturn(Optional.of(new CardTransaction()));

        CardTransactionRequest request = new CardTransactionRequest(
                "tenant-1", "EXT-001", "1234", "Coffee Shop",
                new BigDecimal("15.50"), "USD",
                LocalDate.of(2026, 3, 10), null, null, null
        );

        assertThrows(IllegalArgumentException.class,
                () -> corporateCardService.syncTransaction(request));
    }

    @Test
    void getUnpostedTransactions_returnsUnposted() {
        CardTransaction unposted = new CardTransaction(
                "tenant-1", "EXT-002", "Office Supplies",
                new BigDecimal("99.99"), LocalDate.of(2026, 3, 10));

        when(cardTransactionRepository.findByTenantIdAndPostedFalse("tenant-1"))
                .thenReturn(List.of(unposted));

        List<CardTransaction> result = corporateCardService.getUnpostedTransactions("tenant-1");

        assertEquals(1, result.size());
        assertEquals("EXT-002", result.get(0).getExternalId());
    }
}
