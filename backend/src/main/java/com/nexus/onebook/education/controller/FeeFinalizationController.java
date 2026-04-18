package com.nexus.onebook.education.controller;

import com.nexus.onebook.education.dto.CreateFinalizationRequest;
import com.nexus.onebook.education.dto.FeeFinalizationDto;
import com.nexus.onebook.education.service.FeeFinalizationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for fee finalizations.
 */
@RestController
@RequestMapping("/api/education/finalizations")
public class FeeFinalizationController {

    private final FeeFinalizationService finalizationService;

    public FeeFinalizationController(FeeFinalizationService finalizationService) {
        this.finalizationService = finalizationService;
    }

    /**
     * Retrieves the finalization record for a given enquiry.
     */
    @GetMapping("/by-enquiry/{enquiryId}")
    public ResponseEntity<FeeFinalizationDto> getFinalization(
            @PathVariable UUID enquiryId,
            @RequestParam String tenantId) {
        return ResponseEntity.ok(finalizationService.getFinalization(enquiryId, tenantId));
    }

    /**
     * Finalizes a student enquiry by applying a discount and computing the final payable amount.
     */
    @PostMapping
    public ResponseEntity<FeeFinalizationDto> finalizeEnquiry(
            @RequestParam String tenantId,
            @Valid @RequestBody CreateFinalizationRequest request) {
        FeeFinalizationDto result = finalizationService.finalizeEnquiry(request, tenantId);
        return ResponseEntity.status(201).body(result);
    }
}
