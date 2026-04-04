package com.nexus.onebook.ledger.repository;

import com.nexus.onebook.ledger.model.Payee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayeeRepository extends JpaRepository<Payee, Long> {
    List<Payee> findByTenantId(String tenantId);
    Optional<Payee> findByTenantIdAndCode(String tenantId, String code);
}
