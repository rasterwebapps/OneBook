package com.nexus.onebook.ledger.repository;

import com.nexus.onebook.ledger.model.PayeeBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PayeeBankAccountRepository extends JpaRepository<PayeeBankAccount, Long> {
    List<PayeeBankAccount> findByTenantId(String tenantId);
    List<PayeeBankAccount> findByTenantIdAndPayeeId(String tenantId, Long payeeId);
}
