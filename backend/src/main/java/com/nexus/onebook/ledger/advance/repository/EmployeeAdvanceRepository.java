package com.nexus.onebook.ledger.advance.repository;

import com.nexus.onebook.ledger.advance.model.AdvanceStatus;
import com.nexus.onebook.ledger.advance.model.EmployeeAdvance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeAdvanceRepository extends JpaRepository<EmployeeAdvance, Long> {

    Optional<EmployeeAdvance> findByIdAndTenantId(Long id, String tenantId);

    List<EmployeeAdvance> findByTenantIdAndEmployeeId(String tenantId, Long employeeId);

    List<EmployeeAdvance> findByTenantIdAndEmployeeIdAndStatus(String tenantId, Long employeeId, AdvanceStatus status);

    List<EmployeeAdvance> findByTenantIdAndStatus(String tenantId, AdvanceStatus status);

    List<EmployeeAdvance> findByTenantIdAndDepartmentIdAndStatus(String tenantId, Long departmentId, AdvanceStatus status);

    List<EmployeeAdvance> findByTenantIdAndDepartmentIdIn(String tenantId, List<Long> departmentIds);

    List<EmployeeAdvance> findByTenantIdAndDepartmentIdInAndStatus(String tenantId, List<Long> departmentIds, AdvanceStatus status);
}
