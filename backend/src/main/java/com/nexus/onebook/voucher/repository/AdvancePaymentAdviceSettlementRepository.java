package com.nexus.onebook.voucher.repository;

import com.nexus.onebook.voucher.model.AdvancePaymentAdviceSettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AdvancePaymentAdviceSettlementRepository extends JpaRepository<AdvancePaymentAdviceSettlement, Long> {
    List<AdvancePaymentAdviceSettlement> findByTenantId(String tenantId);
    List<AdvancePaymentAdviceSettlement> findByTenantIdAndAdvanceId(String tenantId, Long advanceId);
    List<AdvancePaymentAdviceSettlement> findByTenantIdAndPaymentAdviceId(String tenantId, Long paymentAdviceId);
}
