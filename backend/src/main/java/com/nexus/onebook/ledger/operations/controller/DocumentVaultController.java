package com.nexus.onebook.ledger.operations.controller;

import com.nexus.onebook.ledger.operations.dto.DocumentUploadRequest;
import com.nexus.onebook.ledger.operations.model.VaultDocument;
import com.nexus.onebook.ledger.operations.service.DocumentVaultService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for the Smart Document Vault.
 * Manages encrypted source document storage and retrieval.
 */
@RestController
@RequestMapping("/api/document-vault")
public class DocumentVaultController {

    private final DocumentVaultService documentVaultService;

    public DocumentVaultController(DocumentVaultService documentVaultService) {
        this.documentVaultService = documentVaultService;
    }

    @PostMapping("/documents")
    public ResponseEntity<VaultDocument> uploadDocument(
            @Valid @RequestBody DocumentUploadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentVaultService.storeDocument(request));
    }

    @GetMapping("/documents")
    public ResponseEntity<List<VaultDocument>> getDocuments(@RequestParam String tenantId) {
        return ResponseEntity.ok(documentVaultService.getDocuments(tenantId));
    }

    @GetMapping("/documents/{id}")
    public ResponseEntity<VaultDocument> getDocument(@PathVariable Long id) {
        return ResponseEntity.ok(documentVaultService.getDocument(id));
    }

    @GetMapping("/documents/transaction/{transactionId}")
    public ResponseEntity<List<VaultDocument>> getDocumentsForTransaction(
            @PathVariable Long transactionId) {
        return ResponseEntity.ok(documentVaultService.getDocumentsForTransaction(transactionId));
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentVaultService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
