package com.nexus.onebook.payment.repository;

import com.nexus.onebook.payment.model.PaymentBatchItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentBatchItemRepository extends JpaRepository<PaymentBatchItem, Long> {
    List<PaymentBatchItem> findByBatchId(Long batchId);
}
