package com.nexus.onebook.voucher.repository;

import com.nexus.onebook.voucher.model.DepartmentLevelApprovalLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DepartmentLevelApprovalLimitRepository extends JpaRepository<DepartmentLevelApprovalLimit, Long> {
    List<DepartmentLevelApprovalLimit> findByTenantId(String tenantId);
    List<DepartmentLevelApprovalLimit> findByTenantIdAndDepartmentId(String tenantId, Long departmentId);
    List<DepartmentLevelApprovalLimit> findByTenantIdAndPayerId(String tenantId, Long payerId);
    List<DepartmentLevelApprovalLimit> findByTenantIdAndDepartmentIdAndPayerId(String tenantId, Long departmentId, Long payerId);
}
