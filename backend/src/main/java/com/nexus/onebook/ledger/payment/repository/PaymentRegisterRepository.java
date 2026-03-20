package com.nexus.onebook.ledger.payment.repository;

import com.nexus.onebook.ledger.payment.model.PaymentRegisterEntry;
import com.nexus.onebook.ledger.payment.model.PaymentRegisterStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRegisterRepository extends JpaRepository<PaymentRegisterEntry, Long> {
    List<PaymentRegisterEntry> findByTenantIdAndStatus(String tenantId, PaymentRegisterStatus status);
    List<PaymentRegisterEntry> findByTenantIdAndVendorAccountIdAndStatus(String tenantId, Long vendorAccountId, PaymentRegisterStatus status);
    List<PaymentRegisterEntry> findByTenantIdAndStatusOrderByDueDateAsc(String tenantId, PaymentRegisterStatus status);
    List<PaymentRegisterEntry> findByIdInAndTenantId(List<Long> ids, String tenantId);
    Optional<PaymentRegisterEntry> findByEventUuid(UUID eventUuid);
    List<PaymentRegisterEntry> findByTenantId(String tenantId);
}
