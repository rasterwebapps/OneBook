package com.nexus.onebook.ledger.foundation.repository;

import com.nexus.onebook.ledger.foundation.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByTenantId(String tenantId);
    Optional<Department> findByTenantIdAndCode(String tenantId, String code);
}
