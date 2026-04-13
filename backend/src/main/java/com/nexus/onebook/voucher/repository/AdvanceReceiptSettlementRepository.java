package com.nexus.onebook.voucher.repository;

import com.nexus.onebook.voucher.model.AdvanceReceiptSettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AdvanceReceiptSettlementRepository extends JpaRepository<AdvanceReceiptSettlement, Long> {
    List<AdvanceReceiptSettlement> findByTenantId(String tenantId);
    List<AdvanceReceiptSettlement> findByTenantIdAndAdvanceId(String tenantId, Long advanceId);
    List<AdvanceReceiptSettlement> findByTenantIdAndReceiptId(String tenantId, Long receiptId);
}
