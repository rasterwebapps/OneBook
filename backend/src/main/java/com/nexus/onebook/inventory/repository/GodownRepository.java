package com.nexus.onebook.inventory.repository;

import com.nexus.onebook.inventory.model.Godown;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GodownRepository extends JpaRepository<Godown, Long> {
    List<Godown> findByTenantId(String tenantId);
    Optional<Godown> findByTenantIdAndGodownCode(String tenantId, String godownCode);
    List<Godown> findByTenantIdAndBranchId(String tenantId, Long branchId);
}
