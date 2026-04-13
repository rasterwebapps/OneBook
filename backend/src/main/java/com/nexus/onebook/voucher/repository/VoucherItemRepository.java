package com.nexus.onebook.voucher.repository;

import com.nexus.onebook.voucher.model.VoucherItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VoucherItemRepository extends JpaRepository<VoucherItem, Long> {
    List<VoucherItem> findByTenantId(String tenantId);
    List<VoucherItem> findByTenantIdAndVoucherId(String tenantId, Long voucherId);
    List<VoucherItem> findByTenantIdAndPayeeId(String tenantId, Long payeeId);
}
