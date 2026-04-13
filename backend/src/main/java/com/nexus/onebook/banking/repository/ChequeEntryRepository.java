package com.nexus.onebook.banking.repository;

import com.nexus.onebook.banking.model.ChequeEntry;
import com.nexus.onebook.banking.model.ChequeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChequeEntryRepository extends JpaRepository<ChequeEntry, Long> {
    List<ChequeEntry> findByTenantId(String tenantId);
    List<ChequeEntry> findByTenantIdAndStatus(String tenantId, ChequeStatus status);
    List<ChequeEntry> findByTenantIdAndBankAccountId(String tenantId, Long bankAccountId);
    Optional<ChequeEntry> findByTenantIdAndChequeNumberAndBankAccountId(
            String tenantId, String chequeNumber, Long bankAccountId);
}
