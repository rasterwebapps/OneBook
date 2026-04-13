package com.nexus.onebook.inventory.repository;

import com.nexus.onebook.inventory.model.StockGodownAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockGodownAllocationRepository extends JpaRepository<StockGodownAllocation, Long> {
    List<StockGodownAllocation> findByTenantIdAndStockItemId(String tenantId, Long stockItemId);
    List<StockGodownAllocation> findByTenantIdAndGodownId(String tenantId, Long godownId);
    Optional<StockGodownAllocation> findByTenantIdAndStockItemIdAndGodownId(
            String tenantId, Long stockItemId, Long godownId);
}
