package com.nexus.onebook.inventory.repository;

import com.nexus.onebook.inventory.model.BillOfMaterials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillOfMaterialsRepository extends JpaRepository<BillOfMaterials, Long> {
    List<BillOfMaterials> findByTenantId(String tenantId);
    Optional<BillOfMaterials> findByTenantIdAndBomCode(String tenantId, String bomCode);
}
