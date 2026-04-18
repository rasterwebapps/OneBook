package com.nexus.onebook.education.controller;

import com.nexus.onebook.education.dto.CreateFeeStructureRequest;
import com.nexus.onebook.education.dto.EnquiryFeeBreakdownDto;
import com.nexus.onebook.education.dto.FeeStructureDto;
import com.nexus.onebook.education.service.FeeStructureService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for fee structures.
 */
@RestController
@RequestMapping("/api/education/fee-structures")
public class FeeStructureController {

    private final FeeStructureService feeStructureService;

    public FeeStructureController(FeeStructureService feeStructureService) {
        this.feeStructureService = feeStructureService;
    }

    /**
     * Retrieves the active fee structure for a course with all items and computed totals.
     */
    @GetMapping("/by-course/{courseId}")
    public ResponseEntity<FeeStructureDto> getFeeStructure(
            @PathVariable UUID courseId,
            @RequestParam String tenantId) {
        return ResponseEntity.ok(feeStructureService.getFeeStructureByCourse(courseId, tenantId));
    }

    /**
     * Retrieves the fee breakdown (generic, hostel, transportation totals) for enquiry creation.
     */
    @GetMapping("/breakdown/{courseId}")
    public ResponseEntity<EnquiryFeeBreakdownDto> getFeeBreakdown(
            @PathVariable UUID courseId,
            @RequestParam String tenantId) {
        return ResponseEntity.ok(feeStructureService.getFeeBreakdown(courseId, tenantId));
    }

    /**
     * Creates or replaces the active fee structure for a course.
     * Any previously active structure is deactivated.
     */
    @PostMapping
    public ResponseEntity<FeeStructureDto> createOrUpdateFeeStructure(
            @RequestParam String tenantId,
            @Valid @RequestBody CreateFeeStructureRequest request) {
        FeeStructureDto result = feeStructureService.createOrUpdateFeeStructure(request, tenantId);
        return ResponseEntity.status(201).body(result);
    }
}
