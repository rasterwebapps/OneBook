package com.nexus.onebook.compliance.controller;

import com.nexus.onebook.compliance.dto.TdsTcsEntryRequest;
import com.nexus.onebook.compliance.model.TdsTcsEntry;
import com.nexus.onebook.compliance.model.TdsTcsStatus;
import com.nexus.onebook.compliance.model.TdsTcsType;
import com.nexus.onebook.compliance.service.TdsTcsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for TDS/TCS — tax deduction and collection at source.
 */
@RestController
@RequestMapping("/api/tds-tcs")
public class TdsTcsController {

    private final TdsTcsService tdsTcsService;

    public TdsTcsController(TdsTcsService tdsTcsService) {
        this.tdsTcsService = tdsTcsService;
    }

    @PostMapping
    public ResponseEntity<TdsTcsEntry> createEntry(@Valid @RequestBody TdsTcsEntryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tdsTcsService.createEntry(request));
    }

    @GetMapping
    public ResponseEntity<List<TdsTcsEntry>> getEntries(@RequestParam String tenantId) {
        return ResponseEntity.ok(tdsTcsService.getEntries(tenantId));
    }

    @GetMapping("/by-type")
    public ResponseEntity<List<TdsTcsEntry>> getByType(@RequestParam String tenantId,
                                                         @RequestParam TdsTcsType type) {
        return ResponseEntity.ok(tdsTcsService.getEntriesByType(tenantId, type));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<TdsTcsEntry>> getPending(@RequestParam String tenantId) {
        return ResponseEntity.ok(tdsTcsService.getPendingEntries(tenantId));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<TdsTcsEntry> updateStatus(@PathVariable Long id,
                                                      @RequestParam TdsTcsStatus status,
                                                      @RequestParam(required = false) String certificateNumber) {
        return ResponseEntity.ok(tdsTcsService.updateStatus(id, status, certificateNumber));
    }
}
