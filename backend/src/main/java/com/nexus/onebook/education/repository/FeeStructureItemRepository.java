package com.nexus.onebook.education.repository;

import com.nexus.onebook.education.model.FeeStructureItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeeStructureItemRepository extends JpaRepository<FeeStructureItem, UUID> {

    List<FeeStructureItem> findAllByFeeStructureIdAndTenantId(UUID feeStructureId, String tenantId);
}
