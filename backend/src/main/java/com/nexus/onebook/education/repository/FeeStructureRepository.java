package com.nexus.onebook.education.repository;

import com.nexus.onebook.education.model.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeeStructureRepository extends JpaRepository<FeeStructure, UUID> {

    Optional<FeeStructure> findByCourseIdAndTenantIdAndIsActiveTrue(UUID courseId, String tenantId);
}
