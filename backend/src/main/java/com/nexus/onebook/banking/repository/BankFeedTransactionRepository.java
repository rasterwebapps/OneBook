package com.nexus.onebook.banking.repository;

import com.nexus.onebook.banking.model.BankFeedTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankFeedTransactionRepository extends JpaRepository<BankFeedTransaction, Long> {
    List<BankFeedTransaction> findByTenantId(String tenantId);
    List<BankFeedTransaction> findByTenantIdAndMatched(String tenantId, boolean matched);
    List<BankFeedTransaction> findByTenantIdAndBankAccountId(String tenantId, Long bankAccountId);
    Optional<BankFeedTransaction> findByTenantIdAndExternalTransactionId(String tenantId, String externalTransactionId);
}
