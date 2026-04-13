package com.nexus.onebook.ledger.accounts.repository;

import com.nexus.onebook.ledger.accounts.model.Enterprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnterpriseRepository extends JpaRepository<Enterprise, Long> {

    List<Enterprise> findByTenantId(String tenantId);

    Optional<Enterprise> findByTenantIdAndCode(String tenantId, String code);
}
