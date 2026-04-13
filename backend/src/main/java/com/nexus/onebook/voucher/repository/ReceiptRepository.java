package com.nexus.onebook.voucher.repository;

import com.nexus.onebook.voucher.model.Receipt;
import com.nexus.onebook.voucher.model.ReceiptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    List<Receipt> findByTenantId(String tenantId);
    Optional<Receipt> findByTenantIdAndReceiptNumber(String tenantId, String receiptNumber);
    List<Receipt> findByTenantIdAndStatus(String tenantId, ReceiptStatus status);
    List<Receipt> findByTenantIdAndVoucherId(String tenantId, Long voucherId);
}
