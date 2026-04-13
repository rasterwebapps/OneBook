package com.nexus.onebook.accounts.repository;

import com.nexus.onebook.accounts.model.CostCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CostCenterRepository extends JpaRepository<CostCenter, Long> {

    List<CostCenter> findByTenantId(String tenantId);

    List<CostCenter> findByTenantIdAndBranchId(String tenantId, Long branchId);

    Optional<CostCenter> findByTenantIdAndBranchIdAndCode(String tenantId, Long branchId, String code);
}
