package com.nexus.onebook.ledger.payment.repository;

import com.nexus.onebook.ledger.payment.model.PaymentBatchItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentBatchItemRepository extends JpaRepository<PaymentBatchItem, Long> {
    List<PaymentBatchItem> findByBatchId(Long batchId);
}
