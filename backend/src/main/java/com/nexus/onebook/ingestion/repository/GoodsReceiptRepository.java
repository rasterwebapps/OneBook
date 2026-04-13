package com.nexus.onebook.ingestion.repository;

import com.nexus.onebook.ingestion.model.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {

    Optional<GoodsReceipt> findByTenantIdAndPoNumber(String tenantId, String poNumber);
}
