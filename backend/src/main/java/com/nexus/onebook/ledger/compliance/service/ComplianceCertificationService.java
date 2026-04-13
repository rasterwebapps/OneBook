package com.nexus.onebook.ledger.compliance.service;

import com.nexus.onebook.ledger.compliance.dto.ComplianceCertificationRequest;
import com.nexus.onebook.ledger.compliance.model.CertificationStatus;
import com.nexus.onebook.ledger.compliance.model.ComplianceCertification;
import com.nexus.onebook.ledger.compliance.repository.ComplianceCertificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Compliance certification service.
 * Tracks industry-specific certifications (SOC 2, ISO 27001, HIPAA, PCI-DSS, etc.)
 * and their lifecycle: application, audit, certification, renewal.
 */
@Service
public class ComplianceCertificationService {

    private final ComplianceCertificationRepository certificationRepository;

    public ComplianceCertificationService(ComplianceCertificationRepository certificationRepository) {
        this.certificationRepository = certificationRepository;
    }

    @Transactional
    public ComplianceCertification createCertification(ComplianceCertificationRequest request) {
        certificationRepository.findByTenantIdAndCertificationName(
                request.tenantId(), request.certificationName())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Certification '" + request.certificationName() + "' already exists");
                });

        ComplianceCertification cert = new ComplianceCertification(
                request.tenantId(), request.certificationName(),
                request.issuingBody(), request.industry());

        if (request.issuedDate() != null) cert.setIssuedDate(request.issuedDate());
        if (request.expiryDate() != null) cert.setExpiryDate(request.expiryDate());
        if (request.certificateReference() != null) cert.setCertificateReference(request.certificateReference());
        if (request.notes() != null) cert.setNotes(request.notes());

        return certificationRepository.save(cert);
    }

    @Transactional
    public ComplianceCertification startCertification(Long id) {
        ComplianceCertification cert = getCertification(id);
        if (cert.getStatus() != CertificationStatus.NOT_STARTED) {
            throw new IllegalArgumentException("Certification must be in NOT_STARTED status");
        }
        cert.setStatus(CertificationStatus.IN_PROGRESS);
        return certificationRepository.save(cert);
    }

    @Transactional
    public ComplianceCertification completeCertification(Long id, String certificateReference) {
        ComplianceCertification cert = getCertification(id);
        if (cert.getStatus() != CertificationStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Certification must be in IN_PROGRESS status");
        }
        cert.setStatus(CertificationStatus.CERTIFIED);
        cert.setCertificateReference(certificateReference);
        return certificationRepository.save(cert);
    }

    @Transactional
    public ComplianceCertification revokeCertification(Long id) {
        ComplianceCertification cert = getCertification(id);
        cert.setStatus(CertificationStatus.REVOKED);
        return certificationRepository.save(cert);
    }

    @Transactional(readOnly = true)
    public List<ComplianceCertification> getCertifications(String tenantId) {
        return certificationRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public ComplianceCertification getCertification(Long id) {
        return certificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Certification not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<ComplianceCertification> getCertificationsByStatus(String tenantId,
                                                                     CertificationStatus status) {
        return certificationRepository.findByTenantIdAndStatus(tenantId, status);
    }
}
