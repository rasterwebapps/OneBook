package com.nexus.onebook.ledger.repository;

import com.nexus.onebook.ledger.model.SubDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubDepartmentRepository extends JpaRepository<SubDepartment, Long> {
    List<SubDepartment> findByTenantId(String tenantId);
    List<SubDepartment> findByTenantIdAndDepartmentId(String tenantId, Long departmentId);
}
