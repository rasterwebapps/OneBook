package com.nexus.onebook.banking.repository;

import com.nexus.onebook.banking.model.ConnectedPayment;
import com.nexus.onebook.banking.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConnectedPaymentRepository extends JpaRepository<ConnectedPayment, Long> {
    List<ConnectedPayment> findByTenantId(String tenantId);
    List<ConnectedPayment> findByTenantIdAndStatus(String tenantId, PaymentStatus status);
    List<ConnectedPayment> findByTenantIdAndBankAccountId(String tenantId, Long bankAccountId);
}
