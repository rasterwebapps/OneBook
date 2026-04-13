package com.nexus.onebook.ledger.operations.repository;

import com.nexus.onebook.ledger.operations.model.BatchStatus;
import com.nexus.onebook.ledger.operations.model.BatchTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BatchTrackingRepository extends JpaRepository<BatchTracking, Long> {
    List<BatchTracking> findByTenantId(String tenantId);
    List<BatchTracking> findByTenantIdAndStockItemId(String tenantId, Long stockItemId);
    Optional<BatchTracking> findByTenantIdAndStockItemIdAndBatchNumber(
            String tenantId, Long stockItemId, String batchNumber);
    List<BatchTracking> findByTenantIdAndStatus(String tenantId, BatchStatus status);
    List<BatchTracking> findByTenantIdAndExpiryDateBefore(String tenantId, LocalDate date);
}
