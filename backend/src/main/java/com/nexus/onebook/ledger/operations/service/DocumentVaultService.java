package com.nexus.onebook.ledger.operations.service;

import com.nexus.onebook.ledger.operations.dto.DocumentUploadRequest;
import com.nexus.onebook.ledger.accounts.model.JournalTransaction;
import com.nexus.onebook.ledger.operations.model.VaultDocument;
import com.nexus.onebook.ledger.accounts.repository.JournalTransactionRepository;
import com.nexus.onebook.ledger.operations.repository.VaultDocumentRepository;
import com.nexus.onebook.ledger.security.FieldEncryptionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Smart Document Vault service.
 * Manages encrypted source document storage in S3/MinIO buckets
 * with each journal entry attachment capability.
 */
@Service
public class DocumentVaultService {

    private final VaultDocumentRepository documentRepository;
    private final JournalTransactionRepository transactionRepository;
    private final FieldEncryptionService encryptionService;
    private final ObjectMapper objectMapper;

    public DocumentVaultService(VaultDocumentRepository documentRepository,
                                 JournalTransactionRepository transactionRepository,
                                 FieldEncryptionService encryptionService,
                                 ObjectMapper objectMapper) {
        this.documentRepository = documentRepository;
        this.transactionRepository = transactionRepository;
        this.encryptionService = encryptionService;
        this.objectMapper = objectMapper;
    }

    /**
     * Stores document metadata in the vault and generates an encrypted storage key.
     * In production, the actual file bytes would be encrypted and sent to S3/MinIO.
     */
    @Transactional
    public VaultDocument storeDocument(DocumentUploadRequest request) {
        // Generate a unique storage key for S3/MinIO
        String storageKey = generateStorageKey(request.tenantId(), request.fileName());

        VaultDocument document = new VaultDocument(
                request.tenantId(), request.fileName(), request.contentType(),
                request.fileSize(), storageKey, request.checksum());

        document.setUploadedBy(request.uploadedBy());

        if (request.journalTransactionId() != null) {
            JournalTransaction txn = transactionRepository.findById(request.journalTransactionId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Journal transaction not found: " + request.journalTransactionId()));
            document.setJournalTransaction(txn);
        }

        return documentRepository.save(document);
    }

    @Transactional(readOnly = true)
    public List<VaultDocument> getDocuments(String tenantId) {
        return documentRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public VaultDocument getDocument(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<VaultDocument> getDocumentsForTransaction(Long journalTransactionId) {
        return documentRepository.findByJournalTransactionId(journalTransactionId);
    }

    @Transactional
    public void deleteDocument(Long id) {
        VaultDocument document = getDocument(id);
        // In production: delete the actual file from S3/MinIO using document.getStorageKey()
        documentRepository.delete(document);
    }

    /**
     * Stores document metadata in the vault and preserves the original MinIO path
     * in the document's metadata JSON field for audit trail purposes.
     *
     * @param request      the document upload request
     * @param originalPath the original MinIO path (e.g. /minio-bucket/pharmacy/invoices/...)
     * @return the persisted VaultDocument with originalPath recorded in metadata
     */
    @Transactional
    public VaultDocument storeDocumentWithOriginalPath(DocumentUploadRequest request, String originalPath) {
        VaultDocument document = storeDocument(request);
        if (originalPath != null && !originalPath.isBlank()) {
            try {
                String metadataJson = objectMapper.writeValueAsString(Map.of("originalPath", originalPath));
                document.setMetadata(metadataJson);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to serialize document metadata", e);
            }
            documentRepository.save(document);
        }
        return document;
    }

    /**
     * Verifies the checksum of a document to ensure integrity.
     */
    public boolean verifyChecksum(String content, String expectedChecksum) {
        String actualChecksum = computeChecksum(content);
        return actualChecksum.equals(expectedChecksum);
    }

    /**
     * Computes SHA-256 checksum for content verification.
     */
    public String computeChecksum(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private String generateStorageKey(String tenantId, String fileName) {
        return String.format("vault/%s/%s/%s",
                tenantId, UUID.randomUUID(), fileName);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
