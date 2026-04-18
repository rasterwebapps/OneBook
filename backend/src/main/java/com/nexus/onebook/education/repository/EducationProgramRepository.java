package com.nexus.onebook.education.repository;

import com.nexus.onebook.education.model.EducationProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EducationProgramRepository extends JpaRepository<EducationProgram, UUID> {

    List<EducationProgram> findAllByTenantIdAndIsActiveTrue(String tenantId);
}
