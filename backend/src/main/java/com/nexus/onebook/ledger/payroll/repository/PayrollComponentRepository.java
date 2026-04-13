package com.nexus.onebook.ledger.payroll.repository;

import com.nexus.onebook.ledger.payroll.model.PayrollComponent;
import com.nexus.onebook.ledger.payroll.model.PayrollComponentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayrollComponentRepository extends JpaRepository<PayrollComponent, Long> {
    List<PayrollComponent> findByEmployeeId(Long employeeId);
    List<PayrollComponent> findByTenantIdAndEmployeeId(String tenantId, Long employeeId);
    List<PayrollComponent> findByTenantIdAndEmployeeIdAndComponentType(
            String tenantId, Long employeeId, PayrollComponentType componentType);
}
