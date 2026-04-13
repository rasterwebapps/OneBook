package com.nexus.onebook.ledger.clientaccount.dto;

import com.nexus.onebook.ledger.clientaccount.model.ClientAccount;
import java.math.BigDecimal;
import java.time.Instant;

public record ClientAccountResponse(
    Long id,
    String tenantId,
    Long ledgerAccountId,
    String ledgerAccountName,
    String clientType,
    String clientName,
    String contactPerson,
    String email,
    String phone,
    String billingAddress,
    String shippingAddress,
    String gstin,
    String pan,
    BigDecimal creditLimit,
    Integer paymentTermsDays,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {
    public static ClientAccountResponse from(ClientAccount entity) {
        return new ClientAccountResponse(
            entity.getId(),
            entity.getTenantId(),
            entity.getLedgerAccount().getId(),
            entity.getLedgerAccount().getAccountName(),
            entity.getClientType().name(),
            entity.getClientName(),
            entity.getContactPerson(),
            entity.getEmail(),
            entity.getPhone(),
            entity.getBillingAddress(),
            entity.getShippingAddress(),
            entity.getGstin(),
            entity.getPan(),
            entity.getCreditLimit(),
            entity.getPaymentTermsDays(),
            entity.isActive(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
