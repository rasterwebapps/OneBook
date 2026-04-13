package com.nexus.onebook.ledger.clientaccount.service;

import com.nexus.onebook.ledger.clientaccount.dto.ClientAccountRequest;
import com.nexus.onebook.ledger.clientaccount.dto.ClientAccountResponse;
import com.nexus.onebook.ledger.clientaccount.model.ClientAccount;
import com.nexus.onebook.ledger.clientaccount.model.ClientAccountType;
import com.nexus.onebook.ledger.accounts.model.LedgerAccount;
import com.nexus.onebook.ledger.clientaccount.repository.ClientAccountRepository;
import com.nexus.onebook.ledger.accounts.repository.LedgerAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientAccountServiceTest {

    @Mock
    private ClientAccountRepository clientAccountRepository;

    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    @InjectMocks
    private ClientAccountService clientAccountService;

    private LedgerAccount createLedgerAccount() {
        LedgerAccount la = new LedgerAccount();
        la.setId(1L);
        la.setAccountName("Test Account");
        return la;
    }

    private ClientAccount createClientAccount(LedgerAccount la) {
        ClientAccount ca = new ClientAccount("tenant-1", la, ClientAccountType.CUSTOMER, "Acme Corp");
        ca.setId(10L);
        ca.setEmail("acme@example.com");
        ca.setPhone("1234567890");
        return ca;
    }

    private ClientAccountRequest createRequest() {
        return new ClientAccountRequest(
                "tenant-1", 1L, "CUSTOMER", "Acme Corp",
                "John Doe", "acme@example.com", "1234567890",
                "123 Main St", "456 Ship St",
                "22AAAAA0000A1Z5", "AAAAA0000A",
                new BigDecimal("100000"), 30);
    }

    @Test
    void create_validRequest_createsClientAccount() {
        LedgerAccount la = createLedgerAccount();
        ClientAccountRequest request = createRequest();

        when(ledgerAccountRepository.findById(1L)).thenReturn(Optional.of(la));
        when(clientAccountRepository.save(any(ClientAccount.class)))
                .thenAnswer(inv -> {
                    ClientAccount saved = inv.getArgument(0);
                    saved.setId(10L);
                    return saved;
                });

        ClientAccountResponse result = clientAccountService.create(request);

        assertNotNull(result);
        assertEquals("Acme Corp", result.clientName());
        assertEquals("CUSTOMER", result.clientType());
        assertEquals("acme@example.com", result.email());
        verify(clientAccountRepository).save(any(ClientAccount.class));
    }

    @Test
    void create_ledgerAccountNotFound_throwsException() {
        ClientAccountRequest request = createRequest();
        when(ledgerAccountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                clientAccountService.create(request));
    }

    @Test
    void create_invalidClientType_throwsException() {
        LedgerAccount la = createLedgerAccount();
        ClientAccountRequest request = new ClientAccountRequest(
                "tenant-1", 1L, "INVALID_TYPE", "Corp",
                null, null, null, null, null, null, null, null, null);

        when(ledgerAccountRepository.findById(1L)).thenReturn(Optional.of(la));

        assertThrows(IllegalArgumentException.class, () ->
                clientAccountService.create(request));
    }

    @Test
    void list_returnsTenantAccounts() {
        LedgerAccount la = createLedgerAccount();
        ClientAccount ca = createClientAccount(la);

        when(clientAccountRepository.findByTenantId("tenant-1")).thenReturn(List.of(ca));

        List<ClientAccountResponse> result = clientAccountService.list("tenant-1");

        assertEquals(1, result.size());
        assertEquals("Acme Corp", result.get(0).clientName());
    }

    @Test
    void listByType_filtersCorrectly() {
        LedgerAccount la = createLedgerAccount();
        ClientAccount ca = createClientAccount(la);

        when(clientAccountRepository.findByTenantIdAndClientType("tenant-1", ClientAccountType.CUSTOMER))
                .thenReturn(List.of(ca));

        List<ClientAccountResponse> result = clientAccountService.listByType("tenant-1", "CUSTOMER");

        assertEquals(1, result.size());
        assertEquals("CUSTOMER", result.get(0).clientType());
    }

    @Test
    void getById_exists_returnsAccount() {
        LedgerAccount la = createLedgerAccount();
        ClientAccount ca = createClientAccount(la);

        when(clientAccountRepository.findByTenantIdAndId("tenant-1", 10L))
                .thenReturn(Optional.of(ca));

        ClientAccountResponse result = clientAccountService.getById("tenant-1", 10L);

        assertNotNull(result);
        assertEquals(10L, result.id());
    }

    @Test
    void getById_notFound_throwsException() {
        when(clientAccountRepository.findByTenantIdAndId("tenant-1", 99L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                clientAccountService.getById("tenant-1", 99L));
    }

    @Test
    void update_existingAccount_updatesFields() {
        LedgerAccount la = createLedgerAccount();
        ClientAccount ca = createClientAccount(la);
        ClientAccountRequest request = new ClientAccountRequest(
                "tenant-1", 1L, "VENDOR", "Updated Corp",
                "Jane", "new@example.com", "9999999999",
                "New Addr", null, null, null,
                new BigDecimal("200000"), 60);

        when(clientAccountRepository.findByTenantIdAndId("tenant-1", 10L))
                .thenReturn(Optional.of(ca));
        when(clientAccountRepository.save(any(ClientAccount.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ClientAccountResponse result = clientAccountService.update("tenant-1", 10L, request);

        assertEquals("Updated Corp", result.clientName());
        assertEquals("VENDOR", result.clientType());
    }

    @Test
    void update_notFound_throwsException() {
        ClientAccountRequest request = createRequest();
        when(clientAccountRepository.findByTenantIdAndId("tenant-1", 99L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                clientAccountService.update("tenant-1", 99L, request));
    }

    @Test
    void deactivate_existingAccount_setsInactive() {
        LedgerAccount la = createLedgerAccount();
        ClientAccount ca = createClientAccount(la);

        when(clientAccountRepository.findByTenantIdAndId("tenant-1", 10L))
                .thenReturn(Optional.of(ca));
        when(clientAccountRepository.save(any(ClientAccount.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        clientAccountService.deactivate("tenant-1", 10L);

        assertFalse(ca.isActive());
        verify(clientAccountRepository).save(ca);
    }

    @Test
    void deactivate_notFound_throwsException() {
        when(clientAccountRepository.findByTenantIdAndId("tenant-1", 99L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                clientAccountService.deactivate("tenant-1", 99L));
    }
}
