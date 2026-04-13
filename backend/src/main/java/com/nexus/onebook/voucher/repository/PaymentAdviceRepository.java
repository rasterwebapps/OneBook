package com.nexus.onebook.voucher.repository;

import com.nexus.onebook.voucher.model.PaymentAdvice;
import com.nexus.onebook.voucher.model.PaymentAdviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentAdviceRepository extends JpaRepository<PaymentAdvice, Long> {
    List<PaymentAdvice> findByTenantId(String tenantId);
    Optional<PaymentAdvice> findByTenantIdAndAdviceNumber(String tenantId, String adviceNumber);
    List<PaymentAdvice> findByTenantIdAndStatus(String tenantId, PaymentAdviceStatus status);
    List<PaymentAdvice> findByTenantIdAndVoucherId(String tenantId, Long voucherId);
}
