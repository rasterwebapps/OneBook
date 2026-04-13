package com.nexus.onebook.ledger.inventory.repository;

import com.nexus.onebook.ledger.inventory.model.StockGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockGroupRepository extends JpaRepository<StockGroup, Long> {
    List<StockGroup> findByTenantId(String tenantId);
    Optional<StockGroup> findByTenantIdAndGroupCode(String tenantId, String groupCode);
}
