package com.nexus.onebook.advance.repository;

import com.nexus.onebook.advance.model.EmployeePaymentAdvice;
import com.nexus.onebook.advance.model.PaymentAdviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeePaymentAdviceRepository extends JpaRepository<EmployeePaymentAdvice, Long> {

    Optional<EmployeePaymentAdvice> findByIdAndTenantId(Long id, String tenantId);

    List<EmployeePaymentAdvice> findByTenantIdAndEmployeeId(String tenantId, Long employeeId);

    List<EmployeePaymentAdvice> findByTenantIdAndStatus(String tenantId, PaymentAdviceStatus status);

    List<EmployeePaymentAdvice> findByTenantIdAndEmployeeIdAndStatus(String tenantId, Long employeeId, PaymentAdviceStatus status);
}
