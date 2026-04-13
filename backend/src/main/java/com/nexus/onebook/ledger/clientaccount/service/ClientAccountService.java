package com.nexus.onebook.ledger.clientaccount.service;

import com.nexus.onebook.ledger.clientaccount.dto.ClientAccountRequest;
import com.nexus.onebook.ledger.clientaccount.dto.ClientAccountResponse;
import com.nexus.onebook.ledger.clientaccount.model.ClientAccount;
import com.nexus.onebook.ledger.clientaccount.model.ClientAccountType;
import com.nexus.onebook.ledger.accounts.model.LedgerAccount;
import com.nexus.onebook.ledger.clientaccount.repository.ClientAccountRepository;
import com.nexus.onebook.ledger.accounts.repository.LedgerAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing client (customer/vendor/employee/intercompany) accounts.
 * Each client account is linked to a LedgerAccount in the Chart of Accounts.
 */
@Service
public class ClientAccountService {

    private final ClientAccountRepository clientAccountRepository;
    private final LedgerAccountRepository ledgerAccountRepository;

    public ClientAccountService(ClientAccountRepository clientAccountRepository,
                                LedgerAccountRepository ledgerAccountRepository) {
        this.clientAccountRepository = clientAccountRepository;
        this.ledgerAccountRepository = ledgerAccountRepository;
    }

    @Transactional
    public ClientAccountResponse create(ClientAccountRequest request) {
        LedgerAccount ledgerAccount = ledgerAccountRepository.findById(request.ledgerAccountId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ledger account not found: " + request.ledgerAccountId()));

        ClientAccountType clientType = parseClientType(request.clientType());

        ClientAccount entity = new ClientAccount(
                request.tenantId(), ledgerAccount, clientType, request.clientName());
        entity.setContactPerson(request.contactPerson());
        entity.setEmail(request.email());
        entity.setPhone(request.phone());
        entity.setBillingAddress(request.billingAddress());
        entity.setShippingAddress(request.shippingAddress());
        entity.setGstin(request.gstin());
        entity.setPan(request.pan());
        entity.setCreditLimit(request.creditLimit());
        entity.setPaymentTermsDays(request.paymentTermsDays());

        ClientAccount saved = clientAccountRepository.save(entity);
        return ClientAccountResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ClientAccountResponse> list(String tenantId) {
        return clientAccountRepository.findByTenantId(tenantId).stream()
                .map(ClientAccountResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClientAccountResponse> listByType(String tenantId, String type) {
        ClientAccountType clientType = parseClientType(type);
        return clientAccountRepository.findByTenantIdAndClientType(tenantId, clientType).stream()
                .map(ClientAccountResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClientAccountResponse getById(String tenantId, Long id) {
        ClientAccount entity = clientAccountRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Client account not found: " + id));
        return ClientAccountResponse.from(entity);
    }

    @Transactional
    public ClientAccountResponse update(String tenantId, Long id, ClientAccountRequest request) {
        ClientAccount entity = clientAccountRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Client account not found: " + id));

        entity.setClientType(parseClientType(request.clientType()));
        entity.setClientName(request.clientName());
        entity.setContactPerson(request.contactPerson());
        entity.setEmail(request.email());
        entity.setPhone(request.phone());
        entity.setBillingAddress(request.billingAddress());
        entity.setShippingAddress(request.shippingAddress());
        entity.setGstin(request.gstin());
        entity.setPan(request.pan());
        entity.setCreditLimit(request.creditLimit());
        entity.setPaymentTermsDays(request.paymentTermsDays());

        ClientAccount saved = clientAccountRepository.save(entity);
        return ClientAccountResponse.from(saved);
    }

    @Transactional
    public void deactivate(String tenantId, Long id) {
        ClientAccount entity = clientAccountRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Client account not found: " + id));
        entity.setActive(false);
        clientAccountRepository.save(entity);
    }

    private ClientAccountType parseClientType(String type) {
        try {
            return ClientAccountType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid client type: " + type
                    + ". Expected one of: CUSTOMER, VENDOR, EMPLOYEE, INTERCOMPANY");
        }
    }
}
