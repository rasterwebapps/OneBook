package com.nexus.onebook.credit.repository;

import com.nexus.onebook.credit.model.CreditLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditLimitRepository extends JpaRepository<CreditLimit, Long> {
    List<CreditLimit> findByTenantId(String tenantId);
    Optional<CreditLimit> findByTenantIdAndAccountId(String tenantId, Long accountId);
    List<CreditLimit> findByTenantIdAndBlockedTrue(String tenantId);
}
