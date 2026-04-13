package com.nexus.onebook.ledger.ingestion.mapper;

import com.nexus.onebook.ledger.accounts.dto.JournalTransactionRequest;
import com.nexus.onebook.ledger.ingestion.model.AdapterType;
import com.nexus.onebook.ledger.payment.model.PaymentRegisterEntry;
import com.nexus.onebook.ledger.accounts.model.LedgerAccount;
import com.nexus.onebook.ledger.accounts.repository.LedgerAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UniversalMapperTest {

    @Mock
    private LedgerAccountRepository accountRepository;

    @InjectMocks
    private UniversalMapper mapper;

    private LedgerAccount debitAccount;
    private LedgerAccount creditAccount;

    @BeforeEach
    void setUp() {
        debitAccount = new LedgerAccount();
        debitAccount.setId(1L);
        debitAccount.setAccountCode("4100");

        creditAccount = new LedgerAccount();
        creditAccount.setId(2L);
        creditAccount.setAccountCode("2100");
    }

    @Test
    void mapToJournalRequest_validEvent_createsBalancedTransaction() {
        when(accountRepository.findByTenantIdAndAccountCode("tenant-1", "4100"))
                .thenReturn(Optional.of(debitAccount));
        when(accountRepository.findByTenantIdAndAccountCode("tenant-1", "2100"))
                .thenReturn(Optional.of(creditAccount));

        PaymentRegisterEntry event = new PaymentRegisterEntry("tenant-1", AdapterType.HL7, "CHARGE");
        event.setAmount(new BigDecimal("1500.00"));
        event.setEventDate(LocalDate.of(2026, 3, 10));
        event.setDescription("Lab Test Fee");
        event.setDebitAccountCode("4100");
        event.setCreditAccountCode("2100");
        event.setIndustryTags("{\"patientId\":\"P-12345\"}");

        JournalTransactionRequest result = mapper.mapToJournalRequest(event);

        assertNotNull(result);
        assertEquals("tenant-1", result.tenantId());
        assertEquals(LocalDate.of(2026, 3, 10), result.transactionDate());
        assertEquals("Lab Test Fee", result.description());
        assertEquals(2, result.entries().size());
        assertEquals(1L, result.entries().get(0).accountId());
        assertEquals("DEBIT", result.entries().get(0).entryType());
        assertEquals(2L, result.entries().get(1).accountId());
        assertEquals("CREDIT", result.entries().get(1).entryType());
    }

    @Test
    void mapToJournalRequest_missingAmount_throws() {
        PaymentRegisterEntry event = new PaymentRegisterEntry("tenant-1", AdapterType.HL7, "CHARGE");
        event.setDebitAccountCode("4100");
        event.setCreditAccountCode("2100");

        assertThrows(IllegalArgumentException.class,
                () -> mapper.mapToJournalRequest(event));
    }

    @Test
    void mapToJournalRequest_missingDebitCode_throws() {
        PaymentRegisterEntry event = new PaymentRegisterEntry("tenant-1", AdapterType.HL7, "CHARGE");
        event.setAmount(new BigDecimal("100"));
        event.setCreditAccountCode("2100");

        assertThrows(IllegalArgumentException.class,
                () -> mapper.mapToJournalRequest(event));
    }

    @Test
    void mapToJournalRequest_accountNotFound_throws() {
        when(accountRepository.findByTenantIdAndAccountCode("tenant-1", "9999"))
                .thenReturn(Optional.empty());

        PaymentRegisterEntry event = new PaymentRegisterEntry("tenant-1", AdapterType.HL7, "CHARGE");
        event.setAmount(new BigDecimal("100"));
        event.setEventDate(LocalDate.now());
        event.setDebitAccountCode("9999");
        event.setCreditAccountCode("2100");

        assertThrows(IllegalArgumentException.class,
                () -> mapper.mapToJournalRequest(event));
    }

    @Test
    void mapToJournalRequest_noDescription_buildsFallback() {
        when(accountRepository.findByTenantIdAndAccountCode("tenant-1", "4100"))
                .thenReturn(Optional.of(debitAccount));
        when(accountRepository.findByTenantIdAndAccountCode("tenant-1", "2100"))
                .thenReturn(Optional.of(creditAccount));

        PaymentRegisterEntry event = new PaymentRegisterEntry("tenant-1", AdapterType.DMS, "VEHICLE_SALE");
        event.setAmount(new BigDecimal("35000"));
        event.setEventDate(LocalDate.now());
        event.setDebitAccountCode("4100");
        event.setCreditAccountCode("2100");
        event.setSourceReference("VIN-123");

        JournalTransactionRequest result = mapper.mapToJournalRequest(event);

        assertTrue(result.description().contains("DMS"));
        assertTrue(result.description().contains("VEHICLE_SALE"));
    }
}
