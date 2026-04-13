package com.nexus.onebook.ledger.foundation.repository;

import com.nexus.onebook.ledger.foundation.model.Advance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AdvanceRepository extends JpaRepository<Advance, Long> {
    List<Advance> findByTenantId(String tenantId);
    List<Advance> findByTenantIdAndSettled(String tenantId, boolean settled);
}
