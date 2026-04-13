package com.nexus.onebook.payroll.repository;

import com.nexus.onebook.payroll.model.PayrollEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollEmployeeRepository extends JpaRepository<PayrollEmployee, Long> {
    List<PayrollEmployee> findByTenantId(String tenantId);
    Optional<PayrollEmployee> findByTenantIdAndEmployeeCode(String tenantId, String employeeCode);
    List<PayrollEmployee> findByTenantIdAndEmployeeGroup(String tenantId, String employeeGroup);
    List<PayrollEmployee> findByTenantIdAndActiveTrue(String tenantId);
}
