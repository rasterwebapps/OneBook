package com.nexus.onebook.reporting.repository;

import com.nexus.onebook.reporting.model.IntercompanyElimination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntercompanyEliminationRepository extends JpaRepository<IntercompanyElimination, Long> {
    List<IntercompanyElimination> findByTenantId(String tenantId);
    List<IntercompanyElimination> findByTenantIdAndEliminated(String tenantId, boolean eliminated);
    List<IntercompanyElimination> findByTenantIdAndSourceBranchId(String tenantId, Long sourceBranchId);
    List<IntercompanyElimination> findByTenantIdAndTargetBranchId(String tenantId, Long targetBranchId);
}
