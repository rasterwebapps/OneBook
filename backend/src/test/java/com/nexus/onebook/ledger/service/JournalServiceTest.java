package com.nexus.onebook.ledger.service;

import com.nexus.onebook.ledger.cache.WarmCacheService;
import com.nexus.onebook.ledger.dto.JournalEntryRequest;
import com.nexus.onebook.ledger.dto.JournalTransactionRequest;
import com.nexus.onebook.ledger.exception.UnbalancedTransactionException;
import com.nexus.onebook.ledger.model.*;
import com.nexus.onebook.ledger.repository.JournalTransactionRepository;
import com.nexus.onebook.ledger.repository.LedgerAccountRepository;
import com.nexus.onebook.ledger.security.AuditLogService;
import com.nexus.onebook.ledger.security.BlindIndexService;
import com.nexus.onebook.ledger.security.FieldEncryptionService;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JournalServiceTest {

    @Mock
    private JournalTransactionRepository transactionRepository;

    @Mock
    private LedgerAccountRepository accountRepository;

    @Mock
    private FieldEncryptionService encryptionService;

    @Mock
    private BlindIndexService blindIndexService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private WarmCacheService warmCacheService;

    @InjectMocks
    private JournalService journalService;

    private LedgerAccount debitAccount;
    private LedgerAccount creditAccount;

    @BeforeEach
    void setUp() {
        debitAccount = new LedgerAccount();
        debitAccount.setId(1L);
        debitAccount.setAccountCode("1000");
        debitAccount.setAccountName("Cash");
        debitAccount.setAccountType(AccountType.ASSET);
        debitAccount.setTenantId("tenant-1");

        creditAccount = new LedgerAccount();
        creditAccount.setId(2L);
        creditAccount.setAccountCode("3000");
        creditAccount.setAccountName("Revenue");
        creditAccount.setAccountType(AccountType.REVENUE);
        creditAccount.setTenantId("tenant-1");

        // Encryption stubs — passthrough for unit tests
        lenient().when(encryptionService.encrypt(anyString())).thenAnswer(inv -> "ENC:" + inv.getArgument(0));
        lenient().when(blindIndexService.generateBlindIndex(anyString())).thenReturn("blind-index-hash");
    }

    // --- validateBalance tests ---

    @Test
    void validateBalance_balanced_succeeds() {
        List<JournalEntryRequest> entries = List.of(
                new JournalEntryRequest(1L, "DEBIT", new BigDecimal("100.0000"), "Debit line", null),
                new JournalEntryRequest(2L, "CREDIT", new BigDecimal("100.0000"), "Credit line", null)
        );

        assertDoesNotThrow(() -> journalService.validateBalance(entries));
    }

    @Test
    void validateBalance_multipleEntriesBalanced_succeeds() {
        List<JournalEntryRequest> entries = List.of(
                new JournalEntryRequest(1L, "DEBIT", new BigDecimal("50.0000"), "Debit 1", null),
                new JournalEntryRequest(1L, "DEBIT", new BigDecimal("50.0000"), "Debit 2", null),
                new JournalEntryRequest(2L, "CREDIT", new BigDecimal("100.0000"), "Credit line", null)
        );

        assertDoesNotThrow(() -> journalService.validateBalance(entries));
    }

    @Test
    void validateBalance_unbalanced_throws() {
        List<JournalEntryRequest> entries = List.of(
                new JournalEntryRequest(1L, "DEBIT", new BigDecimal("100.0000"), "Debit line", null),
                new JournalEntryRequest(2L, "CREDIT", new BigDecimal("75.0000"), "Credit line", null)
        );

        UnbalancedTransactionException ex = assertThrows(
                UnbalancedTransactionException.class,
                () -> journalService.validateBalance(entries)
        );
        assertTrue(ex.getMessage().contains("unbalanced"));
    }

    @Test
    void validateBalance_nullEntries_throws() {
        assertThrows(
                UnbalancedTransactionException.class,
                () -> journalService.validateBalance(null)
        );
    }

    @Test
    void validateBalance_singleEntry_throws() {
        List<JournalEntryRequest> entries = List.of(
                new JournalEntryRequest(1L, "DEBIT", new BigDecimal("100.0000"), "Only debit", null)
        );

        assertThrows(
                UnbalancedTransactionException.class,
                () -> journalService.validateBalance(entries)
        );
    }

    @Test
    void validateBalance_onlyDebits_throws() {
        List<JournalEntryRequest> entries = List.of(
                new JournalEntryRequest(1L, "DEBIT", new BigDecimal("50.0000"), "Debit 1", null),
                new JournalEntryRequest(1L, "DEBIT", new BigDecimal("50.0000"), "Debit 2", null)
        );

        UnbalancedTransactionException ex = assertThrows(
                UnbalancedTransactionException.class,
                () -> journalService.validateBalance(entries)
        );
        assertTrue(ex.getMessage().contains("at least one debit and one credit"));
    }

    @Test
    void validateBalance_onlyCredits_throws() {
        List<JournalEntryRequest> entries = List.of(
                new JournalEntryRequest(1L, "CREDIT", new BigDecimal("50.0000"), "Credit 1", null),
                new JournalEntryRequest(2L, "CREDIT", new BigDecimal("50.0000"), "Credit 2", null)
        );

        UnbalancedTransactionException ex = assertThrows(
                UnbalancedTransactionException.class,
                () -> journalService.validateBalance(entries)
        );
        assertTrue(ex.getMessage().contains("at least one debit and one credit"));
    }

    @Test
    void validateBalance_zeroAmount_throws() {
        List<JournalEntryRequest> entries = List.of(
                new JournalEntryRequest(1L, "DEBIT", BigDecimal.ZERO, "Zero debit", null),
                new JournalEntryRequest(2L, "CREDIT", BigDecimal.ZERO, "Zero credit", null)
        );

        assertThrows(
                UnbalancedTransactionException.class,
                () -> journalService.validateBalance(entries)
        );
    }

    @Test
    void validateBalance_negativeAmount_throws() {
        List<JournalEntryRequest> entries = List.of(
                new JournalEntryRequest(1L, "DEBIT", new BigDecimal("-100.0000"), "Negative", null),
                new JournalEntryRequest(2L, "CREDIT", new BigDecimal("100.0000"), "Positive", null)
        );

        assertThrows(
                UnbalancedTransactionException.class,
                () -> journalService.validateBalance(entries)
        );
    }

    // --- createTransaction tests ---

    @Test
    void createTransaction_balanced_succeeds() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(debitAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(creditAccount));
        when(transactionRepository.save(any(JournalTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        JournalTransactionRequest request = new JournalTransactionRequest(
                "tenant-1",
                LocalDate.of(2026, 3, 9),
                "Test transaction",
                null,
                List.of(
                        new JournalEntryRequest(1L, "DEBIT", new BigDecimal("250.0000"), "Debit", null),
                        new JournalEntryRequest(2L, "CREDIT", new BigDecimal("250.0000"), "Credit", null)
                )
        );

        JournalTransaction result = journalService.createTransaction(request);

        assertNotNull(result);
        assertNotNull(result.getTransactionUuid());
        assertEquals("tenant-1", result.getTenantId());
        assertEquals(2, result.getEntries().size());
        verify(transactionRepository).save(any(JournalTransaction.class));
    }

    @Test
    void createTransaction_autoPostsOnSave() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(debitAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(creditAccount));
        when(transactionRepository.save(any(JournalTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        JournalTransactionRequest request = new JournalTransactionRequest(
                "tenant-1",
                LocalDate.of(2026, 3, 9),
                "Auto-post test",
                null,
                List.of(
                        new JournalEntryRequest(1L, "DEBIT", new BigDecimal("500.0000"), "Debit", null),
                        new JournalEntryRequest(2L, "CREDIT", new BigDecimal("500.0000"), "Credit", null)
                )
        );

        JournalTransaction result = journalService.createTransaction(request);

        assertTrue(result.isPosted(), "Transaction should be auto-posted on save (Tally behavior)");
    }

    @Test
    void createTransaction_unbalanced_doesNotPersist() {
        JournalTransactionRequest request = new JournalTransactionRequest(
                "tenant-1",
                LocalDate.of(2026, 3, 9),
                "Unbalanced",
                null,
                List.of(
                        new JournalEntryRequest(1L, "DEBIT", new BigDecimal("100.0000"), "Debit", null),
                        new JournalEntryRequest(2L, "CREDIT", new BigDecimal("50.0000"), "Credit", null)
                )
        );

        assertThrows(
                UnbalancedTransactionException.class,
                () -> journalService.createTransaction(request)
        );
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_invalidAccount_throws() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        JournalTransactionRequest request = new JournalTransactionRequest(
                "tenant-1",
                LocalDate.of(2026, 3, 9),
                "Bad account",
                null,
                List.of(
                        new JournalEntryRequest(999L, "DEBIT", new BigDecimal("100.0000"), "Debit", null),
                        new JournalEntryRequest(2L, "CREDIT", new BigDecimal("100.0000"), "Credit", null)
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> journalService.createTransaction(request)
        );
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_withMetadata_setsMetadata() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(debitAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(creditAccount));
        when(transactionRepository.save(any(JournalTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String txMetadata = "{\"patientId\": \"P-12345\"}";
        String entryMetadata = "{\"vin\": \"1HGBH41JXMN109186\"}";

        JournalTransactionRequest request = new JournalTransactionRequest(
                "tenant-1",
                LocalDate.of(2026, 3, 9),
                "With metadata",
                txMetadata,
                List.of(
                        new JournalEntryRequest(1L, "DEBIT", new BigDecimal("500.0000"), "Debit", entryMetadata),
                        new JournalEntryRequest(2L, "CREDIT", new BigDecimal("500.0000"), "Credit", null)
                )
        );

        JournalTransaction result = journalService.createTransaction(request);

        assertEquals(txMetadata, result.getMetadata());
        assertEquals(entryMetadata, result.getEntries().get(0).getMetadata());
    }
}
