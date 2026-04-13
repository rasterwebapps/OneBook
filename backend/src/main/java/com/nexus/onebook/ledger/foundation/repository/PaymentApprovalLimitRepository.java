package com.nexus.onebook.ledger.foundation.repository;

import com.nexus.onebook.ledger.foundation.model.PaymentApprovalLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentApprovalLimitRepository extends JpaRepository<PaymentApprovalLimit, Long> {
    List<PaymentApprovalLimit> findByTenantId(String tenantId);
    List<PaymentApprovalLimit> findByTenantIdAndActive(String tenantId, boolean active);
}
