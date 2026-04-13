package com.nexus.onebook.banking.service;

import com.nexus.onebook.banking.dto.ChequeEntryRequest;
import com.nexus.onebook.banking.model.ChequeEntry;
import com.nexus.onebook.banking.model.ChequeStatus;
import com.nexus.onebook.banking.model.ChequeType;
import com.nexus.onebook.accounts.model.LedgerAccount;
import com.nexus.onebook.banking.repository.ChequeEntryRepository;
import com.nexus.onebook.accounts.repository.LedgerAccountRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChequeManagementServiceTest {

    @Mock
    private ChequeEntryRepository chequeEntryRepository;
    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    @InjectMocks
    private ChequeManagementService chequeManagementService;

    private LedgerAccount bankAccount;

    @BeforeEach
    void setUp() {
        bankAccount = new LedgerAccount();
        bankAccount.setId(1L);
    }

    @Test
    void issueCheque_validRequest_succeeds() {
        ChequeEntryRequest request = new ChequeEntryRequest(
                "tenant-1", "CHQ-001", 1L, "Party A",
                new BigDecimal("25000"), LocalDate.of(2024, 4, 1), "PAYMENT");

        when(ledgerAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccount));
        when(chequeEntryRepository.findByTenantIdAndChequeNumberAndBankAccountId("tenant-1", "CHQ-001", 1L))
                .thenReturn(Optional.empty());
        when(chequeEntryRepository.save(any(ChequeEntry.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ChequeEntry result = chequeManagementService.issueCheque(request);

        assertNotNull(result);
        verify(chequeEntryRepository).save(any(ChequeEntry.class));
    }

    @Test
    void issueCheque_bankAccountNotFound_throws() {
        ChequeEntryRequest request = new ChequeEntryRequest(
                "tenant-1", "CHQ-001", 99L, "Party A",
                new BigDecimal("25000"), LocalDate.of(2024, 4, 1), "PAYMENT");

        when(ledgerAccountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                chequeManagementService.issueCheque(request));
    }

    @Test
    void issueCheque_duplicateChequeNumber_throws() {
        ChequeEntryRequest request = new ChequeEntryRequest(
                "tenant-1", "CHQ-001", 1L, "Party A",
                new BigDecimal("25000"), LocalDate.of(2024, 4, 1), "PAYMENT");

        when(ledgerAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccount));
        when(chequeEntryRepository.findByTenantIdAndChequeNumberAndBankAccountId("tenant-1", "CHQ-001", 1L))
                .thenReturn(Optional.of(new ChequeEntry()));

        assertThrows(IllegalArgumentException.class, () ->
                chequeManagementService.issueCheque(request));
    }

    @Test
    void clearCheque_validCheque_setsStatusAndDate() {
        ChequeEntry cheque = new ChequeEntry(
                "tenant-1", "CHQ-001", bankAccount, "Party A",
                new BigDecimal("25000"), LocalDate.of(2024, 4, 1), ChequeType.PAYMENT);
        LocalDate clearingDate = LocalDate.of(2024, 4, 5);

        when(chequeEntryRepository.findById(1L)).thenReturn(Optional.of(cheque));
        when(chequeEntryRepository.save(any(ChequeEntry.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ChequeEntry result = chequeManagementService.clearCheque(1L, clearingDate);

        assertEquals(ChequeStatus.CLEARED, result.getStatus());
        assertEquals(clearingDate, result.getClearingDate());
    }

    @Test
    void bounceCheque_validCheque_setsStatusAndReason() {
        ChequeEntry cheque = new ChequeEntry(
                "tenant-1", "CHQ-002", bankAccount, "Party B",
                new BigDecimal("15000"), LocalDate.of(2024, 4, 1), ChequeType.PAYMENT);

        when(chequeEntryRepository.findById(2L)).thenReturn(Optional.of(cheque));
        when(chequeEntryRepository.save(any(ChequeEntry.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ChequeEntry result = chequeManagementService.bounceCheque(2L, "Insufficient funds");

        assertEquals(ChequeStatus.BOUNCED, result.getStatus());
        assertEquals("Insufficient funds", result.getBounceReason());
    }

    @Test
    void cancelCheque_validCheque_setsCancelledStatus() {
        ChequeEntry cheque = new ChequeEntry(
                "tenant-1", "CHQ-003", bankAccount, "Party C",
                new BigDecimal("10000"), LocalDate.of(2024, 4, 1), ChequeType.RECEIPT);

        when(chequeEntryRepository.findById(3L)).thenReturn(Optional.of(cheque));
        when(chequeEntryRepository.save(any(ChequeEntry.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ChequeEntry result = chequeManagementService.cancelCheque(3L);

        assertEquals(ChequeStatus.CANCELLED, result.getStatus());
    }
}
