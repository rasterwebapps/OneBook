package com.nexus.onebook.ledger.repository;

import com.nexus.onebook.ledger.model.ClientAccount;
import com.nexus.onebook.ledger.model.ClientAccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientAccountRepository extends JpaRepository<ClientAccount, Long> {
    List<ClientAccount> findByTenantId(String tenantId);
    List<ClientAccount> findByTenantIdAndClientType(String tenantId, ClientAccountType clientType);
    List<ClientAccount> findByTenantIdAndActiveTrue(String tenantId);
    Optional<ClientAccount> findByTenantIdAndId(String tenantId, Long id);
    Optional<ClientAccount> findByTenantIdAndLedgerAccountId(String tenantId, Long ledgerAccountId);
}
