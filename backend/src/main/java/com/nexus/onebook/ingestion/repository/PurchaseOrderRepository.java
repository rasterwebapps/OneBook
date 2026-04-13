package com.nexus.onebook.ingestion.repository;

import com.nexus.onebook.ingestion.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByTenantIdAndPoNumber(String tenantId, String poNumber);
}
