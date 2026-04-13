package com.nexus.onebook.voucher.repository;

import com.nexus.onebook.voucher.model.AdvanceVoucherItemSettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AdvanceVoucherItemSettlementRepository extends JpaRepository<AdvanceVoucherItemSettlement, Long> {
    List<AdvanceVoucherItemSettlement> findByTenantId(String tenantId);
    List<AdvanceVoucherItemSettlement> findByTenantIdAndAdvanceId(String tenantId, Long advanceId);
    List<AdvanceVoucherItemSettlement> findByTenantIdAndVoucherItemId(String tenantId, Long voucherItemId);
}
