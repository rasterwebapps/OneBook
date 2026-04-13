package com.nexus.onebook.ledger.ingestion.gateway;

import com.nexus.onebook.ledger.accounts.dto.JournalTransactionRequest;
import com.nexus.onebook.ledger.ingestion.mapper.UniversalMapper;
import com.nexus.onebook.ledger.ingestion.model.AdapterType;
import com.nexus.onebook.ledger.payment.model.PaymentRegisterStatus;
import com.nexus.onebook.ledger.payment.model.PaymentRegisterEntry;
import com.nexus.onebook.ledger.payment.repository.PaymentRegisterRepository;
import com.nexus.onebook.ledger.accounts.model.JournalTransaction;
import com.nexus.onebook.ledger.accounts.service.JournalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialEventGatewayTest {

    @Mock
    private AdapterRegistry adapterRegistry;

    @Mock
    private PaymentRegisterRepository registerRepository;

    @Mock
    private UniversalMapper universalMapper;

    @Mock
    private JournalService journalService;

    @InjectMocks
    private FinancialEventGateway gateway;

    private PaymentRegisterEntry validEvent;
    private JournalTransaction postedTransaction;

    @BeforeEach
    void setUp() {
        validEvent = new PaymentRegisterEntry("tenant-1", AdapterType.HL7, "CHARGE");
        validEvent.setAmount(new BigDecimal("1500.00"));
        validEvent.setCurrency("USD");
        validEvent.setEventDate(LocalDate.of(2026, 3, 10));
        validEvent.setDebitAccountCode("4100");
        validEvent.setCreditAccountCode("2100");

        postedTransaction = new JournalTransaction("tenant-1", LocalDate.of(2026, 3, 10), "Test");
        postedTransaction.setTransactionUuid(UUID.randomUUID());
    }

    @Test
    void ingest_validPayload_postsToLedger() {
        FinancialEventAdapter mockAdapter = mock(FinancialEventAdapter.class);
        when(adapterRegistry.getAdapter(AdapterType.HL7)).thenReturn(mockAdapter);
        when(mockAdapter.parse("tenant-1", "payload")).thenReturn(validEvent);
        when(registerRepository.save(any(PaymentRegisterEntry.class))).thenAnswer(inv -> inv.getArgument(0));
        when(universalMapper.mapToJournalRequest(any())).thenReturn(
                mock(JournalTransactionRequest.class));
        when(journalService.createTransaction(any())).thenReturn(postedTransaction);

        PaymentRegisterEntry result = gateway.ingest("tenant-1", AdapterType.HL7, "payload");

        assertEquals(PaymentRegisterStatus.POSTED, result.getStatus());
        verify(journalService).createTransaction(any());
    }

    @Test
    void ingest_parseError_returnsFailed() {
        FinancialEventAdapter mockAdapter = mock(FinancialEventAdapter.class);
        when(adapterRegistry.getAdapter(AdapterType.HL7)).thenReturn(mockAdapter);
        when(mockAdapter.parse(anyString(), anyString())).thenThrow(
                new IllegalArgumentException("Bad payload"));
        when(registerRepository.save(any(PaymentRegisterEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentRegisterEntry result = gateway.ingest("tenant-1", AdapterType.HL7, "bad");

        assertEquals(PaymentRegisterStatus.FAILED, result.getStatus());
        assertTrue(result.getErrorMessage().contains("Bad payload"));
        verify(journalService, never()).createTransaction(any());
    }

    @Test
    void ingest_mappingError_returnsFailed() {
        FinancialEventAdapter mockAdapter = mock(FinancialEventAdapter.class);
        when(adapterRegistry.getAdapter(AdapterType.HL7)).thenReturn(mockAdapter);
        when(mockAdapter.parse("tenant-1", "payload")).thenReturn(validEvent);
        when(registerRepository.save(any(PaymentRegisterEntry.class))).thenAnswer(inv -> inv.getArgument(0));
        when(universalMapper.mapToJournalRequest(any())).thenThrow(
                new IllegalArgumentException("Account not found"));

        PaymentRegisterEntry result = gateway.ingest("tenant-1", AdapterType.HL7, "payload");

        assertEquals(PaymentRegisterStatus.FAILED, result.getStatus());
        assertTrue(result.getErrorMessage().contains("Account not found"));
    }

    @Test
    void ingestValidateOnly_validPayload_returnsValidated() {
        FinancialEventAdapter mockAdapter = mock(FinancialEventAdapter.class);
        when(adapterRegistry.getAdapter(AdapterType.DMS)).thenReturn(mockAdapter);
        when(mockAdapter.parse("tenant-1", "payload")).thenReturn(validEvent);
        when(registerRepository.save(any(PaymentRegisterEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentRegisterEntry result = gateway.ingestValidateOnly("tenant-1", AdapterType.DMS, "payload");

        assertEquals(PaymentRegisterStatus.VALIDATED, result.getStatus());
        verify(journalService, never()).createTransaction(any());
    }

    @Test
    void ingest_externalApp_skipsJournalPosting() {
        PaymentRegisterEntry externalAppEvent = new PaymentRegisterEntry("tenant-1", AdapterType.EXTERNAL_APP, "PURCHASE_PAYMENT");
        externalAppEvent.setAmount(new BigDecimal("14250.00"));

        FinancialEventAdapter mockAdapter = mock(FinancialEventAdapter.class);
        when(adapterRegistry.getAdapter(AdapterType.EXTERNAL_APP)).thenReturn(mockAdapter);
        when(mockAdapter.parse("tenant-1", "payload")).thenReturn(externalAppEvent);
        when(registerRepository.save(any(PaymentRegisterEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentRegisterEntry result = gateway.ingest("tenant-1", AdapterType.EXTERNAL_APP, "payload");

        assertEquals(PaymentRegisterStatus.VALIDATED, result.getStatus());
        verify(journalService, never()).createTransaction(any());
        verify(universalMapper, never()).mapToJournalRequest(any());
    }
}
