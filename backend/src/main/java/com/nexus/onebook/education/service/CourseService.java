package com.nexus.onebook.education.service;

import com.nexus.onebook.education.dto.CourseDto;
import com.nexus.onebook.education.model.EducationCourse;
import com.nexus.onebook.education.repository.EducationCourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing education courses within programs.
 */
@Service
public class CourseService {

    private final EducationCourseRepository courseRepository;

    public CourseService(EducationCourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public List<CourseDto> listCourses(String tenantId) {
        return courseRepository.findAllByTenantIdAndIsActiveTrue(tenantId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseDto> listCoursesByProgram(UUID programId, String tenantId) {
        return courseRepository.findAllByProgramIdAndTenantIdAndIsActiveTrue(programId, tenantId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private CourseDto toDto(EducationCourse course) {
        return new CourseDto(
                course.getId(),
                course.getProgram().getId(),
                course.getName(),
                course.getCode(),
                course.isActive()
        );
    }
}
