package com.nexus.onebook.voucher.service;

import com.nexus.onebook.voucher.dto.*;
import com.nexus.onebook.voucher.model.*;
import com.nexus.onebook.voucher.repository.UploadedFileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class UploadedFileService {

    private final UploadedFileRepository uploadedFileRepository;

    public UploadedFileService(UploadedFileRepository uploadedFileRepository) {
        this.uploadedFileRepository = uploadedFileRepository;
    }

    @Transactional
    public UploadedFileResponse createUploadedFile(UploadedFileRequest request) {
        UploadedFile file = new UploadedFile(request.tenantId(), request.fileName(),
                request.originalFileName(), request.uploadedBy());
        file.setContentType(request.contentType());
        file.setFileSize(request.fileSize());
        file.setStoragePath(request.storagePath());

        UploadedFile saved = uploadedFileRepository.save(file);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UploadedFileResponse> getByTenant(String tenantId) {
        return uploadedFileRepository.findByTenantId(tenantId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UploadedFileResponse getById(Long id) {
        UploadedFile file = uploadedFileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Uploaded file not found: " + id));
        return toResponse(file);
    }

    @Transactional
    public UploadedFileResponse markProcessed(Long id) {
        UploadedFile file = uploadedFileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Uploaded file not found: " + id));
        file.setStatus(FileStatus.PROCESSED);
        file.setProcessedAt(Instant.now());
        return toResponse(uploadedFileRepository.save(file));
    }

    @Transactional
    public UploadedFileResponse markFailed(Long id, String errorMessage) {
        UploadedFile file = uploadedFileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Uploaded file not found: " + id));
        file.setStatus(FileStatus.FAILED);
        file.setErrorMessage(errorMessage);
        return toResponse(uploadedFileRepository.save(file));
    }

    private UploadedFileResponse toResponse(UploadedFile f) {
        return new UploadedFileResponse(
                f.getId(), f.getTenantId(), f.getFileName(),
                f.getOriginalFileName(), f.getContentType(),
                f.getFileSize(), f.getStatus().name(),
                f.getProcessedAt(), f.getErrorMessage(),
                f.getUploadedBy(), f.getCreatedAt()
        );
    }
}
