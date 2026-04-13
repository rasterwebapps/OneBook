package com.nexus.onebook.payment.repository;

import com.nexus.onebook.payment.model.PaymentBatch;
import com.nexus.onebook.payment.model.PaymentBatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentBatchRepository extends JpaRepository<PaymentBatch, Long> {
    List<PaymentBatch> findByTenantIdAndStatus(String tenantId, PaymentBatchStatus status);
    Optional<PaymentBatch> findByTenantIdAndBatchNumber(String tenantId, String batchNumber);
    long countByTenantIdAndBatchNumberStartingWith(String tenantId, String prefix);
}
