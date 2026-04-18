package com.nexus.onebook.education.repository;

import com.nexus.onebook.education.model.FeeFinalization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeeFinalizationRepository extends JpaRepository<FeeFinalization, UUID> {

    Optional<FeeFinalization> findByEnquiryIdAndTenantId(UUID enquiryId, String tenantId);
}
