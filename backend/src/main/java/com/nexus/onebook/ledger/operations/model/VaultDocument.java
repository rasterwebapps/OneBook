package com.nexus.onebook.ledger.operations.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import com.nexus.onebook.ledger.accounts.model.JournalTransaction;

/**
 * Document metadata for the Smart Document Vault.
 * Tracks encrypted source documents stored in S3/MinIO
 * and optionally linked to journal transactions.
 */
@Entity
@Table(name = "vault_documents")
public class VaultDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "storage_key", nullable = false, length = 500)
    private String storageKey;

    @Column(name = "encryption_key_version", nullable = false)
    private int encryptionKeyVersion = 1;

    @Column(name = "checksum", nullable = false, length = 64)
    private String checksum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_transaction_id")
    private JournalTransaction journalTransaction;

    @Column(name = "uploaded_by")
    private String uploadedBy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata = "{}";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public VaultDocument() {}

    public VaultDocument(String tenantId, String fileName, String contentType,
                         long fileSize, String storageKey, String checksum) {
        this.tenantId = tenantId;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.storageKey = storageKey;
        this.checksum = checksum;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }

    public int getEncryptionKeyVersion() { return encryptionKeyVersion; }
    public void setEncryptionKeyVersion(int encryptionKeyVersion) { this.encryptionKeyVersion = encryptionKeyVersion; }

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }

    public JournalTransaction getJournalTransaction() { return journalTransaction; }
    public void setJournalTransaction(JournalTransaction journalTransaction) { this.journalTransaction = journalTransaction; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public Instant getCreatedAt() { return createdAt; }
}
