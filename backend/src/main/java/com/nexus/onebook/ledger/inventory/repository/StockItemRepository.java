package com.nexus.onebook.ledger.inventory.repository;

import com.nexus.onebook.ledger.inventory.model.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockItemRepository extends JpaRepository<StockItem, Long> {
    List<StockItem> findByTenantId(String tenantId);
    Optional<StockItem> findByTenantIdAndItemCode(String tenantId, String itemCode);
    List<StockItem> findByTenantIdAndStockGroupId(String tenantId, Long stockGroupId);
}
