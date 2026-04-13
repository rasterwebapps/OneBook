package com.nexus.onebook.advance.repository;

import com.nexus.onebook.advance.model.EmployeeAdvanceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeAdvanceConfigRepository extends JpaRepository<EmployeeAdvanceConfig, Long> {

    Optional<EmployeeAdvanceConfig> findByTenantIdAndEmployeeId(String tenantId, Long employeeId);
}
