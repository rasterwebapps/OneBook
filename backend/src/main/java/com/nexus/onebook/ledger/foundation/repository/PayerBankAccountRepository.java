package com.nexus.onebook.ledger.foundation.repository;

import com.nexus.onebook.ledger.foundation.model.PayerBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PayerBankAccountRepository extends JpaRepository<PayerBankAccount, Long> {
    List<PayerBankAccount> findByTenantId(String tenantId);
    List<PayerBankAccount> findByTenantIdAndPayerId(String tenantId, Long payerId);
}
