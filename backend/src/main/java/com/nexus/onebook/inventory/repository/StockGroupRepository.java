package com.nexus.onebook.inventory.repository;

import com.nexus.onebook.inventory.model.StockGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockGroupRepository extends JpaRepository<StockGroup, Long> {
    List<StockGroup> findByTenantId(String tenantId);
    Optional<StockGroup> findByTenantIdAndGroupCode(String tenantId, String groupCode);
}
