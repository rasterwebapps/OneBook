package com.nexus.onebook.education.repository;

import com.nexus.onebook.education.model.EducationCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EducationCourseRepository extends JpaRepository<EducationCourse, UUID> {

    List<EducationCourse> findAllByTenantIdAndIsActiveTrue(String tenantId);

    List<EducationCourse> findAllByProgramIdAndTenantIdAndIsActiveTrue(UUID programId, String tenantId);
}
