package com.nexus.onebook.intelligence.repository;

import com.nexus.onebook.intelligence.model.CorporateAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CorporateActionRepository extends JpaRepository<CorporateAction, Long> {
    List<CorporateAction> findByTenantId(String tenantId);

    List<CorporateAction> findByHoldingId(Long holdingId);

    List<CorporateAction> findByTenantIdAndProcessed(String tenantId, boolean processed);
}
