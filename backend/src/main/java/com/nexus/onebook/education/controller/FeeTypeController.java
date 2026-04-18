package com.nexus.onebook.education.controller;

import com.nexus.onebook.education.dto.FeeTypeDto;
import com.nexus.onebook.education.model.FeeCategory;
import com.nexus.onebook.education.service.FeeTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for fee types.
 */
@RestController
@RequestMapping("/api/education/fee-types")
public class FeeTypeController {

    private final FeeTypeService feeTypeService;

    public FeeTypeController(FeeTypeService feeTypeService) {
        this.feeTypeService = feeTypeService;
    }

    /**
     * Lists all active fee types, optionally filtered by category (GENERIC or ADDITIONAL).
     */
    @GetMapping
    public ResponseEntity<List<FeeTypeDto>> listFeeTypes(
            @RequestParam String tenantId,
            @RequestParam(required = false) String category) {
        if (category != null && !category.isBlank()) {
            FeeCategory feeCategory = FeeCategory.valueOf(category.toUpperCase());
            return ResponseEntity.ok(feeTypeService.listFeeTypesByCategory(feeCategory, tenantId));
        }
        return ResponseEntity.ok(feeTypeService.listFeeTypes(tenantId));
    }
}
