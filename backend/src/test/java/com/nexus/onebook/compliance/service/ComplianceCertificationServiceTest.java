package com.nexus.onebook.compliance.service;

import com.nexus.onebook.compliance.dto.ComplianceCertificationRequest;
import com.nexus.onebook.compliance.model.CertificationStatus;
import com.nexus.onebook.compliance.model.ComplianceCertification;
import com.nexus.onebook.compliance.repository.ComplianceCertificationRepository;
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
class ComplianceCertificationServiceTest {

    @Mock
    private ComplianceCertificationRepository certificationRepository;

    @InjectMocks
    private ComplianceCertificationService certificationService;

    @Test
    void createCertification_validRequest_succeeds() {
        ComplianceCertificationRequest request = new ComplianceCertificationRequest(
                "tenant-1", "SOC 2 Type II", "AICPA", "Technology",
                null, null, null, null);

        when(certificationRepository.findByTenantIdAndCertificationName("tenant-1", "SOC 2 Type II"))
                .thenReturn(Optional.empty());
        when(certificationRepository.save(any(ComplianceCertification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ComplianceCertification result = certificationService.createCertification(request);

        assertNotNull(result);
        assertEquals("SOC 2 Type II", result.getCertificationName());
        assertEquals("AICPA", result.getIssuingBody());
        assertEquals(CertificationStatus.NOT_STARTED, result.getStatus());
    }

    @Test
    void createCertification_duplicate_throws() {
        ComplianceCertificationRequest request = new ComplianceCertificationRequest(
                "tenant-1", "SOC 2 Type II", "AICPA", "Technology",
                null, null, null, null);

        when(certificationRepository.findByTenantIdAndCertificationName("tenant-1", "SOC 2 Type II"))
                .thenReturn(Optional.of(new ComplianceCertification()));

        assertThrows(IllegalArgumentException.class,
                () -> certificationService.createCertification(request));
    }

    @Test
    void startCertification_notStarted_setsInProgress() {
        ComplianceCertification cert = new ComplianceCertification(
                "tenant-1", "ISO 27001", "ISO", "Technology");
        cert.setId(1L);

        when(certificationRepository.findById(1L)).thenReturn(Optional.of(cert));
        when(certificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ComplianceCertification result = certificationService.startCertification(1L);

        assertEquals(CertificationStatus.IN_PROGRESS, result.getStatus());
    }

    @Test
    void startCertification_alreadyInProgress_throws() {
        ComplianceCertification cert = new ComplianceCertification(
                "tenant-1", "ISO 27001", "ISO", "Technology");
        cert.setId(1L);
        cert.setStatus(CertificationStatus.IN_PROGRESS);

        when(certificationRepository.findById(1L)).thenReturn(Optional.of(cert));

        assertThrows(IllegalArgumentException.class,
                () -> certificationService.startCertification(1L));
    }

    @Test
    void completeCertification_inProgress_setsCertified() {
        ComplianceCertification cert = new ComplianceCertification(
                "tenant-1", "HIPAA", "HHS", "Healthcare");
        cert.setId(1L);
        cert.setStatus(CertificationStatus.IN_PROGRESS);

        when(certificationRepository.findById(1L)).thenReturn(Optional.of(cert));
        when(certificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ComplianceCertification result = certificationService.completeCertification(1L, "CERT-2024-001");

        assertEquals(CertificationStatus.CERTIFIED, result.getStatus());
        assertEquals("CERT-2024-001", result.getCertificateReference());
    }

    @Test
    void completeCertification_notInProgress_throws() {
        ComplianceCertification cert = new ComplianceCertification(
                "tenant-1", "HIPAA", "HHS", "Healthcare");
        cert.setId(1L);
        // Status is NOT_STARTED by default

        when(certificationRepository.findById(1L)).thenReturn(Optional.of(cert));

        assertThrows(IllegalArgumentException.class,
                () -> certificationService.completeCertification(1L, "ref"));
    }

    @Test
    void revokeCertification_succeeds() {
        ComplianceCertification cert = new ComplianceCertification(
                "tenant-1", "PCI-DSS", "PCI SSC", "Finance");
        cert.setId(1L);
        cert.setStatus(CertificationStatus.CERTIFIED);

        when(certificationRepository.findById(1L)).thenReturn(Optional.of(cert));
        when(certificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ComplianceCertification result = certificationService.revokeCertification(1L);

        assertEquals(CertificationStatus.REVOKED, result.getStatus());
    }

    @Test
    void getCertification_notFound_throws() {
        when(certificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> certificationService.getCertification(99L));
    }

    @Test
    void getCertifications_returnsList() {
        when(certificationRepository.findByTenantId("tenant-1"))
                .thenReturn(List.of(new ComplianceCertification()));

        List<ComplianceCertification> result = certificationService.getCertifications("tenant-1");

        assertEquals(1, result.size());
    }
}
