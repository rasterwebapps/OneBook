package com.nexus.onebook.ledger.repository;

import com.nexus.onebook.ledger.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByTenantId(String tenantId);
    Optional<Application> findByTenantIdAndApplicationNumber(String tenantId, String applicationNumber);
}
