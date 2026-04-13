package com.nexus.onebook.accounts.repository;

import com.nexus.onebook.accounts.model.LedgerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LedgerAccountRepository extends JpaRepository<LedgerAccount, Long> {

    List<LedgerAccount> findByTenantId(String tenantId);

    Optional<LedgerAccount> findByTenantIdAndAccountCode(String tenantId, String accountCode);

    Optional<LedgerAccount> findByTenantIdAndCostCenterIdAndAccountCode(
            String tenantId, Long costCenterId, String accountCode);

    /**
     * Searches by blind index (HMAC-SHA256 hash of account name).
     * Enables exact-match searches on encrypted fields without exposing plaintext.
     */
    List<LedgerAccount> findByTenantIdAndAccountNameBlindIndex(
            String tenantId, String accountNameBlindIndex);
}
