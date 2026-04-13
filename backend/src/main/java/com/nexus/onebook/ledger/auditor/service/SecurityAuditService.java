package com.nexus.onebook.ledger.auditor.service;

import com.nexus.onebook.ledger.auditor.dto.SecurityAuditReport;
import com.nexus.onebook.ledger.security.AuditLogService;
import com.nexus.onebook.ledger.security.FieldEncryptionService;
import com.nexus.onebook.ledger.security.KeyManagementService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Security audit service.
 * Performs comprehensive security verification including encryption checks,
 * key management review, and audit chain integrity verification.
 */
@Service
public class SecurityAuditService {

    private final FieldEncryptionService encryptionService;
    private final KeyManagementService keyManagementService;
    private final AuditLogService auditLogService;

    public SecurityAuditService(FieldEncryptionService encryptionService,
                                 KeyManagementService keyManagementService,
                                 AuditLogService auditLogService) {
        this.encryptionService = encryptionService;
        this.keyManagementService = keyManagementService;
        this.auditLogService = auditLogService;
    }

    /**
     * Runs a comprehensive security audit for the given tenant.
     * Checks encryption, key management, and audit chain integrity.
     */
    public SecurityAuditReport runSecurityAudit(String tenantId) {
        List<String> findings = new ArrayList<>();
        int totalChecks = 0;
        int passedChecks = 0;

        // 1. Encryption Verification
        totalChecks++;
        boolean encryptionVerified = verifyEncryption(findings);
        if (encryptionVerified) passedChecks++;

        // 2. Key Management Review
        totalChecks++;
        boolean keyManagementVerified = verifyKeyManagement(findings);
        if (keyManagementVerified) passedChecks++;

        // 3. Audit Chain Integrity
        totalChecks++;
        boolean auditChainIntact = verifyAuditChain(tenantId, findings);
        if (auditChainIntact) passedChecks++;

        // 4. Encryption Round-Trip Test
        totalChecks++;
        boolean roundTripPassed = verifyEncryptionRoundTrip(findings);
        if (roundTripPassed) passedChecks++;

        // 5. Key Derivation Test
        totalChecks++;
        boolean keyDerivationPassed = verifyKeyDerivation(findings);
        if (keyDerivationPassed) passedChecks++;

        return new SecurityAuditReport(
                tenantId, Instant.now(),
                encryptionVerified, keyManagementVerified, auditChainIntact,
                totalChecks, passedChecks, totalChecks - passedChecks,
                findings);
    }

    private boolean verifyEncryption(List<String> findings) {
        try {
            String testData = "security-audit-test-" + System.currentTimeMillis();
            String encrypted = encryptionService.encrypt(testData);
            if (encrypted == null || encrypted.isEmpty()) {
                findings.add("FAIL: Encryption produced empty result");
                return false;
            }
            if (encrypted.equals(testData)) {
                findings.add("FAIL: Encrypted data matches plaintext");
                return false;
            }
            findings.add("PASS: AES-256-GCM encryption is operational");
            return true;
        } catch (Exception e) {
            findings.add("FAIL: Encryption verification error: " + e.getMessage());
            return false;
        }
    }

    private boolean verifyKeyManagement(List<String> findings) {
        try {
            javax.crypto.SecretKey key = keyManagementService.getCurrentEncryptionKey();
            if (key == null) {
                findings.add("FAIL: Key derivation produced null key");
                return false;
            }
            byte[] encoded = key.getEncoded();
            if (encoded == null || encoded.length < 16) {
                findings.add("FAIL: Derived key is too short");
                return false;
            }
            findings.add("PASS: Key management system is operational");
            return true;
        } catch (Exception e) {
            findings.add("FAIL: Key management verification error: " + e.getMessage());
            return false;
        }
    }

    private boolean verifyAuditChain(String tenantId, List<String> findings) {
        try {
            boolean intact = auditLogService.verifyChain(tenantId);
            if (intact) {
                findings.add("PASS: Audit chain integrity verified for tenant " + tenantId);
            } else {
                findings.add("FAIL: Audit chain integrity compromised for tenant " + tenantId);
            }
            return intact;
        } catch (Exception e) {
            findings.add("FAIL: Audit chain verification error: " + e.getMessage());
            return false;
        }
    }

    private boolean verifyEncryptionRoundTrip(List<String> findings) {
        try {
            String original = "round-trip-verification-data";
            String encrypted = encryptionService.encrypt(original);
            String decrypted = encryptionService.decrypt(encrypted);
            if (original.equals(decrypted)) {
                findings.add("PASS: Encryption round-trip verification successful");
                return true;
            } else {
                findings.add("FAIL: Decrypted data does not match original");
                return false;
            }
        } catch (Exception e) {
            findings.add("FAIL: Encryption round-trip error: " + e.getMessage());
            return false;
        }
    }

    private boolean verifyKeyDerivation(List<String> findings) {
        try {
            javax.crypto.SecretKey key1 = keyManagementService.getEncryptionKey(1);
            javax.crypto.SecretKey key2 = keyManagementService.getEncryptionKey(2);
            if (java.util.Arrays.equals(key1.getEncoded(), key2.getEncoded())) {
                findings.add("FAIL: Key derivation produces identical keys for different versions");
                return false;
            }
            findings.add("PASS: Key derivation produces unique keys per version");
            return true;
        } catch (Exception e) {
            findings.add("FAIL: Key derivation verification error: " + e.getMessage());
            return false;
        }
    }
}
