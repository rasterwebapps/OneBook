package com.nexus.onebook.security;

import com.nexus.onebook.security.model.AuditLogEntry;
import com.nexus.onebook.security.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Hash-chained audit trail service.
 * <p>
 * Every audit record is cryptographically linked to its predecessor
 * via SHA-256. Tampering with any row (insert, update, or delete of
 * the audit table itself) breaks the chain and is detectable by
 * {@link #verifyChain(String)}.
 */
@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Logs an INSERT operation into the hash-chained audit trail.
     */
    @Transactional
    public AuditLogEntry logInsert(String tenantId, String tableName, Long recordId,
                                   String newValues) {
        return log(tenantId, tableName, recordId, "INSERT", null, newValues);
    }

    /**
     * Logs an UPDATE operation into the hash-chained audit trail.
     */
    @Transactional
    public AuditLogEntry logUpdate(String tenantId, String tableName, Long recordId,
                                   String oldValues, String newValues) {
        return log(tenantId, tableName, recordId, "UPDATE", oldValues, newValues);
    }

    /**
     * Logs a DELETE operation into the hash-chained audit trail.
     */
    @Transactional
    public AuditLogEntry logDelete(String tenantId, String tableName, Long recordId,
                                   String oldValues) {
        return log(tenantId, tableName, recordId, "DELETE", oldValues, null);
    }

    /**
     * Verifies the integrity of the entire audit chain for a given tenant.
     *
     * @return {@code true} if the chain is intact, {@code false} if any record
     *         has been tampered with
     */
    @Transactional(readOnly = true)
    public boolean verifyChain(String tenantId) {
        final List<AuditLogEntry> entries = auditLogRepository.findByTenantIdOrderByIdAsc(tenantId);
        return verifyEntryList(entries);
    }

    /**
     * Verifies the audit chain for a specific record.
     */
    @Transactional(readOnly = true)
    public boolean verifyChainForRecord(String tenantId, String tableName, Long recordId) {
        final List<AuditLogEntry> entries =
                auditLogRepository.findByTenantIdAndTableNameAndRecordIdOrderByIdAsc(
                        tenantId, tableName, recordId);
        return verifyEntryList(entries);
    }

    private boolean verifyEntryList(List<AuditLogEntry> entries) {
        String previousHash = null;
        for (AuditLogEntry entry : entries) {
            final String expectedHash = computeHash(previousHash, entry);
            if (!expectedHash.equals(entry.getHash())) {
                return false; // tampered!
            }
            previousHash = entry.getHash();
        }
        return true;
    }

    // --- Internal ---

    private AuditLogEntry log(String tenantId, String tableName, Long recordId,
                              String operation, String oldValues, String newValues) {
        // Look up the previous hash in the chain for this tenant
        final String prevHash = auditLogRepository.findLatestByTenantId(tenantId)
                .map(AuditLogEntry::getHash)
                .orElse(null);

        final AuditLogEntry entry = new AuditLogEntry();
        entry.setTenantId(tenantId);
        entry.setTableName(tableName);
        entry.setRecordId(recordId);
        entry.setOperation(operation);
        entry.setOldValues(oldValues);
        entry.setNewValues(newValues);
        entry.setPrevHash(prevHash);
        entry.setHash(computeHash(prevHash, entry));

        return auditLogRepository.save(entry);
    }

    /**
     * Computes the SHA-256 hash for a chain link:
     * <pre>SHA-256(prevHash | tableName | recordId | operation | newValues)</pre>
     */
    String computeHash(String prevHash, AuditLogEntry entry) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final String content = String.format("%s|%s|%s|%s|%s|%s",
                    prevHash != null ? prevHash : "",
                    entry.getTableName(),
                    entry.getRecordId(),
                    entry.getOperation(),
                    entry.getOldValues() != null ? entry.getOldValues() : "",
                    entry.getNewValues() != null ? entry.getNewValues() : "");
            final byte[] hashBytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return KeyManagementService.bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
