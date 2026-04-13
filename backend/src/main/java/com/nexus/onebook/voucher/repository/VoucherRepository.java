package com.nexus.onebook.voucher.repository;

import com.nexus.onebook.voucher.model.Voucher;
import com.nexus.onebook.voucher.model.VoucherStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    List<Voucher> findByTenantId(String tenantId);
    Optional<Voucher> findByTenantIdAndVoucherNumber(String tenantId, String voucherNumber);
    List<Voucher> findByTenantIdAndStatus(String tenantId, VoucherStatus status);
    List<Voucher> findByTenantIdAndDepartmentId(String tenantId, Long departmentId);
}
