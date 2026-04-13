package com.nexus.onebook.advance.repository;

import com.nexus.onebook.advance.model.ExpenseVoucher;
import com.nexus.onebook.advance.model.ExpenseVoucherStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseVoucherRepository extends JpaRepository<ExpenseVoucher, Long> {

    Optional<ExpenseVoucher> findByIdAndTenantId(Long id, String tenantId);

    List<ExpenseVoucher> findByTenantIdAndEmployeeId(String tenantId, Long employeeId);

    List<ExpenseVoucher> findByTenantIdAndEmployeeIdAndStatus(String tenantId, Long employeeId, ExpenseVoucherStatus status);

    List<ExpenseVoucher> findByTenantIdAndStatus(String tenantId, ExpenseVoucherStatus status);

    List<ExpenseVoucher> findByTenantIdAndDepartmentIdAndStatus(String tenantId, Long departmentId, ExpenseVoucherStatus status);

    List<ExpenseVoucher> findByTenantIdAndDepartmentIdInAndStatus(String tenantId, List<Long> departmentIds, ExpenseVoucherStatus status);
}
