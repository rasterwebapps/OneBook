package com.nexus.onebook.ledger.inventory.repository;

import com.nexus.onebook.ledger.inventory.model.ReorderLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReorderLevelRepository extends JpaRepository<ReorderLevel, Long> {
    List<ReorderLevel> findByTenantId(String tenantId);
    List<ReorderLevel> findByTenantIdAndActiveTrue(String tenantId);
    Optional<ReorderLevel> findByTenantIdAndStockItemIdAndGodownId(
            String tenantId, Long stockItemId, Long godownId);
}
