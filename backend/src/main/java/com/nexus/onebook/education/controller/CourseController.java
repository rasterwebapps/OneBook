package com.nexus.onebook.education.controller;

import com.nexus.onebook.education.dto.CourseDto;
import com.nexus.onebook.education.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for education courses.
 */
@RestController
@RequestMapping("/api/education/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * Lists all active courses, optionally filtered by programId.
     */
    @GetMapping
    public ResponseEntity<List<CourseDto>> listCourses(
            @RequestParam String tenantId,
            @RequestParam(required = false) UUID programId) {
        if (programId != null) {
            return ResponseEntity.ok(courseService.listCoursesByProgram(programId, tenantId));
        }
        return ResponseEntity.ok(courseService.listCourses(tenantId));
    }

    /**
     * Lists all active courses for a specific program.
     */
    @GetMapping("/by-program/{programId}")
    public ResponseEntity<List<CourseDto>> listCoursesByProgram(
            @PathVariable UUID programId,
            @RequestParam String tenantId) {
        return ResponseEntity.ok(courseService.listCoursesByProgram(programId, tenantId));
    }
}
