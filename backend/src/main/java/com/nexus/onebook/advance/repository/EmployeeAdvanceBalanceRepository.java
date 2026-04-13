package com.nexus.onebook.advance.repository;

import com.nexus.onebook.advance.model.EmployeeAdvanceBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeAdvanceBalanceRepository extends JpaRepository<EmployeeAdvanceBalance, Long> {

    Optional<EmployeeAdvanceBalance> findByTenantIdAndEmployeeId(String tenantId, Long employeeId);
}
