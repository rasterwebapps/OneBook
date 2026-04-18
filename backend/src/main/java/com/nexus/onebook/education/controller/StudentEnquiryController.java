package com.nexus.onebook.education.controller;

import com.nexus.onebook.education.dto.CreateEnquiryRequest;
import com.nexus.onebook.education.dto.StudentEnquiryDto;
import com.nexus.onebook.education.service.StudentEnquiryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for student fee enquiries.
 */
@RestController
@RequestMapping("/api/education/enquiries")
public class StudentEnquiryController {

    private final StudentEnquiryService enquiryService;

    public StudentEnquiryController(StudentEnquiryService enquiryService) {
        this.enquiryService = enquiryService;
    }

    @GetMapping
    public ResponseEntity<List<StudentEnquiryDto>> listEnquiries(@RequestParam String tenantId) {
        return ResponseEntity.ok(enquiryService.listEnquiries(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentEnquiryDto> getEnquiry(
            @PathVariable UUID id,
            @RequestParam String tenantId) {
        return ResponseEntity.ok(enquiryService.getEnquiry(id, tenantId));
    }

    @PostMapping
    public ResponseEntity<StudentEnquiryDto> createEnquiry(
            @RequestParam String tenantId,
            @Valid @RequestBody CreateEnquiryRequest request) {
        StudentEnquiryDto created = enquiryService.createEnquiry(request, tenantId);
        return ResponseEntity.status(201).body(created);
    }
}
