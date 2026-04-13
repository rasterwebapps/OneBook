package com.nexus.onebook.ledger.advance.repository;

import com.nexus.onebook.ledger.advance.model.EmployeeAdvanceBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeAdvanceBalanceRepository extends JpaRepository<EmployeeAdvanceBalance, Long> {

    Optional<EmployeeAdvanceBalance> findByTenantIdAndEmployeeId(String tenantId, Long employeeId);
}
