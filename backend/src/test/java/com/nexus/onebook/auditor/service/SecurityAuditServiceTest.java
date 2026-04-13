package com.nexus.onebook.auditor.service;

import com.nexus.onebook.auditor.dto.SecurityAuditReport;
import com.nexus.onebook.security.AuditLogService;
import com.nexus.onebook.security.FieldEncryptionService;
import com.nexus.onebook.security.KeyManagementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityAuditServiceTest {

    @Mock
    private FieldEncryptionService encryptionService;
    @Mock
    private KeyManagementService keyManagementService;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private SecurityAuditService securityAuditService;

    @Test
    void runSecurityAudit_allChecksPass() {
        when(encryptionService.encrypt(anyString())).thenReturn("encrypted-data");
        when(encryptionService.decrypt("encrypted-data")).thenReturn("round-trip-verification-data");

        javax.crypto.SecretKey key1 = new javax.crypto.spec.SecretKeySpec(new byte[32], "AES");
        javax.crypto.SecretKey key2 = new javax.crypto.spec.SecretKeySpec(
                new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
                        17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32}, "AES");
        when(keyManagementService.getCurrentEncryptionKey()).thenReturn(key1);
        when(keyManagementService.getEncryptionKey(1)).thenReturn(key1);
        when(keyManagementService.getEncryptionKey(2)).thenReturn(key2);
        when(auditLogService.verifyChain("tenant-1")).thenReturn(true);

        SecurityAuditReport report = securityAuditService.runSecurityAudit("tenant-1");

        assertNotNull(report);
        assertEquals("tenant-1", report.tenantId());
        assertTrue(report.encryptionVerified());
        assertTrue(report.keyManagementVerified());
        assertTrue(report.auditChainIntact());
        assertEquals(5, report.totalChecks());
        assertNotNull(report.auditTimestamp());
    }

    @Test
    void runSecurityAudit_encryptionFails_reportsFinding() {
        when(encryptionService.encrypt(anyString())).thenThrow(new RuntimeException("Key error"));

        javax.crypto.SecretKey key1 = new javax.crypto.spec.SecretKeySpec(new byte[32], "AES");
        javax.crypto.SecretKey key2 = new javax.crypto.spec.SecretKeySpec(
                new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
                        17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32}, "AES");
        when(keyManagementService.getCurrentEncryptionKey()).thenReturn(key1);
        when(keyManagementService.getEncryptionKey(1)).thenReturn(key1);
        when(keyManagementService.getEncryptionKey(2)).thenReturn(key2);
        when(auditLogService.verifyChain("tenant-1")).thenReturn(true);

        SecurityAuditReport report = securityAuditService.runSecurityAudit("tenant-1");

        assertFalse(report.encryptionVerified());
        assertTrue(report.failedChecks() > 0);
        assertTrue(report.findings().stream().anyMatch(f -> f.contains("FAIL")));
    }

    @Test
    void runSecurityAudit_auditChainBroken_reportsFinding() {
        when(encryptionService.encrypt(anyString())).thenReturn("encrypted");
        when(encryptionService.decrypt("encrypted")).thenReturn("round-trip-verification-data");

        javax.crypto.SecretKey key1 = new javax.crypto.spec.SecretKeySpec(new byte[32], "AES");
        javax.crypto.SecretKey key2 = new javax.crypto.spec.SecretKeySpec(
                new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
                        17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32}, "AES");
        when(keyManagementService.getCurrentEncryptionKey()).thenReturn(key1);
        when(keyManagementService.getEncryptionKey(1)).thenReturn(key1);
        when(keyManagementService.getEncryptionKey(2)).thenReturn(key2);
        when(auditLogService.verifyChain("tenant-1")).thenReturn(false);

        SecurityAuditReport report = securityAuditService.runSecurityAudit("tenant-1");

        assertFalse(report.auditChainIntact());
        assertTrue(report.findings().stream().anyMatch(f -> f.contains("compromised")));
    }

    @Test
    void runSecurityAudit_reportHasCorrectStructure() {
        when(encryptionService.encrypt(anyString())).thenReturn("enc");
        when(encryptionService.decrypt("enc")).thenReturn("round-trip-verification-data");

        javax.crypto.SecretKey key1 = new javax.crypto.spec.SecretKeySpec(new byte[32], "AES");
        javax.crypto.SecretKey key2 = new javax.crypto.spec.SecretKeySpec(
                new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
                        17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32}, "AES");
        when(keyManagementService.getCurrentEncryptionKey()).thenReturn(key1);
        when(keyManagementService.getEncryptionKey(1)).thenReturn(key1);
        when(keyManagementService.getEncryptionKey(2)).thenReturn(key2);
        when(auditLogService.verifyChain("tenant-1")).thenReturn(true);

        SecurityAuditReport report = securityAuditService.runSecurityAudit("tenant-1");

        assertEquals(report.totalChecks(), report.passedChecks() + report.failedChecks());
        assertFalse(report.findings().isEmpty());
    }
}
