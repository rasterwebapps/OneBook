package com.nexus.onebook.voucher.controller;

import com.nexus.onebook.voucher.dto.*;
import com.nexus.onebook.voucher.service.UploadedFileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/uploaded-files")
public class UploadedFileController {
    private final UploadedFileService uploadedFileService;

    public UploadedFileController(UploadedFileService uploadedFileService) {
        this.uploadedFileService = uploadedFileService;
    }

    @PostMapping
    public ResponseEntity<UploadedFileResponse> create(@Valid @RequestBody UploadedFileRequest request) {
        return ResponseEntity.status(201).body(uploadedFileService.createUploadedFile(request));
    }

    @GetMapping
    public ResponseEntity<List<UploadedFileResponse>> list(@RequestParam String tenantId) {
        return ResponseEntity.ok(uploadedFileService.getByTenant(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UploadedFileResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(uploadedFileService.getById(id));
    }

    @PostMapping("/{id}/mark-processed")
    public ResponseEntity<UploadedFileResponse> markProcessed(@PathVariable Long id) {
        return ResponseEntity.ok(uploadedFileService.markProcessed(id));
    }

    @PostMapping("/{id}/mark-failed")
    public ResponseEntity<UploadedFileResponse> markFailed(@PathVariable Long id, @RequestParam String errorMessage) {
        return ResponseEntity.ok(uploadedFileService.markFailed(id, errorMessage));
    }
}
