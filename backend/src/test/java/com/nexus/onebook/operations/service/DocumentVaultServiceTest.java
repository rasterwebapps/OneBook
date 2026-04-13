package com.nexus.onebook.operations.service;

import com.nexus.onebook.operations.dto.DocumentUploadRequest;
import com.nexus.onebook.accounts.model.JournalTransaction;
import com.nexus.onebook.operations.model.VaultDocument;
import com.nexus.onebook.accounts.repository.JournalTransactionRepository;
import com.nexus.onebook.operations.repository.VaultDocumentRepository;
import com.nexus.onebook.security.FieldEncryptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentVaultServiceTest {

    @Mock
    private VaultDocumentRepository documentRepository;
    @Mock
    private JournalTransactionRepository transactionRepository;
    @Mock
    private FieldEncryptionService encryptionService;
    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private DocumentVaultService documentVaultService;

    @Test
    void storeDocument_validRequest_succeeds() {
        DocumentUploadRequest request = new DocumentUploadRequest(
                "tenant-1", "invoice.pdf", "application/pdf",
                1024L, "abc123checksum", null, "admin");

        when(documentRepository.save(any(VaultDocument.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        VaultDocument result = documentVaultService.storeDocument(request);

        assertNotNull(result);
        assertEquals("tenant-1", result.getTenantId());
        assertEquals("invoice.pdf", result.getFileName());
        assertEquals("application/pdf", result.getContentType());
        assertEquals(1024L, result.getFileSize());
        assertNotNull(result.getStorageKey());
        assertTrue(result.getStorageKey().startsWith("vault/tenant-1/"));
        assertEquals("admin", result.getUploadedBy());
    }

    @Test
    void storeDocument_withTransaction_linksTransaction() {
        JournalTransaction txn = new JournalTransaction();
        txn.setId(42L);

        DocumentUploadRequest request = new DocumentUploadRequest(
                "tenant-1", "receipt.jpg", "image/jpeg",
                2048L, "def456checksum", 42L, null);

        when(transactionRepository.findById(42L)).thenReturn(Optional.of(txn));
        when(documentRepository.save(any(VaultDocument.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        VaultDocument result = documentVaultService.storeDocument(request);

        assertNotNull(result.getJournalTransaction());
        assertEquals(42L, result.getJournalTransaction().getId());
    }

    @Test
    void storeDocument_transactionNotFound_throws() {
        DocumentUploadRequest request = new DocumentUploadRequest(
                "tenant-1", "doc.pdf", "application/pdf",
                512L, "checksum", 99L, null);

        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> documentVaultService.storeDocument(request));
    }

    @Test
    void getDocuments_returnsList() {
        when(documentRepository.findByTenantId("tenant-1"))
                .thenReturn(List.of(new VaultDocument()));

        List<VaultDocument> result = documentVaultService.getDocuments("tenant-1");

        assertEquals(1, result.size());
    }

    @Test
    void getDocument_notFound_throws() {
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> documentVaultService.getDocument(99L));
    }

    @Test
    void deleteDocument_removesDocument() {
        VaultDocument doc = new VaultDocument();
        doc.setId(1L);

        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        documentVaultService.deleteDocument(1L);

        verify(documentRepository).delete(doc);
    }

    @Test
    void computeChecksum_deterministic() {
        String checksum1 = documentVaultService.computeChecksum("test content");
        String checksum2 = documentVaultService.computeChecksum("test content");
        assertEquals(checksum1, checksum2);
    }

    @Test
    void computeChecksum_is64HexChars() {
        String checksum = documentVaultService.computeChecksum("test");
        assertEquals(64, checksum.length());
        assertTrue(checksum.matches("[0-9a-f]+"));
    }

    @Test
    void verifyChecksum_validContent_returnsTrue() {
        String content = "document content";
        String checksum = documentVaultService.computeChecksum(content);
        assertTrue(documentVaultService.verifyChecksum(content, checksum));
    }

    @Test
    void verifyChecksum_tamperedContent_returnsFalse() {
        String checksum = documentVaultService.computeChecksum("original");
        assertFalse(documentVaultService.verifyChecksum("tampered", checksum));
    }

    @Test
    void storeDocumentWithOriginalPath_preservesMinioPathInMetadata() {
        DocumentUploadRequest request = new DocumentUploadRequest(
                "tenant-1", "invoice.pdf", "application/pdf",
                2048576L, "abc123checksum", null, "pharmacy-system");

        when(documentRepository.save(any(VaultDocument.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        VaultDocument result = documentVaultService.storeDocumentWithOriginalPath(
                request, "/minio-bucket/pharmacy/invoices/2026/invoice.pdf");

        assertNotNull(result);
        assertNotNull(result.getMetadata());
        assertTrue(result.getMetadata().contains("originalPath"));
        assertTrue(result.getMetadata().contains("/minio-bucket/pharmacy/invoices/2026/invoice.pdf"));
        // Should have been saved twice: once in storeDocument, once after setting metadata
        verify(documentRepository, times(2)).save(any(VaultDocument.class));
    }

    @Test
    void storeDocumentWithOriginalPath_nullOriginalPath_doesNotUpdateMetadata() {
        DocumentUploadRequest request = new DocumentUploadRequest(
                "tenant-1", "invoice.pdf", "application/pdf",
                2048576L, "abc123checksum", null, "pharmacy-system");

        when(documentRepository.save(any(VaultDocument.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        VaultDocument result = documentVaultService.storeDocumentWithOriginalPath(request, null);

        assertNotNull(result);
        // Should only be saved once (by storeDocument)
        verify(documentRepository, times(1)).save(any(VaultDocument.class));
    }
}
