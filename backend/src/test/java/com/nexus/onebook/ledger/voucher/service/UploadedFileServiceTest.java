package com.nexus.onebook.ledger.voucher.service;

import com.nexus.onebook.ledger.voucher.dto.*;
import com.nexus.onebook.ledger.voucher.model.*;
import com.nexus.onebook.ledger.voucher.repository.UploadedFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadedFileServiceTest {

    @Mock private UploadedFileRepository uploadedFileRepository;

    @InjectMocks
    private UploadedFileService uploadedFileService;

    private UploadedFile sampleFile;

    @BeforeEach
    void setUp() {
        sampleFile = new UploadedFile("tenant1", "uuid-file.pdf", "invoice.pdf", "admin");
        sampleFile.setId(1L);
        sampleFile.setContentType("application/pdf");
        sampleFile.setFileSize(1024L);
    }

    @Test
    void createUploadedFile_shouldPersistAndReturn() {
        UploadedFileRequest request = new UploadedFileRequest(
                "tenant1", "uuid-file.pdf", "invoice.pdf",
                "application/pdf", 1024L, "/uploads/uuid-file.pdf", "admin");

        when(uploadedFileRepository.save(any(UploadedFile.class))).thenAnswer(i -> {
            UploadedFile f = i.getArgument(0);
            f.setId(1L);
            return f;
        });

        UploadedFileResponse response = uploadedFileService.createUploadedFile(request);

        assertNotNull(response);
        assertEquals("uuid-file.pdf", response.fileName());
        assertEquals("UPLOADED", response.status());
    }

    @Test
    void getByTenant_shouldReturnList() {
        when(uploadedFileRepository.findByTenantId("tenant1")).thenReturn(List.of(sampleFile));

        List<UploadedFileResponse> results = uploadedFileService.getByTenant("tenant1");

        assertEquals(1, results.size());
    }

    @Test
    void markProcessed_shouldUpdateStatus() {
        when(uploadedFileRepository.findById(1L)).thenReturn(Optional.of(sampleFile));
        when(uploadedFileRepository.save(any(UploadedFile.class))).thenAnswer(i -> i.getArgument(0));

        UploadedFileResponse response = uploadedFileService.markProcessed(1L);

        assertEquals("PROCESSED", response.status());
    }

    @Test
    void markFailed_shouldUpdateStatusAndError() {
        when(uploadedFileRepository.findById(1L)).thenReturn(Optional.of(sampleFile));
        when(uploadedFileRepository.save(any(UploadedFile.class))).thenAnswer(i -> i.getArgument(0));

        UploadedFileResponse response = uploadedFileService.markFailed(1L, "Parsing error");

        assertEquals("FAILED", response.status());
        assertEquals("Parsing error", response.errorMessage());
    }
}
