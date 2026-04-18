package com.nexus.onebook.education.repository;

import com.nexus.onebook.education.model.FeeCategory;
import com.nexus.onebook.education.model.FeeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeeTypeRepository extends JpaRepository<FeeType, UUID> {

    List<FeeType> findAllByTenantIdAndIsActiveTrue(String tenantId);

    List<FeeType> findAllByCategoryAndTenantIdAndIsActiveTrue(FeeCategory category, String tenantId);
}
